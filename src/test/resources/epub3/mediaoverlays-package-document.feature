Feature: EPUB 3 ▸ Media Overlays ▸ Package Document Checks


  Checks conformance to the EPUB Media Overlays 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-mediaoverlays.html

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub3/files/package-document/'
    And EPUBCheck configured to check a Package Document


  # 3.5 Packaging
  
  ## 3.5.1 Including Media Overlays
  
  Scenario: a Media Overlay must have the media type 'application/smil+xml'
    When checking file 'mediaoverlays-type-invalid-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must be of the "application/smil+xml" type'
    And no other errors or warnings are reported

  Scenario: Report use of the media-overlay attribute on non-EPUB Content Documents
    When checking file 'mediaoverlays-non-contentdoc-error.opf'
    Then error RSC-005 is reported
    And the message contains 'media-overlay attribute is only allowed'
    And no other errors or warnings are reported

  ## 3.5.2 Package Metadata
  
  Scenario: the entire publication duration must be defined
    When checking file 'mediaoverlays-duration-global-not-defined-error.opf'
    Then error RSC-005 is reported
    And the message contains "global media:duration meta element not set"
    And no other errors or warnings are reported
    
  Scenario: the duration of each Media Overlay must be defined
    When checking file 'mediaoverlays-duration-single-not-defined-error.opf'
    Then error RSC-005 is reported
    And the message contains "item media:duration meta element not set"
    And no other errors or warnings are reported

  Scenario: the total duration should be the sum of all Media Overlay durations
    When checking file 'mediaoverlays-duration-total-not-sum-warning.opf'
    Then warning MED-016 is reported
    And no other errors or warnings are reported
  

  # C. Media Overlays Metadata Vocabulary
  # see https://www.w3.org/publishing/epub32/epub-mediaoverlays.html#app-overlays-vocab
  
  Scenario: the 'media:duration' property can be expressed as a full clock value
    When checking file 'mediaoverlays-duration-fullclock-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: the 'media:duration' property can be expressed as a timecount value
    When checking file 'mediaoverlays-duration-timecount-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: Report a 'media:active-class' property with a refines attribute
    When checking file 'mediaoverlays-active-class-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not be used with the media:active-class property"
    And no other errors or warnings are reported
  
  Scenario: Report a 'media:playback-active-class' property with a refines attribute
    When checking file 'mediaoverlays-playback-active-class-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not be used with the media:playback-active-class property"
    And no other errors or warnings are reported

  Scenario: Report 'media:duration' properties with non-clock values
    When checking file 'mediaoverlays-duration-clock-values-error.opf'
    Then the following errors are reported
      | RSC-005 | must be a valid SMIL3 clock value |
      | RSC-005 | must be a valid SMIL3 clock value |
      | RSC-005 | must be a valid SMIL3 clock value |
    And no other errors or warnings are reported

