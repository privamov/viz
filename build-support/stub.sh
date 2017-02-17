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

# See https://coderwall.com/p/ssuaxa/how-to-make-a-jar-file-linux-executable
#!/bin/sh

MYSELF=$(which "$0" 2>/dev/null)
if [ $? -gt 0 -a -f "$0" ]; then
    MYSELF="./$0"
fi
JAVA=java
if [ -n "$JAVA_HOME" ]; then
	JAVA="$JAVA_HOME/bin/java"
fi
exec ${JAVA} ${JAVA_OPTS} -jar ${MYSELF} $@
exit 1