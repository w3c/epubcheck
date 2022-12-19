 Feature: EPUB 3 — Minimal Publications
  
  Checks minimal publications against the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/


  Background: 
    Given EPUB test files located at '/epub3/00-minimal/files/'
    And EPUBCheck with default settings

  Scenario: Verify a minimal expanded EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  Scenario: Verify a minimal packaged EPUB
    When checking EPUB 'minimal.epub'
    Then no errors or warnings are reported

  Scenario: Verify a minimal package document 
    When checking file 'minimal.opf'
    Then no errors or warnings are reported

  Scenario: Verify a minimal XHTML content document 
    When checking file 'minimal.xhtml'
    Then no errors or warnings are reported
