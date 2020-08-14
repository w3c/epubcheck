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

  ### The audio Element

  Scenario: Report an audio clip that is not a Core Media Type
    When checking EPUB 'mediaoverlays-audio-non-cmt-error'
    Then error MED-005 is reported
    And no other errors or warnings are reported

  ## 3. Creating Media Overlays
  
  ### 3.2.1 Structure

  Scenario: Report an overlay document whose text elements do not match the dom order of the corresponding content document
    When checking EPUB 'mediaoverlays-text-reading-order-error'
    Then error MED-016 is reported
    And no other errors or warnings are reported
