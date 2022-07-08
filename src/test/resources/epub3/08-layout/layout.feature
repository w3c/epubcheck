Feature: EPUB 3 ▸ Layout Rendering Control


  Checks conformance to the "Layout rendering control" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-rendering-control


  Background: 
    Given EPUB test files located at '/epub3/06-content-document/files/'
    And EPUBCheck with default settings

  ##  8.2 Fixed layouts

  Scenario: Verify a fixed-layout SVG
    When checking EPUB 'content-fxl-svg-valid'
    Then no errors or warnings are reported

  ###  8.2.2 Fixed-layout package settings


  ####  8.2.2.6 Content document dimensions

  Scenario: Report a fixed-layout XHTML document with no viewport
    When checking EPUB 'content-fxl-xhtml-viewport-missing-error'
    Then error HTM-046 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout XHTML document with an invalid viewport
    When checking EPUB 'content-fxl-xhtml-viewport-invalid-error'
    Then error HTM-047 is reported
    And no other errors or warnings are reported
    
  Scenario: Verify that the initial containing block rules are not checked on embedded svg elements
    When checking EPUB 'content-fxl-svg-no-viewbox-on-inner-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration
    When checking EPUB 'content-fxl-svg-no-viewbox-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in units)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-units-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in percent)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-percent-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported