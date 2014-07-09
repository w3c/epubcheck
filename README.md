EpubCheck
=========

*epubcheck 4.0 repository Pre-Release (`master` branch contains current 4.0 development in alpha stage)*

EpubCheck is a tool to validate EPUB files. It can detect many
types of errors in EPUB. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool or used as a Java library.


## Releases

Check the [release page](https://github.com/IDPF/epubcheck/releases) to get the latest distribution.

[EpubCheck 3.0.1](https://github.com/IDPF/epubcheck/releases/tag/v3.0.1) is the latest recommended version to validate both EPUB 2 and 3 files.


## Documentation

Documentation on how to **use** or how to **contribute** is available on the [EpubCheck wiki](wiki).

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
 
## Building the 4.0 pre-release

To build epubcheck from the sources you need Java Development Kit (JDK) 1.6 or above
and [Apache Maven](http://maven.apache.org/) 2.3 or above installed
as well as [Node.js](http://nodejs.org/).
On Windows, you should build in a git bash shell (see http://github.com)

You will also need Python to be able to run the BookReporter and related tools.


Build and run tests:

```
$ mvn install
```
