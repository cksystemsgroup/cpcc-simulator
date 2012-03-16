#!/bin/sh
#
# @(#) mkTarArchive.sh - create a TAR archive of the source
#

ARCHIVE="mapper-algorithms-ext-$(date +'%Y%m%d_%H%M%S').tar.bz2";

echo "Creating archive $ARCHIVE";

tar -cjf $ARCHIVE pom.xml mkTarArchive.sh cfg/*.cfg \
	$(find src -name \*.java -o -name \*.properties)

