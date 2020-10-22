---
title: Apps and Tools
---

Many software tools integrate EPUBCheck one way or another:

- [EPUBCheck apps](#epubcheck-apps) provide a graphical user interface and can be great user-friendly alternatives to running EPUBCheck on the command line
- [EPUB editors](#epub-editors) allow you to run EPUBCheck natively or with the help of plugins
- [API wrappers](#api-wrappers) make it possible to call EPUBCheck from other programming languages
- [Other tools](#other-tools) include any utilities that do not fit in the previous categories

:::note
If you know of other tools, or want to add your own to the list, please [let us know](/docs/creating-issues/)!
:::

## EPUBCheck apps

The official distribution of EPUBCheck is a command line tool.
But many users prefer to use a graphical user interface.
If that is your case, do not worry, you're not alone!
The applications listed below provide a user-friendly graphical interface to EPUBCheck:

- [pagina EPUB-Checker](https://www.pagina.gmbh/produkte/epub-checker/), by pagina GmbH  
  Available on Windows, macOS, and Linux
- [EPUBCheckGUI](https://github.com/hnrhn/epubcheckgui), by Peter Hanrahan  
  Available on Windows, macOS, and Linux
- [epubcheck GUI](http://www.publishing-systems.org/downloads.php), by Stefan Kreutzer  
  Available on Windows and Linux
- [ECheck](https://github.com/josejuanqm/ECheck), by Jose Quintero  
  Available on macOS
- [EPUBCheckFX](https://github.com/Wandmalfarbe/EPUBCheckFX), by Pascal Wagler  
  Available on Windows, macOS, and Linux

## EPUBCheck web interfaces

The wesites listed below provide online services to run EPUBCheck from a web browser:

- [epubcheck.net](https://epubcheck.net), by Data Mountain
- [epubcheck-standalone](https://jlarmstrongiv.github.io/epubcheck-standalone/), by John L. Armstrong IV, an experimental compilation of EPUBCheck to JavaScript with TeaVM, which runs entirely in your browser (no file upload).

## EPUB editors

Sometimes it is convenient to be able to check your EPUB right within your editing environment…
The editors or plugins listed below will all you to do just that!

- [Oxygen XML Author](https://www.oxygenxml.com/xml_author/epub.html#epubcheck-validation), by SyncRO Soft
- [EPUBCheck plugin for Calibre](https://www.mobileread.com/forums/showthread.php?t=282067), by Doitsu
- [EPUBCheck plugin for Sigil](https://www.mobileread.com/forums/showthread.php?t=248186), by Doitsu


## API wrappers

- [epubchecker](https://github.com/chialab/epubchecker), a command-line interface and Node.js library for running EPUBCheck, by @chialab.
- [Python wrapper for EPUBcheck](https://pypi.org/project/epubcheck/), a Python package to call EPUBcheck directly from Python scripts

## Other tools

- [EPUB3-tests](https://github.com/wareid/EPUB3-tests), a convenient script for running a folder of files through EPUBCheck and outputting results in a single text file, by Wendy Reid.
- [JHOVE](http://jhove.openpreservation.org/) (the JSTOR/Harvard Object Validation Environment), an extensible software framework for performing format identification, validation, and characterization of digital objects
- [FlightDeck](https://ebookflightdeck.com), by Firebrand Technologies, a quality assurance tool for EPUB, integrating EPUBCheck among other validation checks.
- [EPUB Fixer](https://epub-fixer.com), a web tool for authors and ebook formatters who have an EPUB that opens locally but fails KDP, Kindle Previewer, or EPUBCheck. It integrates EPUBCheck in its scan and post-repair validation workflow.
- [epubveri](https://veripublica.github.io/epubveri/), a pure-Rust alternative EPUB validator which aims at feature-parity with EPUBCheck.