package com.twitter.querulous.testing

import java.sql.ResultSet

import com.twitter.querulous.query.Query

class FakeQuery(resultSets: Seq[ResultSet]) extends Query {
  override def cancel(): Unit = {}

  override def select[A](f: ResultSet => A): Seq[A] = {
    resultSets.map(f)
  }

  override def execute(): Int = 0

  override def addParams(params: Any*): Unit = {}
}