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

import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import fr.cnrs.liris.common.geo.LatLng
import fr.cnrs.liris.privamov.core.model.{Event, Trace}
import fr.cnrs.liris.privamov.spotme.auth.View
import org.joda.time.Instant

import scala.collection.JavaConverters._

class FilesystemStore(root: Path, override val name: String) extends EventStore with LazyLogging {
  override def contains(id: String): Boolean = traceFile(id).toFile.exists

  override def sources(views: Set[View]): Seq[String] = {
    if (views.isEmpty) {
      Seq.empty
    } else {
      val ids = root
        .toFile
        .list
        .toSeq
        .filter(_.endsWith(".csv"))
        .map(filename => filename.take(filename.indexOf(".")))
        .sorted
      if (views.isEmpty || views.exists(_.source.isEmpty)) {
        ids
      } else {
        ids.intersect(views.flatMap(_.source).toSeq)
      }
    }
  }

  override def apply(id: String): Source = {
    val trace = readTrace(id)
    val activeDays = trace.events.map(_.time.toDateTime.toLocalDate).distinct
    Source(id, trace.size, activeDays)
  }

  override def features(views: Set[View], limit: Option[Int], sample: Boolean): Seq[Event] = {
    val traces = sources(views).map(readTrace).map { trace =>
      val maybeView = views.find(_.source == trace.user).orElse(views.find(_.source.isEmpty))
      maybeView match {
        case Some(view) => trace.filter { event =>
          (view.area.isEmpty || view.area.get.contains(event.point)) &&
            (view.startAfter.isEmpty || event.time.isAfter(view.startAfter.get)) &&
            (view.endBefore.isEmpty || event.time.isBefore(view.endBefore.get))
        }
        case None => trace
      }
    }.filter(_.nonEmpty)
    var events = traces.flatMap(_.events).sorted
    if (limit.isDefined) {
      if (sample) {
        val outOf = events.size.toDouble / limit.get
        events = events.zipWithIndex.filter { case (_, idx) => idx % outOf < 1 }.map(_._1)
      } else {
        events = events.take(limit.get)
      }
    }
    events
  }

  override def countFeatures(views: Set[View]): Int = features(views).size

  private def traceFile(id: String) = root.resolve(s"$id.csv")

  private def readTrace(id: String) = {
    val file = traceFile(id)
    if (file.toFile.exists) {
      val events = Files.readAllLines(file)
        .asScala
        .zipWithIndex
        .flatMap { case (line, idx) => readLine(file.getFileName.toString, idx, line) }
      Trace(id, events)
    } else {
      Trace(id, Seq.empty)
    }
  }

  private def readLine(filename: String, idx: Int, line: String): Option[Event] = {
    if (line.isEmpty) {
      None
    } else {
      val parts = line.split(",")
      if (parts.length < 3 || parts.length > 4) {
        logger.warn(s"Invalid line in $filename at $idx")
        None
      } else {
        val (user, lat, lng, time) = if (parts.length == 4) {
          (parts(0), parts(1).toDouble, parts(2).toDouble, parts(3).toLong)
        } else {
          (filename.take(filename.indexOf(".")), parts(0).toDouble, parts(1).toDouble, parts(2).toLong)
        }
        val point = LatLng.degrees(lat, lng).toPoint
        Some(new Event(user, point, new Instant(time), Map.empty))
      }
    }
  }
}

/**
 * Factory for [[FilesystemStore]].
 */
class FilesystemStoreFactory extends EventStoreFactory {
  override def create(name: String): EventStore = {
    val upperName = name.toUpperCase
    requireEnvVar("FILESYSTEM", name, "ROOT")
    val root = Paths.get(sys.env(s"FILESYSTEM__${upperName}_ROOT"))
    new FilesystemStore(root, name)
  }
}