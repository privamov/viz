package com.twitter.querulous.testing

import com.twitter.querulous.StatsCollector

import scala.collection.mutable
import scala.compat.Platform

class FakeStatsCollector extends StatsCollector {
  val counts = mutable.Map.empty[String, Int]
  val times = mutable.Map.empty[String, Long]

  override def incr(name: String, count: Int): Unit = {
    counts(name) = count + counts.getOrElseUpdate(name, 0)
  }

  override def time[A](name: String)(f: => A): A = {
    val start = Platform.currentTime
    val rv = f
    val end = Platform.currentTime
    times(name) = (end - start) + times.getOrElseUpdate(name, 0L)
    rv
  }
}