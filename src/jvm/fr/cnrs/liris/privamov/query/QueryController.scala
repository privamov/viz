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

package fr.cnrs.liris.privamov.query

import com.google.common.geometry.{S2Cell, S2Point}
import com.google.inject.{Inject, Singleton}
import com.twitter.bijection.twitter_util.UtilBijections
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.finatra.validation.OneOf
import com.twitter.util.{Future => TwitterFuture}
import fr.cnrs.liris.common.geo.{FeatureCollection, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future => ScalaFuture}

case class ListDatasetsRequest(filters: Seq[QueryFilter] = Seq.empty)

case class GetDatasetRequest(
    @RouteParam dataset: String,
    filters: Seq[QueryFilter] = Seq.empty)

case class QueryEventsRequest(
    @RouteParam dataset: String,
    filters: Seq[QueryFilter] = Seq.empty,
    aggregate: Option[QueryAggregate],
    limit: Option[Int],
    offset: Option[Int],
    @QueryParam @OneOf(Array("geojson", "json")) format: String = "json")

/**
 * Controller handling API endpoints.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
@Singleton
class QueryController @Inject()(queryExecutor: QueryExecutor) extends Controller {
  private[this] val datasetNames = Seq("cabspotting")

  get("/datasets") { request: ListDatasetsRequest =>
    val futures = datasetNames.map { name =>
      val future = queryExecutor.timeRange(name, request.filters)
      toTwitterFuture(future).map(timeRange => Dataset(name, timeRange._1, timeRange._2))
    }
    TwitterFuture.collect(futures)
  }

  get("/datasets/:dataset") { request: GetDatasetRequest =>
    if (datasetNames.contains(request.dataset)) {
      val future = queryExecutor.timeRange(request.dataset, request.filters)
      toTwitterFuture(future).map(timeRange => Dataset(request.dataset, timeRange._1, timeRange._2))
    } else {
      response.notFound
    }
  }

  post("/datasets/:dataset/events") { request: QueryEventsRequest =>
    val future = request.aggregate match {
      case Some(agg: QueryAggregateHeatmap) =>
        queryExecutor.events(request.dataset, request.filters, agg)
      case _ => ScalaFuture.failed(new IllegalArgumentException("Unsupported aggregate"))
    }
    toTwitterFuture(future)
        .map(buckets => if (request.format == "geojson") createHeatMap(buckets) else buckets)
        .map(response.ok.contentTypeJson().body)
  }

  private def createHeatMap(buckets: Seq[HeatmapBucket]) = {
    val features = buckets.map { bucket =>
      val cell = new S2Cell(bucket.cellId)
      val ring = (Seq.tabulate(4)(cell.getVertex) ++ Seq(cell.getVertex(0))).map(point => GeoPoint(LatLng(point)))
      val properties = Map("id" -> bucket.cellId.toToken, "value" -> bucket.count)
      Feature(Polygon.lines(Seq(LineString.points(ring))), properties)
    }
    val bbox = boundingBox(buckets.map(bucket => new S2Cell(bucket.cellId).getCenter))
    FeatureCollection(features, bbox)
  }

  private[this] val pointOrdering = implicitly[Ordering[S2Point]]

  private def boundingBox(points: Iterable[S2Point]) = {
    if (points.nonEmpty) {
      val min = LatLng(points.min(pointOrdering))
      val max = LatLng(points.max(pointOrdering))
      Some(Seq(min.lng.degrees, min.lat.degrees, max.lng.degrees, max.lat.degrees))
    } else {
      None
    }
  }

  private def toTwitterFuture[T](future: ScalaFuture[T]) =
    UtilBijections.twitter2ScalaFuture(global).invert(future)
}