package com.twitter.querulous.query

import java.sql.{Connection, PreparedStatement, SQLException, Types}

import com.twitter.querulous.query.NullValues._
import fr.cnrs.liris.testing.UnitSpec
import org.scalamock.scalatest.MockFactory

class SqlQuerySpec extends UnitSpec with MockFactory {
  "SqlQuery" should "typecast arrays" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects "SELECT * FROM foo WHERE id IN (?,?,?)" returning statement
    (statement.setInt _) expects(1, 1)
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 3)
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    new SqlQuery(connection, "SELECT * FROM foo WHERE id IN (?)", Seq(1, 2, 3)).select { _ => 1 }
  }

  it should "typecast sets" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects "SELECT * FROM foo WHERE id IN (?,?,?)" returning statement
    (statement.setInt _) expects(1, 1)
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 3)
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    new SqlQuery(connection, "SELECT * FROM foo WHERE id IN (?)", Set(1, 2, 3)).select { _ => 1 }
  }

  it should "typecast arrays of pairs" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects "SELECT * FROM foo WHERE (id, uid) IN ((?,?),(?,?))" returning statement
    (statement.setInt _) expects(1, 1)
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 3)
    (statement.setInt _) expects(4, 4)
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    new SqlQuery(connection, "SELECT * FROM foo WHERE (id, uid) IN (?)", List((1, 2), (3, 4))).select { _ => 1 }
  }

  it should "typecast arrays of tuple3s" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]
    (connection.prepareStatement(_: String)) expects "SELECT * FROM foo WHERE (id1, id2, id3) IN ((?,?,?))" returning statement
    (statement.setInt _) expects(1, 1)
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 3)
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    new SqlQuery(connection, "SELECT * FROM foo WHERE (id1, id2, id3) IN (?)", List((1, 2, 3))).select { _ => 1 }
  }

  it should "typecast arrays of tuple4s" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects "SELECT * FROM foo WHERE (id1, id2, id3, id4) IN ((?,?,?,?))" returning statement
    (statement.setInt _) expects(1, 1)
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 3)
    (statement.setInt _) expects(4, 4)
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    new SqlQuery(connection, "SELECT * FROM foo WHERE (id1, id2, id3, id4) IN (?)", List((1, 2, 3, 4))).select { _ => 1 }
  }

  it should "create a query string" in {
    val queryString = "INSERT INTO table (col1, col2, col3, col4, col5) VALUES (?, ?, ?, ?, ?)"
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects queryString returning statement
    (statement.setString _) expects(1, "one")
    (statement.setInt _) expects(2, 2)
    (statement.setInt _) expects(3, 0x03)
    (statement.setLong _) expects(4, 4L)
    (statement.setDouble _) expects(5, 5d)
    (statement.executeUpdate _) expects()

    new SqlQuery(connection, queryString, "one", 2, 0x03, 4L, 5.0).execute()
  }

  it should "insert nulls" in {
    val queryString = "INSERT INTO TABLE (null1, null2, null3, null4, null5, null6) VALUES (?, ?, ?, ?, ?, ?)"
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects queryString returning statement
    (statement.setNull(_: Int, _: Int)) expects(1, Types.VARCHAR)
    (statement.setNull(_: Int, _: Int)) expects(2, Types.INTEGER)
    (statement.setNull(_: Int, _: Int)) expects(3, Types.DOUBLE)
    (statement.setNull(_: Int, _: Int)) expects(4, Types.BOOLEAN)
    (statement.setNull(_: Int, _: Int)) expects(5, Types.BIGINT)
    (statement.setNull(_: Int, _: Int)) expects(6, Types.VARBINARY)
    new SqlQuery(connection, queryString, NullString, NullInt, NullDouble, NullBoolean, NullLong, NullValues(Types.VARBINARY))
      .execute()
  }

  it should "throw illegal argument exception if type passed in is unrecognized" in {
    val queryString = "INSERT INTO TABLE (col1) VALUES (?)"
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]
    val unrecognizedType = connection

    (connection.prepareStatement(_: String)) expects queryString returning statement

    an[IllegalArgumentException] shouldBe thrownBy {
      new SqlQuery(connection, queryString, unrecognizedType).execute()
    }
  }

  it should "throw chained-exception" in {
    val queryString = "INSERT INTO TABLE (col1) VALUES (?)"
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]
    val expectedCauseException = new SQLException("")

    (connection.prepareStatement(_: String)) expects queryString returning statement
    (statement.setString _) expects(1, "one") throwing expectedCauseException

    try {
      new SqlQuery(connection, queryString, "one").execute()
      fail("should throw")
    } catch {
      case e: Exception => e.getCause shouldBe expectedCauseException
      case e: Throwable => fail("unknown throwable")
    }
  }

  it should "add annotations to query" in {
    val connection = mock[Connection]
    val statement = mock[PreparedStatement]

    (connection.prepareStatement(_: String)) expects "select * from table /*~{\"key\" : \"value2\", " +
      "\"key2\" : \"*\\/select 1\", \"key3\" : \"{:}\"}*/" returning statement
    (statement.executeQuery _) expects()
    (statement.getResultSet _) expects()

    val query = new SqlQuery(connection, "select * from table")
    query.annotate("key", "value")
    query.annotate("key", "value2") // we'll only keep this
    query.annotate("key2", "*/select 1") // trying to end the comment early
    query.annotate("key3", "{:}") // going all json on your ass
    query.select(result => fail("should not return any data"))
  }
}