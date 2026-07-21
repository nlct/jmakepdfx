ifeq ($(shell javac -version 2>&1 | grep 1.8),)
# Java 8 extended support until December 2030
  target_version := --release 8 -Xlint:-options
else
  target_version =
endif

tjhxml2bib=tjhxml2bib --copy-overwrite-xml
tjhziphelpset=tjhziphelpset
texjavahelpmk=texjavahelpmk #--debug-mode sty-data --log texparser.log
arara=arara --verbose

texjavahelplib=$(shell kpsewhich --format=texmfscripts texjavahelplib.jar)

ifeq ($(texjavahelplib),)
  texjavahelplib = $(HOME)/programming/java/texjavahelp/src/lib/texjavahelplib.jar
endif

lib/jmakepdfx.jar	: java/Manifest.txt \
	classes/com/dickimawbooks/jmakepdfx/Jmakepdfx.class \
	classes/com/dickimawbooks/jmakepdfx/dictionaries/ \
	dictionaries/jmakepdfx-en.xml \
	lib/jmakepdfx-helpset.tjh \
	classes/com/dickimawbooks/jmakepdfx/icons/ \
	java/icons/*.png
	cp dictionaries/jmakepdfx-*.xml classes/com/dickimawbooks/jmakepdfx/dictionaries/
	cp java/icons/*.png classes/com/dickimawbooks/jmakepdfx/icons/
	cd classes; \
	jar cmf ../java/Manifest.txt ../lib/jmakepdfx.jar \
	com/dickimawbooks/jmakepdfx/*.class \
	com/dickimawbooks/jmakepdfx/dictionaries \
	com/dickimawbooks/jmakepdfx/dictionaries/*.xml \
	com/dickimawbooks/jmakepdfx/icons/*.png

classes/com/dickimawbooks/jmakepdfx/dictionaries/	:
	mkdir -p classes/com/dickimawbooks/jmakepdfx/dictionaries/

classes/com/dickimawbooks/jmakepdfx/icons/	:
	mkdir -p classes/com/dickimawbooks/jmakepdfx/icons/

classes/com/dickimawbooks/jmakepdfx/Jmakepdfx.class	: classes/com/dickimawbooks/jmakepdfx \
	java/*.java
	cd java; \
	javac $(target_version) -d ../classes \
	-Xlint:unchecked -Xlint:deprecation \
	-cp $(texjavahelplib) \
	*.java

classes/com/dickimawbooks/jmakepdfx	:
	mkdir -p classes/com/dickimawbooks/jmakepdfx

lib/jmakepdfx-helpset.tjh	: doc/gpl-3.0-standalone.html \
	lib/helpset/en/index.xml \
	lib/helpset/en-GB/index.xml 
	$(tjhziphelpset) \
	--license-file doc/gpl-3.0-standalone.html '' \
	--locales 'en,en-GB' \
	lib \
	--output lib/jmakepdfx-helpset.tjh

lib/helpset/en/index.xml	: doc/en/jmakepdfx-en.pdf
	$(texjavahelpmk) doc/en/jmakepdfx-en.tex lib/helpset/en
	@grep -E 'Package glossaries(-extra)? Warning' doc/en/jmakepdfx-en.log || true

doc/en/jmakepdfx-en.pdf	: doc/en/jmakepdfx-main.tex \
	doc/en/jmakepdfx.bib \
	doc/en/jmakepdfx-dict-en.bib \
	doc/en/jmakepdfx-en.tex
	$(arara) --working-directory doc/en jmakepdfx-en

doc/en/jmakepdfx-dict-en.bib	: dictionaries/jmakepdfx-en.xml
	$(tjhxml2bib) \
	--resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	dictionaries/jmakepdfx-en.xml \
	-o doc/en/jmakepdfx-dict-en.bib

lib/helpset/en-GB/index.xml	: doc/en/jmakepdfx-en-GB.pdf
	$(texjavahelpmk) doc/en/jmakepdfx-en-GB.tex lib/helpset/en-GB
	@grep -E 'Package glossaries(-extra)? Warning' doc/en/jmakepdfx-en-GB.log || true

doc/en/jmakepdfx-en-GB.pdf	: doc/en/jmakepdfx-main.tex \
	doc/en/jmakepdfx.bib \
	doc/en/jmakepdfx-dict-en-GB.bib \
	doc/en/jmakepdfx-en-GB.tex
	$(arara) --working-directory doc/en jmakepdfx-en-GB

doc/en/jmakepdfx-dict-en-GB.bib	: dictionaries/jmakepdfx-en.xml \
	dictionaries/jmakepdfx-en-GB.xml
	$(tjhxml2bib) \
	--resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
	dictionaries/jmakepdfx-en.xml \
	dictionaries/jmakepdfx-en-GB.xml \
	-o doc/en/jmakepdfx-dict-en-GB.bib

clean	:
	\rm -rf classes
	\rm -f doc/en/*.glstex
	\rm -f doc/en/*.aux
	\rm -f doc/en/*.toc
	\rm -f doc/en/*.log
	\rm -f doc/en/*.out
	\rm -f doc/en/*.pdf
	\rm -f doc/en/*.glg
	\rm -f doc/en/jmakepdfx-dict-en.glg
	\rm -f lib/jmakepdfx.jar
	\rm -f lib/jmakepdfx-helpset.tjh
	\rm -rf lib/helpset
