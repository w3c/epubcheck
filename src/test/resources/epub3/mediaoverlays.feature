Feature: EPUB 3 Media Overlays
  
  Checks conformance to specification rules related to EPUB 3 Media Overlays:
  https://www.w3.org/publishing/epub32/epub-mediaoverlays.html
  
  This feature file contains tests for EPUBCheck running in defaut mode to check
  full EPUB publications.
  
  Note: Tests that do not require a full publication but a single Media Overlays
        Document (.smil) are defined in the `mediaoverlays-document.feature`
        feature file.   
  
  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
