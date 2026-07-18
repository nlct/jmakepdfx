# jmakepdfx
A Java GUI application that uses GhostScript to convert PDF files to
PDF/X.

Home page: https://www.dickimaw-books.com/software/jmakepdfx/

Although jmakepdfx is designed as a GUI application, it can also be
run from the command line in batch mode. Use `jmakepdfx --help` for
command line options.

## Installation

There are two installation methods.  The distribution is designed to
comply with TeX Live requirements.  This means no duplicate
filenames (except for `README` etc) and no generic filenames (such
as `save.png`). This means that `texjavahelplib.jar` can't be
bundled up with jmakepdfx (as it's also used with other
applications). Instead, the TeX Java Help (TJH) system needs to be
provided separately.  All icon files and localisation files are
bundled in the jar files, and the HTML and XML files for the
in-application manual (accessed via Help &rarr; Manual) are provided in a TJH file.

### TEXMF Install

TEXMF denotes the distribution root. For example
`/usr/local/texlive/2026/texmf-dist` or `~/texmf` (Unix-like) or
`c:\texlive\2026\texmf-dist`.

  - TEXMF`/scripts/jmakepdfx/jmakepdfx-helpset.tjh`
  - TEXMF`/scripts/jmakepdfx/jmakepdfx.jar`
  - TEXMF`/scripts/jmakepdfx/jmakepdfx.tlu`
  - TEXMF`/doc/latex/jmakepdfx/jmakepdfx.pdf`
  - TEXMF`/doc/latex/jmakepdfx/README.md`
  - TEXMF`/doc/latex/jmakepdfx/CHANGES`
  - TEXMF`/doc/latex/jmakepdfx/LICENSE`
  - TEXMF`/doc/latex/jmakepdfx/DEPENDS.txt`

Note that TeX Java Help also needs to be installed:

  - TEXMF`/scripts/texjavahelp/texjavahelplib.jar`

(There will be other files in the `texjavahelp` subdirectory. They
are only required if building the documentation. They are not needed
to run jmakepdfx.)

For Unix-like systems, add a symbolic link to the TeXLua script
`jmakepdfx.tlu` called `jmakepdfx` somewhere on your system path.
This script locates `texjavahelplib.jar` and adds it to Java's class
path when running `jmakepdfx.jar` (the application jar file).

On Windows, copy `runscript.exe` (which should be in the same folder
as `latex.exe` `pdflatex.exe` etc) to `jmakepdfx.exe`. This should
find the `jmakepdfx.tlu` script and run it.

### Standard Install 

The `jmakepdfx.jar` file includes a manifest that identifies the
main class and the class path. The `texjavahelplib.jar` file will
need to be in the same directory as `jmakepdfx.jar`.

  - `jmakepdfx-helpset.tjh`
  - `jmakepdfx.jar`
  - `texjavahelplib.jar`

In this case, the TeXLua script is not required. The `jmakepdfx`
application can be run as normal for your system. For example:

```bash
java -jar /path/to/jmakepdfx.jar
```

(Replace `/path/to/jmakepdfx.jar` with the path to `jmakepdfx.jar`.)

## Source Code

The source code depends on the [TeX Java Help library](https://github.com/nlct/texjavahelp).
The `texjavahelplib.jar` library needs to be added to the class path.
The easiest way is to add it to the `lib` directory.
Alternatively, if `texjavahelplib.jar` is in
_TEXMF_`/tex/scripts/texjavahelp/`, you will need to use
`jmakepdfx.tlu` (in `bin`) to find `texjavahelplib.jar` and add it
to the class path.

The `icons` and `dictionaries` directories need to be included in
the `jar` file.

Replace `texjavahelplib.jar` with the path to `texjavahelplib.jar`:

```bash
cd java
javac -d ../classes -cp texjavahelplib.jar *.java
cd ../classes
mkdir -p com/dickimawbooks/jmakepdfx/dictionaries/
cp ../dictionaries/jmakepdfx-*.xml com/dickimawbooks/jmakepdfx/dictionaries/
mkdir -p com/dickimawbooks/jmakepdfx/icons/
cp ../java/icons/*.png com/dickimawbooks/jmakepdfx/icons/
jar cmf ../java/Manifest.txt ../lib/jmakepdfx.jar \
com/dickimawbooks/jmakepdfx/*.class \
com/dickimawbooks/jmakepdfx/dictionaries \
com/dickimawbooks/jmakepdfx/dictionaries/*.xml \
com/dickimawbooks/jmakepdfx/icons/*.png
```

Note: jar files are a special type of zip file. It's possible for
the zip archive to omit a directory name but include the contents of
the directory. The above explicitly creates a `dictionaries`
sub-directory in the jar file but not an `icons` sub-directory.
The TJH system requires the presence of the `dictionaries` jar entry
for the localisation files. There's no requirement for the `icons` directory.

The `jmakepdfx-helpset.tjh` file must be created before jmakepdfx is
run otherwise there will be an error about a missing helpset.
This is done as follows:

Create the `jmakepdfx-dict.bib` file from the appropriate
localisation files. This is a bib2gls file that can be created
with `tjhxml2bib`:

```bash
tjhxml2bib --resource /com/dickimawbooks/texjavahelplib/dictionaries/texjavahelplib-en.xml \
dictionaries/jmakepdfx-en.xml \
-o doc/jmakepdfx-dict.bib
```

Build the PDF from LaTeX source:

```bash
cd doc
arara jmakepdfx
```

If you don't have arara:

```bash
lualatex jmakepdfx
bib2gls jmakepdfx
lualatex jmakepdfx
lualatex jmakepdfx
```

Don't remove the aux, toc and glstex files as they are all required in
the next step.

Create the helpset files:

```bash
texjavahelpmk doc/jmakepdfx.tex lib/helpset
```

Create the TJH file (from `lib/helpset` and the license file):

```bash
tjhziphelpset --license-file doc/gpl-3.0-standalone.html en \
lib \
--output lib/jmakepdfx-helpset.tjh
```

The `lib/helpset` directory and the other temporary files can now be removed if you want.

## Licence

License GPLv3+: GNU GPL version 3 or later
http://gnu.org/licenses/gpl.html
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
