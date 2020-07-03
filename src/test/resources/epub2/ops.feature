Feature: EPUB 2.0.1 Open Publication Structure
  
  Checks conformance to rules for Content Documents defined in the Open
  Publication Structure (OPS) 2.0.1 specification:
  
    http://idpf.org/epub/20/spec/OPS_2.0.1_draft.htm
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications
  
  Note:
  - Tests related to single EPUB 2.0.1 OPS Content Documents are defined in the 
    `ops-document-*.feature` feature files.   
  - Tests related to EPUB 3 Content Documents are defined in the `epub3` directory.

  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules

  ## 1.0: Overview
  
  ### 1.4: Conformance

  ####  1.4.1.2: XHTML Content Document Requirements

  Scenario: Report an XHTML content document without an `.xhtml` extension
    When checking EPUB 'ops-xhtml-extension-error'
    Then warning HTM-014 is reported
    And no other errors or warnings are reported
