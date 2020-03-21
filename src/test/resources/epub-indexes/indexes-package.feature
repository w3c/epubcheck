Feature: EPUB Indexes Packages
  
  Checks conformance to rules for Packages defined in the
  EPUB Indexes specification:
    
    http://idpf.org/epub/idx/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
    
  Note: 
  - Tests related to EPUB Indexes package rules in individual package documents
    are defined in the `indexes-package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub-indexes/files/epub/'
    And EPUBCheck with default settings


  ##  2.3 Identification of an Index
  
  ###  2.3.1 Identification of a Publication Containing Only Index(es) 

  Scenario: Verify an index publication
    Given EPUBCheck configured for the 'idx' profile
    When checking EPUB 'index-whole-pub-valid'
    Then no errors or warnings are reported

  Scenario: Report an index publication without an index
    Given EPUBCheck configured for the 'idx' profile
    When checking EPUB 'index-whole-pub-no-index-error'
    Then error RSC-005 is reported
    And the message contains "At least one 'index' element must be present in a document declared as an index in the OPF"
    And no other errors or warnings are reported


  ###  2.3.2 Identification of Index Content Document(s)

  ####  2.3.2.1 Single-File Index(es) 

  Scenario: Verify a single-file index
    When checking EPUB 'index-single-file-valid'
    Then no errors or warnings are reported

  Scenario: Report a single-file index without an index
    When checking EPUB 'index-single-file-no-index-error'
    Then error OPF-015 is reported
    And the message contains "The property 'index' should not be declared in the OPF file."
    And error RSC-005 is reported
    And the message contains "At least one 'index' element must be present in a document declared as an index in the OPF"
    And no other errors or warnings are reported


  ####  2.3.2.2 Multi-File Index(es) and the collection Element

  Scenario: Verify an index collection
    When checking EPUB 'index-collection-valid'
    Then no errors or warnings are reported

  Scenario: Report an index collection without an index
    When checking EPUB 'index-collection-no-index-error'
    Then error RSC-005 is reported
    And the message contains "At least one 'index' element must be present in a document declared as an index in the OPF"
    And no other errors or warnings are reported

