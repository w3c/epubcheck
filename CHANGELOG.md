# EPUBCheck change log

<a name="4.2.0-beta"></a>
## [4.2.0-beta](https://github.com/w3c/epubcheck/compare/v4.1.1...v4.2.0-beta) (2019-02-25)

This is a beta release of the forthcoming EPUBCheck v4.2.0, which
will provide support for checking conformance to EPUB 3.2.

It features updates to the various EPUB vocabularies (for epub:type and Package Document properties), support for new Core Media Types, as well as various other bug fixes and small improvements.

Note: Starting from this release, EPUBCheck is distributed to the Maven Central Repository under the [`org.w3c` group ID](https://search.maven.org/search?q=g:org.w3c%20AND%20a:epubcheck&core=gav), instead of the older `org.idpf` group ID.

This release was made by the DAISY Consortium, for the W3C.

**EPUBCheck 4.2.0-beta is a _preview_ release of EPUBCheck, to be used for testing and bug reporting only!**

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
