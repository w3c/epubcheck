Feature: EPUB 3 — Content Documents — XHTML


  Checks conformance to the "XHTML content documents" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-xhtml


  Background: 
    Given EPUB test files located at '/epub3/06-content-document/files/'
    And EPUBCheck with default settings


  ##  6.1 XHTML Content Documents
  
  Scenario: Verify that an XHTML Content Document can have any extension
    When checking EPUB 'content-xhtml-file-extension-unusual-valid'
    Then no errors or warnings are reported

  ###  6.1.2 XHTML requirements

  @spec @xref:sec-xhtml-req
  Scenario: Minimal Content Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-req
  Scenario: Report RelaxNG schema errors when checking a Content Document in a full publication
    When checking EPUB 'content-xhtml-relaxng-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-req
  Scenario: Report Schematron schema errors when checking a Content Document in a full publication
    When checking EPUB 'content-xhtml-schematron-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported


  ####  Document Properties - HTML Conformance
  
  #### Encoding
  
  Scenario: Report an XHMTL document not encoded as UTF-8
    When checking document 'encoding-utf16-error.xhtml'
    Then error HTM-058 is reported
    And no other errors or warnings are reported

  ####  Document Title

  Scenario: Report empty `title` element
    When checking document 'title-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains '"title" must not be empty'
    And no other errors or warnings are reported

  Scenario: Report missing `title` element
    When checking document 'title-missing-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains 'The "head" element should have a "title" child element.'
    And no other errors or warnings are reported


  ####  DOCTYPE

  Scenario: Verify versionless HTML `doctype`
    When checking document 'doctype-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `doctype` with legacy string
    When checking document 'doctype-legacy-compat-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `doctype` with obsolete public identifier
    When checking document 'doctype-obsolete-error.xhtml'
    Then error HTM-004 is reported
    And no other errors or warnings are reported


  ####  Entities

  Scenario: Verify that character references are allowed
    When checking document 'entities-character-references-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that character entity references in comments or CDATA sections are ignored 
    When checking document 'entities-comments-cdata-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that internal entity declarations are allowed
    When checking document 'entities-internal-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report external entities
    When checking document 'entities-external-error.xhtml'
    Then error HTM-003 is reported
    And no other errors or warnings are reported
  
  Scenario: Report entity references not ending with a semicolon
    When checking document 'entities-no-semicolon-error.xhtml'
    Then fatal error RSC-016 is reported
    And the message contains 'must end with the \';\' delimiter'
    And no other errors or warnings are reported
  
  Scenario: Report unknown entity references
    When checking document 'entities-unknown-error.xhtml'
    Then fatal error RSC-016 is reported
    And the message contains 'was referenced, but not declared'
    And no other errors or warnings are reported


  ####  IDs

  Scenario: Report duplicate `id` attribute values
    When checking document 'id-duplicate-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate ID'
    And no other errors or warnings are reported

  Scenario: Verify `id` attribute with non-alphanumeric in its value
    When checking document 'id-not-ncname-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that ID-referencing attributes can refer to non-NCName IDs
    When checking document 'id-ref-non-ncname-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report ID-referencing attributes that refer to non-existing IDs
    When checking document 'id-ref-not-found-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'must refer to elements in the same document (target ID missing)'
    And no other errors or warnings are reported
  
  ####  ARIA

  Scenario: Verify ARIA role allowed on an `a` element with no `href` attribute
    When checking document 'aria-role-a-nohref-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report non-existent ARIA `describedat` attribute
    When checking document 'aria-describedAt-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "aria-describedat" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `footer`
    When checking document 'aria-roles-footer-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `h1`-'h6`
    When checking document 'aria-roles-h1-h6-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `header`
    When checking document 'aria-roles-header-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `img`
    When checking document 'aria-roles-img-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report deprecated DPUB-ARIA roles on `li`
    When checking document 'aria-roles-li-deprecated-warning.xhtml'
    Then following warnings are reported:
      | RSC-017 | "doc-endnote" role is deprecated |
      | RSC-017 | "doc-endnote" role is deprecated |
      | RSC-017 | "doc-biblioentry" role is deprecated |
      | RSC-017 | "doc-biblioentry" role is deprecated |
    Then no other errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `nav`
    When checking document 'aria-roles-nav-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `section`
    When checking document 'aria-roles-section-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify ARIA attributes allowed on SVG elements
    When checking document 'svg-aria-valid.xhtml'
    Then no errors or warnings are reported

  
  ####  Attributes (General)

  Scenario: Verify the value of HTML boolean attributes and enumerated attributes are parsed in a case-insensitive manner
    When checking document 'attrs-case-insensitive-valid.xhtml'
    Then no errors or warnings are reported

  ####  base

  Scenario: Verify that a base url can be set
    When checking EPUB 'content-xhtml-base-url-valid'
    Then no errors or warnings are reported

  Scenario: Report relative paths as remote resources when HTML `base` is set to an extenal URL (issue 155)
    When checking EPUB 'content-xhtml-base-url-remote-relative-path-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report relative paths as remote resources when `xml:base` is set to an extenal URL (issue 155)
    When checking EPUB 'content-xhtml-xml-base-url-remote-relative-path-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  ####  Canvas

  Scenario: Verify canvas element with a fallback
    When checking document 'canvas-valid.xhtml'
    Then no errors or warnings are reported

  ####  Custom Elements

  Scenario: Verify custom elements are not rejected
    When checking document 'custom-elements-valid.xhtml'
    Then no errors or warnings are reported

  ####  data attributes

  Scenario: Verify `data-*` attributes are allowed 
    When checking document 'data-attr-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report invalid `data-*` attributes
    When checking document 'data-attr-invalid-error.xhtml'
    Then error HTM-061 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report invalid elements after a `data-*` attribute
    See issue 189 - was allowed by stripping of `data-*` attributes
    When checking EPUB 'content-xhtml-data-attr-removal-markup-error'
    Then error RSC-005 is reported
    And the message contains 'element "somebadxhtmlformatting" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify fragment identifiers are allowed in attributes after a `data-*` declaration
    See issue 198 - caused error from stripping of `data-*` attributes
    When checking EPUB 'content-xhtml-data-attr-removal-fragments-valid'
    Then no errors or warnings are reported


  ####  hyperlinks

  Scenario: Do not report escaped hyperlinks to resources in the local file system
    See issue #1182
    When checking EPUB 'content-xhtml-link-to-local-file-escaped-valid'
    Then no errors or warnings are reported
    
  @spec @xref:sec-container-iri
  Scenario: Report a hyperlink to a resource missing from the publication
    When checking EPUB 'content-xhtml-link-to-missing-doc-error'
    Then error RSC-007 is reported
    And no errors or warnings are reported

  Scenario: Report a hyperlink to a missing identifier
    When checking EPUB 'content-xhtml-link-to-missing-id-error'
    Then error RSC-012 is reported
    And no errors or warnings are reported

  Scenario: Report a hyperlink to a missing identifier in the Nav Doc
    When checking EPUB 'content-xhtml-link-to-missing-id-in-nav-doc-error'
    Then error RSC-012 is reported
    And no errors or warnings are reported

  Scenario: Report a hyperlink to a mising identifier in another document
    When checking EPUB 'content-xhtml-link-to-missing-id-xref-error'
    Then error RSC-012 is reported
    And no errors or warnings are reported

  Scenario: Verify that href values that only contain whitepace are allowed
    See issue 225 - asked for a warning, but an empty string is a valid URL
    When checking EPUB 'content-xhtml-link-href-empty-valid'
    Then no errors or warnings are reported

  Scenario: Verify that relative paths starting with a single dot are resolved properly
    See issue 270
    When checking EPUB 'content-xhtml-link-rel-path-dot-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-manifest-elem
  Scenario: Report a reference from an XHTML doc to a resource not declared in the manifest
    When checking EPUB 'content-xhtml-referenced-resource-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report fragment identifiers used in stylesheet URLs
    When checking document 'content-xhtml-link-stylesheet-fragment-id-error'
    Then error RSC-013 is reported
    Then no other errors or warnings are reported

  # FIXME not sure this error is legit
  Scenario: Report a hyperlink to SVG symbol ("incompatible resource type")
    When checking document 'content-xhtml-link-to-svg-fragment-error'
    Then error RSC-014 is reported
    Then no other errors or warnings are reported

  Scenario: Allow valid hyperlink URLs
    When checking document 'a-href-valid.xhtml'
    Then no errors or warnings are reported
    
  ####  iframes
  
  Scenario: Verify that an `iframe` can reference another XHTML document
    When checking EPUB 'content-xhtml-iframe-basic-valid'
    Then no errors or warnings are reported


  ####  img

  Scenario: Report an `img` element with an empty `src` attribute
    When checking document 'img-src-empty-error.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Report an `img` element with no `alt` attribute
    When checking document 'img-alt-missing-error.xhtml'
    # FIXME see issue #446
    #Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Allow an `img` element with no `alt` attribute when it has a `title` attribute
    When checking document 'img-alt-missing-with-title-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Allow an `img` element with no `alt` attribute when it is in a captioned figure
    When checking document 'img-alt-missing-in-figure-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that `img` element can reference SVG fragments
    When checking EPUB 'content-xhtml-img-fragment-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report non-SVG images referenced as fragments
    When checking EPUB 'content-xhtml-img-fragment-non-svg-warning'
    Then warning RSC-009 is reported 2 times (1 for an HTML `img` element, 1 for an SVG `image` element)
    And no other errors or warnings are reported

  @spec @xref:sec-manifest-elem
  Scenario: Report references to undeclared resources in `img srcset`
    When checking EPUB 'content-xhtml-img-srcset-undeclared-error'
    Then error RSC-008 is reported (undeclared resource in srcset)
    And no other errors or warnings are reported

  Scenario: Allow an `img` element with a video resource
    When checking EPUB 'content-xhtml-img-video-valid'
    Then no errors or warnings are reported


  ####  lang
  
  Scenario: Verify empty language tag allowed (issue 777)
    When checking document 'lang-empty-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a mismatch in `lang` and `xml:lang` attributes
    When checking document 'content-xhtml-lang-xml-lang-mismatch-error'
    Then error RSC-005 is reported
    And the message contains 'lang and xml:lang attributes must have the same value'
    And no other errors or warnings are reported

  Scenario: Verify that three-character language codes are allowed (issue 615)
    When checking EPUB 'lang-three-char-code-valid.xhtml'
    Then no errors or warnings are reported


  ####  Links

  Scenario: Verify a `link` element with a known alt style tag
    Given the reporting level is set to usage
    When checking document 'link-alt-style-tags-known-valid.xhtml'
    Then no usages are reported
    And no errors or warnings are reported

  Scenario: Report a `link` element with an unknown alt style tag
    Given the reporting level is set to usage
    When checking document 'link-alt-style-tags-unknown-valid.xhtml'
    Then no errors or warnings are reported
    And no usages are reported

  Scenario: Report a `link` element with an unknown alt style tag
    Given the reporting level is set to usage
    When checking document 'link-alt-style-tags-conflict-usage.xhtml'
    Then no errors or warnings are reported
    And usage CSS-005 is reported
    But no other usages are reported

  Scenario: Verify `link` elements used to declare alternative stylesheets
    When checking document 'link-rel-stylesheet-alternate-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `link` element defining an alternative stylesheet with no title
    When checking document 'link-rel-stylesheet-alternate-no-title-error.xhtml'
    Then error CSS-015 is reported 2 times (one for a missing title, one for an empty title)
    And no other errors or warnings are reported

  ####  Lists

  Scenario: Verify an `li` with a `value` attribute (issue 248) 
    When checking document 'li-with-value-attr-valid.xhtml'
    Then no errors or warnings are reported


  ####  Main

  Scenario: Verify `main` element is allowed
    See issue 340
    When checking document 'main-valid.xhtml'
    Then no errors or warnings are reported


  ####  Map
  
  Scenario: Verify image map
    See issue 696
    When checking document 'map-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report invalid image map
    See issue 696
    When checking document 'map-usemap-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "usemap" is invalid'
    And no errors or warnings are reported

  ####  MathML

  @spec @xref:sec-manifest-elem
  Scenario: Allow a MathML formula with an alternative image
    When checking EPUB 'content-xhtml-mathml-altimg-valid'
    Then no other errors or warnings are reported

  @spec @xref:sec-manifest-elem
  Scenario: Report a MathML formula with an alternative image that cannot be found
    When checking EPUB 'content-xhtml-mathml-altimg-not-found-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported


  ####  meta

  Scenario: Verify `http-equiv` declaration
    When checking document 'http-equiv-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify case-insensitive `http-equiv` declaration
    When checking document 'http-equiv-case-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `http-equiv` declaration of non-utf8 charset
    When checking document 'http-equiv-non-utf8-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'must have the value "text/html; charset=utf-8"'
    And no other errors or warnings are reported

  Scenario: Report both `http-equiv` and `charset` are declared
    When checking document 'http-equiv-and-charset-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'must not contain both a meta element in encoding declaration state (http-equiv=\'content-type\') and a meta element with the charset attribute'
    And no other errors or warnings are reported
    
  @spec @xref:sec-fxl-content-dimensions
  Scenario: Verify that `viewport meta` declaration is not checked for non-fixed layout documents (issue 419)
    When checking EPUB 'content-xhtml-meta-viewport-non-fxl-valid'
    Then no errors or warnings are reported


  #### Microdata

  Scenario: Verify that microdata attributes are allowed on elements
    When checking document 'microdata-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report use of microdata attributes on elements where they are not allowed
    When checking document 'microdata-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "a" missing required attribute "href"'
    And error RSC-005 is reported 2 times
    And the message contains 'If the itemprop is specified on'
    And no other errors or warnings are reported

  ####  Non-conforming Features

  Scenario: Report the obsolete `typemustmatch` attribute
    When checking document 'obsolete-typemustmatch-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "typemustmatch" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report the obsolete `contextmenu` attribute
    When checking document 'obsolete-contextmenu-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "contextmenu" not allowed here'
    And no other errors or warnings are reported
  
  Scenario: Report the obsolete `dropzone` attribute
    When checking document 'obsolete-dropzone-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "dropzone" not allowed here'
    And no other errors or warnings are reported
  
  Scenario: Report the obsolete `keygen` element
    When checking document 'obsolete-keygen-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "keygen" not allowed here'
    And no other errors or warnings are reported
  
  Scenario: Report obsolete features of the `menu` element
    When checking document 'obsolete-menu-features-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "type" not allowed here'
    And error RSC-005 is reported
    And the message contains 'element "command" not allowed here'
    And error RSC-005 is reported
    And the message contains 'element "button" not allowed here'
    And no other errors or warnings are reported
  
  Scenario: Report obsolete `pubdate` attribute
    When checking document 'obsolete-pubdate-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "pubdate" not allowed here'
    And no other errors or warnings are reported
  
  Scenario: Report obsolete `seamless` attribute
    When checking document 'obsolete-seamless-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "seamless" not allowed here'
    And no other errors or warnings are reported


  #### Objects

  Scenario: Allow fragment identifiers on PDFs
    When checking EPUB 'object-pdf-fragment-valid'
    Then no errors or warnings are reported

  ####  Schematron Assertions

  @spec @xref:sec-xhtml-req
  Scenario: Verify passing schematron assertions
    When checking document 'schematron-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-req
  Scenario: Report failing schematron assertions
    When checking document 'schematron-error.xhtml'
    Then all messages have line and column info
    And error RSC-005 is reported 43 times

  ####  script
  
  #//TODO verify script core media types
  
  #### source
  
  Scenario: Verify non-HTML `source` elements are skipped
    See https://github.com/w3c/epubcheck/issues/1514
    When checking EPUB 'dc-source-valid'
    Then no errors or warnings are reported

  ####  Style

  Scenario: Verify use of `style` element in the header
    When checking document 'style-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `style` element without a `type` declaration
    When checking document 'style-no-type-error.xhtml'
    Then error CSS-008 is reported
    And no other errors or warnings are reported
  
  Scenario: Report `style` element in the body
    When checking document 'style-in-body-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "style" not allowed here'
    And error RSC-005 is reported
    And the message contains 'attribute "scoped" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify the `style` attribute is allowed with valid syntax
    When checking document 'style-attr-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report use of `style` attribute with invalid css syntax
    When checking document 'style-attr-syntax-error.xhtml'
    Then error CSS-008 is reported
    And no other errors or warnings are reported
    
  ####  SVG

  Scenario: Verify that an SVG image can be referenced from `img`, `object` and `iframe` elements
    When checking EPUB 'content-xhtml-svg-reference-valid'
    And no errors or warnings are reported

  Scenario: Verify that `svg:switch` doesn't trigger the package document `switch` property check
    When checking EPUB 'content-xhtml-svg-switch-valid'
    Then no errors or warnings are reported

  Scenario: Verify that `svgView` fragments are allowed when associated to SVG documents
    When checking EPUB 'content-xhtml-svg-fragment-svgview-valid'
    Then no errors or warnings are reported

  ####  Tables

  Scenario: Verify `border` attribute allowed on tables 
    When checking document 'table-border-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `table` with invalid `border` attribute value 
    When checking document 'table-border-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "border" is invalid'
    And no other errors or warnings are reported


  ####  Time

  Scenario: Verify various `datetime` values (incl. issue 341)
    When checking document 'time-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Report various invalid `datetime` formats
    When checking document 'time-error.xhtml'
    Then error RSC-005 is reported 25 times
    And the message contains 'value of attribute "datetime" is invalid'
    And no other errors or warnings are reported

  Scenario: Report a `time` element nested inside another
    When checking document 'time-nested-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "time" not allowed here'
    And no other errors or warnings are reported
  

  ####  URLs

  @spec @xref:sec-container-iri
  Scenario: Verify valid URLs
    When checking document 'url-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report non-conforming URLs
    When checking document 'url-invalid-error.xhtml'
    Then error RSC-020 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a URL host that cannot be parsed
    When checking document 'url-host-unparseable-warning.xhtml'
    # Some apparently invalid host parse OK with Galimatias. To be investigated.
    And error RSC-020 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report unregistered URL scheme
    When checking document 'url-unregistered-scheme-warning.xhtml'
    And warning HTM-025 is reported
    And no other errors or warnings are reported

  ####  XML Support

  Scenario: Report an XML 1.1 version declaration
    When checking document 'xml11-error.xhtml'
    Then error HTM-001 is reported
    And no other errors or warnings are reported

	### 6.1.3 HTML Extensions

  #### 6.1.3.1 Structural semantics

  @spec @xref:sec-xhtml-structural-semantics
  Scenario: Verify `epub:type` attribute on allowed content
    When checking document 'epubtype-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-structural-semantics
  Scenario: Report `epub:type` attribute on 'head' or metadata content 
    When checking document 'epubtype-disallowed-error.xhtml'
    Then error RSC-005 is reported 8 times
    And no other errors or warnings are reported
  
  Scenario: Verify `epub:type` attribute with reserved vocabulary
    When checking document 'epubtype-reserved-vocab-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `epub:type` attribute with author-declared vocabulary
    When checking document 'epubtype-declared-vocab-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `epub:type` attribute with unknown semantic
    Given the reporting level set to usage
    When checking document 'epubtype-unknown-usage.xhtml'
    Then usage OPF-088 is reported
    And no other errors or warnings are reported

  Scenario: Verify `epub:type` attribute with deprecated semantic
    Given the reporting level set to usage
    When checking document 'epubtype-deprecated-usage.xhtml'
    Then usage OPF-086b is reported 10 times
    And no other errors or warnings are reported

  Scenario: Verify `epub:type` attribute that does not follow usage suggestions
    Given the reporting level set to usage
    When checking document 'epubtype-misuse-usage.xhtml'
    Then usage OPF-087 is reported 7 times
    And no other errors or warnings are reported

  #### 6.1.3.2 RDFa

  @spec @xref:sec-xhtml-rdfa
  Scenario: Verify RDFa attributes are allowed on HTML elements
    When checking document 'rdfa-valid.xhtml'
    Then no errors or warnings are reported



  #### 6.1.3.3 Content Switching (Deprecated)

  Scenario: Report `epub:switch` is deprecated
    When checking document 'switch-deprecated-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported
  
  Scenario: Report `epub:switch` with invalid mathml
    When checking document 'switch-mathml-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "math" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report an `epub:switch` with a `default` before any `case` elements 
    When checking document 'switch-default-before-case-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed yet'
    And error RSC-005 is reported
    And the message contains 'element "epub:case" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report an `epub:switch` with multiple `default` elements
    When checking document 'switch-multiple-default-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report `epub:switch` without any `case` elements
    When checking document 'switch-no-case-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed yet'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report `epub:switch` element without a `default`
    When checking document 'switch-no-default-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:switch" incomplete'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report `epub:case` without a `required-namespace` attribute
    When checking document 'switch-no-case-namespace-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:case" missing required attribute "required-namespace"'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported


  #### 6.1.3.4 The epub:trigger Element (Deprecated)

  Scenario: Report `epub:trigger` is deprecated
    When checking document 'trigger-deprecated-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains 'The "epub:trigger" element is deprecated'
    And no other errors or warnings are reported

  Scenario: Report `epub:trigger` that references non-existent IDs
    When checking document 'trigger-badrefs-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'The ref attribute must refer to an element in the same document'
    And error RSC-005 is reported
    And the message contains 'The ev:observer attribute must refer to an element in the same document'
    And warning RSC-017 is reported 2 times
    And the message contains 'The "epub:trigger" element is deprecated'
    And no other errors or warnings are reported


  #### 6.1.3.5 Custom Attributes

  @spec @xref:sec-xhtml-custom-attributes
  Scenario: Verify attributes in custom namespaces are ignored
    When checking document 'attrs-custom-ns-valid.xhtml'
    Then no errors or warnings are reported
  
  @spec @xref:sec-xhtml-custom-attributes
  Scenario: Report custom attributes using reserved strings in their namespace
    When checking document 'attrs-custom-ns-reserved-error.xhtml'
    Then error HTM-054 is reported 2 times
    And no other errors or warnings are reported
    

  Scenario: Verify SSML attributes are allowed
    When checking document 'ssml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report SSML `ph` attribute without a value
    When checking document 'ssml-empty-ph-warning.xhtml'
    Then warning HTM-007 is reported 2 times
    And no other errors or warnings are reported

  ###  6.1.4 HTML deviations and constraints

  #### 6.1.4.1 Embedded MathML

  Scenario: Verify MathML markup with prefixed elements
    When checking document 'mathml-prefixed-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with unprefixed elements
    When checking document 'mathml-unprefixed-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-deviations
  Scenario: Allow MathML deprecated features
    When checking EPUB 'mathml-deprecated-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup without alternative text
    Given the reporting level set to usage
    When checking document 'mathml-noalt-usage.xhtml'
    Then usage ACC-009 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-deviations
  Scenario: Report MathML markup with only content MathML
    When checking document 'mathml-contentmathml-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "apply" not allowed here'
    And error RSC-005 is reported
    And the message contains 'element "cn" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify MathML with tex annotation
    When checking document 'mathml-anno-tex-valid.xhtml'
    Then no errors or warnings are reported
  
  @spec @xref:sec-xhtml-deviations
  Scenario: Verify MathML with content MathML annotation
    When checking document 'mathml-anno-contentmathml-valid.xhtml'
    Then no errors or warnings are reported
  
  @spec @xref:sec-xhtml-deviations
  Scenario: Verify MathML with presentation MathML annotation
    When checking document 'mathml-anno-presmathml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML with descendant MathML markup in an XHTML annotation
    When checking document 'mathml-anno-xhtml-with-mathml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report Content MathML annotation without a `name` attribute
    When checking document 'mathml-anno-noname-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "annotation-xml" missing required attribute "name"'
    And no other errors or warnings are reported

  Scenario: Report MathML annotation with an invalid `name` attribute
    When checking document 'mathml-anno-name-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "name" is invalid'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-deviations
  Scenario: Report MathML annotation with an invalid `encoding` attribute
    When checking document 'mathml-anno-encoding-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "apply" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify MathML markup with an XHTML annotation
    When checking document 'mathml-anno-xhtml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with XHTML annotation missing name attribute
    When checking document 'mathml-anno-xhtml-noname-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-deviations
  Scenario: Verify MathML markup with `annotation-xml` `name` attribute set to `contentequiv`
    When checking document 'mathml-anno-xhtml-contentequiv-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with an HTML encoded annotation
    When checking document 'mathml-anno-xhtml-html-encoding-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify MathML markup with an SVG annotation
    When checking document 'mathml-anno-svg-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a MathML annotation with the XHTML encoding reversed (application/xml+xhtml)
    When checking document 'mathml-anno-xhtml-encoding-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "encoding" is invalid; must be equal to'
    And no other errors or warnings are reported


  #### 6.1.4.2 Embedded SVG

  @spec @xref:sec-xhtml-svg
  Scenario: Verify inclusion of SVG markup
    When checking document 'svg-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify conforming SVG markup does not create false-positives
    When checking document 'svg-regression-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify the SVG IDs can be any valid HTML ID
    When checking document 'svg-id-valid.xhtml'
    Then no errors or warnings are reported

  @spec @xref:sec-xhtml-svg @xref:sec-svg-req
  Scenario: Verify that `epub:type` attribute can be used on SVG
    When checking document 'svg-epubtype-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that SVG validation erors are reported as USAGE
    Given the reporting level set to usage 
    When checking document 'svg-invalid-usage.xhtml'
    Then usage RSC-025 is reported 
    And the message contains 'element "foo" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify unprefixed HTML elements allowed inside prefixed `foreignObject`
    When checking document 'svg-foreignObject-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that the `requiredExtensions` attribute can have any value
    When checking document 'svg-foreignObject-requiredExtensions-valid.xhtml'
    And no other errors or warnings are reported

  Scenario: Report `foreignObject` with a body element
    When checking document 'svg-foreignObject-with-body-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "body" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report HTML validation errors within `foreignObject` content
    When checking document 'svg-foreignObject-html-invalid-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report `foreignObject` without flow content
    When checking document 'svg-foreignObject-not-flow-content-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "title" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify `title` valid content model
    When checking document 'svg-title-content-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `title` with non-HTML elements
    When checking document 'svg-title-content-not-html-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'elements from namespace "https://example.org" are not allowed'
    Then error RSC-005 is reported
    And the message contains 'elements from namespace "http://www.w3.org/2000/svg" are not allowed'
    And no other errors or warnings are reported
    
  Scenario: Report HTML validation errors within `title` content
    When checking document 'svg-title-content-invalid-html-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify RDF elements can be embedded in SVG
    When checking document 'svg-rdf-valid.xhtml'
    Then no errors or warnings are reported


  #### 6.1.4.3 Discouraged Constructs

  Scenario: Report `base` as a discouraged construct
    Given the reporting level is set to usage
    When checking document 'discouraged-base-warning.xhtml'
    Then usage HTM-055 is reported
    And the message contains 'base'
    And no errors or warnings are reported

  Scenario: Report `embed` as a discouraged construct
    Given the reporting level is set to usage
    When checking document 'discouraged-embed-warning.xhtml'
    Then usage HTM-055 is reported
    And the message contains 'embed'
    And no other errors or warnings are reported

  Scenario: Report `rp` as a discouraged construct
    Given the reporting level is set to usage
    When checking document 'discouraged-rp-warning.xhtml'
    Then usage HTM-055 is reported 2 times
    And the message contains 'rp'
    And no other errors or warnings are reported

  #### Other

  Scenario: Report an unrecognized `epub` namespace (informative)
    Given the reporting level is set to usage
    When checking document 'ns-epub-unknown-info.xhtml'
    Then usage HTM-010 is reported
    And the message contains 'Namespace "http://http://www.idpf.org/2007/ops" is unusual'
    And no errors or warnings are reported
