/*
 * Priva'Mov is a program whose purpose is to collect and analyze mobility traces.
 * Copyright (C) 2016-2017 Vincent Primault <vincent.primault@liris.cnrs.fr>
 *
 * Priva'Mov is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Priva'Mov is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Priva'Mov.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnrs.liris.privamov.indexer

import com.github.nscala_time.time.Imports._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.twitter.app.App
import fr.cnrs.liris.privamov.core.io.{CabspottingSource, GeolifeSource}
import fr.cnrs.liris.privamov.core.model.Trace
import org.elasticsearch.common.settings.Settings

object IndexerJobMain extends IndexerJob

class IndexerJob extends App {
  private[this] val typeFlag = flag[String]("type", "Data source type")
  private[this] val timezoneFlag = flag("timezone", "Europe/Paris", "Data timezone")
  private[this] val addrFlag = flag("addr", "127.0.0.1:9300", "Elasticsearch URI")

  def main(): Unit = {
    val elasticClient = ElasticClient.transport(
      Settings.builder.put("client.transport.ping_timeout", "30s").build,
      ElasticsearchClientUri(addrFlag()))

    val indexer = new Indexer(elasticClient, indexName = "event")
    try {
      args.foreach { url =>
        val dataset = createDataset(typeFlag(), url)
        indexer.run(dataset, typeFlag(), DateTimeZone.forID(timezoneFlag()))
      }
    } finally {
      elasticClient.close()
    }
  }

  private def createDataset(typ: String, url: String): Iterable[Trace] = {
    val source = typ match {
      case "cabspotting" => CabspottingSource(url)
      case "geolife" => GeolifeSource(url)
      case _ => throw new IllegalArgumentException(s"Unknown type '$typ'")
    }
    new Iterable[Trace] {
      override def iterator: Iterator[Trace] = source.keys.iterator.flatMap(source.read)
    }
  }
}