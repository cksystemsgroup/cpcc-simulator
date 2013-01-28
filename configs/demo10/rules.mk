
.DEFAULT_GOAL := all
.PHONY: all clean
.SUFFIXES: .ds.csv, .ms.csv

all: $(TARGETS)

SRC = $(shell ls rep-*.bz2)

%.ds.csv: $(SRC)
	../getDeliveredSpeed.pl $(SRC) > $@;

%.ms.csv: $(SRC)
	../getVarAndStdev.pl $(SRC) > $@;

%.dsrs.csv: $(SRC)
	../getDeliveredAndRequiredSpeed.pl $(SRC) > $@;

clean:
	$(RM) $(TARGETS) *.m
