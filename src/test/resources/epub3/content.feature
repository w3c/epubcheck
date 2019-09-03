Feature: EPUB 3 Content
  
  Checks conformance to specification rules related to EPUB Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html
  
  This feature file contains tests for EPUBCheck running in defaut mode to check
  full EPUB publications.
  
  Note: Tests that do not require a full publication but a single Content
        Document are defined in the `content-document-xhtml.feature` and
        `content-document-svg.feature` feature files.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings

  Scenario: Minimal EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  Scenario: XHTML with duplicate ID
    When checking EPUB 'xhtml-invalid-duplicate-id'
    Then the following errors are reported
      | RSC-005 | Duplicate ID |
      | RSC-005 | Duplicate ID |
    And no other errors or warnings are reported

  Scenario: XHTML with invalid content model (RelaxNG schema)
    When checking EPUB 'xhtml-invalid-content-model-relaxng'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: XHTML with invalid content model (Schematron schema)
    When checking EPUB 'xhtml-invalid-content-model-schematron'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
