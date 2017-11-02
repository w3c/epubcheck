[![Current Release](https://img.shields.io/github/release/idpf/epubcheck.svg)](https://github.com/idpf/epubcheck/releases/latest) [![Github All Releases Downloads](https://img.shields.io/github/downloads/idpf/epubcheck/total.svg?colorB=A9A9A9)](https://github.com/idpf/epubcheck/releases/) [![Build Status](https://travis-ci.org/IDPF/epubcheck.svg?branch=master)](https://travis-ci.org/IDPF/epubcheck/)

----
> :warning: We're running out of code maintainers by the end of 2017 and are actively looking for new developers! :warning:
> Are you interested? Please have a look at the [Welcome](https://github.com/IDPF/epubcheck/wiki/Welcome-New-Contributor), [Contribute](https://github.com/IDPF/epubcheck/wiki/Contribute) or [Translate](https://github.com/IDPF/epubcheck/wiki/Translating) wiki pages. Thank you!
----


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

Technical discussions are held on our public [email list](https://lists.w3.org/Archives/Public/public-epubcheck/). To subscribe to the email list, send an email with subject `subscribe` to public-epubcheck-request@w3.org. To participate in the discussion, simply send an email to public-epubcheck@w3.org.

Historical archives of discussions prior to October 2017 are stored at the old [EpubCheck Google Group](https://lists.w3.org/Archives/Public/public-epubcheck/). An archive of the email list beginning in October 2017 can be found at https://lists.w3.org/Archives/Public/public-epubcheck/.


## Credits

EpubCheck is a project coordinated by the EpubCheck TaskForce (part of the [W3C EPUB3 Community Group](https://github.com/w3c/publ-cg/wiki)). Most of the EpubCheck functionality comes from the schema validation tool [Jing](http://www.thaiopensource.com/relaxng/jing.html) and schemas that were developed by [IDPF](http://www.idpf.org/) and [DAISY](http://www.daisy.org/). Initial EpubCheck development was largely done at [Adobe Systems](http://www.adobe.com/).

Initial (pre 2012) authors and contributors to EpubCheck include:
> Peter Sorotokin, Garth Conboy, Markus Gylling, Piotr Kula, Paul Norton, Jessica Hekman, Liza Daly, George Bina, Bogdan Iordache, Ionut-Maxim Margelatu

EpubCheck 4.0 was largely developed by
* [DAISY](http://www.daisy.org/), namely:
  > Romain Deltour
* [Barnes & Noble](https://www.barnesandnoble.com), namely:
  > Steve Antoch, Arwen Pond

Regular contributors between 2012 and 2017 include:
> Romain Deltour, Tobias Fischer, Markus Gylling, Thomas Ledoux, Masayoshi Takahashi, Satoshi KOJIMA

Other contributors between 2012 and 2017 include:
> Emiliano Molina, Jostein Austvik Jacobsen, Stephan Kreutzer, Alberto Pettarin, MURATA Makoto, Tomohiko Hayashi, Matt Garrish, `dilbirligi`, Francisco Sanchez, Andrew Neitsch, Alexander Walters, Dave Cramer, Tzviya Siegman, Martin Kraetke
 
The project is currently maintained by:
> Romain Deltour, Tobias Fischer, the *EpubCheck TaskForce* (part of the [W3C EPUB3 Community Group](https://github.com/w3c/publ-cg/wiki))


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
