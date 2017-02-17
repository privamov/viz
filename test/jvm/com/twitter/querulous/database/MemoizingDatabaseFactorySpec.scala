package com.twitter.querulous.database

import com.twitter.querulous.driver.DatabaseDriver
import fr.cnrs.liris.testing.UnitSpec
import org.scalamock.scalatest.MockFactory

class MemoizingDatabaseFactorySpec extends UnitSpec with MockFactory {
  val username = "username"
  val password = "password"
  val hosts = List("foo")

  "MemoizingDatabaseFactory" should "apply" in {
    val database1 = mock[Database]
    val database2 = mock[Database]
    val databaseFactory = mock[DatabaseFactory]
    val memoizingDatabase = new MemoizingDatabaseFactory(databaseFactory)

    (databaseFactory.apply(_: Seq[String], _: String, _: String, _: String, _: Map[String, String], _: DatabaseDriver)) expects(hosts, "bar", username, password, Map.empty[String, String], Database.DefaultDriver) returning database1
    (databaseFactory.apply(_: Seq[String], _: String, _: String, _: String, _: Map[String, String], _: DatabaseDriver)) expects(hosts, "bar", username, password, Map.empty[String, String], Database.DefaultDriver) returning database2

    memoizingDatabase(hosts, "bar", username, password) shouldBe database1
    memoizingDatabase(hosts, "bar", username, password) shouldBe database1
    memoizingDatabase(hosts, "baz", username, password) shouldBe database2
    memoizingDatabase(hosts, "baz", username, password) shouldBe database2
  }

  it should "not cache" in {
    val database = mock[Database]
    val factory = mock[DatabaseFactory]
    val memoizingDatabase = new MemoizingDatabaseFactory(factory)

    (factory.apply(_: Seq[String], _: String, _: String)) expects(hosts, username, password) returning database twice

    memoizingDatabase(hosts, username, password) shouldBe database
    memoizingDatabase(hosts, username, password) shouldBe database
  }
}
