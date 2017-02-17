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

package fr.cnrs.liris.common.util

import java.nio.file.Files

import fr.cnrs.liris.testing.UnitSpec

/**
 * Unit tests for [[FileUtils]].
 */
class FileUtilsSpec extends UnitSpec {
  "FileUtils::safeDelete" should "delete a directory" in {
    val dir = Files.createTempDirectory("FileUtilsSpec-")
    dir.toFile.deleteOnExit()
    dir.toFile.exists() shouldBe true

    FileUtils.safeDelete(dir)
    dir.toFile.exists() shouldBe false
  }

  it should "delete a file" in {
    val file = Files.createTempFile("FileUtilsSpec-", ".txt")
    file.toFile.deleteOnExit()
    file.toFile.exists() shouldBe true

    FileUtils.safeDelete(file)
    file.toFile.exists() shouldBe false
  }

  it should "recursively delete a directory" in {
    val dir = Files.createTempDirectory("FileUtilsSpec-")
    Files.createDirectory(dir.resolve("foo"))
    Files.createDirectory(dir.resolve("bar"))
    Files.createFile(dir.resolve("foo/foo.txt"))
    Files.createFile(dir.resolve("foo/foo2.txt"))
    dir.toFile.exists() shouldBe true
    dir.toFile.deleteOnExit()

    FileUtils.safeDelete(dir)
    dir.toFile.exists() shouldBe false
  }

  "FileUtils::expand" should "replace an initial '~/' with the home directory" in {
    FileUtils.expand("~/foo/bar") shouldBe sys.props("user.home") + "/foo/bar"
    FileUtils.expand("~foo/bar") shouldBe "~foo/bar"
    FileUtils.expand("abc/~foo/bar") shouldBe "abc/~foo/bar"
  }

  it should "replace an initial './' with the current directory" in {
    FileUtils.expand("./foo/bar") shouldBe sys.props("user.dir") + "/foo/bar"
    FileUtils.expand(".foo/bar") shouldBe ".foo/bar"
    FileUtils.expand("abc/./foo/bar") shouldBe "abc/./foo/bar"
  }

  "FileUtils::recursiveCopy" should "copy files" in {
    val srcFile = Files.createTempFile("FileUtilsSpec-", ".txt")
    Files.write(srcFile, "foobar".getBytes)
    val dstFile = Files.createTempDirectory("FileUtilsSpec-").resolve("file.txt")
    FileUtils.recursiveCopy(srcFile, dstFile)

    dstFile.toFile.exists shouldBe true
    dstFile.toFile.isFile shouldBe true
    new String(Files.readAllBytes(dstFile)) shouldBe "foobar"

    // Cleanup.
    FileUtils.safeDelete(srcFile)
    FileUtils.safeDelete(dstFile.getParent)
  }

  it should "copy directories" in {
    val srcDir = Files.createTempDirectory("FileUtilsSpec-")
    Files.createDirectory(srcDir.resolve("foo"))
    Files.createDirectory(srcDir.resolve("bar"))
    Files.write(srcDir.resolve("foobar.txt"), "foobar".getBytes)
    Files.write(srcDir.resolve("foo/foo.txt"), "foo/foo".getBytes)
    Files.write(srcDir.resolve("foo/bar.txt"), "foo/bar".getBytes)
    Files.write(srcDir.resolve("bar/foo.txt"), "bar/foo".getBytes)
    Files.write(srcDir.resolve("bar/bar.txt"), "bar/bar".getBytes)
    val dstDir = Files.createTempDirectory("FileUtilsSpec-").resolve("dir")
    FileUtils.recursiveCopy(srcDir, dstDir)

    new String(Files.readAllBytes(dstDir.resolve("foobar.txt"))) shouldBe "foobar"
    new String(Files.readAllBytes(dstDir.resolve("foo/foo.txt"))) shouldBe "foo/foo"
    new String(Files.readAllBytes(dstDir.resolve("foo/bar.txt"))) shouldBe "foo/bar"
    new String(Files.readAllBytes(dstDir.resolve("bar/foo.txt"))) shouldBe "bar/foo"
    new String(Files.readAllBytes(dstDir.resolve("bar/bar.txt"))) shouldBe "bar/bar"

    // Cleanup.
    FileUtils.safeDelete(srcDir)
    FileUtils.safeDelete(dstDir.getParent)
  }
}