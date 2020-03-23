Feature: EPUB Indexes Content
  
  Checks conformance to rules for index content defined in the
  EPUB Indexes specification:
    
    http://idpf.org/epub/idx/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.

  Background: 
    Given EPUB test files located at '/epub-indexes/files/epub/'
    And EPUBCheck with default settings

  ##  2.2 Content Documents and Components

  ###  2.2.1 Index, Index Head Notes

  Scenario: Report an index publication with an invalid content model
    Given EPUBCheck configured with the 'idx' profile
    When checking EPUB 'index-whole-pub-content-model-error'
    Then error RSC-005 is reported
    And the message contains "An 'index' must contain one and only one 'index-entry-list'"
    And no other errors or warnings are reported

  Scenario: Report a single-file index with an invalid content model
    When checking EPUB 'index-single-file-content-model-error'
    Then error RSC-005 is reported
    And the message contains "An 'index' must contain one and only one 'index-entry-list'"
    And no other errors or warnings are reported

  Scenario: Report an index collection with an invalid content model
    When checking EPUB 'index-collection-content-model-error'
    Then error RSC-005 is reported
    And the message contains "An 'index' must contain one and only one 'index-entry-list'"
    And no other errors or warnings are reported
