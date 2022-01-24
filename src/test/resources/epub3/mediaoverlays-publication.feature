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

  Scenario: Verify that a Media Overlays Document can have any extension
    When checking EPUB 'mediaoverlays-file-extension-unusual-valid'
    Then no errors or warnings are reported


  ## 2. Media Overlay Document Definition
  
  ### 2.2 Content Conformance
  
  Scenario: Report an EPUB content document that is declared in more than one overlay
    When checking EPUB 'mediaoverlays-multiple-overlay-ref-error'
    Then error MED-011 is reported
    And no other errors or warnings are reported

  Scenario: Report empty fragment identifiers
    When checking EPUB 'mediaoverlays-fragid-invalid-error'
    Then error MED-014 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report a fragment identifier that does not resolve to an element
    When checking EPUB 'mediaoverlays-fragid-resolve-error'
    Then error RSC-012 is reported
    And no other errors or warnings are reported

  ### 2.4.8 The audio Element

  Scenario: Report an audio clip that is not a Core Media Type
    When checking EPUB 'mediaoverlays-audio-non-cmt-error'
    Then error MED-005 is reported
    And no other errors or warnings are reported

  ## 3. Creating Media Overlays
  
  ### 3.2.1 Structure

  Scenario: Report an overlay document whose text elements do not match the dom order of the corresponding content document
    Given the reporting level is set to USAGE
    When checking EPUB 'mediaoverlays-text-reading-order-error'
    Then usage MED-015 is reported
    And no other errors or warnings are reported

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

