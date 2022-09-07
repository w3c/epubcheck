Feature: EPUB 3 — Vocabularies — Media overlays vocabulary


  Checks conformance to the "Media overlays vocabulary" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#app-overlays-vocab


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings

  ## D.8 Media Overlays Metadata Vocabulary

  ### D.8.2 duration
  
  @spec @xref:sec-duration
  Scenario: the 'media:duration' property can be expressed as a full clock value
    When checking file 'mediaoverlays-duration-fullclock-valid.opf'
    Then no errors or warnings are reported
  
  @spec @xref:sec-duration
  Scenario: the 'media:duration' property can be expressed as a timecount value
    When checking file 'mediaoverlays-duration-timecount-valid.opf'
    Then no errors or warnings are reported
  
  @spec @xref:sec-duration
  Scenario: Report 'media:duration' properties with non-clock values
    When checking file 'mediaoverlays-duration-clock-values-error.opf'
    Then the following errors are reported
      | RSC-005 | must be a valid SMIL3 clock value |
      | RSC-005 | must be a valid SMIL3 clock value |
      | RSC-005 | must be a valid SMIL3 clock value |
    And no other errors or warnings are reported