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
import com.twitter.conversions.time._
import com.twitter.inject.TwitterModule
import com.twitter.querulous.database.ApachePoolingDatabaseFactory
import com.twitter.querulous.evaluator.StandardQueryEvaluatorFactory
import com.twitter.querulous.query.SqlQueryFactory
import fr.cnrs.liris.privamov.spotme.store.{EventStoreFactory, PostgresStoreFactory}
import net.codingwell.scalaguice.ScalaMapBinder

/**
 * Provide support for PostgreSQL/PostGIS event stores.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
object PostgresGuiceModule extends TwitterModule {
  override protected def configure(): Unit = {
    val stores = ScalaMapBinder.newMapBinder[String, EventStoreFactory](binder)
    stores.addBinding("postgres").to[PostgresStoreFactory]
  }

  @Provides
  def providesPostgresStoreFactory: PostgresStoreFactory = {
    val databaseFactory = new ApachePoolingDatabaseFactory(10, 10, 1.second, 10.millis, false, 60.seconds)
    val queryFactory = new SqlQueryFactory
    val queryEvaluatorFactory = new StandardQueryEvaluatorFactory(databaseFactory, queryFactory)
    new PostgresStoreFactory(queryEvaluatorFactory)
  }
}