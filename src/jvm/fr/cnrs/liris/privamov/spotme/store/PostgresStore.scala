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

package fr.cnrs.liris.privamov.spotme.store

import com.google.inject.Inject
import com.google.inject.name.Named
import com.twitter.querulous.driver.DatabaseDriver
import com.twitter.querulous.evaluator.{QueryEvaluator, QueryEvaluatorFactory}
import fr.cnrs.liris.common.geo.LatLng
import fr.cnrs.liris.privamov.core.model.Event
import fr.cnrs.liris.privamov.spotme.auth.View
import org.joda.time.{Instant, LocalDate}

import scala.collection.mutable

/**
 * Data provider reading data from a PostgreSQL/PostGIS database.
 *
 * @param evaluator A query evaluator
 * @param name      Store name
 * @param tableName Table name where to read events.
 */
class PostgresStore(evaluator: QueryEvaluator, override val name: String, tableName: String) extends EventStore {
  override def contains(user: String): Boolean = {
    val sql = s"select count(1) from $tableName where user = ?"
    evaluator.count(sql, user) > 0
  }

  override def sources(views: Set[View]): Seq[String] = {
    val sql = s"select distinct user " +
      s"from $tableName " +
      s"where ${sourcesWhereClause(views)} " +
      s"order by user"
    evaluator.select(sql)(rs => rs.getString(1))
  }

  override def features(views: Set[View], limit: Option[Int] = None, sample: Boolean = false): Seq[Event] = {
    var selectTail = ""
    val whereClause = s" where ${locationWhereClause(views)}"
    val limitClause = if (limit.isDefined && !sample) s" limit ${limit.get}" else ""

    if (views.exists(_.source.isDefined)) {
      selectTail += ", user"
    }
    if (limit.isDefined && sample) {
      selectTail += ", row_number() over() rnum"
    }

    var sql = s"select extract(epoch from timestamp), st_y(position), st_x(position) $selectTail" +
      s" from $tableName $whereClause order by timestamp $limitClause"

    if (limit.isDefined && sample) {
      val count = evaluator.count(s"select count(1) from $tableName $whereClause")
      if (count > limit.get) {
        val outOf = count.toDouble / limit.get
        sql = s"select * from ($sql) q where rnum % $outOf < 1"
      }
    }

    evaluator.select(sql) { rs =>
      val user = rs.getString(4)
      val point = LatLng.degrees(rs.getDouble(2), rs.getDouble(3)).toPoint
      val time = new Instant(rs.getLong(1) * 1000)
      Event(user, point, time)
    }
  }

  override def countFeatures(views: Set[View]): Int = {
    evaluator.count(s"select count(1) from $tableName where ${locationWhereClause(views)}")
  }

  override def apply(id: String): Source = {
    val countSql = s"select count(1) from $tableName where user = ?"
    val count = evaluator.count(countSql, id)
    if (count > 0) {
      val activeDaysSql = s"select distinct extract(epoch from date_trunc('day', timestamp)) " +
        s"from $tableName " +
        s"where user = ? " +
        s"order by extract(epoch from date_trunc('day', timestamp))"
      val activeDays = evaluator.select(activeDaysSql, id)(rs => new LocalDate(rs.getLong(1) * 1000))
      Source(id, count, activeDays)
    } else {
      Source(id, 0, Seq.empty)
    }
  }

  private def sourcesWhereClause(views: Set[View]): String = {
    if (views.exists(_.source.isEmpty)) {
      "true"
    } else if (views.isEmpty) {
      "false"
    } else {
      val users = views.flatMap(_.source).map(id => s"'$id'")
      if (users.nonEmpty) {
        s"user in(${users.mkString(",")})"
      } else {
        "false"
      }
    }
  }

  private def locationWhereClause(views: Set[View]): String = {
    if (views.isEmpty) {
      "false"
    } else {
      views.map(view => locationWhereClause(view)).mkString(" or ")
    }
  }

  private def locationWhereClause(view: View): String = {
    val where = mutable.ListBuffer.empty[String]
    view.source.foreach { id =>
      where += s"user = '$id'"
    }
    view.startAfter.foreach { startAfter =>
      where += s"timestamp >= timestamp '$startAfter'"
    }
    view.endBefore.foreach { startAfter =>
      where += s"timestamp <= timestamp '$startAfter'"
    }
    if (where.nonEmpty) where.mkString(" and ") else "true"
  }
}

/**
 * Factory for [[PostgresStore]].
 *
 * @param queryEvaluatorFactory A query evaluator factory
 */
class PostgresStoreFactory(queryEvaluatorFactory: QueryEvaluatorFactory) extends EventStoreFactory {
  override def create(name: String): EventStore = {
    val upperName = name.toUpperCase
    requireEnvVar("QUERULOUS", name, "HOST", "BASE", "PASS", "USER", "TABLE")
    val hostname = sys.env(s"QUERULOUS__${upperName}_HOST")
    val dbname = sys.env(s"QUERULOUS__${upperName}_BASE")
    val username = sys.env(s"QUERULOUS__${upperName}_USER")
    val password = sys.env(s"QUERULOUS__${upperName}_PASS")
    val table = sys.env(s"QUERULOUS__${upperName}_TABLE")
    val driver = DatabaseDriver("postgresql")
    val queryEvaluator = queryEvaluatorFactory.apply(Seq(hostname), dbname, username, password, driver = driver)
    new PostgresStore(queryEvaluator, name, table)
  }
}