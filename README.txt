This folder contains the sources of epubcheck project.

EpubCheck is a tool to validate IDPF Epub files. It can detect many
types of errors in Epub. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool, installed as a web application or
used as a library.

Epubcheck project home has moved to Github:  http://github.com/IDPF/epubcheck


BUILDING

To build epubcheck from the sources you need Java Development Kit (JDK) 1.5 or above
and Apache Maven (http://maven.apache.org/) 2.3 or above installed
as well as Node.js  (http://nodejs.org/).
On windows, you should build in a git bash shell (see http://github.com)


You will also need Python to be able to run the BookReporter and related tools.


Run:

    mvn install


