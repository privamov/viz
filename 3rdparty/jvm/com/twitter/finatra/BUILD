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

# When you update this version, please make sure you update other libraries that are included here, to
# avoid dependency hell. Known dependencies that must stay in sync:
#   - com/twitter/finagle
#   - com/twitter/util
#   - com/twitter/inject
#   - com/fasterxml/jackson
VERSION = '2.7.0'

jar_library(name='http', jars=[scala_jar('com.twitter', 'finatra-http', VERSION)])
jar_library(name='httpclient', jars=[scala_jar('com.twitter', 'finatra-httpclient', VERSION)])
jar_library(name='thrift', jars=[scala_jar('com.twitter', 'finatra-thrift', VERSION)])
jar_library(name='jackson', jars=[scala_jar('com.twitter', 'finatra-jackson', VERSION)])
