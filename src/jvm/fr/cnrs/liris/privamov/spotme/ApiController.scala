/*
 * Priva'Mov is a program whose purpose is to collect and analyze mobility traces.
 * Copyright (C) 2016-2017 Vincent Primault <vincent.primault@liris.cnrs.fr>
 *
 * Priva'Mov is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Priva'Mov is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Priva'Mov.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnrs.liris.privamov.spotme

import javax.inject.{Inject, Singleton}

import com.twitter.finagle.http.Response
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.inject.annotations.Flag
import fr.cnrs.liris.common.geo._
import fr.cnrs.liris.common.geo.Distance
import fr.cnrs.liris.privamov.core.lppm.{Laplace, SpeedSmoothing}
import fr.cnrs.liris.privamov.core.model.GeoJsonConverters._
import fr.cnrs.liris.privamov.core.model.{Event, Trace}
import fr.cnrs.liris.privamov.spotme.auth.{AccessToken, Firewall, Scope, View}
import fr.cnrs.liris.privamov.spotme.store.{EventStore, StoreRegistry}
import org.joda.time.DateTime

/**
 * Controller handling API endpoints.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
@Singleton
class ApiController @Inject()(
  @Flag("viz.standard_limit") standardLimit: Int,
  @Flag("viz.extended_limit") extendedLimit: Int,
  stores: StoreRegistry,
  firewall: Firewall
) extends Controller {

  get("/api/datasets") { request: ListDatasetsRequest =>
    authenticated(request.accessToken, Scope.Datasets) { token =>
      stores
        .filter(token.acl.accessible)
        .map(store => Dataset(store.name, store.getClass.getSimpleName))
    }
  }

  get("/api/datasets/:name") { request: GetDatasetRequest =>
    authenticated(request.accessToken, Scope.Datasets) { token =>
      withStore(request.name, token) { store =>
        Dataset(request.name, store.getClass.getSimpleName)
      }
    }
  }

  get("/api/datasets/:name/sources") { request: ListSourcesRequest =>
    authenticated(request.accessToken, Scope.Datasets) { token =>
      withStore(request.name, token) { store =>
        val accessibleViews = token.acl.resolve(store.name).views
        store.sources(accessibleViews)
      }
    }
  }

  get("/api/datasets/:name/sources/:id") { request: GetSourceRequest =>
    authenticated(request.accessToken, Scope.Datasets) { token =>
      withStore(request.name, token) { store =>
        if (store.contains(request.id)) {
          store.apply(request.id)
        } else {
          response.notFound
        }
      }
    }
  }

  get("/api/datasets/:name/features") { request: ListFeaturesRequest =>
    authenticated(request.accessToken, Scope.Datasets) { token =>
      withStore(request.name, token) { store =>
        val views = if (request.sources.nonEmpty) {
          request.sources.map(source => View(Some(source), request.startAfter, request.endBefore, None))
        } else {
          Set(View(None, request.startAfter, request.endBefore, None))
        }
        val allowedViews = token.acl.resolve(store.name).resolve(views).views
        val limit = request.limit.map(math.min(_, extendedLimit)).getOrElse(extendedLimit)
        val sample = request.sample.getOrElse(false)
        val rawData = store.features(allowedViews, Some(limit), sample)
        val transformedData = request.transform match {
          case Some(spec) => transform(rawData, spec)
          case None => rawData
        }
        toGeoJson(transformedData)
      }
    }
  }

  private def transform(events: Seq[Event], spec: String) = {
    if (events.isEmpty) {
      events
    } else {
      val parts = spec.split("=")
      val res = parts.head match {
        case "geoind" =>
          require(parts.size == 2, "Transformation 'geoind' needs a parameter")
          val epsilon = parts(1).toDouble
          new Laplace(epsilon).transform(Trace(events))
        case "smooth" =>
          require(parts.size == 2, "Transformation 'smooth' needs a parameter")
          val epsilon = Distance.parse(parts(1))
          new SpeedSmoothing(epsilon).transform(Trace(events))
        case name => throw new IllegalArgumentException(s"Unknown transformation '$name'")
      }
      res.events
    }
  }

  private def toGeoJson(events: Seq[Event]) = {
    val features = events.map { event =>
      event.set("distance", event.point.distance(events.head.point).meters).toGeoJson
    }
    FeatureCollection(features)
  }

  private def authenticated[T](bearer: Option[String], requiredScopes: Scope*)(fn: AccessToken => T) = {
    bearer.flatMap(firewall.authenticate) match {
      case Some(token) =>
        if (requiredScopes.forall(token.in)) {
          unauthenticated {
            fn(token)
          }
        } else {
          response.unauthorized(
            Error.authenticationError(Some(s"Required scopes ${requiredScopes.mkString(",")}")))
        }
      case None => response.unauthorized(Error.authenticationError(Some("Invalid token")))
    }
  }

  private def withStore[T](name: String, token: AccessToken)(fn: EventStore => T) = {
    stores.get(name).filter(token.acl.accessible) match {
      case Some(store) => fn(store)
      case None => response.notFound
    }
  }

  private def unauthenticated(fn: => Any): Any = try {
    fn
  } catch {
    case t: Throwable => errorToResponse(t)
  }

  private def errorToResponse(t: Throwable): Response = t match {
    case ex: IllegalArgumentException =>
      val message = ex.getMessage.stripPrefix("requirement failed:").trim
      response.badRequest(Error.invalidRequest(Some(message)))
    case t: Throwable =>
      Option(t.getCause) match {
        case Some(cause) => errorToResponse(cause)
        case None =>
          logger.error("Error while processing request", t)
          response.internalServerError(Error.apiError(Some(t.getMessage)))
      }
  }
}

case class ListDatasetsRequest(@QueryParam accessToken: Option[String])

case class GetDatasetRequest(
  @RouteParam name: String,
  @QueryParam accessToken: Option[String])

case class ListSourcesRequest(
  @RouteParam name: String,
  @QueryParam accessToken: Option[String],
  @QueryParam startAfter: Option[String],
  @QueryParam limit: Option[Int])

case class GetSourceRequest(
  @RouteParam name: String,
  @RouteParam id: String,
  @QueryParam accessToken: Option[String])

case class ListFeaturesRequest(
  @RouteParam name: String,
  @QueryParam accessToken: Option[String],
  @QueryParam sources: Set[String],
  @QueryParam startAfter: Option[DateTime],
  @QueryParam endBefore: Option[DateTime],
  @QueryParam limit: Option[Int],
  @QueryParam sample: Option[Boolean],
  @QueryParam transform: Option[String])

/**
 * A dataset holding some mobility data.
 *
 * @param name        Dataset name
 * @param storage     Dataset storage type
 * @param description Dataset description
 */
case class Dataset(name: String, storage: String, description: Option[String] = None)

case class Error(`type`: String, message: Option[String], param: Option[String])

object Error {
  def invalidRequest(message: Option[String] = None, param: Option[String] = None): Error =
    new Error("invalid_request_error", message, param)

  def apiError(message: Option[String] = None): Error = new Error("api_error", message, None)

  def authenticationError(message: Option[String] = None): Error =
    new Error("authentication_error", message, None)
}