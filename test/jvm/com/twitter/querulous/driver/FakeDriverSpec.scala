package com.twitter.querulous.driver

import java.sql.{DriverManager, SQLException}

import com.twitter.conversions.time._
import com.twitter.querulous.ConfiguredSpecification
import com.twitter.querulous.database._
import com.twitter.querulous.testing._
import org.apache.commons.dbcp2.DelegatingConnection

class FakeDriverSpec extends ConfiguredSpecification {
  val host = config.hostnames.mkString(",")
  val connString = FakeDriver.DRIVER_NAME + "://" + host

  private def testFactory(factory: DatabaseFactory) = {
    val db = factory(config.hostnames.toList, null, config.username, config.password, Map.empty, new FakeDatabaseDriver)
    val conn = db.open() match {
      case c: DelegatingConnection[_] => c.getInnermostDelegate
      case c => c
    }

    conn.getClass.getSimpleName shouldBe "FakeConnection"
  }

  "SingleConnectionDatabaseFactory" should "the real connection shouldBe FakeConnection" in {
    val factory = new SingleConnectionDatabaseFactory(Map.empty)
    testFactory(factory)
  }

  "ApachePoolingDatabaseFactory" should "the real connection shouldBe FakeConnection" in {
    val factory = new ApachePoolingDatabaseFactory(
      minOpenConnections = 10,
      maxOpenConnections = 10,
      checkConnectionHealthWhenIdleFor = 1.second,
      maxWaitForConnectionReservation = 10.millis,
      checkConnectionHealthOnReservation = false,
      evictConnectionIfIdleFor = 0.second,
      defaultUrlOptions = Map.empty)
    testFactory(factory)
  }

  "ThrottledPoolingDatabaseFactory" should "the real connection shouldBe FakeConnection" in {
    val factory = new ThrottledPoolingDatabaseFactory(10, 1.second, 10.millis, 1.seconds, Map.empty)
    testFactory(factory)
  }

  "Getting connection from FakeDriver" should "return a FakeConnection" in {
    DriverManager.getConnection(connString, null) shouldBe a[FakeConnection]
  }

  it should "throw an exception when db is marked down" in {
    FakeContext.markServerDown(host)
    try {
      an[CommunicationsException] shouldBe thrownBy {
        DriverManager.getConnection(connString, null)
      }
    } finally {
      FakeContext.markServerUp(host)
    }
  }

  "Prepareing statement" should "throw an exception when underlying connection is closed" in {
    val conn = DriverManager.getConnection(connString, null)
    conn.close()
    an[SQLException] shouldBe thrownBy {
      conn.prepareStatement("SELECT 1 FROM DUAL")
    }
  }

  it should "throw an exception when underlying db is down" in {
    val conn = DriverManager.getConnection(connString, null)
    try {
      FakeContext.markServerDown(host)
      an[CommunicationsException] shouldBe thrownBy {
        conn.prepareStatement("SELECT 1 FROM DUAL")
      }
    } finally {
      FakeContext.markServerUp(host)
    }
  }

  "FakePreparedStatement" should "throw an exception when trying to executeQuery while underlying connection is closed" in {
    val conn = DriverManager.getConnection(connString, null)
    val stmt = conn.prepareStatement("SELECT 1 FROM DUAL")
    conn.close()
    an[SQLException] shouldBe thrownBy {
      stmt.executeQuery()
    }
  }

  it should "throw an exception when trying to executeQuery while underlying db is down" in {
    val conn = DriverManager.getConnection(connString, null)
    val stmt = conn.prepareStatement("SELECT 1 FROM DUAL")
    try {
      FakeContext.markServerDown(host)
      an[CommunicationsException] shouldBe thrownBy {
        stmt.executeQuery()
      }
    } finally {
      FakeContext.markServerUp(host)
    }
  }

  private def setFakeResult(): Unit =
    FakeContext.setQueryResult(host, "SELECT 1 FROM DUAL", Array(Array[java.lang.Object](1.asInstanceOf[AnyRef])))

  "FakeResultSet" should "return result as being registered" in {
    setFakeResult()
    val stmt = DriverManager.getConnection(connString, null).prepareStatement("SELECT 1 FROM DUAL")
    val rs = stmt.executeQuery()
    rs.next() shouldBe true
    rs.getInt(1) shouldBe 1
    rs.next() shouldBe false
  }

  it should "throw an exception when iterating through a closed one" in {
    setFakeResult()
    val stmt = DriverManager.getConnection(connString, null).prepareStatement("SELECT 1 FROM DUAL")
    val rs = stmt.executeQuery()
    rs.close()
    an[SQLException] shouldBe thrownBy {
      rs.next()
    }
  }

  it should "throw an exception when iterating through it while underlying connection is closed" in {
    setFakeResult()
    val conn = DriverManager.getConnection(connString, null)
    val stmt = conn.prepareStatement("SELECT 1 FROM DUAL")
    val rs = stmt.executeQuery()
    conn.close()
    an[SQLException] shouldBe thrownBy {
      rs.next()
    }
  }

  it should "throw an exception when iterating through it while underlying db is down" in {
    setFakeResult()
    val stmt = DriverManager.getConnection(connString, null).prepareStatement("SELECT 1 FROM DUAL")
    val rs = stmt.executeQuery()
    try {
      FakeContext.markServerDown(host)
      an[CommunicationsException] shouldBe thrownBy {
        rs.next()
      }
    } finally {
      FakeContext.markServerUp(host)
    }
  }
}
