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

package fr.cnrs.liris.privamov.core.io

import java.nio.file.{Files, Path, Paths}

import com.google.common.base.MoreObjects
import fr.cnrs.liris.common.geo.LatLng
import fr.cnrs.liris.privamov.core.model.{Event, Trace}
import org.joda.time.Instant

import scala.reflect._
import scala.sys.process._

/**
 * Support for the [[http://research.microsoft.com/apps/pubs/?id=152176 Geolife dataset]].
 * Each trace is stored inside its own directory splitted into multiple roughly one per day,
 * oldest events first.
 *
 * @param uri Path to the directory from where to read.
 */
case class GeolifeSource(uri: String) extends DataSource[Trace] {
  private[this] val path = Paths.get(uri)
  private[this] val decoder = new TextLineDecoder(new GeolifeDecoder, headerLines = 6)
  require(path.toFile.isDirectory, s"$uri is not a directory")
  require(path.toFile.canRead, s"$uri is unreadable")

  override lazy val keys: Seq[String] =
    path.toFile
      .listFiles
      .filter(_.isDirectory)
      .flatMap(_.toPath.resolve("Trajectory").toFile.listFiles)
      .map(_.toPath.getParent.getParent.getFileName.toString)
      .toSeq
      .sorted

  override def read(key: String): Option[Trace] = {
    val events = path.resolve("Trajectory").toFile.listFiles.sortBy(_.getName).flatMap(file => read(key, file.toPath))
    if (events.nonEmpty) Some(Trace(events)) else None
  }

  override def toString: String =
    MoreObjects.toStringHelper(this)
      .add("uri", uri)
      .toString

  private def read(key: String, path: Path) =
    decoder.decode(key, Files.readAllBytes(path.resolve(s"$key.plt"))).getOrElse(Seq.empty)
}

/**
 * Factory for [[GeolifeSource]].
 */
object GeolifeSource {
  def download(dest: Path): GeolifeSource = {
    val zipFile = dest.resolve("geolife.zip").toFile
    var exitCode = (s"curl -L http://ftp.research.microsoft.com/downloads/b16d359d-d164-469e-9fd4-daa38f2b2e13/Geolife\\ Trajectories\\ 1.3.zip" #> zipFile).!
    if (0 != exitCode) {
      throw new RuntimeException(s"Error while fetching the Geolife archive: $exitCode")
    }
    exitCode = s"unzip -d ${dest.toAbsolutePath} ${zipFile.getAbsolutePath}".!
    if (0 != exitCode) {
      throw new RuntimeException(s"Error while extracting the Geolife archive: $exitCode")
    }
    zipFile.delete()
    new GeolifeSource(dest.toAbsolutePath.toString)
  }
}

class GeolifeDecoder extends Decoder[Event] {
  override def decode(key: String, bytes: Array[Byte]): Option[Event] = {
    val line = new String(bytes)
    val parts = line.trim.split(",")
    if (parts.length < 7) {
      None
    } else {
      val lat = parts(0).toDouble
      val lng = parts(1).toDouble
      val time = Instant.parse(s"${parts(5)}T${parts(6)}Z")
      try {
        Some(Event(key, LatLng.degrees(lat, lng).toPoint, time))
      } catch {
        //Error in original data, skip event.
        case e: IllegalArgumentException => None
      }
    }
  }

  override def elementClassTag: ClassTag[Event] = classTag[Event]
}
