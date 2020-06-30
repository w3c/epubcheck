Feature: EPUB for Education Package Document
  
  Checks conformance to rules for EDUPUB publications as defined in the
  EPUB for Education specification (aka “EDUPUB”):
    
    http://idpf.org/epub/profiles/edu/spec/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note: 
  - Tests related to EDUPUB package document rules in a single package document
    are defined in the `edupub-package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub-edupub/files/epub/'
    And EPUBCheck configured with the 'edupub' profile

  ##  3. Education Document Models

  ###  3.1 Reflowable Publications

  Scenario: Verify a basic edupub publication
    When checking EPUB 'edupub-basic-valid'
    Then no errors or warnings are reported


  ###  3.2 Fixed-Layout Publications

  Scenario: Verify an edupub publication with fixed-layout documents
    When checking EPUB 'edupub-fxl-valid'
    Then no errors or warnings are reported


  #  4. Content Structure

  ##  4.2 Sectioning

  Scenario: Verify an non-linear content does not have to follow the sectioning rules
    When checking EPUB 'edupub-non-linear-valid'
    Then no errors or warnings are reported


  ##  4.5 Semantic Enrichment

  Scenario: Report an edupub publication with microdata attributes
    When checking EPUB 'edupub-microdata-warning'
    Then warning HTM-051 is reported
    And no other errors or warnings are reported


  ##  4.6 Pagination

  Scenario: Report an edupub publication missing a page list
    When checking EPUB 'edupub-pagelist-missing-error'
    Then error NAV-003 is reported
    And no other errors or warnings are reported

  Scenario: Report an edupub publication with a page list but the source of the pagination is not identified
    When checking EPUB 'edupub-pagelist-no-source-error'
    Then error OPF-066 is reported
    And no other errors or warnings are reported
