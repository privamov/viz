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

package fr.cnrs.liris.privamov.query

import com.google.inject.{Provides, Singleton}
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.twitter.inject.TwitterModule

object PrivamovQueryModule extends TwitterModule {
  val elasticUri = flag("elastic_uri", "localhost:9300", "Elasticsearch URI")

  @Provides
  @Singleton
  def providesElasticClient(): ElasticClient = {
    ElasticClient.transport(ElasticsearchClientUri(elasticUri()))
      //Settings.settingsBuilder.put("client.transport.ping_timeout", "30s").build,
  }
}