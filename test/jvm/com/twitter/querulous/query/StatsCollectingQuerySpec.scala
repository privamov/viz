package com.twitter.querulous.query

import java.sql.ResultSet

import com.twitter.conversions.time._
import com.twitter.querulous.testing.{FakeQuery, FakeStatsCollector}
import com.twitter.util.Time
import fr.cnrs.liris.testing.UnitSpec
import org.scalamock.scalatest.MockFactory

class StatsCollectingQuerySpec extends UnitSpec with MockFactory {
  "StatsCollectingQuery" should "collect stats" in {
    Time.withCurrentTimeFrozen { time =>
      val latency = 1.second
      val stats = new FakeStatsCollector
      val testQuery = new FakeQuery(Seq(mock[ResultSet])) {
        override def select[A](f: ResultSet => A) = {
          time.advance(latency)
          super.select(f)
        }
      }
      val statsCollectingQuery = new StatsCollectingQuery(testQuery, QueryClass.Select, stats)

      statsCollectingQuery.select(_ => 1) shouldBe Seq(1)

      stats.counts("db-select-count") shouldBe 1
      stats.times("db-timing") shouldBe latency.inMilliseconds
    }
  }

  it should "collect timeout stats" in {
    Time.withCurrentTimeFrozen { time =>
      val stats = new FakeStatsCollector
      val testQuery = new FakeQuery(List(mock[ResultSet]))
      val statsCollectingQuery = new StatsCollectingQuery(testQuery, QueryClass.Select, stats)
      val e = new SqlQueryTimeoutException(0.second)

      an[SqlQueryTimeoutException] mustBe thrownBy {
        statsCollectingQuery.select(_ => throw e)
      }

      stats.counts("db-query-timeout-count") shouldBe 1
      stats.counts(s"db-query-${QueryClass.Select.name}-timeout-count") shouldBe 1
    }
  }
}