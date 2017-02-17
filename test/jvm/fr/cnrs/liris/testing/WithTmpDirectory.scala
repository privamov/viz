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

package fr.cnrs.liris.testing

import java.nio.file.{Files, Path}

import fr.cnrs.liris.common.util.FileUtils
import org.scalatest.{BeforeAndAfterEach, Suite}

/**
 * Trait for unit tests that need a temporary directory to be initialized before each test.
 */
trait WithTmpDirectory extends BeforeAndAfterEach {
  this: Suite =>
  private[this] var _tmpDir: Path = null

  protected def tmpDir: Path = _tmpDir

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    _tmpDir = Files.createTempDirectory(getClass.getSimpleName)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    FileUtils.safeDelete(tmpDir)
    _tmpDir = null
  }
}