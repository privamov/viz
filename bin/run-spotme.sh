#!/usr/bin/env bash

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

QUERULOUS__PRIVAMOV_HOST=db \
QUERULOUS__PRIVAMOV_BASE=privamov \
QUERULOUS__PRIVAMOV_USER=priva \
QUERULOUS__PRIVAMOV_PASS=pass \
java -Xmx2G -jar dist/privamov-spotme.jar \
  -ui \
  -viz.stores privamov \
  -doc.root=fr/cnrs/liris/privamov/spotme