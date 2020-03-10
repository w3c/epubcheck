Feature: EPUB 3 XHTML Content Document
  
  Checks conformance to specification rules defined for EPUB XHTML Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html#sec-xhtml
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Note: Tests related to EPUB XHTML Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And test files located at '/epub3/files/content-document-xhtml/'

  Scenario: Minimal Content Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  #############################################################################
  ###  ARIA			 													###
  #############################################################################
  #
  
  Scenario: Verify ARIA role allowed on an `a` element with no `href` attribute
    When checking document 'core-aria-role-a-nohref-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report non-existent ARIA `describedat` attribute
    When checking document 'core-aria-describedAt-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "aria-describedat" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Verify the `doc-endnote` role is allowed on list items
    When checking document 'core-aria-role-doc-endnote-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify ARIA attributes allowed on SVG elements
    When checking document 'core-svg-aria-valid.xhtml'
    Then no errors or warnings are reported

  
  #############################################################################
  ###  Attributes (General) 												###
  #############################################################################
  #

  Scenario: Verify attributes in custom namespaces are ignored
    When checking document 'core-attrs-custom-ns-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify the value of HTML boolean attributes and enumerated attributes are parsed in a case-insensitive manner
    When checking document 'core-attrs-case-insensitive-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Canvas   															###
  #############################################################################
  #

  Scenario: Verify general uses of the canvas element
    When checking document 'core-canvas-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report canvas fallback
    When checking document 'core-canvas-fallback-error.xhtml'
    Then error MED_002 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Elements (General)   												###
  #############################################################################
  #

  Scenario: Verify various general HTML markup patterns
    When checking document 'core-common-elements-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Custom Elements  													###
  #############################################################################
  #

  Scenario: Verify custom elements are not rejected
    When checking document 'core-custom-elements-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Data Attributes  													###
  #############################################################################
  #

  Scenario: Verify `data-*` attributes are allowed 
    When checking document 'core-data-attr-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Document Title   													###
  #############################################################################
  #

  Scenario: Report missing `title` element
    When checking document 'core-title-missing-error.xhtml'
    Then warning RSC_017 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  DOCTYPE		  													###
  #############################################################################
  #

  Scenario: Verify versionless HTML `doctype`
    When checking document 'core-doctype-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `doctype` with legacy string
    When checking document 'core-doctype-legacy-compat-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `doctype` with obsolete public identifier
    When checking document 'core-doctype-obsolete-error.xhtml'
    Then error HTM_004 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Embedded Content Elements											###
  #############################################################################
  #

  Scenario: Verify general uses of the embedded content elements
    When checking document 'core-embed-valid.xhtml'
    Then no errors or warnings are reported

  #############################################################################
  ###  Edit elements														###
  #############################################################################
  #

  Scenario: Verify general uses of the editing elements `del` and `ins`
    When checking document 'core-edits-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  epub:switch  														###
  #############################################################################
  #

  Scenario: Report `epub:switch` is deprecated
    When checking document 'core-switch-deprecated-warning.xhtml'
    Then warning RSC_017 is reported
    And no other errors or warnings are reported
  
  Scenario: Report `epub:switch` with invalid mathml
    When checking document 'core-switch-mathml-error.xhtml'
    Then error RSC_005 is reported
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report an `epub:switch` with a `default` before any `case` elements 
    When checking document 'core-switch-default-before-case-error.xhtml'
    # one error is epub:default too soon, the other for epub:case too late
    Then error RSC_005 is reported 2 times
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report an `epub:switch` with multiple `default` elements
    When checking document 'core-switch-multipe-default-error.xhtml'
    Then error RSC_005 is reported
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report `epub:switch` without any `case` elements
    When checking document 'core-switch-no-case-error.xhtml'
    Then error RSC_005 is reported
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report `epub:switch` element without a `default`
    When checking document 'core-switch-no-default-error.xhtml'
    Then error RSC_005 is reported
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report `epub:case` without a `required-namespace` attribute
    When checking document 'core-switch-no-case-namespace-error.xhtml'
    Then error RSC_005 is reported
    # also raises a warning that epub:switch is deprecated
    And warning RSC_017 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  epub:trigger 														###
  #############################################################################
  #

  Scenario: Report `epub:trigger` is deprecated
    When checking document 'core-trigger-deprecated-warning.xhtml'
    Then warning RSC_017 is reported
    And no other errors or warnings are reported

  Scenario: Report `epub:trigger` that references non-existent IDs
    When checking document 'xhtml/invalid/trigger-badrefs.xhtml'
    # errors for bad ref and ev:observer references
    Then error RSC_005 is reported 2 times
    # also raises two warnings for the deprecated switch elements 
    And warning RSC_017 is reported 2 times
    And no other errors or warnings are reported


  #############################################################################
  ###  epub:type															###
  #############################################################################
  #

  Scenario: Verify `epub:type` attribute with valid semantic
    When checking document 'core-epubtype-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify `epub:type` attribute in document header
    When checking document 'core-epubtype-in-head-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `epub:type` attribute with reserved vocabulary
    When checking document 'core-epubtype-reserved-vocab-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `epub:type` attribute with author-declared vocabulary
    When checking document 'core-epubtype-declared-vocab-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `epub:type` attribute with unknown semantic
    When checking document 'core-epubtype-unknown-usage.xhtml'
    Then usage OPF_088 is reported
    And no other errors or warnings are reported

  Scenario: Verify `epub:type` attribute with deprecated semantic
    When checking document 'core-epubtype-deprecated-usage.xhtml'
    Then usage OPF_086b is reported 10 times;
    And no other errors or warnings are reported

  Scenario: Verify `epub:type` attribute that does not follow usage suggestions
    When checking document 'core-epubtype-disallowed-usage.xhtml'
    Then usage OPF_087 is reported 7 times;
    And no other errors or warnings are reported

  Scenario: Report `epub:type` attribute with a semantic from an undeclared vocabulary
    When checking document 'core-epubtype-prefix-undeclared-error.xhtml'
    Then error OPF_028 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Form elements														###
  #############################################################################
  #

  Scenario: Verify general uses of form elements
    When checking document 'core-forms-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Global Attributes													###
  #############################################################################
  #

  Scenario: Verify general uses of global attributes
    When checking document 'core-global-attrs-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  IDs			  													###
  #############################################################################
  #

  Scenario: Report duplicate `id` attribute values
    When checking document 'core-id-duplicate-error.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Verify `id` attribute with non-alphanumeric in its value
    When checking document 'core-id-not-ncname-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Links																###
  #############################################################################
  #

  Scenario: Verify a `link` element with a known alt style tag
    When checking document 'core-alt-style-tags-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a `link` element with an unknown alt style tag
    When checking document 'core-alt-style-tags-error.xhtml'
    Then error OPF_027 is reported
    And error CSS_005 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Lists																###
  #############################################################################
  #

  Scenario: Verify an `li` with a `value` attribute (issue 248) 
    When checking document 'core-li-with-value-attr-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Main 																###
  #############################################################################
  #

  Scenario: Verify `main` element is allowed (issue 340)
    When checking document 'core-main-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Mathml   															###
  #############################################################################
  #

  Scenario: Verify MathML markup with prefixed elements
    When checking document 'core-mathml-prefixed-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with unprefixed elements
    When checking document 'core-mathml-unprefixed-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup without alternative text
    When checking document 'core-mathml-noalt-usage.xhtml'
    Then usage ACC_009 is reported
    And no other errors or warnings are reported

  Scenario: Report MathML markup with only content MathML
    When checking document 'core-mathml-contentmathml-error.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Verify MathML with tex annotation
    When checking document 'core-mathml-anno-tex-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify MathML with content MathML annotation
    When checking document 'core-mathml-anno-contentmathml-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify MathML with presentation MathML annotation
    When checking document 'core-mathml-anno-presmathml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML with descendant MathML markup in an XHTML annotation
    When checking document 'core-mathml-anno-xhtml-with-mathml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report Content MathML annotation without a `name` attribute
    When checking document 'core-mathml-anno-mathml-noname-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Report MathML annotation with an invalid `name` attribute
    When checking document 'core-mathml-anno-name-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Report MathML annotation with an invalid `encoding` attribute
    When checking document 'core-mathml-anno-encoding-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Verify MathML markup with an XHTML annotation
    When checking document 'core-mathml-anno-xhtml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with XHTML annotation missing name attribute
    When checking document 'core-mathml-anno-xhtml-noname-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with `annotation-xml` `name` attribute set to `contentequiv`
    When checking document 'core-mathml-anno-xhtml-contentequiv-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify MathML markup with an HTML encoded annotation
    When checking document 'core-mathml-anno-xhtml-html-encoding-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify MathML markup with an SVG annotation
    When checking document 'core-mathml-anno-svg-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a MathML annotation with the XHTML encoding reversed (application/xml+xhtml)
    When checking document 'core-mathml-anno-xhtml-encoding-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Meta 																###
  #############################################################################
  #

  Scenario: Verify `http-equiv` declaration
    When checking document 'core-http-equiv-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify case-insensitive `http-equiv` declaration
    When checking document 'core-http-equiv-case-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `http-equiv` declaration of non-utf8 charset
    When checking document 'core-http-equiv-non-utf8-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Report both `http-equiv` and `charset` are declared
    When checking document 'core-http-equiv-and-charset-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Microdata															###
  #############################################################################
  #

  Scenario: Verify that microdata attributes are allowed on elements
    When checking document 'core-microdata-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report use of microdata attributes on elements where they are not allowed
    When checking document 'core-microdata-invalid.xhtml'
    Then error RSC_005 is reported 3 times
    And no other errors or warnings are reported


  #############################################################################
  ###  Objects  															###
  #############################################################################
  #

  Scenario: Verify that `typemustmatch` attribute is allowed on `object` (issue 282)
    When checking document 'core-object-typemustmatch-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  RDFa 																###
  #############################################################################
  #

  Scenario: Verify RDFa attributes are allowed on HTML elements
    When checking document 'core-rdfa-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Ruby 																###
  #############################################################################
  #

  Scenario: Verify general uses of ruby markup
    When checking document 'core-ruby-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Schematron Assertions												###
  #############################################################################
  #

  Scenario: Verify no schematron assertions
    When checking document 'core-schematron-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report schematron assertions without line or column numbers
    When checking document 'core-schematron-error.xhtml'
    Then error MED_002 is reported 1 times
    And error RSC_005 is reported 43 times

    #    // unclear how to port this function
    #    new ExtraReportTest
    #            @Override
    #      Scenario: test(ValidationReport testReport)
    #                for (ItemReport error : testReport.errorList)
    #                    assertTrue("Error '" + error.message + "' has no line number.", error.line != -1);
    #          assertTrue("Error '" + error.message + "' has no column number.", error.column != -1);
    #                      });


  #############################################################################
  ###  Sectioning Elements  												###
  #############################################################################
  #

  Scenario: Verify general use of HTML sectioning elements
    When checking document 'core-sectioning-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  SSML 																###
  #############################################################################
  #

  Scenario: Verify general use of SSML attributes
    When checking document 'core-ssml-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report SSML `ph` attribute without a value
    When checking document 'core-ssml-empty-ph-warning.xhtml'
    Then warning HTM_007 is reported 2 times
    And no other errors or warnings are reported


  #############################################################################
  ###  Style																###
  #############################################################################
  #

  Scenario: Verify use of `style` element in the header
    When checking document 'core-style-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `style` element without a `type` declaration
    When checking document 'core-style-no-type-error.xhtml'
    Then error CSS_008 is reported
    And no other errors or warnings are reported
  
  Scenario: Report `style` element in the body
    When checking document 'core-style-in-body-error.xhtml'
    # one error for the style element, one for the scoped attribute
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Verify general use of the `style` attribute
    When checking document 'core-style-attr-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report use of `style` attribute with invalid css syntax
    When checking document 'core-style-attr-syntax-error.xhtml'
    Then error CSS_008 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  SVG  																###
  #############################################################################
  #

  Scenario: Verify inclusion of SVG markup
    When checking document 'core-svg-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify that `epub:type` attribute can be used on SVG
    When checking document 'core-svg-with-epubtype-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report SVG with invalid content model
    When checking document 'core-svg-error.xhtml'
    Then error RSC_005 is reported 
    And no other errors or warnings are reported

  Scenario: Report SVG with incorrect `requiredExtensions` attribute value
    When checking document 'core-svg-requiredExtensions-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Verify `xlink:href` allowed on SVG elements
    When checking document 'svg-links.xhtml'
    Then no errors or warnings are reported

  Scenario: Report an SVG link without a recommended title
    When checking document 'xhtml/invalid/svg-link-no-title-warning.xhtml'
    Then warning ACC_011 is reported
    And no other errors or warnings are reported

  Scenario: Verify unprefixed HTML elements allowed inside prefixed `foreignObject`
    When checking document 'core-svg-foreignobject-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `foreignObject` with disallowed body element
    When checking document 'core-svg-foreignobject-with-body-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Report `foreignObject` without flow content
    When checking document 'core-svg-foreignobject-no-flow-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Tables   															###
  #############################################################################
  #

  Scenario: Verify general tables markup
    When checking document 'core-tables-valid.xhtml'
    Then no errors or warnings are reported
  
  Scenario: Verify `border` attribute allowed on tables 
    When checking document 'core-table-border-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `table` with invalid `border` attribute value 
    When checking document 'core-table-border-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported


  #############################################################################
  ###  Text-level Elements  												###
  #############################################################################
  #

  Scenario: Verify general HTML text-level elements
    When checking document 'core-text-valid.xhtml'
    Then no errors or warnings are reported

  
  #############################################################################
  ###  Time 																###
  #############################################################################
  #

  Scenario: Verify `datetime` value without a day (issue 341)
    When checking document 'core-time-datetime-no-day-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  URLs 																###
  #############################################################################
  #

  Scenario: Verify valid URLs
    When checking document 'core-url-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report non-conforming URL schemes and domains (issues 288 and 708)
    When checking document 'core-url-error.xhtml'
    Then error RSC_020 is reported
    And warning HTM_025 is reported
    And warning RSC_023 is reported 2 times
    And error RSC_020 is reported
    And no other errors or warnings are reported

  Scenario: Report unregistered URL scheme
    When checking document 'core-url-unregistered-scheme-warning.xhtml'
    And warning HTM_025 is reported
    And no other errors or warnings are reported

  Scenario: Verify irc scheme in URL (issue 296)
    When checking document 'core-url-irc-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  Video																###
  #############################################################################
  #

  Scenario: Verify general use of the `video` element
    When checking document 'core-video-valid.xhtml'
    Then no errors or warnings are reported


  #############################################################################
  ###  XML Support	  													###
  #############################################################################
  #

  Scenario: Report an XML 1.1 version declaration
    When checking document 'core-xml11-error.xhtml'
    Then error HTM_001 is reported
    And no other errors or warnings are reported
  
