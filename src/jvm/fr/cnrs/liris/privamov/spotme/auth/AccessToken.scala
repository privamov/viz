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

package fr.cnrs.liris.privamov.spotme.auth

import fr.cnrs.liris.privamov.spotme.store.EventStore

case class AccessToken(scopes: Set[Scope], acl: AccessControlList) {
  def in(scope: Scope): Boolean = scopes.contains(scope)
}

object AccessToken {
  def everything: AccessToken = new AccessToken(Scope.values, AccessControlList.everything)

  def apply(scopes: Set[Scope], aces: AccessControlEntry*): AccessToken =
    new AccessToken(scopes, AccessControlList(aces.toSet))
}

sealed trait Scope

object Scope {

  case object Datasets extends Scope

  case object Exports extends Scope

  def values: Set[Scope] = Set(Datasets, Exports)

}

case class AccessControlList(entries: Set[AccessControlEntry]) {
  def accessible(store: EventStore): Boolean =
    entries.exists(e => e.dataset.isEmpty || e.dataset.contains(store.name))

  def resolve(dataset: String): AccessControlEntry = {
    val allowedViews = entries.filter(e => e.dataset.isEmpty || e.dataset.contains(dataset)).flatMap(_.views)
    AccessControlEntry(Some(dataset), allowedViews)
  }
}

object AccessControlList {
  def everything: AccessControlList = new AccessControlList(Set(AccessControlEntry.everything))

  def empty: AccessControlList = new AccessControlList(Set.empty)
}

case class AccessControlEntry(dataset: Option[String], views: Set[View]) {
  def canonicalize: AccessControlEntry = {
    var prevSize = 0
    var res = views
    do {
      prevSize = res.size
      res = res.filterNot(v1 => res.exists(v2 => v1 != v2 && v2.includes(v1)))
    } while (res.size > 1 && res.size < prevSize)
    AccessControlEntry(dataset, views)
  }

  def resolve(views: Set[View]): AccessControlEntry = {
    val allowedViews = views.flatMap(_.restrict(this.views))
    val ace = AccessControlEntry(dataset, allowedViews)
    ace.canonicalize
  }
}

object AccessControlEntry {
  def everything: AccessControlEntry = new AccessControlEntry(None, Set(View.everything))
}