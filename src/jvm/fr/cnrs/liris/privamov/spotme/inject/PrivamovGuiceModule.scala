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
import com.twitter.conversions.time._
import com.twitter.finatra.httpclient.{HttpClient, RichHttpClient}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule
import com.twitter.querulous.database.{ApachePoolingDatabaseFactory, DatabaseFactory}
import com.twitter.querulous.evaluator.{QueryEvaluatorFactory, StandardQueryEvaluatorFactory}
import com.twitter.querulous.query.{QueryFactory, SqlQueryFactory}
import fr.cnrs.liris.privamov.spotme.auth.{Firewall, FleetFirewall}
import fr.cnrs.liris.privamov.spotme.store.{EventStoreFactory, PrivamovStoreFactory}
import net.codingwell.scalaguice.ScalaMapBinder

/**
 * Provide support for Priva'Mov event stores. They are essentially PostGIS stores interacting with
 * the Fleet service to determine which data to display.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
object PrivamovGuiceModule extends TwitterModule {
  private[this] val fleetServer = flag[String]("viz.fleet_server", "privamov.liris.cnrs.fr:80", "Address of the Fleet API")

  override protected def configure(): Unit = {
    val stores = ScalaMapBinder.newMapBinder[String, EventStoreFactory](binder)
    stores.addBinding("privamov").to[PrivamovStoreFactory]
    bind[Firewall].to[FleetFirewall]
  }

  @Provides
  @Named("privamov")
  def providesDatabaseFactory: DatabaseFactory =
    new ApachePoolingDatabaseFactory(10, 10, 1.second, 10.millis, false, 60.seconds)

  @Provides
  @Named("privamov")
  def providesQueryFactory: QueryFactory = new SqlQueryFactory

  @Provides
  @Named("privamov")
  def providesQueryEvaluatorFactory(@Named("privamov") databaseFactory: DatabaseFactory, @Named("privamov") queryFactory: QueryFactory): QueryEvaluatorFactory =
    new StandardQueryEvaluatorFactory(databaseFactory, queryFactory)

  @Provides
  @Named("fleet")
  def providesFleetHttpClient(mapper: FinatraObjectMapper): HttpClient = {
    val address = fleetServer().split(":")
    val hostname = address.head
    val httpService = RichHttpClient.newClientService(dest = fleetServer())
    new HttpClient(hostname, httpService, mapper = mapper)
  }
}