Feature: EPUB 2.0.1 OPF Package Document 
  
  Checks conformance to rules for OPF Package Documents defined in the Open
  Packaging FOrmat (OPF) 2.0.1 specification:
  
    http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note:
  - Tests related to EPUB 2.0.1 OPF rules in a full EPUB publication are defined
    in the `package.feature` feature file.   
  - Tests related to EPUB 3 Package Documents are defined in the `epub3` directory.

  Background: 
    Given test files located at '/epub2/files/opf/'
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And EPUBCheck configured to check a Package Document

  # 1.4 Conformance
  
  ## 1.4.1.1 Package Conformance

	Scenario: the minimal OPF document is reported as valid 
    When checking EPUB 'minimal.opf'
    Then no errors or warnings are reported
    
  Scenario: the default namespace must be 'http://www.idpf.org/2007/opf' 
  	When checking EPUB 'xml-namespace-wrongdefault-error.opf'
    Then error RSC-005 is reported 4 times (1 for the NS, 3 as side effects) 
    And no other errors or warnings are reported
    
  Scenario: duplicate IDs are reported
    When checking EPUB 'xml-id-duplicate-error.opf'
    Then error RSC-005 is reported 2 times (1 for each ID)
    And no other errors or warnings are reported
    
  ## 2.0 The OPF Package Document
    
  ## 2.1 Package Identity
  
  Scenario: the unique identifier must not be empty
    When checking EPUB 'metadata-identifier-empty-error.opf'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
        
  ## 2.2 Publication Metadata
  
  Scenario: an identifier starting with "urn:uuid:" should be a valid UUID  
    When checking EPUB 'metadata-identifier-uuid-as-urn-invalid-warning.opf'
    Then warning OPF-085 is reported
    And no other errors or warnings are reported

  Scenario: an identifier with a "uuid" scheme should be a valid UUID  
    When checking EPUB 'metadata-identifier-uuid-as-scheme-invalid-warning.opf'
    Then warning OPF-085 is reported
    And no other errors or warnings are reported
    
  ## 2.3 Manifest
  
  Scenario: item paths should not contain spaces 
    When checking EPUB 'item-href-contains-spaces-warning.opf'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported
  
  ### 2.3.1 Fallback Items
  
  ## 2.4 Spine
  
  ## 2.5 Tours
  
  ## 2.6 Guide
  
  Scenario: a valid 'guide' element is allowed 
    When checking EPUB 'guide-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'guide' element must contain chidlren entries
    When checking EPUB 'guide-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains "element \"guide\" incomplete"
    And no other errors or warnings are reported
    
  Scenario:  'guide' should not contain two entries of the same type pointing to the same resource 
    When checking EPUB 'guide-duplicates-warning.opf'
    Then warning RSC-017 is reported 2 times (once for each entry)
    And the message contains "Duplicate 'reference' elements with the same 'type' and 'href' attributes"
    And no other errors or warnings are reported
    