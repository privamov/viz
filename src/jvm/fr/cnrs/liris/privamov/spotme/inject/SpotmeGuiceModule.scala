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

package fr.cnrs.liris.privamov.spotme.inject

import com.twitter.inject.TwitterModule
import fr.cnrs.liris.privamov.spotme.store.EventStoreFactory
import net.codingwell.scalaguice.ScalaMapBinder

object SpotmeGuiceModule extends TwitterModule {
  flag[String]("viz.stores", "", "Comma-separated list of stores to use (type.name)")
  flag[Int]("viz.standard_limit", 100, "Maximum number of elements that can be retrieved in standard listings)")
  flag[Int]("viz.extended_limit", 2000, "Maximum number of elements that can be retrieved in extended listings)")

  override protected def configure(): Unit = {
    // Event stores are configured as plugins added to this map binder.
    ScalaMapBinder.newMapBinder[String, EventStoreFactory](binder)
  }
}