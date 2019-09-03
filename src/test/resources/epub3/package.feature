Feature: EPUB 3 Packages
  
  Checks conformance to specification rules related to EPUB Packages:
  https://www.w3.org/publishing/epub32/epub-packages.html
  
  This feature file contains tests for EPUBCheck running in defaut mode to check
  full EPUB publications.
  
  Note:
  - Tests that do not require a full publication but a single Package Document
    are defined in the `package-document.feature` feature file.   
  - Tests related to EPUB Navigation Documents are defined in the `navigation.feature`
    and `navigation-document.feature` feature files.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
