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

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import com.twitter.conversions.time._
import com.twitter.finatra.httpclient.{HttpClient, RequestBuilder}
import com.twitter.util.{Await, Try}
import org.joda.time.DateTime

/**
 * A lending represents a device lent to somebody.
 *
 * @param imei      Device's IMEI
 * @param firstName Person's first name
 * @param lastName  Person's first name
 * @param email     Person's email address
 * @param segment   Campaign identifier
 * @param started   Lending start time
 * @param ended     Lending end time (not set if the lending is still on-going)
 */
case class Lending(
    imei: String,
    segment: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    email: Option[String],
    started: DateTime,
    ended: Option[DateTime])

/**
 * Simple client providing access to the Fleet API.
 *
 * @param httpClient HTTP client, bound to Fleet server
 */
@Singleton
class FleetClient @Inject()(@Named("fleet") httpClient: HttpClient) {
  /**
   * Return the lending associated with a given token, if it exists.
   *
   * @param token Token
   */
  def getLending(token: String): Option[Lending] = {
    val future = httpClient.executeJson[Lending](RequestBuilder.get(s"/fleet/api/lending/$token"))
    Try {
      Await.result(future, 5.seconds)
    }.toOption
  }
}