Feature: EPUB 3 SVG Content Document
  
  Checks conformance to specification rules defined for EPUB SVG Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html#sec-svg
  
  This feature file contains tests for EPUBCheck running in `svg` mode to check
  single SVG Content Documents (`.svg` files).
  
  Note: Tests related to EPUB SVG Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
