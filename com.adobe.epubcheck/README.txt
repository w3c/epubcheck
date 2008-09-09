This folder contains the distribution of epubcheck project.

EpubCheck is a tool to validate IDPF Epub files. It can detect many
types of errors in Epub. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool, installed as a web application or
used as a library.

Epubcheck project home: http://code.google.com/p/epubcheck/

BUILDING

To build epubcheck from the sources you need Java Development Kit (JDK) 1.5 or above
and Apache ant (http://ant.apache.org/) 1.6 or above installed

Run

ant -f build.xml

RUNNING

To run the tool you need Java Runtime (1.5 or above). Any OS should do. Run
it from the command line: 

java -jar epubcheck-x.x.x.jar file.epub

All detected errors are simply printed to stderr. 

USING AS A LIBRARY

You can also use EpubCheck as a library in your Java application. EpubCheck
public interfaces can be found in com.adobe.epubcheck.api package. EpubCheck
class can be used to instantiate a validation engine. Use one of its
constructors and then call validate() method. Report is an interface that
you can implement to get a list of the errors and warnings reported by the
validation engine (instead of the error list being printed out).

LICENSING

See COPYING.txt

AUTHORS

Peter Sorotokin 
Garth Conboy 
Markus Gylling 
Piotr Kula

Most of the EpubCheck functionality comes from the schema validation tool Jing
and schemas that were developed by IDPF and DAISY. EpubCheck development was
largely done at Adobe Systems. 










