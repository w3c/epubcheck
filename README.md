[![Current Release](https://img.shields.io/github/release/w3c/epubcheck.svg)](https://github.com/w3c/epubcheck/releases/latest) [![Github All Releases Downloads](https://img.shields.io/github/downloads/w3c/epubcheck/total.svg?colorB=A9A9A9)](https://github.com/w3c/epubcheck/releases/) [![Build Status](https://travis-ci.org/w3c/epubcheck.svg?branch=master)](https://travis-ci.org/w3c/epubcheck/)


EpubCheck
=========

EpubCheck is a tool to validate EPUB files. It can detect many
types of errors in EPUB. OCF container structure, OPF and OPS mark-up,
and internal reference consistency are checked. EpubCheck can be run
as a standalone command-line tool or used as a Java library.

**We Need Your Support!!**  
Financial support is critical to the development of EPUBCheck, the tool we all use to validate EPUB files.
We need to make sure that the resources are adequate to both update the tool and provide for its continued maintenance over the next two years;
please [help us fund and support EPUBCheck](https://www.w3.org/publishing/epubcheck_fundraising)!


## Downloads

Check the [releases page](https://github.com/w3c/epubcheck/releases) to get the latest distribution.

[EpubCheck 4.0.2](https://github.com/w3c/epubcheck/releases/tag/v4.0.2) is the latest recommended version to validate both EPUB 2 and 3 files.


## Documentation

Documentation on how to **use** EpubCheck, to **contribute** to the project or to **translate** messages is available on the [EpubCheck wiki](https://github.com/w3c/epubcheck/wiki).

Technical discussions are held on our public [email list](https://lists.w3.org/Archives/Public/public-epubcheck/). To subscribe to the email list, send an email with subject `subscribe` to public-epubcheck-request@w3.org. To participate in the discussion, simply send an email to public-epubcheck@w3.org.

Historical archives of discussions prior to October 2017 are stored at the old [EpubCheck Google Group](https://lists.w3.org/Archives/Public/public-epubcheck/). An archive of the email list beginning in October 2017 can be found at https://lists.w3.org/Archives/Public/public-epubcheck/.


## Development

The EpubCheck project is coordinated by the W3C [EPUB 3 Community Group](https://www.w3.org/publishing/groups/epub3-cg/). The lead developers on this projects are:

* [Romain Deltour](https://github.com/rdeltour)
* [Tobias Fischer](https://github.com/tofi86)

There is currently a **severe shortage of developers** working on the tool, however. The project only has approximately 1/3 FTE in developer time allocated to its maintenance and development through its leads. This is barely enough to handle critical bug fixes, and not nearly enough to undertake the necessary upgrades needed to keep the tool relevant, such as developing support for EPUB 3.2.

If you, or your organization, benefits from the ability to check your EPUB publications against the standards, please consider contributing to the development of this important tool.

For more information on how to get involved, please see the [wiki page on contributing](https://github.com/w3c/epubcheck/wiki/Contribute).


## Credits

Most of the EpubCheck functionality comes from the schema validation tool [Jing](http://www.thaiopensource.com/relaxng/jing.html) and schemas that were developed by [IDPF](http://www.idpf.org/) and [DAISY](http://www.daisy.org/). Initial EpubCheck development was largely done at [Adobe Systems](http://www.adobe.com/).

Initial (pre 2012) authors and contributors to EpubCheck include:
> Peter Sorotokin, Garth Conboy, Markus Gylling, Piotr Kula, Paul Norton, Jessica Hekman, Liza Daly, George Bina, Bogdan Iordache, Ionut-Maxim Margelatu

EpubCheck 4.0 was largely developed by
* [DAISY](http://www.daisy.org/), namely:
  > Romain Deltour
* [Barnes & Noble](https://www.barnesandnoble.com), namely:
  > Steve Antoch, Arwen Pond

Regular contributors between 2012 and 2017 include:
> Romain Deltour, Tobias Fischer, Markus Gylling, Thomas Ledoux, Masayoshi Takahashi, Satoshi KOJIMA

Many thanks are also extended to the numerous people who have contributed to the evolution of EpubCheck through bug reports and patches.


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
