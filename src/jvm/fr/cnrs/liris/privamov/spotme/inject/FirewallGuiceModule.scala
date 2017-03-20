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

package fr.cnrs.liris.privamov.spotme.inject

import com.google.inject.Provides
import com.google.inject.name.Named
import com.twitter.finatra.httpclient.{HttpClient, RichHttpClient}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule
import fr.cnrs.liris.privamov.spotme.auth.{Firewall, FleetFirewall, NoFirewall}

/**
 * Provide support for Fleet firewall.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
object FirewallGuiceModule extends TwitterModule {
  private[this] val firewallType = flag[String]("viz.firewall", "none", "Type of firewall to use")
  private[this] val fleetServer = flag[String]("viz.fleet_server", "privamov.liris.cnrs.fr:80", "Address of the Fleet API")

  override protected def configure(): Unit = {
    firewallType() match {
      case "fleet" => bind[Firewall].to[FleetFirewall]
      case "none" => bind[Firewall].to[NoFirewall]
      case invalid => throw new IllegalArgumentException(s"Invalid firewall: $invalid")
    }
  }

  @Provides
  @Named("fleet")
  def providesFleetHttpClient(mapper: FinatraObjectMapper): HttpClient = {
    val address = fleetServer().split(":")
    val hostname = address.head
    val httpService = RichHttpClient.newClientService(dest = fleetServer())
    new HttpClient(hostname, httpService, mapper = mapper)
  }
}