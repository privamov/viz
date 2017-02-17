package com.twitter.querulous.unit

import java.sql.{Connection, SQLException}

import com.twitter.querulous.ConfiguredSpecification
import com.twitter.querulous.TestEvaluator._
import com.twitter.querulous.evaluator.StandardQueryEvaluator
import com.twitter.querulous.query._
import com.twitter.querulous.test.FakeDBConnectionWrapper
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter

import scala.collection.mutable

class QueryEvaluatorSpec extends ConfiguredSpecification with MockFactory with BeforeAndAfter {
  val queryEvaluator = testEvaluatorFactory(config)
  val rootQueryEvaluator = testEvaluatorFactory(config.withoutDatabase)
  val queryFactory = new SqlQueryFactory

  before {
    rootQueryEvaluator.execute("CREATE DATABASE IF NOT EXISTS db_test")
  }

  after {
    queryEvaluator.execute("DROP TABLE IF EXISTS foo")
  }

  "QueryEvaluator" should "connection pooling transactionally" in {
    val connection = mock[Connection]
    val database = new FakeDBConnectionWrapper(connection)
    val queryEvaluator = new StandardQueryEvaluator(database, queryFactory)
    (connection.setAutoCommit _) expects false
    (connection.prepareStatement(_)) expects "SELECT 1"
    (connection.commit _) expects()
    (connection.setAutoCommit _) expects true

    queryEvaluator.transaction { transaction =>
      transaction.selectOne("SELECT 1") {
        _.getInt("1")
      }
    }
  }

  it should "connection pooling nontransactionally" in {
    val connection = mock[Connection]
    val database = new FakeDBConnectionWrapper(connection)
    val queryEvaluator = new StandardQueryEvaluator(database, queryFactory)

    (connection.prepareStatement(_)) expects "SELECT 1"
    queryEvaluator.selectOne("SELECT 1") {
      _.getInt("1")
    }
  }

  it should "select rows" in {
    var list = new mutable.ListBuffer[Int]
    queryEvaluator.select("SELECT 1 as one") {
      resultSet =>
        list += resultSet.getInt("one")
    }
    list.toList shouldEqual List(1)
  }

  it should "fallback to a read slave" in {
    // should always succeed if you have the right mysql driver.
    val queryEvaluator = testEvaluatorFactory(
      "localhost:12349" :: config.hostnames.toList, config.database, config.username, config.password)
    queryEvaluator.selectOne("SELECT 1") {
      row => row.getInt(1)
    }.toList shouldEqual List(1)

    an[SQLException] shouldBe thrownBy {
      queryEvaluator.execute("CREATE TABLE foo (id INT)")
    }
  }

  it should "transaction when there is an exception" in {
    queryEvaluator.execute("CREATE TABLE foo (bar INT) ENGINE=INNODB")
    try {
      queryEvaluator.transaction {
        transaction =>
          transaction.execute("INSERT INTO foo VALUES (1)")
          throw new Exception("oh noes")
      }
    } catch {
      case _: Throwable =>
    }
    queryEvaluator.select("SELECT * FROM foo")(_.getInt("bar")).toList shouldEqual Nil
  }

  it should "transaction when there is not an exception" in {
    queryEvaluator.execute("CREATE TABLE foo (bar VARCHAR(50), baz INT) ENGINE=INNODB")
    queryEvaluator.transaction(t => t.execute("INSERT INTO foo VALUES (?, ?)", "one", 2))
    val list = queryEvaluator.select("SELECT * FROM foo")(row => (row.getString("bar"), row.getInt("baz"))).toList
    list shouldEqual List(("one", 2))
  }
}