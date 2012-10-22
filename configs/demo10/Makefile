
.DEFAULT_GOAL := all
.PHONY: all clean deploy

MFS = $(shell ls exp-*/Makefile)

DIRS = $(MFS:Makefile=)

all: 
	@for d in $(DIRS); do make -C "$$d" all; done
	./getFigures.pl

clean:
	@for d in $(DIRS); do make -C "$$d" clean; done

deploy: all
	@md5sum exp*/* > experiments.md5
	@sha1sum exp*/* > experiments.sha1
	@rsync -av exp* results-* experiments.md5 ckrainer@cs.uni-salzburg.at:~/public_html/CPCC/experiments/

