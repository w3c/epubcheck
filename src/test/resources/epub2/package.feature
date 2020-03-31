Feature: EPUB 2.0.1 OPF Packages
  
  Checks conformance to rules for OPF Packages defined in the Open
  Packaging Format (OPF) 2.0.1 specification:
  
    http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications
  
  Note:
  - Tests related to single EPUB 2.0.1 package files are defined in the `opf-document.feature` feature file.   
  - Tests related to EPUB 3 Packages are defined in the `epub3` directory.

  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules

  ###  1.4.1: Package Conformance

  ####  1.4.1.2: Publication Conformance

  Scenario: Verify that the 'clr' MARC code is allowed in the `opf:role` attribute (issue 205)
    When checking epub 'manifest-creator-role-clr-valid'
    Then no errors or warnings are reported


  #  2.0: The OPF Package Document

  Scenario: Verify that package IDs with leading/trailing spaces are allowed (issue 332)
    When checking epub 'package-id-spaces-valid'
    Then no errors or warnings are reported


  ##  2.3: Manifest

  Scenario: Report a reference to a resource that is not listed in the manifest
    When checking epub 'manifest-resource-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Verify that operating system files (`.DS_STORE`, `thumbs.db`) are ignored (issue 256)
    When checking epub 'manifest-os-files-ignore-valid'
    Then no errors or warnings are reported


  ###  2.3.1: Fallback Items
  
  ####  2.3.1.1: Items That Are Not OPS Core Media Types

  Scenario: Report a manifest fallback that does not resolve to a resource in the publication
    When checking epub 'fallback-non-resolving-error'
    # first error is for the missing resource
    Then error OPF-040 is reported
    # second is that a fallback isn't provided
    And error MED-003 is reported
    And no other errors or warnings are reported


  ##  2.4: Spine 

  Scenario: Report repeated spine items (issue 182)
    When checking epub 'spine-itemref-repeated-error'
    Then error OPF-034 is reported
    And no other errors or warnings are reported


  ###  2.4.1: Declarative Global Navigation — the NCX

  ####  2.4.1.2: Key NCX Requirements

  Scenario: Report an NCX reference to a resource that is not in the publication 
    When checking epub 'ncx-missing-resource-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report an NCX `pageTarget` with a `type` attribute value that is not one of "front", "normal" or "special"
    When checking epub 'ncx-pagetarget-type-error'
    Then error RSC-005 is reported
    And the message conains 'value of attribute "type" is invalid'
    And no other errors or warnings are reported

  Scenario: Verify that leading/trailing spaces in an NCX `uid` attribute are allowed
    When checking epub 'ncx-uid-spaces-valid'
    The no errors or warnings are reported

  Scenario: Report an NCX `uid` attribute value that does not match the publication's unique identifier (issue 329)
    When checking epub 'ncx-uid-mismatch-error'
    Then error NCX-001 is reported
    And no other errors or warnings are reported
