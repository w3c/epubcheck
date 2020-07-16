Feature: EPUB for Education ▸ Package Document Checks


  Checks conformance to the EPUB for Education specification:
    http://idpf.org/epub/profiles/edu/spec/
  
  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub-edupub/files/package-document/'
    And EPUBCheck configured to check a Package Document
    And EPUBCheck configured with the 'edupub' profile


  Scenario: a minimal EDUPUB publication is reported as valid 
    When checking file 'edupub-minimal-valid.opf'
    Then no errors or warnings are reported

  ## 3.4 Teacher's Editions and Guides
  
  Scenario: a minimal EDUPUB teachers’s edition is reported as valid
    When checking file 'edupub-teacher-edition-minimal-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: an EDUPUB teachers’s edition must declare the type 'edupub'
    When checking file 'edupub-teacher-edition-metadata-type-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The dc:type identifier "edupub" is required'
    And no other errors or warnings are reported
  
  Scenario: an EDUPUB teachers’s edition should declare the source student edition
    When checking file 'edupub-teacher-edition-metadata-source-missing-warning.opf'
    Then warning RSC-017 is reported
    And the message contains 'A teacher’s edition should identify the corresponding student edition'
    And no other errors or warnings are reported
    
  ## 8.1 Profile Identification
  
  Scenario: an EDUPUB publication must declare the type 'edupub'
    When checking file 'edupub-metadata-type-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The dc:type identifier "edupub" is required'
    And no other errors or warnings are reported
    
  ## 8.3 Accessibility Metadata
    
  Scenario: an EDUPUB’s accessibility features must be declared
    When checking file 'edupub-metadata-accessibilityFeature-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'At least one schema:accessibilityFeature declaration is required'
    And no other errors or warnings are reported
    
  Scenario: an EDUPUB’s accessibility features must at least include 'tableOfContents' 
    When checking file 'edupub-metadata-accessibilityFeature-none-error.opf'
    Then error RSC-005 is reported (since the only feature declared is 'none')
    And the message contains 'value "none" is not valid in edupub'
    And no other errors or warnings are reported
