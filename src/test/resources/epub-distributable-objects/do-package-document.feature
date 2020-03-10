Feature: EPUB Distributable Objects Package Document
  
  Checks conformance to rules for Package Documents defined in the
  EPUB Distributable Objects 1.0 specification:
    
    http://idpf.org/epub/do/
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note: 
  - Tests related to EPUB Distributable Objects package rules in a full EPUB
    publication are defined in the `do-package.feature` feature file.
  - Tests related to general conformance of Package Documents are defined
    in the `epub3/package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub-distributable-objects/files/package-document/'
    And EPUBCheck configured to check a Package Document

	## 2.2 Embedded Objects
	  
  Scenario: a simple EPUB Embedded Object 
    When checking file 'do-collection-valid.opf'
    Then no errors or warnings are reported
    
  ### 2.2.3 Metadata

  Scenario: an embedded object must have a `dc:identifier` metadata 
    When checking file 'do-collection-metadata-identifier-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "must include exactly one identifier"
    And no other errors or warnings are reported
