EPUBCheck
=========

This folder contains the distribution of the EPUBCheck project.

EPUBCheck is a tool to validate the conformance of EPUB publications against
the EPUB specifications. EPUBCheck can be run as a standalone command-line tool
or used as a Java library.

EPUBCheck is open source software, maintained by the DAISY Consortium on behalf
of the W3C.

EPUBCheck project home: https://github.com/w3c/epubcheck


RUNNING
-------

To run the tool you need a Java runtime (1.7 or above).
Any Operating System should do.

Run it from the command line:

>  java -jar epubcheck.jar file.epub

All detected errors are simply printed to the standard error stream.

Print the commandline help with the --help argument:

>  java -jar epubcheck.jar --help


USING AS A LIBRARY
------------------

You can also use EPUBCheck as a library in your Java application. EPUBCheck
public interfaces can be found in the `com.adobe.epubcheck.api` package.
EPUBCheck class can be used to instantiate a validation engine. Use one of its
constructors and then call validate() method. Report is an interface that you
can implement to get a list of the errors and warnings reported by the
validation engine (instead of the error list being printed out).


LICENSING
---------

EPUBCheck is made available under the terms of the 3-Clause BSD License, a
copy of which is available in the file LICENSE.txt.

The list of licenses of third-party software components is detailed in the
file THIRD-PARTY.txt


AUTHORS / CONTRIBUTORS
----------------------

This distribution of EPUBCheck was made by the DAISY Consortium, for the W3C.

Previous contributors include:

- Alberto Pettarin
- Alexander Walters
- Andrew Neitsch
- Arwen Pond
- Bobby Tung
- Bogdan Iordache
- Dave Cramer
- dilbirligi
- Emiliano Molina
- Elisa Molinari (Fondazione LIA)
- Francisco Sanchez
- Garth Conboy
- George Bina
- Gregorio Pellegrino (Fondazione LIA)
- Ionut-Maxim Margelatu
- Jessica Hekman
- Jostein Austvik Jacobsen
- Liza Daly
- Marianne Gulstad (Publizon)
- Markus Gylling
- Martin Kraetke
- Masayoshi Takahashi
- Matt Garrish
- Merijn de Haen
- MURATA Makoto
- Paul Norton
- Peter Sorotokin
- Piotr Kula
- Romain Deltour
- Satoshi KOJIMA
- Stephan Kreutzer
- Steve Antoch
- Thiago de Oliveira Pereira
- Thomas Ledoux
- Tobias Fischer
- Tomohiko Hayashi
- Tzviya Siegman
- Vincent Gros
- Woongyoung Park


Most of the EPUBCheck functionality comes from the schema validation tool Jing
and schemas that were developed by IDPF and DAISY. Initial EPUBCheck development
was largely done at Adobe Systems. EPUBCheck 4.0 was largely developed by
DAISY and Barnes & Noble.
