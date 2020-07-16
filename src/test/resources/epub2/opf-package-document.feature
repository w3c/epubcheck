Feature: EPUB 2 ▸ Open Packaging Format ▸ Package Document Checks


  Checks conformance to the Open Packaging Format (OPF) 2.0.1 specification:
    http://idpf.org/epub/20/spec/OPF_2.0_latest.htm

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given test files located at '/epub2/files/opf-document/'
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And EPUBCheck configured to check a Package Document


	Scenario: the minimal OPF document is reported as valid 
    When checking EPUB 'minimal.opf'
    Then no errors or warnings are reported

  ## 1.3 Relationship to other specifications

  Scenario: the default namespace must be 'http://www.idpf.org/2007/opf' 
    When checking EPUB 'xml-namespace-wrongdefault-error.opf'
    Then error RSC-005 is reported 4 times (1 for the NS, 3 as side effects) 
    And no other errors or warnings are reported

  Scenario: Report a missing version attribute
    When checking EPUB 'version-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'missing required attribute "version"'
    And no other errors or warnings are reported

  ### 1.3.2 Relationship to XML Namespaces

  ## 1.4 Conformance

  ### 1.4.1.1 Package Conformance

  Scenario: duplicate IDs are reported
    When checking EPUB 'xml-id-duplicate-error.opf'
    Then error RSC-005 is reported 2 times (1 for each ID)
    And no other errors or warnings are reported

  Scenario: unknown elements are reported
    When checking EPUB 'xml-element-unknown-error.opf'
    Then error RSC-005 is reported
    And the message contains 'not allowed anywhere'
    And no other errors or warnings are reported

  Scenario: Report an invalid doctype in the OPF document
    See issue #194
    # FIXME the error code should start with OPF
    When checking EPUB 'doctype-invalid-error.opf'
    Then error HTM-009 is reported
    And no other errors or warnings are reported

  Scenario: Ignore legacy OEB 1.2 Package doctype
    See issue #194
    When checking EPUB 'doctype-legacy-oeb12-valid.opf'
    Then no errors or warnings are reported
    
  ## 2.0 The OPF Package Document
    
  ## 2.1 Package Identity

  Scenario: the unique identifier must not be empty
    When checking EPUB 'unique-identifier-attribute-missing-error.opf'
    # FIXME OPF-048 could be removed, as it is already reported as RSC-005
    Then the following errors are reported
      | RSC-005 | missing required attribute                       |
      | OPF-048 | missing its required unique-identifier attribute |
    And no other errors or warnings are reported
  
  Scenario: the unique identifier must not be empty
    When checking EPUB 'metadata-identifier-empty-error.opf'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  # FIXME this error should be detected and reported in single-document mode
  Scenario: the unique identifier must not be empty
    When checking EPUB 'unique-identifier-not-found-error.opf'
    #Then error OPF-030 is reported
    And no other errors or warnings are reported
        
  ## 2.2 Publication Metadata

  Scenario: Report a 'dc:creator' metadata with an unknown role
    When checking EPUB 'metadata-creator-role-unknown-error.opf'
    Then error OPF-052 is reported
    And no other errors or warnings are reported
  
  Scenario: an identifier starting with "urn:uuid:" should be a valid UUID  
    When checking EPUB 'metadata-identifier-uuid-as-urn-invalid-warning.opf'
    Then warning OPF-085 is reported
    And no other errors or warnings are reported

  Scenario: an identifier with a "uuid" scheme should be a valid UUID  
    When checking EPUB 'metadata-identifier-uuid-as-scheme-invalid-warning.opf'
    Then warning OPF-085 is reported
    And no other errors or warnings are reported

  Scenario: report an empty 'dc:date' metadata
    When checking EPUB 'metadata-date-empty-error.opf'
    Then error OPF-054 is reported
    And no other errors or warnings are reported

  Scenario: report a 'dc:date' value not conforming to ISO-8601
    When checking EPUB 'metadata-date-invalid-syntax-error.opf'
    Then error OPF-054 is reported
    And no other errors or warnings are reported

  Scenario: report an empty 'dc:title' metadata
    When checking EPUB 'metadata-title-empty-warning.opf'
    Then warning OPF-055 is reported
    And no other errors or warnings are reported
    
  ## 2.3 Manifest

  Scenario: item paths should not contain spaces
    When checking EPUB 'item-mediatype-oeb1-css-deprecated-warning.opf'
    Then warning OPF-037 is reported
    And no other errors or warnings are reported

  Scenario: item paths should not contain spaces 
    When checking EPUB 'item-href-contains-spaces-warning.opf'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported
  
  ### 2.3.1 Fallback Items

	Scenario: Verify conforming fallbacks
    When checking EPUB 'fallback-valid.opf'
    Then no errors or warnings are reported

	Scenario: Report a 'fallback-style' attribute pointing to a non-existing ID
    When checking EPUB 'fallback-style-not-found-error.opf'
    Then error OPF-041 is reported
    And no other errors or warnings are reported

  ## 2.4 Spine

  Scenario: Report an image used as a spine item
    When checking EPUB 'spine-image-error.opf'
    Then error OPF-042 is reported
    And no other errors or warnings are reported

  Scenario: Report a spine with only non-linear resources
    When checking EPUB 'spine-linear-all-no-error.opf'
    Then error OPF-033 is reported
    And no other errors or warnings are reported

  Scenario: Report a spine with no 'toc' attribute
    When checking EPUB 'spine-toc-attribute-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'missing required attribute "toc"'
    And no other errors or warnings are reported
  
  ## 2.5 Tours
  
  ## 2.6 Guide
  
  Scenario: a valid 'guide' element is allowed 
    When checking EPUB 'guide-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'guide' element must contain chidlren entries
    When checking EPUB 'guide-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains 'element "guide" incomplete'
    And no other errors or warnings are reported
    
  Scenario:  'guide' should not contain two entries of the same type pointing to the same resource 
    When checking EPUB 'guide-duplicates-warning.opf'
    Then warning RSC-017 is reported 2 times (once for each entry)
    And the message contains 'Duplicate "reference" elements with the same "type" and "href" attributes'
    And no other errors or warnings are reported
    