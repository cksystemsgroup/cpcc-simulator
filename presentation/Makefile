
FIG_FILES = \
	vl-20101025-fig01.fig \
	vl-pres-fig01.fig

PDF_FILES = $(FIG_FILES:.fig=.pdf)

all: $(PS_FILES) $(PDF_FILES) $(TEX_FILES)

%.pdf: %.fig
	fig2dev -L pdf -c $< $@

clean:
	$(RM) $(PDF_FILES) *.bak

