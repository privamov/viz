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

package fr.cnrs.liris.privamov.spotme.store

import fr.cnrs.liris.privamov.core.model.Event
import fr.cnrs.liris.privamov.spotme.auth.View
import org.joda.time.LocalDate

case class Source(id: String, count: Long, activeDays: Seq[LocalDate])

/**
 * An event store is responsible for retrieving collected data.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
trait EventStore {
  /**
   * Return this event store name.
   */
  def name: String

  /**
   * Check whether this data provider contains data for the given source.
   *
   * @param id A source identifier
   * @return True if this source exists, false otherwise
   */
  def contains(id: String): Boolean

  /**
   * Return the sources this event store is composed of, ordered in lexicographical order of source
   * identifier.
   */
  def sources(views: Set[View]): Seq[String]

  /**
   * Return metadata about a source.
   *
   * @param id A source identifier
   * @throws IllegalArgumentException If the source does not exist
   */
  @throws[IllegalArgumentException]
  def apply(id: String): Source

  /**
   * Return features from this event store.
   */
  def features(views: Set[View], limit: Option[Int] = None, sample: Boolean = false): Seq[Event]

  def countFeatures(views: Set[View]): Int
}

/**
 * Factory for event stores.
 *
 * @author Vincent Primault <vincent.primault@liris.cnrs.fr>
 */
trait EventStoreFactory {
  /**
   * Create a new event store for the given name (unique among all event stores).
   *
   * @param name A name for the event store
   */
  def create(name: String): EventStore

  final protected def requireEnvVar(prefix: String, name: String, variables: String*): Unit = {
    variables.foreach { v =>
      require(sys.env.contains(s"${prefix}__${name.toUpperCase}_$v"),
        s"You must define environment variable ${prefix}__${name.toUpperCase}_$v")
    }
  }
}