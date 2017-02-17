package com.twitter.querulous.testing

import java.sql.Connection

import com.twitter.conversions.time._
import com.twitter.querulous.database.Database
import com.twitter.querulous.driver.DatabaseDriver

trait FakeDatabase extends Database {
  val driverName = "fake:driver"
  val hosts = Seq("fakehost")
  val name = "fake"
  val username = "fakeuser"
  val openTimeout = 500 millis
  val extraUrlOptions = Map.empty[String, String]
  val driver = DatabaseDriver("mysql")
}

class FakeDBConnectionWrapper(connection: Connection, before: Option[String => Unit])
  extends Database with FakeDatabase {
  def this(connection: Connection) = this(connection, None)

  def this(connection: Connection, before: String => Unit) = this(connection, Some(before))

  override def open(): Connection = {
    before.foreach(_.apply("open"))
    connection
  }

  override def close(connection: Connection): Unit = {
    before.foreach(_.apply("close"))
  }

  override def shutdown(): Unit = {}
}
