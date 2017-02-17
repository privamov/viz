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

package fr.cnrs.liris.privamov.core.model

import breeze.stats.DescriptiveStats
import com.google.common.base.MoreObjects
import fr.cnrs.liris.common.geo.Point
import fr.cnrs.liris.common.geo.Distance
import fr.cnrs.liris.common.util.Identified

/**
 * A set of POIs belonging to a single user. This is essentially a wrapper around a basic set, providing some
 * useful methods to manipulate POIs.
 *
 * @param user User identifier.
 * @param pois List of unique POIs.
 */
case class PoiSet(user: String, pois: Seq[Poi]) extends Identified {

  override def id: String = user

  /**
   * Check whether the set of POIs is not empty.
   *
   * @return True if there is at least one POI, false otherwise.
   */
  def nonEmpty: Boolean = pois.nonEmpty

  /**
   * Check whether the set of POIs is empty.
   *
   * @return True if there is no POIs, false otherwise.
   */
  def isEmpty: Boolean = pois.isEmpty

  /**
   * Return the number of POIs inside this set.
   */
  def size: Int = pois.size

  /**
   * Compute the minimum distance between POIs inside this set and another POI.
   *
   * @param poi POI to compute the distance with.
   */
  def distance(poi: Poi): Distance = Point.nearest(poi.centroid, pois.map(_.centroid)).distance

  /**
   * Compute the distance with another set of POIs (it is symmetrical).
   *
   * @param that Another set of POIs to compute the distance with.
   */
  def distance(that: PoiSet): Distance = distance(pois, that.pois)

  /**
   * Compute the distance between to sets of POIs (it is symmetrical).
   *
   * @param as First set of POIs.
   * @param bs Second set of POIs.
   */
  private def distance(as: Iterable[Poi], bs: Iterable[Poi]): Distance = {
    val a = as.map(_.centroid)
    val b = bs.map(_.centroid)
    val d = distances(a, b) ++ distances(b, a)
    if (d.nonEmpty) {
      Distance.meters(DescriptiveStats.percentile(d, 0.5))
    } else {
      Distance.Infinity
    }
  }

  /**
   * Compute all distances between each point in `a` and the closest point in `b` (it is *not* symmetrical).
   *
   * @param a A first set of points
   * @param b A second set of points
   */
  private def distances(a: Iterable[Point], b: Iterable[Point]): Iterable[Double] =
  if (b.isEmpty) {
    Iterable.empty[Double]
  } else {
    a.map(point => Point.nearest(point, b).distance.meters).filterNot(_.isInfinite)
  }

  override def toString: String =
    MoreObjects.toStringHelper(this)
      .add("user", user)
      .add("size", size)
      .toString
}

/**
 * Factory for [[PoiSet]].
 */
object PoiSet {
  /**
   * Create a new set of POIs.
   *
   * @param user User identifier.
   * @param pois List of POIs.
   */
  def apply(user: String, pois: Iterable[Poi]): PoiSet = new PoiSet(user, pois.toSeq.distinct)
}