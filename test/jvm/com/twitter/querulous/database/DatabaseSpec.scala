package com.twitter.querulous.database

import java.sql.Connection

import com.mysql.jdbc.{ConnectionImpl => MySQLConnection}
import com.twitter.conversions.time._
import com.twitter.querulous.ConfiguredSpecification
import org.apache.commons.dbcp2.DelegatingConnection

class DatabaseSpec extends ConfiguredSpecification {
  val defaultProps = Map("socketTimeout" -> "41", "connectTimeout" -> "42")

  "SingleConnectionDatabaseFactory" should "allow specification of default query options" in {
    val factory = new SingleConnectionDatabaseFactory(defaultProps)
    testDefaultQueryOptions(factory)
  }

  it should "allow override of default query options" in {
    val factory = new SingleConnectionDatabaseFactory(defaultProps)
    testOverrideQueryOptions(factory)
  }

  "ApachePoolingDatabaseFactory" should "allow specification of default query options" in {
    val factory = new ApachePoolingDatabaseFactory(10, 10, 1 second, 10 millis, false, 0 seconds, defaultProps)
    testDefaultQueryOptions(factory)
  }

  it should "allow override of default query options" in {
    val factory = new ApachePoolingDatabaseFactory(10, 10, 1 second, 10 millis, false, 0 seconds, defaultProps)
    testOverrideQueryOptions(factory)
  }

  private def mysqlConn(conn: Connection): MySQLConnection = conn match {
    case c: DelegatingConnection[_] =>
      c.getInnermostDelegate.asInstanceOf[MySQLConnection]
    case c: MySQLConnection => c
  }

  private def testOverrideQueryOptions(factory: DatabaseFactory) = skipIfCI {
    val db = factory(
      config.hostnames.toList,
      null,
      config.username,
      config.password,
      Map("connectTimeout" -> "43"))
    val props = mysqlConn(db.open()).getProperties

    props.getProperty("connectTimeout") shouldEqual "43"
    props.getProperty("socketTimeout") shouldEqual "41"
  }


  private def testDefaultQueryOptions(factory: DatabaseFactory) = skipIfCI {
    val db = factory(config.hostnames.toList, null, config.username, config.password)
    val props = mysqlConn(db.open()).getProperties

    props.getProperty("connectTimeout") shouldEqual "42"
    props.getProperty("socketTimeout") shouldEqual "41"
  }
}
