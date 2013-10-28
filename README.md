epubcheck
=============

epubcheck 4.0 repository Pre-Release


This folder contains the sources of epubcheck project.
Technical discussions are hosted on the [EpubCheck Google Group](https://groups.google.com/forum/#!forum/epubcheck)

EpubCheck is a tool to validate IDPF Epub files. It can detect many
types of errors in Epub. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool, installed as a web application or
used as a library.

EpubCheck is a project coordinated by [IDPF](http://idpf.org/). Most of the EpubCheck functionality comes from the schema validation tool [Jing](http://www.thaiopensource.com/relaxng/jing.html) and schemas that were developed by [IDPF](http://www.idpf.org/) and [DAISY](http://www.daisy.org/). Initial EpubCheck development was largely done at [Adobe Systems](http://www.adobe.com/).

Authors and contributors to EpubCheck include:

 * Peter Sorotokin
 * Garth Conboy
 * Markus Gylling
 * Piotr Kula
 * Paul Norton
 * Jessica Hekman
 * Liza Daly
 * George Bina
 * Bogdan Iordache
 * Romain Deltour
 * Thomas Ledoux
 * Tobias Fischer
 * Steve Antoch
 * Arwen Pond

## License

EpubCheck is made available under the terms of the [New BSD License](http://opensource.org/licenses/BSD-3-Clause)
 
BUILDING

To build epubcheck from the sources you need Java Development Kit (JDK) 1.6 or above
and Apache Maven (http://maven.apache.org/) 2.3 or above installed

Build and run tests:

    mvn install
