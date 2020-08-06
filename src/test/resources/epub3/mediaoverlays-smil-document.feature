Feature: EPUB 3 ▸ Media Overlays ▸ SMIL Document Checks


  Checks conformance to the EPUB Media Overlays 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-mediaoverlays.html

  In the scenarios below, checks are run against single SMIL Documents.
  EPUBCheck is launched in 'mo' mode.


  Background: 
    Given test files located at '/epub3/files/mediaoverlays-document/'
    And EPUBCheck configured to check a Media Overlays Document


  ## 2.4 Media Overlay Document Definition
  
  Scenario: Verify a minimal Media Overlay document
    When checking document 'minimal.smil'
    Then no other errors or warnings are reported

  ### 2.4.2 The `head` element

  Scenario: Report a `meta` element used in the `head` container 
    When checking document 'metadata-syntax-invalid-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "meta" not allowed here'
    And no other errors or warnings are reported

  ###  2.4.3 The `metadata` element

  Scenario: Allow a `metadata` element with custom metadata properties
    When checking document 'metadata-properties-valid.smil'
    Then no errors or warnings are reported

  ### 2.4.5 The `seq` element

  Scenario: Report media clips used as direct children of a `seq` element
    When checking document 'seq-with-direct-media-children-error.smil'
    Then the following errors are reported
      | RSC-005 | element "text" not allowed here |
      | RSC-005 | element "audio" not allowed here |
    And no other errors or warnings are reported

  ### 2.4.6 The `par` element

  Scenario: Report a `par` element with more than one `text` child
    When checking document 'par-with-multiple-text-elements-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "text" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report a `par` element with a `seq` child
    When checking document 'par-with-seq-child-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "seq" not allowed here'
    And no other errors or warnings are reported

  ### Section 2.4.8 The `audio` element

  Scenario: Allow clock values with the full clock syntax (`hh:mm:ss.milli`)
    When checking document 'clock-value-full-syntax-valid.smil'
    Then no errors or warnings are reported

  Scenario: Allow clock values with the partial clock syntax (`mm:ss.milli`)
    When checking document 'clock-value-partial-syntax-valid.smil'
    Then no errors or warnings are reported

  Scenario: Allow clock values with the timecount syntax (`XXmin`)
    When checking document 'clock-value-timecount-syntax-valid.smil'
    Then no errors or warnings are reported

  Scenario: Report invalid clock values
    When checking document 'clock-value-syntax-invalid-error.smil'
    Then error RSC-005 is reported 6 times
    And no other errors or warnings are reported

  Scenario: Report if the `clipBegin` value is after the `clipEnd` value
    When checking document 'clipBegin-after-clipEnd-error.smil'
    Then error MED-008 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report if the `clipEnd` value equals the `clipBegin` value
    When checking document 'clip-times-equal-error.smil'
    Then error MED-009 is reported 4 times
    And no other errors or warnings are reported

 ## 3.3 Semantic Inflection

  Scenario: Allow epub:type properties in the default vocabulary
    When checking document 'epubtype-valid.smil'
    Then no errors or warnings are reported
    
  Scenario: Allow custom epub:type properties with a declared prefix
    When checking document 'epubtype-prefix-declared-valid.smil'
    Then no errors or warnings are reported

  Scenario: Report an epub:type property with an undeclared prefix
    When checking document 'epubtype-prefix-undeclared-error.smil'
    Then error OPF-028 is reported
    And the message contains 'Undeclared prefix: "my"'
    And no other errors or warnings are reported

  Scenario: Allow unknown epub:type properties in the default vocabulary
    Given the reporting level set to usage
    When checking document 'epubtype-unknown-usage.smil'
    Then usage OPF-088 is reported
    And no other errors or warnings are reported
