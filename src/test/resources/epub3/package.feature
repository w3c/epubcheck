Feature: EPUB 3 Packages
  
  Checks conformance to specification rules related to EPUB Packages:
  https://www.w3.org/publishing/epub32/epub-packages.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note:
  - Tests that do not require a full publication but a single Package Document
    are defined in the `package-document.feature` feature file.   
  - Tests related to EPUB Navigation Documents are defined in the `navigation.feature`
    and `navigation-document.feature` feature files.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ###  3.4.2 Shared Attributes

  ####  xml:lang

  Scenario: Verify that three-character language codes are allowed (isue 615)
    When checking EPUB 'lang-three-char-code-valid'
    Then no errors or warnings are reported


  ####  3.4.4.2 The item Element

  Scenario: Report duplicate declarations of a resource in the package document manifest
    When checking EPUB 'manifest-duplicate-resource-error'
    Then error OPF-074 is reported
    And no other errors or warnings are reported

  Scenario: Report a resource declared in the package document but missing from the container
    When checking EPUB 'manifest-missing-item-error'
    Then error RSC-001 is reported
    And no other errors or warnings are reported

  Scenario: Report fonts declared in the package document but missing from the container
    When checking EPUB 'manifest-missing-fonts-error'
    Then error RSC_001 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report a CSS file declared with an invalid media type and no fallback
    When checking EPUB 'manifest-css-wrong-media-type-error'
    Then error CSS-010 is reported
    And no errors or warnings are reported


  ####  3.4.4.3 Manifest Fallbacks

  Scenario: Report a circular manifest fallback chain
    When checking EPUB 'manifest-fallback-circular-error'
    Then error OPF_045 is reported 4 times
    And error MED-003 is reported
    And no other errors or warnings are reported

  Scenario: Report a manifest fallback that references a non-existent resource
    When checking EPUB 'manifest-fallback-non-resolving-error'
    Then error RSC-005 is reported
    And the message contains 'manifest item element fallback attribute must resolve to another manifest item'
    And error MED-003 is reported
    And no other errors or warnings are reported


  ### 4.3.4  Fixed-Layout Properties

  Scenario: testFXL_WithSVG {
    When checking EPUB 'valid/fxl-svg/'
    Then no errors or warnings are reported

  Scenario: testFXL_WithSVG_InnerSVG {
    // tests that the ICB-defining rules are only checked on the outer svg element
    When checking EPUB 'valid/fxl-svg-no-viewbox-on-inner-svg'
    Then no errors or warnings are reported

  Scenario: testFXL_WithSVG_NoViewbox {
    When checking EPUB 'invalid/fxl-svg-no-viewbox-no-heightwidth'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: testFXL_WithSVG_NoViewbox_WidthHeight{
    When checking EPUB 'invalid/fxl-svg-no-viewbox'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: testFXL_WithSVG_NoViewbox_WidthHeightInPercent{
    When checking EPUB 'invalid/fxl-svg-no-viewbox-widthheight-in-percent'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: testFXL_WithSVGNotInSpine {
    // test that FXL requirements do not apply to non-top-level SVG
    When checking EPUB 'valid/fxl-svg-notinspine/'
    Then no errors or warnings are reported


  #  E. Manifest Properties

  ##  E.2 Manifest item Properies
  
  ###  E.2.2 mathml

  Scenario: Verify content documents are identified as containing mathml
    When checking EPUB 'package-mathml-valid'
    Then no errors or warnings are reported


  ###  E.2.4 remote-resources

  Scenario: Report a reference a remote resource when the `remote-resources` property is not set in the manifest
    When checking EPUB 'manifest-prop-remote-resource-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report the incorrect use of the `remote-resources` property for a resource defined in an `object` `param` element (issue 249)
    When checking EPUB 'manifest-prop-remote-resource-object-param-warning'
    Then warning OPF-018 is reported
    And no other errors or warnings are reported


  ###  E.2.5 scripted

  Scenario: Report a scripted document without the `scripted` property declared in the package document
    When checking EPUB 'manifest-prop-scripted-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Verify that script data blocks do not require the `scripted` property to be defined in the manifest
    When checking EPUB 'script-data-block-valid'
    Then no errors or warnings are reported


  ###  E.2.6 svg

  Scenario: Report references to embedded SVG when the `svg` property is not set in the manifest 
    When checking EPUB 'manifest-prop-svg-undeclared-error'
    Then error OPF_014 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report reference to an embedded SVG when the `svg` property is not set in the manifest (one reference is set properly)
    When checking EPUB 'manifest-prop-svg-undeclared-partial-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported


  ###  E.2.7 switch

  Scenario: Report a content document without the `switch` property declared in the manifest
    When checking EPUB 'manifest-prop-switch-not-declared-error'
    Then error OPF-014 is reported
    And warning RSC-017 is reported
    And the message contains "The 'epub:switch' element is deprecated"
    And no other errors or warnings are reported





  Scenario: Report an unknown manifest item property
    When checking EPUB 'manifest-prop-unknown-error'
    Then error OPF_027 is reported
    And no other errors or warnings are reported



##### ???

#  /**
#   * Also tests locale-independent character case transformations (such as
#   * lower-casing). Specifically, in issue 711, when the default locale is set
#   * to Turkish, lower-casing resulted in incorrect vocabulary strings (for
#   * "PAGE_LIST" enum constant name relevant to the original issue report, as
#   * well as for numerous other strings). Therefore, a Turkish locale is set as
#   * the default at the beginning of the test (the original locale is restored
#   * at the end of the test).
#   */
#  Scenario: testPageList
#    Locale previousLocale = Locale.getDefault;
#    try
#      // E.g., tests that I is not lower-cased to \u0131 based on locale's collation rules:
#      Locale.setDefault(new Locale("tr", "TR")
#      When checking EPUB 'page-list-valid'
#    } finally // restore the original locale
#      Locale.setDefault(previousLocale

