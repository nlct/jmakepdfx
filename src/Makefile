ifeq ($(shell javac -version 2>&1 | grep 1.8),)
# Java 8 extended support until December 2030
  target_version := --release 8 -Xlint:-options
else
  target_version =
endif

tjhxml2bib=tjhxml2bib
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

lib/jmakepdfx-helpset.tjh	: lib/helpset/index.xml doc/gpl-3.0-standalone.html
	$(tjhziphelpset) \
	--license-file doc/gpl-3.0-standalone.html en \
	lib \
	--output lib/jmakepdfx-helpset.tjh

lib/helpset/index.xml	: doc/jmakepdfx.pdf
	$(texjavahelpmk) doc/jmakepdfx.tex lib/helpset
	@grep -E 'Package glossaries(-extra)? Warning' doc/jmakepdfx.log || true

doc/jmakepdfx.pdf	: doc/jmakepdfx-dict.bib doc/jmakepdfx.bib doc/jmakepdfx.tex
	$(arara) --working-directory doc jmakepdfx

doc/jmakepdfx-dict.bib	: dictionaries/jmakepdfx-en.xml
	$(tjhxml2bib) \
	dictionaries/jmakepdfx-en.xml \
	-o doc/jmakepdfx-dict.bib

clean	:
	\rm -rf classes
	\rm -f doc/*.glstex
	\rm -f doc/*.aux
	\rm -f doc/*.toc
	\rm -f doc/*.log
	\rm -f doc/*.out
	\rm -f doc/*.pdf
	\rm -f lib/jmakepdfx.jar
	\rm -f lib/jmakepdfx-helpset.tjh
	\rm -rf lib/helpset
