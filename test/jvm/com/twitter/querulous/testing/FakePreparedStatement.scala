package com.twitter.querulous.testing

import java.sql._

import org.apache.commons.dbcp2.TesterPreparedStatement

class FakePreparedStatement(
  val conn: Connection,
  val sql: String,
  var resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY,
  var resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY
) extends TesterPreparedStatement(conn, sql, resultSetType, resultSetConcurrency) with PreparedStatement {

  private[this] var resultSet: Option[ResultSet] = None

  @throws[SQLException]
  override def executeQuery(sqlQuery: String): ResultSet = {
    checkOpen()
    takeTimeToExecQuery()
    resultSet = Some(new FakeResultSet(this, getFakeResult(sqlQuery), resultSetType, resultSetConcurrency))
    resultSet.get
  }

  @throws[SQLException]
  override def executeQuery(): ResultSet = {
    checkOpen()
    takeTimeToExecQuery()
    resultSet = Some(new FakeResultSet(this, getFakeResult(this.sql), resultSetType, resultSetConcurrency))
    resultSet.get
  }

  @throws[SQLException]
  override def getResultSet: ResultSet = {
    checkOpen()
    resultSet.orNull
  }

  private[this] def getFakeResult(query: String): scala.Array[scala.Array[java.lang.Object]] = {
    FakeContext.getQueryResult(this.conn.asInstanceOf[FakeConnection].host, query)
  }

  @throws[SQLException]
  override def isClosed: Boolean = {
    !this._open
  }

  @throws[SQLException]
  override protected[testing] def checkOpen(): Unit = {
    FakeConnection.checkAliveness(conn.asInstanceOf[FakeConnection])
    if (this.isClosed) {
      throw new SQLException("Statement is closed")
    }
    if (conn.isClosed) {
      throw new SQLException("Connection is closed")
    }
  }

  private def takeTimeToExecQuery(): Unit = {
    Thread.sleep(FakeContext.getTimeTakenToExecQuery(conn.asInstanceOf[FakeConnection].host).inMillis)
  }
}
