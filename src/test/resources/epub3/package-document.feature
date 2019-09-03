Feature: EPUB 3 Package Document
  
  Checks conformance to specification rules defined for EPUB Package Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note:
  - Tests related to EPUB Packages rules in a full EPUB publication are defined
    in the `package.feature` feature file.   
  - Tests related to EPUB Navigation Documents are defined in the `navigation.feature`
    and `navigation-document.feature` feature files.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
