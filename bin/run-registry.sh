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

# Name of the Docker container running for the registry.
CONTAINER_NAME=registry

# Generate TLS certificate and key if needed.
if [ ! -f ~/certs/${CONTAINER_NAME}.key ]; then
  echo Generating TLS certificate and key
  openssl req \
    -x509 \
    -nodes \
    -days 365 \
    -newkey rsa:2048 \
    -keyout ~/certs/${CONTAINER_NAME}.key \
    -out ~/certs/${CONTAINER_NAME}.crt
fi

# Start a Docker container for the registry if needed.
if [ -z "$(docker ps -f name=${CONTAINER_NAME} -q)" ]; then
  echo Launching Docker container ${CONTAINER_NAME}
  docker run \
    --restart=always \
    -d \
    -p 5050:5000 \
    --name ${CONTAINER_NAME} \
    -v ~/certs:/certs \
    -v /var/lib/registry:/var/lib/registry \
    -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/${CONTAINER_NAME}.crt \
    -e REGISTRY_HTTP_TLS_KEY=/certs/${CONTAINER_NAME}.key \
    registry:2
else
  echo Docker container ${CONTAINER_NAME} already running
fi