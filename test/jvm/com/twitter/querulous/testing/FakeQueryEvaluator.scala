package com.twitter.querulous.testing

import java.sql.ResultSet

import com.twitter.querulous.evaluator.{ParamsApplier, QueryEvaluator, Transaction}
import com.twitter.querulous.query.QueryClass

class FakeQueryEvaluator(trans: Transaction, resultSets: Seq[ResultSet]) extends QueryEvaluator {
  override def select[A](queryClass: QueryClass, query: String, params: Any*)(f: ResultSet => A): Seq[A] = resultSets.map(f)

  override def selectOne[A](queryClass: QueryClass, query: String, params: Any*)(f: ResultSet => A): Option[A] = None

  override def count(queryClass: QueryClass, query: String, params: Any*): Int = 0

  override def execute(queryClass: QueryClass, query: String, params: Any*): Int = 0

  override def executeBatch(queryClass: QueryClass, query: String)(f: ParamsApplier => Unit): Int = 0

  override def insert(queryClass: QueryClass, query: String, params: Any*): Long = 0

  override def transaction[T](f: Transaction => T): T = f(trans)

  override def shutdown(): Unit = {}
}