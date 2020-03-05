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
  ###  Common Elements  													###
  #############################################################################
  #

  Scenario: Verify various general HTML markup patterns
    When checking document 'core-common-elements-valid.xhtml'
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

  Scenario: Verify that `epub:switch` is allowed with a deprecation warning
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

  Scenario: Verify that `epub:trigger` is allowed with deprecation warning
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
  ###  Ruby	     														###
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
  ###  URLs 																###
  #############################################################################
  #

  Scenario: Report non-conforming URL schemes and domains
    When checking document 'core-url-issue-708-error.xhtml'
    Then error RSC_020 is reported
    And warning HTM_025 is reported
    And warning RSC_023 is reported 2 times
    And no other errors or warnings are reported


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
  
