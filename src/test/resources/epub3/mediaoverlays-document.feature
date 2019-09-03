Feature: EPUB 3 Media Overlays Document
  
  Checks conformance to specification rules defined for EPUB Media Overlays Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in `mo` mode to check
  single Media Overlays Documents (`.smil` files).
  
  Note: Tests related to EPUB Media Overlays rules in a full EPUB publication
        are defined in the `mediaoverlays.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
