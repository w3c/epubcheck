Feature: EPUB 3 Media Overlays
  
  Checks conformance to specification rules related to EPUB 3 Media Overlays:
  https://www.w3.org/publishing/epub32/epub-mediaoverlays.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note: Tests that do not require a full publication but a single Media Overlays
        Document (.smil) are defined in the `mediaoverlays-document.feature`
        feature file.   
  
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
