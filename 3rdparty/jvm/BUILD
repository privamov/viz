# Priva'Mov is a program whose purpose is to collect and analyze mobility traces.
# Copyright (C) 2016-2017 Vincent Primault <vincent.primault@liris.cnrs.fr>
#
# Priva'Mov is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Priva'Mov is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Priva'Mov.  If not, see <http://www.gnu.org/licenses/>.

jar_library(
  name='junit',
  jars=[jar('junit', 'junit', '4.12')]
)

jar_library(
  name='scalatest',
  jars=[
    scala_jar('org.scalatest', 'scalatest', '2.2.6'),
    scala_jar('org.scalamock', 'scalamock-scalatest-support', '3.2.2'),
  ],
  dependencies=[
    ':junit',
  ]
)

jar_library(
  name='findbugs',
  jars=[jar('com.google.code.findbugs', 'jsr305', '3.0.1')]
)

jar_library(
  name='scala-guice',
  jars=[scala_jar('net.codingwell', 'scala-guice', '4.1.0')]
)

jar_library(
  name='logback',
  jars=[jar('ch.qos.logback', 'logback-classic', '1.1.5')]
)

jar_library(
  name='scala-time',
  jars=[scala_jar('com.github.nscala-time', 'nscala-time', '2.12.0')]
)

jar_library(
  name='thrift',
  jars=[jar('org.apache.thrift', 'libthrift', '0.9.2')]
)

jar_library(
  name='scrooge-core',
  jars=[scala_jar('com.twitter', 'scrooge-core', '4.13.0')],
  dependencies=[
    ':thrift',
  ]
)