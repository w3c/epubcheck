Feature: EPUB 3 ▸ Media Overlays ▸ Full Publication Checks


  Checks conformance to the EPUB Media Overlays 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-mediaoverlays.html

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings

  ## 1. Introduction

  Scenario: Verify a minimal EPUB 3 publication with Media Overlays
    When checking EPUB 'mediaoverlays-minimal-valid'
    Then no errors or warnings are reported


  ## 2. Media Overlay Document Definition
  
  ### 2.2 Content Conformance
  
  Scenario: Report an EPUB content document that is declared in more than one overlay
    When checking EPUB 'mediaoverlays-multiple-overlay-ref-error'
    Then error MED-011 is reported
    And no other errors or warnings are reported

  ### The audio Element

  Scenario: Report an audio clip that is not a Core Media Type
    When checking EPUB 'mediaoverlays-audio-non-cmt-error'
    Then error MED-005 is reported
    And no other errors or warnings are reported

  ## 3. Creating Media Overlays
  
  ### 3.5.1 Including Media Overlays

  Scenario: Report an EPUB content document referenced from an overlay that is missing its media-overlay attribute
    When checking EPUB 'mediaoverlays-missing-mo-attr-error'
    Then error MED-010 is reported
    And no other errors or warnings are reported

  Scenario: Report an EPUB content document that references the wrong overlay
    When checking EPUB 'mediaoverlays-incorrect-overlay-ref-error'
    Then error MED-012 is reported
    And no other errors or warnings are reported

  Scenario: Report an EPUB content document that declares a media-overlay attribute but is not referenced from the overlay
    When checking EPUB 'mediaoverlays-no-overlay-ref-error'
    Then error MED-013 is reported
    And no other errors or warnings are reported
