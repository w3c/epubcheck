# EPUBCheck change log

<a name="4.1.1"></a>
## [4.1.1](https://github.com/w3c/epubcheck/compare/v4.1.0...v4.1.1) (2019-01-22)

This is a maintenance release of EPUBCheck, the conformance validator for EPUB publications. It mostly consists of bug fixes and updated localized messages.

This release was made by the DAISY Consortium, for the W3C.

Many thanks to the various people who contributed to this release, notably the team of translators including Elisa Molinari, Emiliano Molina, Gregorio Pellegrino, Masayoshi Takahashi, Merijn de Haen, Thiago de Oliveira Pereira, Vincent Gros, Woongyoung Park… and especially Tobias Fischer, who has been coordinating the translation contributions!

**Note:** this release is part of the 4.1.x stable stream, which checks conformance to the EPUB 3.0.1 family of specifications. It does _not_ include support for the newer EPUB 3.2 specifications. For a preview of EPUB 3.2 conformance checking, feel free to try the latest release in the 4.2.x beta stream (currently [v4.2.0-alpha-1](https://github.com/w3c/epubcheck/releases/tag/v4.2.0-alpha-1))!

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
