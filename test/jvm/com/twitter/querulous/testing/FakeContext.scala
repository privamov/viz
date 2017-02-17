package com.twitter.querulous.testing

import com.twitter.conversions.time._
import com.twitter.util.Duration

import scala.collection.mutable

object FakeContext {
  private[this] val configMap = mutable.HashMap.empty[String, FakeServerConfig]

  def markServerDown(host: String): Unit = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.isDown = true
        case None => configMap.put(host, FakeServerConfig(isDown = true))
      }
    }
  }

  def markServerUp(host: String): Unit = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.isDown = false
        case None =>
      }
    }
  }

  /**
   * @return true if the server is down; otherwise, false
   */
  def isServerDown(host: String): Boolean = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.isDown
        case None => false
      }
    }
  }

  def setTimeTakenToOpenConn(host: String, numSecs: Duration): Unit = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.timeTakenToOpenConn = numSecs
        case None =>
          if (numSecs.inSeconds > 0) {
            configMap.put(host, FakeServerConfig(timeTakenToOpenConn = numSecs))
          }
      }
    }
  }

  /**
   * @return time taken to open a connection
   */
  def getTimeTakenToOpenConn(host: String): Duration = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.timeTakenToOpenConn
        case None => 0.second
      }
    }
  }

  def setTimeTakenToExecQuery(host: String, numSecs: Duration): Unit = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.timeTakenToExecQuery = numSecs
        case None =>
          if (numSecs.inSeconds > 0) {
            configMap.put(host, FakeServerConfig(timeTakenToExecQuery = numSecs))
          }
      }
    }
  }

  /**
   * @return time taken to exec query
   */
  def getTimeTakenToExecQuery(host: String): Duration = {
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.timeTakenToExecQuery
        case None => 0.second
      }
    }
  }

  def setQueryResult(host: String, statement: String, result: Array[Array[java.lang.Object]]): Unit =
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.resultMap.put(statement, result)
        case None =>
          if (result != null && result.length > 0) {
            configMap.put(host, FakeServerConfig(
              resultMap = mutable.HashMap[String, Array[Array[java.lang.Object]]]((statement, result))))
          }
      }
    }

  def getQueryResult(host: String, statement: String): Array[Array[java.lang.Object]] =
    configMap.synchronized {
      configMap.get(host) match {
        case Some(c) => c.resultMap.getOrElse(statement, Array[Array[java.lang.Object]]())
        case None => Array[Array[java.lang.Object]]()
      }
    }
}

case class FakeServerConfig(
  var isDown: Boolean = false,
  var timeTakenToOpenConn: Duration = 0.seconds,
  var timeTakenToExecQuery: Duration = 0.seconds,
  resultMap: mutable.Map[String, Array[Array[java.lang.Object]]] = mutable.HashMap.empty[String, Array[Array[java.lang.Object]]])
