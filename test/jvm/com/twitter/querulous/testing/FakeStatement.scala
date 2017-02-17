package com.twitter.querulous.testing

import java.sql._

import org.apache.commons.dbcp2.TesterStatement

class FakeStatement(conn: Connection, resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY, resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY)
  extends TesterStatement(conn, resultSetType, resultSetConcurrency) {

  @throws[SQLException]
  override def isClosed: Boolean = {
    !this._open
  }

  @throws[SQLException]
  override protected[testing] def checkOpen(): Unit = {
    FakeConnection.checkAliveness(conn.asInstanceOf[FakeConnection])
    if (isClosed) {
      throw new SQLException("Statement is closed")
    }
    if (conn.isClosed) {
      throw new SQLException("Connection is closed")
    }
  }
}