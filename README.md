[![Current Release](https://img.shields.io/github/release/idpf/epubcheck.svg)](https://github.com/idpf/epubcheck/releases/latest) [![Github All Releases Downloads](https://img.shields.io/github/downloads/idpf/epubcheck/total.svg?colorB=A9A9A9)](https://github.com/idpf/epubcheck/releases/) [![Build Status](https://travis-ci.org/IDPF/epubcheck.svg?branch=master)](https://travis-ci.org/IDPF/epubcheck/)


EpubCheck
=========

EpubCheck is a tool to validate EPUB files. It can detect many
types of errors in EPUB. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool or used as a Java library.


## Downloads

Check the [releases page](https://github.com/IDPF/epubcheck/releases) to get the latest distribution.

[EpubCheck 4.0.2](https://github.com/IDPF/epubcheck/releases/tag/v4.0.2) is the latest recommended version to validate both EPUB 2 and 3 files.


## Documentation

Documentation on how to **use** EpubCheck, to **contribute** to the project or to **translate** messages is available on the [EpubCheck wiki](https://github.com/IDPF/epubcheck/wiki).

Technical discussions are hosted on the [EpubCheck Google Group](https://groups.google.com/forum/#!forum/epubcheck)


## Credits

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
 * Masayoshi Takahashi
 * Satoshi KOJIMA

## License

EpubCheck is made available under the terms of the [New BSD License](http://opensource.org/licenses/BSD-3-Clause)

----

## Building EpubCheck

To build epubcheck from the sources you need Java Development Kit (JDK) 1.7 or above and [Apache Maven](http://maven.apache.org/) 3.0 or above installed.
On Windows, you should build in a git bash shell (see http://github.com help)

You will also need Python to be able to run the BookReporter and related tools.


Build and run tests:

```
$ mvn install
```
Will copy `.*jar` files and packages to `target/` folder...
