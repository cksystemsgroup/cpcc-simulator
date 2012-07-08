#!/bin/sh

DAT=$(date +'%Y%m%d_%H%M%S');

ARCHIVE="cpcc-sim-config-$DAT.tar.bz2"

tar -cjf $ARCHIVE \
	engine-1.cfg \
	engine-2.cfg \
	engine-3.cfg \
	engine-4.cfg \
	engine-central.cfg \
	gm-viewer-central.cfg \
	mapper-central.cfg \
	pilot-1*.cfg \
	pilot-2*.cfg \
	pilot-3*.cfg \
	pilot-4*.cfg

