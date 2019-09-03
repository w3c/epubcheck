Feature: EPUB 3 Navigation Document
  
  Checks conformance to specification rules defined for EPUB Navigation Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in `nav` mode to check
  single Navigation Documents (`.xhtml` files).
  
  Note: Tests related to EPUB navigation rules in a full EPUB publication are
        defined in the `navigation.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
