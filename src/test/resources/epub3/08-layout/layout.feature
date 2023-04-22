Feature: EPUB 3 — Layout Rendering Control


  Checks conformance to the "Layout rendering control" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-rendering-control


  Background: 
    Given EPUB test files located at '/epub3/08-layout/files/'
    And EPUBCheck with default settings

  ##  8.2 Fixed layouts

  Scenario: Verify a fixed-layout SVG
    When checking EPUB 'content-fxl-svg-valid'
    Then no errors or warnings are reported

  ###  8.2.2 Fixed-layout package settings

  #### 8.2.2.1 Layout

  @spec @xref:layout
  Scenario: the 'rendition:layout' property can be used to define the global layout preference
    When checking file 'rendition-layout-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:layout
  Scenario: a 'rendition:layout' property with no value is reported
    See also issue #727
    When checking file 'rendition-layout-global-empty-error.opf'
    Then the following errors are reported (one for the empty element, one for the consequently unexpected value)
      | RSC-005 | character content of element "meta" invalid          |
      | RSC-005 | The value of the "rendition:layout" property must be |
    And no other errors or warnings are reported

  @spec @xref:layout
  Scenario: a 'rendition:layout' property with an unknown value is reported
    When checking file 'rendition-layout-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:layout" property must be'
    And no other errors or warnings are reported

  @spec @xref:layout
  Scenario: the 'rendition:layout' property cannot be declared more than once
    When checking file 'rendition-layout-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:layout" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:layout
  Scenario: the 'rendition:layout' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-layout-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  ##### 8.2.2.1.1 Layout overrides

  @spec @xref:layout-overrides 
  Scenario: the 'rendition:layout' property can be used as a spine override
    When checking file 'rendition-layout-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:layout-overrides 
  Scenario: the 'rendition:layout' spine overrides values are mutually exclusive
    When checking file 'rendition-layout-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported


  #### 8.2.2.2 Orientation

  @spec @xref:orientation
  Scenario: the 'rendition:orientation' property can be used to define the global orientation preference
    When checking file 'rendition-orientation-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:orientation
  Scenario: a 'rendition:orientation' property with an unknown value is reported
    When checking file 'rendition-orientation-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:orientation" property must be'
    And no other errors or warnings are reported

  @spec @xref:orientation
  Scenario: the 'rendition:orientation' property cannot be declared more than once
    When checking file 'rendition-orientation-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:orientation" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:orientation
  Scenario: the 'rendition:orientation' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-orientation-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  ##### 8.2.2.2.1 Orientation overrides
  
  @spec @xref:orientation-overrides
  Scenario: the 'rendition:orientation' property can be used as a spine override
    When checking file 'rendition-orientation-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:orientation-overrides
  Scenario: the 'rendition:orientation' spine overrides values are mutually exclusive
    When checking file 'rendition-orientation-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported


  #### 8.2.2.3 Synthetic spreads

  @spec @xref:spread
  Scenario: the 'rendition:spread' property can be used to define the global spread preference
    When checking file 'rendition-spread-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:spread
  Scenario: a 'rendition:spread' property with an unknown value is reported
    When checking file 'rendition-spread-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:spread" property must be'
    And no other errors or warnings are reported

  @spec @xref:spread
  Scenario: the 'rendition:spread' property cannot be declared more than once
    When checking file 'rendition-spread-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:spread" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:spread
  Scenario: the 'rendition:spread' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-spread-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  @spec @xref:spread
  Scenario: the 'rendition:spread' 'portrait' value is deprecated as a global value
    When checking file 'rendition-spread-portrait-global-deprecated-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported

  #### 8.2.2.3.1 Synthetic spread overrides
  
  @spec @xref:spread-overrides
  Scenario: the 'rendition:spread' property can be used as a spine override
    When checking file 'rendition-spread-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:spread-overrides
  Scenario: the 'rendition:spread' spine overrides values are mutually exclusive
    When checking file 'rendition-spread-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported


  #### 8.2.2.4 Spread placement
  
  @spec @xref:page-spread
  Scenario: the 'rendition:page-spread-*' properties can be used without the prefix
    When checking file 'rendition-page-spread-itemref-unprefixed-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:page-spread
  Scenario: the 'rendition:page-spread-*' properties values are mutually exclusive
    When checking file 'rendition-page-spread-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  @spec @xref:spread
  Scenario: the 'rendition:spread-portrait' value is deprecated as a spine override
    When checking file 'rendition-spread-portrait-itemref-deprecated-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported


  #### 8.2.2.5 Viewport dimensions (deprecated)

	@spec @xref:viewport
  Scenario: the 'rendition:viewport' property is deprecated
    When checking file 'rendition-viewport-deprecated-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported

  Scenario: the 'rendition:viewport' property syntax errors are reported
    When checking file 'rendition-viewport-syntax-error.opf'
    Then warning OPF-086 is reported (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The value of the "rendition:viewport" property must be of the form'
    And no other errors or warnings are reported

  Scenario: the 'rendition:viewport' property cannot be declared more than once
    When checking file 'rendition-viewport-duplicate-error.opf'
    Then warning OPF-086 is reported 2 times (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The "rendition:viewport" property must not occur more than one time as a global value'
    And no other errors or warnings are reported


  #### 8.2.2.6 Content document dimensions

  ##### Expressing the ICB in XHTML

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
  Scenario: Report a single fixed-layout XHTML document with an invalid viewport in a reflowable publication
    When checking EPUB 'content-fxl-item-xhtml-viewport-invalid-error'
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

  ##### Expressing the ICB in SVG

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


  ## 8.3 Reflowable layouts
  
  ### 8.3.1 The rendition:flow property
  
  @spec @xref:flow
  Scenario: the 'rendition:flow' property can be used to define the global flow preference
    When checking file 'rendition-flow-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:flow
  Scenario: a 'rendition:flow' property with an unknown value is reported
    When checking file 'rendition-flow-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:flow" property must be'
    And no other errors or warnings are reported

  @spec @xref:flow
  Scenario: the 'rendition:flow' property cannot be declared more than once
    When checking file 'rendition-flow-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:flow" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:flow
  Scenario: the 'rendition:flow' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-flow-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  ### 8.3.1.1 Spine overrides
  
  @spec @xref:flow-overrides
  Scenario: the 'rendition:flow' property can be used as a spine override
    When checking file 'rendition-flow-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:flow-overrides
  Scenario: the 'rendition:flow' spine overrides values are mutually exclusive
    When checking file 'rendition-flow-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported
