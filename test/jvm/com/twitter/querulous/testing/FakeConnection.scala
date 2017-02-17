package com.twitter.querulous.testing

import java.net.SocketException
import java.sql._
import java.util.Properties

import com.twitter.querulous.DestroyableConnection
import org.apache.commons.dbcp2.TesterConnection

import scala.util.control.NonFatal

class CommunicationsException(cause: Throwable = null) extends Exception(cause)

class FakeConnection(val url: String, val info: Properties, val user: String, val passwd: String)
  extends TesterConnection(user, passwd) with DestroyableConnection {
  private[testing] val host = FakeConnection.host(url)
  private[this] val properties: Properties = FakeConnection.properties(url, info)
  private[this] var destroyed: Boolean = false

  FakeConnection.checkAliveness(host)

  FakeConnection.checkTimeout(host, properties)

  override def destroy(): Unit = {
    close()
    destroyed = true
  }

  @throws(classOf[SQLException])
  override def createStatement(): Statement = {
    checkOpen()
    new FakeStatement(this)
  }

  @throws(classOf[SQLException])
  override def prepareStatement(sql: String): PreparedStatement = {
    checkOpen()
    new FakePreparedStatement(this, sql)
  }

  @throws(classOf[SQLException])
  override protected def checkOpen(): Unit = {
    if (this.isClosed) {
      throw new SQLException("Connection has been closed")
    }

    try {
      FakeConnection.checkAliveness(host)
    } catch {
      case e: CommunicationsException =>
        this.close()
        throw e
      case NonFatal(e) => throw e
    }
    FakeConnection.checkAliveness(host)
  }

}

object FakeConnection {
  def host(url: String): String = {
    if (url == null || url == "") {
      ""
    } else {
      url.indexOf('?') match {
        case -1 => url.substring(FakeDriver.DRIVER_NAME.length + 3)
        case _ => url.substring(FakeDriver.DRIVER_NAME.length + 3, url.indexOf('?'))
      }
    }
  }

  def properties(url: String, info: Properties): Properties = {
    url.indexOf('?') match {
      case -1 => info
      case _ => {
        val newInfo = new Properties(info)
        url.substring(url.indexOf('?') + 1).split("&") foreach { nameVal =>
          nameVal.split("=").toList match {
            case Nil =>
            case n :: Nil =>
            case n :: v :: _ => newInfo.put(n, v)
          }
        }
        newInfo
      }
    }
  }

  @throws[CommunicationsException]
  def checkAliveness(host: String): Unit = {
    if (FakeContext.isServerDown(host)) {
      throw new CommunicationsException(new Exception("Communication link failure"))
    }
  }

  @throws[CommunicationsException]
  def checkAliveness(conn: FakeConnection): Unit = {
    if (FakeContext.isServerDown(conn.host)) {
      // real driver mark the connection as closed when running into communication problem too
      conn.close()
      throw new CommunicationsException(new Exception("Communication link failure"))
    }
  }

  @throws[SocketException]
  def checkTimeout(host: String, properties: Properties): Unit = {
    val connectTimeoutInMillis: Long = Option(properties) match {
      case None => 0L
      case Some(_) => properties.getProperty("connectTimeout", "0").toLong
    }

    val timeTakenToOpenConnInMiills = FakeContext.getTimeTakenToOpenConn(host).inMillis
    if (timeTakenToOpenConnInMiills > connectTimeoutInMillis) {
      Thread.sleep(connectTimeoutInMillis)
      throw new SocketException("Connection timeout")
    } else {
      Thread.sleep(timeTakenToOpenConnInMiills)
    }
  }
}
