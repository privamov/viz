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

package fr.cnrs.liris.privamov.testing

import com.google.common.io.Resources
import fr.cnrs.liris.privamov.core.io.{CsvEventCodec, CsvTraceCodec}
import fr.cnrs.liris.privamov.core.model.Trace

trait WithCabspotting {
  // It doesn't work with a val..
  private def decoder = new CsvTraceCodec(new CsvEventCodec)

  lazy val abboipTrace = cabspottingTrace("abboip")

  def cabspottingTrace(key: String): Trace = {
    val bytes = Resources.toByteArray(Resources.getResource(s"fr/cnrs/liris/privamov/testing/$key.csv"))
    decoder.decode(key, bytes).get
  }
}