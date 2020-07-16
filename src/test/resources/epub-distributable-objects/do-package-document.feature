Feature: EPUB Distributable Objects ▸ Package Document Checks


  Checks conformance to the EPUB Distributable Objects 1.0 specification:
    http://idpf.org/epub/do/

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


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
