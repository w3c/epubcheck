Feature: EPUB 3 — Vocabularies — Package rendering vocabulary


  Checks conformance to the "Package rendering vocabulary" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#app-rendering-vocab


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings
    
  # Note: 
  # The properties themselves are tested in the "layout.feature" file,
  # since all the properties are defined in that section and not in the
  # vocabulary appendix.

  ## D.4 Package rendering vocabulary

  ### D.4.1 Layout control

  ### D.4.1.1 rendition:layout

  @spec @xref:layout-def
  Scenario: the 'rendition:layout' property can be used to define the global layout preference
    When checking file 'rendition-layout-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:layout-def
  Scenario: a 'rendition:layout' property with no value is reported
    See also issue #727
    When checking file 'rendition-layout-global-empty-error.opf'
    Then the following errors are reported (one for the empty element, one for the consequently unexpected value)
      | RSC-005 | character content of element "meta" invalid          |
      | RSC-005 | The value of the "rendition:layout" property must be |
    And no other errors or warnings are reported

  @spec @xref:layout-def
  Scenario: a 'rendition:layout' property with an unknown value is reported
    When checking file 'rendition-layout-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:layout" property must be'
    And no other errors or warnings are reported

  @spec @xref:layout-def
  Scenario: the 'rendition:layout' property cannot be declared more than once
    When checking file 'rendition-layout-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:layout" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:layout-def
  Scenario: the 'rendition:layout' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-layout-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported


  ### D.4.1.2 Layout overrides

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


  ### D.4.2 Synthetic spread placement

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

  #### Synthetic spread overrides

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


  #### Spread placement
  
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


  ## D.5 Custom rendering properties

  @spec @xref:sec-rendering-custom-properties
  Scenario: Report a custom rendition property using the 'rendition' prefix
    When checking file 'rendition-property-unknown-error.opf'
    Then error OPF-027 is reported
    And no other errors or warnings are reported
  
