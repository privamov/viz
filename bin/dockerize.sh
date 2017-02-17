#!/bin/bash
set -e
THISDIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
TMPDIR=$(mktemp -d)
cp ${THISDIR}/../dist/$1.jar ${TMPDIR}/app.jar
cp ${THISDIR}/../build-support/Dockerfile ${TMPDIR}
cd ${TMPDIR} && docker build -t $1 .
rm -rf ${TMPDIR}