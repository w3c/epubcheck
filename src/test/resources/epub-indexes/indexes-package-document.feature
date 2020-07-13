Feature: EPUB Indexes Package Document
  
  Checks conformance to rules for Package Documents defined in the
  EPUB Indexes specification:
    
    http://idpf.org/epub/idx/
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note: 
  - Tests related to EPUB Indexes package rules in a full EPUB publication
    are defined in the `indexes-package.feature` feature file.
  - Tests related to general conformance of Package Documents are defined
    in the `epub3/package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub-indexes/files/package-document/'
    And EPUBCheck configured to check a Package Document

  ##  2.3.2.2 Multi-File Index(es) and the collection Element

  Scenario: An index collection is reported as valid
    When checking file 'index-collection-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: An index collection may contain index-group child collections
    When checking file 'index-collection-index-group-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: An index collection must only contain links to XHTML Content Documents
    When checking file 'index-collection-resource-not-xhtml-error.opf'
    Then error OPF-071 is reported
    And the message contains "Index collections must only contain resources pointing to XHTML Content Documents"
    And no other errors or warnings are reported

  Scenario: An index collection must not contain child collections other than index-group
    When checking file 'index-collection-subcollection-error.opf'
    Then error RSC-005 is reported
    And the message contains 'An "index" collection must not have sub-collections other than "index-group"'
    And no other errors or warnings are reported
    
  Scenario: An index-group collection must not contain child collections
    When checking file 'index-collection-index-group-subcollection-error.opf'
    Then error RSC-005 is reported
    And the message contains 'An "index-group" collection must not have child collections'
    And no other errors or warnings are reported
    
  Scenario: An index-group collection must be a child collection of an index collection
    When checking file 'index-collection-index-group-top-level-error.opf'
    Then error RSC-005 is reported
    And the message contains 'An "index-group" collection must be a child of an "index" collection'
    And no other errors or warnings are reported
