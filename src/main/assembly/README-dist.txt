This folder contains the distribution of EPUBCheck project.

EPUBCheck is a tool to validate EPUB files. It can detect many
types of errors in EPUB. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EPUBCheck can be run
as a standalone command-line tool, installed as a web application or
used as a Java library.

EPUBCheck project home: https://github.com/idpf/epubcheck


RUNNING

To run the tool you need Java (1.6 or above, 1.7+ recommended).
Any Operating System should do. Run it from the command line:

  java -jar epubcheck.jar file.epub

All detected errors are simply printed to stderr.

Print the commandline help with the --help argument:
  java -jar epubcheck.jar --help


USING AS A LIBRARY

You can also use EPUBCheck as a library in your Java application. EPUBCheck
public interfaces can be found in com.adobe.epubcheck.api package. EPUBCheck
class can be used to instantiate a validation engine. Use one of its
constructors and then call validate() method. Report is an interface that
you can implement to get a list of the errors and warnings reported by the
validation engine (instead of the error list being printed out).


LICENSING

See COPYING.txt and THIRD-PARTY.txt


AUTHORS / CONTRIBUTORS

Peter Sorotokin
Garth Conboy
Markus Gylling
Piotr Kula
Paul Norton
Jessica Hekman
Liza Daly
George Bina
Bogdan Iordache
Ionut-Maxim Margelatu
Romain Deltour
Steve Antoch
Arwen Pond
Thomas Ledoux
Tobias Fischer
Masayoshi Takahashi
Satoshi KOJIMA
Emiliano Molina
Jostein Austvik Jacobsen
Stephan Kreutzer
Alberto Pettarin
MURATA Makoto
Tomohiko Hayashi
Matt Garrish
dilbirligi
Francisco Sanchez
Andrew Neitsch
Alexander Walters
Dave Cramer
Tzviya Siegman
Martin Kraetke


Most of the EPUBCheck functionality comes from the schema validation tool Jing
and schemas that were developed by IDPF and DAISY. Initial EPUBCheck development
was largely done at Adobe Systems. EPUBCheck 4.0 was largely developed by
DAISY and Barnes & Noble.
