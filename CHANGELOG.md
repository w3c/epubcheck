# EPUBCheck change log

<a name="4.2.5"></a>
## [4.2.5](https://github.com/w3c/epubcheck/compare/v4.2.4...v4.2.5) (2021-03-15)

This is the **latest production-ready** release of EPUBCheck, which provides complete support for checking conformance to the [EPUB 3.2](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html) family of specifications.

Version 4.2.5 is a maintenance release. It adds several new checks for Media Overlays, updates the HTML schemas to the latest version availabe, and  includes various other bug fixes and adjustments (see the detailed list below).

This release was made by the DAISY Consortium for the W3C. Many thanks to everyone who contributed and reported issues!

This EPUBCheck version is also available in the Maven Central Repository as [`org.w3c:epubcheck:4.2.5`](https://search.maven.org/artifact/org.w3c/epubcheck/4.2.5/jar).

### Features

* check playback properties do not have 'refines' attribute ([05a6a20](https://github.com/w3c/epubcheck/commit/05a6a20))
* check reading order of Media Overlays text elements ([e35bd05](https://github.com/w3c/epubcheck/commit/e35bd05))
* check references between Media Overlays and Content documents ([f49aa84](https://github.com/w3c/epubcheck/commit/f49aa84))
* check remote resource usage in Media Overlays ([df16ede](https://github.com/w3c/epubcheck/commit/df16ede))
* check required cardinality of meta properties ([edcd253](https://github.com/w3c/epubcheck/commit/edcd253)), closes [#1121](https://github.com/w3c/epubcheck/issues/1121)
* check that Media Overlays are only defined for XHTML and SVG content documents ([5ae1aa9](https://github.com/w3c/epubcheck/commit/5ae1aa9))
* check the epub:textref attribute on Media Overlays body and seq elements ([eea1574](https://github.com/w3c/epubcheck/commit/eea1574))
* improve checking of audio clip times in Media Overlays ([11b652e](https://github.com/w3c/epubcheck/commit/11b652e))
* report unknown 'epub:type' values in overlays as USAGE only ([#1171](https://github.com/w3c/epubcheck/issues/1171)) ([f8a2517](https://github.com/w3c/epubcheck/commit/f8a2517))
* update HTML schemas from the HTML Checker ([56dcbd1](https://github.com/w3c/epubcheck/commit/56dcbd1))
* verify 'media:duration' property use valid SMIL clock values ([794b7ce](https://github.com/w3c/epubcheck/commit/794b7ce)), closes [#1174](https://github.com/w3c/epubcheck/issues/1174)

### Bug Fixes

* allow empty `xml:lang` attributes in Package Documents ([177af8f](https://github.com/w3c/epubcheck/commit/177af8f)), closes [#777](https://github.com/w3c/epubcheck/issues/777)
* allow the 'glossary' manifest item property ([d1727d8](https://github.com/w3c/epubcheck/commit/d1727d8)), closes [#1170](https://github.com/w3c/epubcheck/issues/1170)
* do not report fragment-only CSS URLs ([6fa3312](https://github.com/w3c/epubcheck/commit/6fa3312)), closes [#1198](https://github.com/w3c/epubcheck/issues/1198)
* do not require the Navigation Document to have an index in an Index Publication ([33f2f99](https://github.com/w3c/epubcheck/commit/33f2f99)), closes [#1122](https://github.com/w3c/epubcheck/issues/1122)
* do not treat escaped <a> elements as hyperlinks in HTM-053 ([5949b6c](https://github.com/w3c/epubcheck/commit/5949b6c)), closes [#1182](https://github.com/w3c/epubcheck/issues/1182)
* remove the user directory only at the start of paths (in messages) ([5ee72e7](https://github.com/w3c/epubcheck/commit/5ee72e7)), closes [#1181](https://github.com/w3c/epubcheck/issues/1181)


<a name="4.2.4"></a>
## [4.2.4](https://github.com/w3c/epubcheck/compare/v4.2.3...v4.2.4) (2020-06-23)

### Bug Fixes

* allow 'doc-dedication' role on 'aside' elements ([023b783](https://github.com/w3c/epubcheck/commit/023b783))
* allow 'doc-glossary' role on 'section' elements ([12cda43](https://github.com/w3c/epubcheck/commit/12cda43))

<a name="4.2.3"></a>
## [4.2.3](https://github.com/w3c/epubcheck/compare/v4.2.2...v4.2.3) (2020-06-22)

### Features

* update HTML schemas from the HTML Checker ([22fa3b1](https://github.com/w3c/epubcheck/commit/22fa3b1)), closes [#1111](https://github.com/w3c/epubcheck/issues/1111)
* downgrade PKG-012 (non-ASCII filenames) to USAGE ([f368ee5](https://github.com/w3c/epubcheck/commit/f368ee5)), closes [#1097](https://github.com/w3c/epubcheck/issues/1097) (thanks @slonopotamus!)
* downgrade RSC-004 (cannot decrypt resource) to INFO ([#1136](https://github.com/w3c/epubcheck/issues/1136)) ([e732068](https://github.com/w3c/epubcheck/commit/e732068))
* report empty `title` elements in XHTML Content Documents ([#1135](https://github.com/w3c/epubcheck/issues/1135)) ([f115730](https://github.com/w3c/epubcheck/commit/f115730)), closes [#1093](https://github.com/w3c/epubcheck/issues/1093)
* **ARIA:** allow doc-epigraph on 'section' and doc-cover on 'img' ([84a0979](https://github.com/w3c/epubcheck/commit/84a0979))
* update the XML ouput to the new JHOVE schema ([0b346fd](https://github.com/w3c/epubcheck/commit/0b346fd)) (thanks @tledoux!)

### Bug Fixes

* avoid `OutOfMemoryError` when computing image size ([929806b](https://github.com/w3c/epubcheck/commit/929806b)) (thanks @DocJM!)
* fail gracefully when running on a non-EPUB file ([#1134](https://github.com/w3c/epubcheck/issues/1134)) ([2083f05](https://github.com/w3c/epubcheck/commit/2083f05)), closes [#1050](https://github.com/w3c/epubcheck/issues/1050)
* allow 'a11y:certifierCredential' as a link ([484786f](https://github.com/w3c/epubcheck/commit/484786f)), closes [#1140](https://github.com/w3c/epubcheck/issues/1140)
* allow foreign namespaces in EPUB 2 SVGs ([e3ffc37](https://github.com/w3c/epubcheck/commit/e3ffc37))


### Other Improvements

* improve reporting of invalid URL host parts ([d2728ee](https://github.com/w3c/epubcheck/commit/d2728ee)), closes [#1034](https://github.com/w3c/epubcheck/issues/1034) [#1079](https://github.com/w3c/epubcheck/issues/1079)
* harmonize quotes usage in messages ([#1132](https://github.com/w3c/epubcheck/issues/1132)) ([659ab76](https://github.com/w3c/epubcheck/commit/659ab76)), closes [#1071](https://github.com/w3c/epubcheck/issues/1071)
* add an Automatic-Module-Name entry to the jar manifest ([ee06724](https://github.com/w3c/epubcheck/commit/ee06724)), closes [#1128](https://github.com/w3c/epubcheck/issues/1128) (thanks @io7m!)
* **deps** upgrade commons-compress to v1.20 to remediate CVE-2019-12402 ([928c26a](https://github.com/w3c/epubcheck/commit/928c26a)), closes [#1078](https://github.com/w3c/epubcheck/issues/1078)
* **deps** upgrade guava to v24.1.1 to remediate CVE-2018-10237 ([cec01fe](https://github.com/w3c/epubcheck/commit/cec01fe))


<a name="4.2.2"></a>
## [4.2.2](https://github.com/w3c/epubcheck/compare/v4.2.1...v4.2.2) (2019-07-18)

### Localization

* complete translation for Traditional Chinese (+ Danish/German/Korean tweaks) ([#1058](https://github.com/w3c/epubcheck/issues/1058)) ([64558be](https://github.com/w3c/epubcheck/commit/64558be)), closes [#1054](https://github.com/w3c/epubcheck/issues/1054)


### Features

* revert the spine/toc nav order check to a `WARNING` ([#1056](https://github.com/w3c/epubcheck/issues/1056)) ([1f6a882](https://github.com/w3c/epubcheck/commit/1f6a882)), closes [#1036](https://github.com/w3c/epubcheck/issues/1036)


<a name="4.2.1"></a>
## [4.2.1](https://github.com/w3c/epubcheck/compare/v4.2.0...v4.2.1) (2019-05-20)

### Bug Fixes

* allow `doc-endnote` on `li` children of `ol` ([275fcd1](https://github.com/w3c/epubcheck/commit/275fcd1)), closes [#1041](https://github.com/w3c/epubcheck/issues/1041)
* update schemas from upstream HTML Checker ([4d5a24d](https://github.com/w3c/epubcheck/commit/4d5a24d))


<a name="4.2.0"></a>
## [4.2.0](https://github.com/w3c/epubcheck/compare/v4.2.0-rc...v4.2.0) (2019-04-23)

### Bug Fixes

* allow any role on `a` elem with no `href` ([b9ed8f6](https://github.com/w3c/epubcheck/commit/b9ed8f6)), closes [#1022](https://github.com/w3c/epubcheck/issues/1022)
* check trailing spaces in mimetype file ([123c69f](https://github.com/w3c/epubcheck/commit/123c69f))
* remove restrictions on MathML annotation-xml ([8a1b650](https://github.com/w3c/epubcheck/commit/8a1b650)), closes [#1024](https://github.com/w3c/epubcheck/issues/1024)
* report ZIP checks after the 'Validating…' message ([73b0ee8](https://github.com/w3c/epubcheck/commit/73b0ee8)), closes [#1025](https://github.com/w3c/epubcheck/issues/1025)

### Features

* add new 'voicing' link relationship ([97e9f1c](https://github.com/w3c/epubcheck/commit/97e9f1c))

### Localization

* update localizedmessages for Danish, French, German, Italian, Japanese, Korean, and Spanish. ([2f0d1716](https://github.com/w3c/epubcheck/commit/2f0d1716))


<a name="4.2.0-rc"></a>
## [4.2.0-rc](https://github.com/w3c/epubcheck/compare/v4.2.0-beta...v4.2.0-rc) (2019-03-18)

### Bug Fixes

* allow `epub:type` on all HTML elements ([2cafe64](https://github.com/w3c/epubcheck/commit/2cafe64)), closes [#986](https://github.com/w3c/epubcheck/issues/986)
* allow ARIA role `doc-glossary` on `section` elements ([0d9462f](https://github.com/w3c/epubcheck/commit/0d9462f)), closes [#997](https://github.com/w3c/epubcheck/issues/997)
* allow images to point to SVG fragments ([18afa9d](https://github.com/w3c/epubcheck/commit/18afa9d)), closes [#987](https://github.com/w3c/epubcheck/issues/987)
* ignore SVG view fragments when checking cross refs ([50b29f1](https://github.com/w3c/epubcheck/commit/50b29f1)), closes [#987](https://github.com/w3c/epubcheck/issues/987)
* localize singular/plurals variants in summary ([93b700f](https://github.com/w3c/epubcheck/commit/93b700f)), closes [#958](https://github.com/w3c/epubcheck/issues/958)
* parse CSS custom properties ([0036e93](https://github.com/w3c/epubcheck/commit/0036e93)), closes [#790](https://github.com/w3c/epubcheck/issues/790)
* report all `epub:type` value checks as `USAGE` ([f248483](https://github.com/w3c/epubcheck/commit/f248483)), closes [#1009](https://github.com/w3c/epubcheck/issues/1009)
* report duplicate landmarks nav entries as `ERROR` ([d7be97c](https://github.com/w3c/epubcheck/commit/d7be97c)), closes [#298](https://github.com/w3c/epubcheck/issues/298)
* revert FXL SVG rules to EPUB 3.0.1’s logic ([ced3c15](https://github.com/w3c/epubcheck/commit/ced3c15))
* revert the deprecation of `display-seq` ([f81d8b8](https://github.com/w3c/epubcheck/commit/f81d8b8)), closes [#990](https://github.com/w3c/epubcheck/issues/990)
* script data blocks don’t need to be declared ([05e5ac2](https://github.com/w3c/epubcheck/commit/05e5ac2))
* support ARIA global attributes in SVG ([56ebcd8](https://github.com/w3c/epubcheck/commit/56ebcd8)), closes [#846](https://github.com/w3c/epubcheck/issues/846)

### Features

* allow fonts to be remote resources ([4d5a5a9](https://github.com/w3c/epubcheck/commit/4d5a5a9)), closes [#871](https://github.com/w3c/epubcheck/issues/871) [#672](https://github.com/w3c/epubcheck/issues/672)
* allow remote resources in scripted content ([1c90ae9](https://github.com/w3c/epubcheck/commit/1c90ae9)), closes [#869](https://github.com/w3c/epubcheck/issues/869)
* check duplicate IDs in the OCF encryption file ([cab45e6](https://github.com/w3c/epubcheck/commit/cab45e6)), closes [#306](https://github.com/w3c/epubcheck/issues/306)
* check image sources in `picture` and `srcset` ([11bf628](https://github.com/w3c/epubcheck/commit/11bf628)), closes [#781](https://github.com/w3c/epubcheck/issues/781)
* check that toc & page-list nav are in reading order ([8ba384f](https://github.com/w3c/epubcheck/commit/8ba384f)), closes [#888](https://github.com/w3c/epubcheck/issues/888)
* disallow remote links in toc/landmarks/page-list nav ([dd0805f](https://github.com/w3c/epubcheck/commit/dd0805f)), closes [#890](https://github.com/w3c/epubcheck/issues/890)
* report use of non-preferred Core Media Types ([ab13779](https://github.com/w3c/epubcheck/commit/ab13779)), closes [#873](https://github.com/w3c/epubcheck/issues/873)

### Localization

* new localization: Danish ([28ef69b](https://github.com/w3c/epubcheck/commit/28ef69b), [40f7b64](https://github.com/w3c/epubcheck/commit/40f7b64))
* new localization: Chinese (Taiwan) ([46195ad](https://github.com/w3c/epubcheck/commit/46195ad))
* update localized messages for German ([40f7b64](https://github.com/w3c/epubcheck/commit/40f7b64))
* update localized messages for Italian ([d954ae7](https://github.com/w3c/epubcheck/commit/d954ae7), [864282a](https://github.com/w3c/epubcheck/commit/864282a))

Note: the new messages introduced in v4.2.0 are _not_ localized yet.


<a name="4.2.0-beta"></a>
## [4.2.0-beta](https://github.com/w3c/epubcheck/compare/v4.1.1...v4.2.0-beta) (2019-02-25)

### Bug Fixes

* allow foreign namespaces in SVG content documents ([ca29c89](https://github.com/w3c/epubcheck/commit/ca29c89)), closes [#491](https://github.com/w3c/epubcheck/issues/491)
* do not restrict ID refs to non-colon names ([365e6e6](https://github.com/w3c/epubcheck/commit/365e6e6)), closes [#783](https://github.com/w3c/epubcheck/issues/783)
* improve RelaxNG datatypes of date/time/duration attributes ([ef5b94c](https://github.com/w3c/epubcheck/commit/ef5b94c)), closes [#775](https://github.com/w3c/epubcheck/issues/775)
* parse boolean/enumerated HTML attributes as case-insensitive ([5b3533a](https://github.com/w3c/epubcheck/commit/5b3533a)), closes [#941](https://github.com/w3c/epubcheck/issues/941)


### Features

* add `application/javascript` as core media type ([f4566b6](https://github.com/w3c/epubcheck/commit/f4566b6)), closes [#874](https://github.com/w3c/epubcheck/issues/874)
* add new MO-only `aside` epub:type value ([4404fff](https://github.com/w3c/epubcheck/commit/4404fff))
* allow the `Compression` element in the encryption file ([ed6f1c4](https://github.com/w3c/epubcheck/commit/ed6f1c4)), closes [#904](https://github.com/w3c/epubcheck/issues/904)
* basic schema support for HTML custom elements ([356fac0](https://github.com/w3c/epubcheck/commit/356fac0)), closes [#932](https://github.com/w3c/epubcheck/issues/932)
* implement Structure Semantics Vocab changes for EPUB 3.2 ([3454da5](https://github.com/w3c/epubcheck/commit/3454da5)), closes [#531](https://github.com/w3c/epubcheck/issues/531) [#903](https://github.com/w3c/epubcheck/issues/903) [#962](https://github.com/w3c/epubcheck/issues/962) [#963](https://github.com/w3c/epubcheck/issues/963)
* improve wording of message OPF-025 ([cecaa76](https://github.com/w3c/epubcheck/commit/cecaa76)), closes [#959](https://github.com/w3c/epubcheck/issues/959)
* remove `epubsc` from reserved prefixes ([02397ec](https://github.com/w3c/epubcheck/commit/02397ec)), closes [#875](https://github.com/w3c/epubcheck/issues/875)
* report CSS absolute/fixed positioning as USAGE ([672ac6d](https://github.com/w3c/epubcheck/commit/672ac6d)), closes [#889](https://github.com/w3c/epubcheck/issues/889)
* set reported 3.x version to "3.2" ([9965c19](https://github.com/w3c/epubcheck/commit/9965c19)), closes [#943](https://github.com/w3c/epubcheck/issues/943)
* support new rules related to FXL SVG sizing ([17f5eee](https://github.com/w3c/epubcheck/commit/17f5eee)), closes [#902](https://github.com/w3c/epubcheck/issues/902)
* suppress a couple irrelevant USAGE reports ([b342db2](https://github.com/w3c/epubcheck/commit/b342db2))
* update Jing (schema processor) to v20181222 ([cfca41b](https://github.com/w3c/epubcheck/commit/cfca41b))
* update Package vocabularies and link element checks ([99f882a](https://github.com/w3c/epubcheck/commit/99f882a)), closes [#883](https://github.com/w3c/epubcheck/issues/883) [#884](https://github.com/w3c/epubcheck/issues/884) [#885](https://github.com/w3c/epubcheck/issues/885) [#886](https://github.com/w3c/epubcheck/issues/886) [#887](https://github.com/w3c/epubcheck/issues/887)
* update recognized font Core Media types ([ac2f1bd](https://github.com/w3c/epubcheck/commit/ac2f1bd)), closes [#872](https://github.com/w3c/epubcheck/issues/872) [#339](https://github.com/w3c/epubcheck/issues/339)
* update the HTML schemas to the latest Nu HTML Checker version ([8d3c77e](https://github.com/w3c/epubcheck/commit/8d3c77e))


<a name="4.2.0-alpha-1"></a>
## [4.2.0-alpha-1](https://github.com/w3c/epubcheck/compare/v4.1.0...v4.2.0-alpha-1) (2019-01-14)

### Bug Fixes

* check MathML and XHTML content in epub:switch ([976b9f6](https://github.com/w3c/epubcheck/commit/976b9f6)), closes [#835](https://github.com/w3c/epubcheck/issues/835)
* messages of missing/incorrect locale cannot be translated ([91fac12](https://github.com/w3c/epubcheck/commit/91fac12))


### Features

* add 'authority' and 'term' as valid package metadata properties ([2fe66cd](https://github.com/w3c/epubcheck/commit/2fe66cd))
* allow missing `alttext` or `annotation-xml` on MathML ([e7bdbd2](https://github.com/w3c/epubcheck/commit/e7bdbd2)), closes [#897](https://github.com/w3c/epubcheck/issues/897)
* deprecate `epub:trigger` and `epub:switch` ([08123b2](https://github.com/w3c/epubcheck/commit/08123b2)), closes [#894](https://github.com/w3c/epubcheck/issues/894) [#895](https://github.com/w3c/epubcheck/issues/895)
* report aria-describedat as an ERROR ([b4a9e7c](https://github.com/w3c/epubcheck/commit/b4a9e7c)), closes [#896](https://github.com/w3c/epubcheck/issues/896)
* update the Package Document schema for EPUB 3.2 ([1f12512](https://github.com/w3c/epubcheck/commit/1f12512)), closes [#883](https://github.com/w3c/epubcheck/issues/883) [#882](https://github.com/w3c/epubcheck/issues/882) [#881](https://github.com/w3c/epubcheck/issues/881) [#880](https://github.com/w3c/epubcheck/issues/880) [#879](https://github.com/w3c/epubcheck/issues/879) [#878](https://github.com/w3c/epubcheck/issues/878) [#877](https://github.com/w3c/epubcheck/issues/877) [#876](https://github.com/w3c/epubcheck/issues/876)
* update XHTML, SVG and MathML schemas ([47d4926](https://github.com/w3c/epubcheck/commit/47d4926)), closes [#892](https://github.com/w3c/epubcheck/issues/892) [#779](https://github.com/w3c/epubcheck/issues/779) [#896](https://github.com/w3c/epubcheck/issues/896) [#893](https://github.com/w3c/epubcheck/issues/893) [#448](https://github.com/w3c/epubcheck/issues/448)
* **API:** deprecate `EpubChecker#processEpubFile` ([3ed77fe](https://github.com/w3c/epubcheck/commit/3ed77fe))

<a name="4.1.1"></a>
## [4.1.1](https://github.com/w3c/epubcheck/compare/v4.1.0...v4.1.1) (2019-01-22)

### Bug Fixes

* `switch` item property doesn't apply to `svg:switch` ([91b84ad](https://github.com/w3c/epubcheck/commit/91b84ad)), closes [#857](https://github.com/w3c/epubcheck/issues/857)
* better check remote resources ([85b5f77](https://github.com/w3c/epubcheck/commit/85b5f77)), closes [#852](https://github.com/w3c/epubcheck/issues/852)
* better support the EPUB Accessibility Vocabulary ([8a9cf63](https://github.com/w3c/epubcheck/commit/8a9cf63)), closes [#810](https://github.com/w3c/epubcheck/issues/810)
* consider all `video/*` as video media types ([27ad571](https://github.com/w3c/epubcheck/commit/27ad571))
* do not check entity references in comment or CDATA ([#949](https://github.com/w3c/epubcheck/issues/949)) ([4307542](https://github.com/w3c/epubcheck/commit/4307542)), closes [#800](https://github.com/w3c/epubcheck/issues/800)
* do not report CSS 'font-size: 0' as an error ([4e17714](https://github.com/w3c/epubcheck/commit/4e17714)), closes [#922](https://github.com/w3c/epubcheck/issues/922)
* keep on processing after failing to read GIFs ([2a244e7](https://github.com/w3c/epubcheck/commit/2a244e7))
* messages of missing/incorrect locale cannot be translated ([91fac12](https://github.com/w3c/epubcheck/commit/91fac12))
* resolve relative URIs against their base ([d42ccd6](https://github.com/w3c/epubcheck/commit/d42ccd6)), closes [#527](https://github.com/w3c/epubcheck/issues/527)
* valid JPEG files were considered 'corrupted' ([c4a2cfa](https://github.com/w3c/epubcheck/commit/c4a2cfa)), closes [#850](https://github.com/w3c/epubcheck/issues/850)
* wrong report of duplicate landmarks in Nav Doc ([#942](https://github.com/w3c/epubcheck/issues/942)) ([e06a552](https://github.com/w3c/epubcheck/commit/e06a552)), closes [#926](https://github.com/w3c/epubcheck/issues/926)


### Features

* update localized messages for several languages ([116e61b](https://github.com/w3c/epubcheck/commit/116e61b))

<a name="4.1.0"></a>
## [4.1.0](https://github.com/w3c/epubcheck/compare/v4.0.2...v4.1.0) (2018-11-26)

### Bug Fixes

* silence a Saxon warning (Schematron XSLT) ([5045d78b](https://github.com/w3c/epubcheck/commit/5045d78b)), closes #859
* fix path resolution in EpubNCXCheck (ctc package) ([f572a861](https://github.com/w3c/epubcheck/commit/f572a861))
* handle `IllegalStateException` in NCX checker ([25336894](https://github.com/w3c/epubcheck/commit/25336894)), closes #666
* check that the `mimetype` file is uncompressed ([6764e250](https://github.com/w3c/epubcheck/commit/6764e250)), closes #303
* fix wrong exit message for single file validation ([68af5a9a](https://github.com/w3c/epubcheck/commit/68af5a9a)), closes #740
* allow ARIA `role` attributes in SVG ([49412e05](https://github.com/w3c/epubcheck/commit/49412e05)), closes #769
* allow empty `xml:lang` attributes ([392c2f68](https://github.com/w3c/epubcheck/commit/392c2f68)), closes #777
* handle no src uri in fonts, correct embedded font boolean in the XML output ([a26f9c13](https://github.com/w3c/epubcheck/commit/a26f9c13)), closes #773
* fix issues with landmarks checks `ACC-008` ([74d0bdd1](https://github.com/w3c/epubcheck/commit/74d0bdd1)), closes #457, #734
* fix focus issue when using EPUBCheck in a GUI app ([cd63a166](https://github.com/w3c/epubcheck/commit/cd63a166)), closes #665
* fix incorrect warning `ACC_011` ([5e6a69af](https://github.com/w3c/epubcheck/commit/5e6a69af)), closes #680
* make the `type` attribute optional on SVG `style` elements ([275f6b6a](https://github.com/w3c/epubcheck/commit/275f6b6a)), closes #688
* exit with error when directory is not found in expanded mode ([e42d189c](https://github.com/w3c/epubcheck/commit/e42d189c)), closes #525
* fix a `NullPointerException` when checking an empty meta rendition element in OPF ([42d75297](https://github.com/w3c/epubcheck/commit/42d75297)), closes #727
* fix `DefaultReportImpl` to avoid duplicate path info in message locations ([9321355b](https://github.com/w3c/epubcheck/commit/9321355b)), closes #729
* fix broken `OPF_060` and `OPF_061` message format ([9f0e7d12](https://github.com/w3c/epubcheck/commit/9f0e7d12)), closes #658
* fix broken `OPF_060` and `OPF_061` checks for duplicate ZIP entries ([05e96f40](https://github.com/w3c/epubcheck/commit/05e96f40)), closes #728

### Features

* allow the configuration of EPUBCheck’s locale ([9b249956](https://github.com/w3c/epubcheck/commit/9b249956)), closes #650, #498
* report invalid `dc:identifier` UUIDs validation (as `WARNING`) ([48800a04](https://github.com/w3c/epubcheck/commit/48800a04)), closes #853
* change `--version` and `-version` command line options to output EPUBCheck version ([e498c61d](https://github.com/w3c/epubcheck/commit/e498c61d)), closes #743
* check files with extensions other than `.epub` ([1b67e046](https://github.com/w3c/epubcheck/commit/1b67e046)), closes #490
* report `file://` URL as `INFO` ([8f7a2b7d](https://github.com/w3c/epubcheck/commit/8f7a2b7d)), closes #289
* improve messages for `OPF-058` and `OPF-059` ([5e33645e](https://github.com/w3c/epubcheck/commit/5e33645e)), closes #804
* enable `NCX_001` check also for EPUB 3 when an NCX file is present ([9715c352](https://github.com/w3c/epubcheck/commit/9715c352))
* report non-matching identifiers in OPF and NCX as an error again ([515682dc](https://github.com/w3c/epubcheck/commit/515682dc))
* improved css font size validation ([25c0b372](https://github.com/w3c/epubcheck/commit/25c0b372)), closes #529
* issue a `WARNING` when landmarks anchors are not unique ([557308ef](https://github.com/w3c/epubcheck/commit/557308ef)), closes #493
* issue a `WARNING` when guide/reference elements are not unique ([25f28c01](https://github.com/w3c/epubcheck/commit/25f28c01)), closes #493
* partial update of OPF 2.0 RelaxNG schema to latest version (changing datatype `text` to `anyURI` for `href` attributes) ([251aa936](https://github.com/w3c/epubcheck/commit/251aa936)), closes #725
* display error/warning count in EPUBCheck results ([b7babedf](https://github.com/w3c/epubcheck/commit/b7babedf)), closes #655
* add file path info in `uri` attributes of the XML report ([c958c117](https://github.com/w3c/epubcheck/commit/c958c117)), closes #540
* update the XHTML 1.1 RelaxNG schema to latest version ([4c6fb49a](https://github.com/w3c/epubcheck/commit/4c6fb49a))
* update the OPF20 RNG schema in sync with official schema to validate empty guide elements ([6540b03d](https://github.com/w3c/epubcheck/commit/6540b03d))
* report an `ERROR` when `@clipBegin` equals `@clipEnd` in SMIL Media Overlays ([00716768](https://github.com/w3c/epubcheck/commit/00716768)), closes #568
* improve Nav Doc validation ([d32de854](https://github.com/w3c/epubcheck/commit/d32de854)), closes #763, #759
* update the NCX RelaxNG schema to add fixed list of `pageTarget` type values ([b2c9e939](https://github.com/w3c/epubcheck/commit/b2c9e939)), closes #761
* improve URL checks ([a44a596b](https://github.com/w3c/epubcheck/commit/a44a596b)), closes #708
* rephrase messages `RSC-005`, `RSC-016`, `RSC-017` ([5ef44973](https://github.com/w3c/epubcheck/commit/5ef44973))
* add JHove XSD schema declaration in XML output ([e55039c9](https://github.com/w3c/epubcheck/commit/e55039c9)), closes #736
* add detailed resource info in `RSC-008` messages ([5f5ef7b7](https://github.com/w3c/epubcheck/commit/5f5ef7b7)), closes #720
* add detailed resource info in `RSC-007` messages ([71a76ee4](https://github.com/w3c/epubcheck/commit/71a76ee4)), closes #475

### Maintenance

* change the project name to 'EPUBCheck' ([dfd7fd27](https://github.com/w3c/epubcheck/commit/dfd7fd27))
* update the minimum source code compatibility to Java 1.7 ([9b249956](https://github.com/w3c/epubcheck/commit/9b249956))
* update the Saxon dependency to v9.8 ([bf10f380](https://github.com/w3c/epubcheck/commit/bf10f380))
* update the Apache `commons-compress` dependency  to v1.18 ([e7dfedd8](https://github.com/w3c/epubcheck/commit/e7dfedd8))
* update the Google Guava dependency to v24.0 ([befd9fc3](https://github.com/w3c/epubcheck/commit/befd9fc3))
* update the continuous integration build matrix, now testing from Java 7 up to Java 11 ([fb84b23c](https://github.com/w3c/epubcheck/commit/fb84b23c))
* various translation updates ([39a9a093](https://github.com/w3c/epubcheck/commit/39a9a093), [6e3a8b41](https://github.com/w3c/epubcheck/commit/6e3a8b41))
