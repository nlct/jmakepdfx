# jmakepdfx
A Java GUI application that uses ghostscript to convert PDF files to
PDF/X.

Home page: https://www.dickimaw-books.com/software/jmakepdfx/

## Source Code

The source code depends on the [TeX Java Help library](https://github.com/nlct/texjavahelp).
The `texjavahelplib.jar` library needs to be added to the class path.
The easiest way is to add it to the `lib` directory.
Alternatively, if `texjavahelplib.jar` is in
_TEXMF_`/tex/scripts/texjavahelp/`, you will need to use
`jmakepdfx.tlu` (in `bin`) to find `texjavahelplib.jar` and add it
to the class path.

## Licence

License GPLv3+: GNU GPL version 3 or later
http://gnu.org/licenses/gpl.html
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
