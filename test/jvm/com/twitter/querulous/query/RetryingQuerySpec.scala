package com.twitter.querulous.query

import java.sql.{ResultSet, SQLException}

import com.twitter.querulous.testing.FakeQuery
import fr.cnrs.liris.testing.UnitSpec
import org.scalamock.scalatest.MockFactory

class RetryingQuerySpec extends UnitSpec with MockFactory {
  val retries = 5

  "RetryingQuery" should "when the operation throws a SQLException" in {
    var tries = 0
    val query = new FakeQuery(Seq(mock[ResultSet])) {
      override def select[A](f: ResultSet => A) = {
        tries += 1
        if (tries < retries) {
          throw new SQLException
        }
        super.select(f)
      }
    }
    val retryingQuery = new RetryingQuery(query, retries)

    retryingQuery.select(r => 1) shouldBe Seq(1)
    tries shouldBe retries
  }

  it should "when the operation throws a non-SQLException" in {
    var tries = 0
    val query = new FakeQuery(List(mock[ResultSet])) {
      override def select[A](f: ResultSet => A) = {
        tries += 1
        throw new Exception
      }
    }
    val retryingQuery = new RetryingQuery(query, retries)

    an[Exception] shouldBe thrownBy {
      retryingQuery.select { r => 1 }
    }
    tries shouldBe 1
  }
}
