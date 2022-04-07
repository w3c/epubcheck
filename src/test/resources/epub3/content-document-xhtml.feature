Feature: EPUB 3 ▸ Content Documents ▸ XHTML Document Checks


  Checks conformance to the EPUB Content Documents 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-contentdocs.html

  In the scenarios below, checks are run against single XHTML Content Documents.
  EPUBCheck is launched in 'xhtml' mode.


  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And test files located at '/epub3/files/content-document-xhtml/'


  ## 2. XHTML Content Documents
  
  Scenario: Minimal Content Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  ### 2.2 Content Conformance -- HTML
  
  ####  ARIA

  Scenario: Verify ARIA role allowed on an `a` element with no `href` attribute
    When checking document 'aria-role-a-nohref-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report non-existent ARIA `describedat` attribute
    When checking document 'aria-describedAt-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "aria-describedat" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Verify the DPUB-ARIA roles allowed on `a`
    When checking document 'aria-roles-img-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `aside`
    When checking document 'aria-roles-img-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `footer`
    When checking document 'aria-roles-footer-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `h1`-'h6`
    When checking document 'aria-roles-h1-h6-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `header`
    When checking document 'aria-roles-header-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `hr`
    When checking document 'aria-roles-header-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `img`
    When checking document 'aria-roles-img-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the DPUB-ARIA roles allowed on `li`
    When checking document 'aria-roles-li-valid.xhtml'
    Then no errors or warnings are reported

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


  ####  Canvas

  Scenario: Verify canvas element with a fallback
    When checking document 'canvas-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report canvas that fallbacks back to a canvas
    When checking document 'canvas-fallback-error.xhtml'
    Then error MED-002 is reported
    And no other errors or warnings are reported


  ####  Custom Elements

  Scenario: Verify custom elements are not rejected
    When checking document 'custom-elements-valid.xhtml'
    Then no errors or warnings are reported


  ####  Data Attributes

  Scenario: Verify `data-*` attributes are allowed 
    When checking document 'data-attr-valid.xhtml'
    Then no errors or warnings are reported


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
    And error RSC-005 is reported
    And the message contains 'must end with the \';\' delimiter'
    And no other errors or warnings are reported
  
  Scenario: Report unknown entity references
    When checking document 'entities-unknown-error.xhtml'
    Then fatal error RSC-016 is reported
    And error RSC-005 is reported
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

  ###   Images

  Scenario: Report an `img` element with an empty `src` attribute
    When checking document 'img-src-empty-error.xhtml'
    Then error HTM-008 is reported
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

  ####  Language
  
  Scenario: Verify empty language tag allowed (issue 777)
    When checking document 'lang-empty-valid.xhtml'
    Then no errors or warnings are reported


  ####  Links

  Scenario: Verify a `link` element with a known alt style tag
    When checking document 'link-alt-style-tags-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a `link` element with an unknown alt style tag
    When checking document 'link-alt-style-tags-error.xhtml'
    Then error OPF-027 is reported
    And error CSS-005 is reported
    And no other errors or warnings are reported

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

  Scenario: Verify `main` element is allowed (issue 340)
    When checking document 'main-valid.xhtml'
    Then no errors or warnings are reported


  ####  Map
  
  Scenario: Verify image map (issue 696)
    When checking document 'map-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report invalid image map (issue 696)
    When checking document 'map-usemap-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "usemap" is invalid'
    And no errors or warnings are reported


  ####  Meta

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
  

  ####  RDFa

  Scenario: Verify RDFa attributes are allowed on HTML elements
    When checking document 'rdfa-valid.xhtml'
    Then no errors or warnings are reported


  ####  Schematron Assertions

  Scenario: Verify no schematron assertions
    When checking document 'schematron-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report schematron assertions without line or column numbers
    When checking document 'schematron-error.xhtml'
    Then error MED-002 is reported 1 times
    And error RSC-005 is reported 43 times

    #    // unclear how to port this function
    #    new ExtraReportTest
    #            @Override
    #      Scenario: test(ValidationReport testReport)
    #                for (ItemReport error : testReport.errorList)
    #                    assertTrue("Error '" + error.message + "' has no line number.", error.line != -1);
    #          assertTrue("Error '" + error.message + "' has no column number.", error.column != -1);
    #                      });


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

  Scenario: Verify valid URLs
    When checking document 'url-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report non-conforming URLs
    When checking document 'url-invalid-error.xhtml'
    Then error RSC-020 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report a URL host that cannot be parsed
    When checking document 'url-host-unparseable-warning.xhtml'
    # Some apparently invalid host parse OK with Galimatias. To be investigated.
    And error RSC-020 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report unregistered URL scheme
    When checking document 'url-unregistered-scheme-warning.xhtml'
    And warning HTM-025 is reported
    And no other errors or warnings are reported


  ####  XML Support

  Scenario: Report an XML 1.1 version declaration
    When checking document 'xml11-error.xhtml'
    Then error HTM-001 is reported
    And no other errors or warnings are reported
  
  
  ## 2.4 HTML Extensions

  ### 2.4.1 Semantic Inflection

  Scenario: Verify `epub:type` attribute with valid semantic
    When checking document 'epubtype-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify `epub:type` attribute in document header
    When checking document 'epubtype-in-head-valid.xhtml'
    Then no errors or warnings are reported

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
    When checking document 'epubtype-disallowed-usage.xhtml'
    Then usage OPF-087 is reported 7 times
    And no other errors or warnings are reported

  Scenario: Report `epub:type` attribute with a semantic from an undeclared vocabulary
    When checking document 'epubtype-prefix-undeclared-error.xhtml'
    Then error OPF-028 is reported
    And no other errors or warnings are reported


  ### 2.4.2 Semantic Enrichment

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


  ### 2.4.3 SSML Attributes

  Scenario: Verify SSML attributes are allowed
    When checking document 'ssml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report SSML `ph` attribute without a value
    When checking document 'ssml-empty-ph-warning.xhtml'
    Then warning HTM-007 is reported 2 times
    And no other errors or warnings are reported


  ### 2.4.4 Content Switching (Deprecated)

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


  ### 2.4.5 The epub:trigger Element (Deprecated)

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


  ### 2.4.6 Custom Attributes

  Scenario: Verify attributes in custom namespaces are ignored
    When checking document 'attrs-custom-ns-valid.xhtml'
    Then no errors or warnings are reported
    
  Scenario: Report custom attributes using reserved strings in their namespace
    When checking document 'attrs-custom-ns-reserved-error.xhtml'
    Then error HTM-054 is reported 2 times
    And no other errors or warnings are reported

  ## 2.5 HTML Deviations and Constraints

  ### 2.5.1 Embedded MathML

  Scenario: Verify MathML markup with prefixed elements
    When checking document 'mathml-prefixed-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with unprefixed elements
    When checking document 'mathml-unprefixed-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup without alternative text
    Given the reporting level set to usage
    When checking document 'mathml-noalt-usage.xhtml'
    Then usage ACC-009 is reported
    And no other errors or warnings are reported

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
  
  Scenario: Verify MathML with content MathML annotation
    When checking document 'mathml-anno-contentmathml-valid.xhtml'
    Then no errors or warnings are reported
  
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


  ### 2.5.2 Embedded SVG

  Scenario: Verify inclusion of SVG markup
    When checking document 'svg-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify conforming SVG markup does not create false-positives
    When checking document 'svg-regression-valid.xhtml'
    Then no errors or warnings are reported
  Scenario: Verify the SVG IDs can be any valid HTML ID
    When checking document 'svg-id-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that `epub:type` attribute can be used on SVG
    When checking document 'svg-with-epubtype-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that SVG validation erors are reported as USAGE
    Given the reporting level set to usage 
    When checking document 'svg-invalid-usage.xhtml'
    Then usage RSC-025 is reported 
    And the message contains 'element "foo" not allowed here'
    And no other errors or warnings are reported

  #TODO review if this warning is relevant
  Scenario: Report an SVG link without a recommended title
    When checking document 'svg-links-no-title-warning.xhtml'
    Then warning ACC-011 is reported
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

  Scenario: Verify `title` can contain text
    When checking document 'svg-title-text-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `title` can contain HTML phrasing content
    When checking document 'svg-title-phrasing-content-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `title` with non-phrasing content
    When checking document 'svg-title-not-phrasing-content-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "h1" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report HTML validation errors within `title` content
    When checking document 'svg-title-html-invalid-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify RDF elements can be embedded in SVG
    When checking document 'svg-rdf-valid.xhtml'
    Then no errors or warnings are reported

  ## Discouraged Constructs

  Scenario: Report `base` as a discouraged construct
    When checking document 'discouraged-base-warning.xhtml'
    Then warning HTM-055 is reported
    And the message contains 'base'
    And no other errors or warnings are reported

  Scenario: Report `embed` as a discouraged construct
    When checking document 'discouraged-embed-warning.xhtml'
    Then warning HTM-055 is reported
    And the message contains 'embed'
    And no other errors or warnings are reported

  Scenario: Report `rp` as a discouraged construct
    When checking document 'discouraged-rp-warning.xhtml'
    Then warning HTM-055 is reported 2 times
    And the message contains 'rp'
    And no other errors or warnings are reported