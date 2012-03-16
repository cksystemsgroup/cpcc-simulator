#!/bin/sh

DAT=$(date +'%Y%m%d_%H%M%S');

ARCHIVE="cpcc-sim-config-$DAT.tar.bz2"

tar -cjf $ARCHIVE \
	engine-1.cfg \
	engine-2.cfg \
	engine-3.cfg \
	engine-central.cfg \
	gm-viewer-central.cfg \
	mapper-central.cfg \
	pilot-1.cfg \
	pilot-2.cfg \
	pilot-3.cfg \
	RV-4.1.crs \
	RV-4.2.crs \
	RV-4.3.crs \
	VV-4.1.zip \
	VV-4.2.zip \
	VV-4.3.zip \
	VV-4.4.zip
