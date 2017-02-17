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

import com.google.inject.{Inject, Singleton}
import com.twitter.inject.annotations.Flag

@Singleton
class StoreRegistry @Inject()(
    @Flag("viz.stores") specs: String,
    factories: Map[String, EventStoreFactory])
  extends Iterable[EventStore] {
  private[this] val stores = specs.split(",").filter(_.nonEmpty).map(str => createStore(str.trim)).toMap

  def contains(name: String): Boolean = stores.contains(name)

  def get(name: String): Option[EventStore] = stores.get(name)

  def apply(name: String): EventStore = stores(name)

  override def iterator: Iterator[EventStore] = stores.values.iterator

  private def createStore(str: String) = {
    val pos = str.indexOf(":")
    val (name, typ) = if (pos > -1) {
      (str.substring(0, pos), str.substring(pos + 1))
    } else {
      (str, str)
    }
    require(factories.contains(typ),
      s"Invalid store type for '$str' (got: $typ, available: ${factories.keys.mkString(", ")})")
    name -> factories(typ).create(name)
  }
}