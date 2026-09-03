Feature: EPUB 3 — Content Documents — Fixed-layout documents


  Checks conformance to the "Fixed-layout documents" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#sec-fxl-docs


  Background: 
    Given EPUB test files located at '/epub3/07-content-documents/files/'
    And EPUBCheck with default settings


  ##  7.3 Fixed-layout documents

  Scenario: Verify a fixed-layout SVG
    When checking EPUB 'content-fxl-svg-valid'
    Then no errors or warnings are reported

  ### 7.3.2 Fixed-layout document dimensions

  #### Expressing the ICB in XHTML

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Verify a fixed-layout XHTML document with a valid viewport
    When checking EPUB 'content-fxl-xhtml-viewport-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Verify a fixed-layout XHTML document with non-integer viewport dimensions
    When checking EPUB 'content-fxl-xhtml-viewport-float-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Verify a fixed-layout XHTML document with a valid viewport with whitespace
    When checking EPUB 'content-fxl-xhtml-viewport-whitespace-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Verify a fixed-layout XHTML document with a valid viewport using keywords value
    When checking EPUB 'content-fxl-xhtml-viewport-keywords-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with no viewport
    When checking EPUB 'content-fxl-xhtml-viewport-missing-error'
    Then error HTM-046 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with a syntactically invalid viewport
    When checking EPUB 'content-fxl-xhtml-viewport-syntax-invalid-error'
    Then error HTM-047 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with a viewport with no height
    When checking EPUB 'content-fxl-xhtml-viewport-height-missing-error'
    Then error HTM-056 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with a viewport with no width
    When checking EPUB 'content-fxl-xhtml-viewport-width-missing-error'
    Then error HTM-056 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with a viewport with an empty height value
    When checking EPUB 'content-fxl-xhtml-viewport-height-empty-error'
    Then error HTM-057 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with a viewport using units
    When checking EPUB 'content-fxl-xhtml-viewport-units-invalid-error'
    Then error HTM-057 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout XHTML document with duplicate width/height in a single viewport meta tag
    When checking EPUB 'content-fxl-xhtml-viewport-duplicate-width-height-error'
    Then error HTM-059 is reported 2 times (1 for width, 1 for height)
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Do not check more than one viewport meta tag in a fixed-layout document (but report as usage) 
    Given the reporting level is set to usage 
    When checking EPUB 'content-fxl-xhtml-viewport-multiple-usage-valid'
    Then usage HTM-060a is reported 2 times
    But no other usages are reported
    And no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Do not check viewport metadata in reflowable content documents (but report as usage)
    Given the reporting level is set to usage
    When checking EPUB 'content-reflow-xhtml-viewport-height-missing-valid'
    Then usage HTM-060b is reported
    But no other usages are reported
    And no errors or warnings are reported

  ### Expressing the ICB in SVG

  Scenario: Verify that the initial containing block rules are not checked on embedded svg elements
    When checking EPUB 'content-fxl-svg-no-viewbox-on-inner-svg-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout SVG without a `viewbox` declaration
    When checking EPUB 'content-fxl-svg-no-viewbox-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in units)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-units-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fxl-content-dimensions
  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in percent)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-percent-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported


