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

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.Future
import fr.cnrs.liris.privamov.spotme.inject._

object SpotmeServerMain extends SpotmeServer

/**
 * Priva'Mov SpotME server.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
class SpotmeServer extends HttpServer {
  private[this] val uiFlag = flag("ui", false, "Whether to enable the UI")

  override protected def modules = Seq(SpotmeGuiceModule, PrivamovGuiceModule)

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
        .filter[CorsFilter](beforeRouting = true)
        .add[ApiController]
    if (uiFlag()) {
      router.add[UiController]
    }
  }
}

private class CorsFilter extends SimpleFilter[Request, Response] {
  private[this] val cors = {
    val allowsOrigin = { origin: String => Some(origin) }
    val allowsMethods = { method: String => Some(Seq("GET", "POST", "PUT", "DELETE")) }
    val allowsHeaders = { headers: Seq[String] => Some(headers) }

    val policy = Cors.Policy(allowsOrigin, allowsMethods, allowsHeaders)
    new Cors.HttpFilter(policy)
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] =
    cors.apply(request, service)
}
