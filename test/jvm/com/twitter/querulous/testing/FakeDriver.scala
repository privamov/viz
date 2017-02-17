package com.twitter.querulous.testing

import java.sql._
import java.util.Properties
import java.util.logging.Logger

import com.twitter.querulous.driver.DatabaseDriver

class FakeDatabaseDriver extends DatabaseDriver {
  override def name: String = FakeDriver.DRIVER_NAME

  override def driverClassName: String = FakeDriver.getClass.getName

  override def defaultUrlOptions: Map[String, String] = Map.empty
}

class FakeDriver extends Driver {
  @throws[SQLException]
  def connect(url: String, info: Properties): Connection = {
    if (acceptsURL(url)) {
      val (user, password) = info match {
        case null => ("root", "")
        case _ => (info.getProperty("user", "root"), info.getProperty("password", ""))
      }
      new FakeConnection(url, info, user, password)
    } else {
      null
    }
  }

  @throws[SQLException]
  def acceptsURL(url: String): Boolean = url.startsWith(FakeDriver.DRIVER_NAME)

  def getMajorVersion: Int = FakeDriver.MAJOR_VERSION

  def getMinorVersion: Int = FakeDriver.MINOR_VERSION

  def jdbcCompliant: Boolean = true

  def getPropertyInfo(url: String, info: Properties): scala.Array[DriverPropertyInfo] =
    scala.Array.empty[DriverPropertyInfo]

  override def getParentLogger: Logger = throw new SQLFeatureNotSupportedException
}

object FakeDriver {
  val MAJOR_VERSION = 1
  val MINOR_VERSION = 0
  val DRIVER_NAME = "jdbc:twitter:querulous:mockdriver"

  try {
    DriverManager.registerDriver(new FakeDriver())
  } catch {
    case e: SQLException => e.printStackTrace()
  }
}
