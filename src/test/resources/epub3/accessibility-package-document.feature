Feature: EPUB 3 ▸ Accessibility ▸ Package Document Checks


  Checks conformance to the EPUB Accessibility 1.0 specification:
    http://www.idpf.org/epub/latest/accessibility

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub3/files/package-document/'
    And EPUBCheck configured to check a Package Document


  ## 3.2 Package Metadata
  
  Scenario: Verify an 'a11y' prefix used in metadata properties without being declared
    When checking file 'property-prefix-a11y-not-declared-valid.opf'
    Then no errors or warnings are reported

  Scenario: Report unknown 'a11y' metadata
    When checking file 'property-prefix-a11y-unknown-value-error.opf'
    Then error OPF-027 is reported 2 times (1 unknown 'meta', 1 unknown 'link' property) 
    And no other errors or warnings are reported

  ## 4.5 Conformance Reporting

  Scenario: Verify an 'a11y:certifierCredential' property can be defined as a link
    When checking file 'link-rel-a11y-certifierCredential-valid.opf'
    Then no errors or warnings are reported
