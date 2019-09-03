Feature: EPUB 3 Navigation
  
  Checks conformance to specification rules related to EPUB 3 navigation:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in defaut mode to check
  full EPUB publications.
  
  Note: Tests that do not require a full publication but a single Navigation
        Document are defined in the `navigation-document.feature` feature file.   
  
  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
