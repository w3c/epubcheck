Feature: EPUB 3 ▸ Accessibility


  Checks conformance to the "Accessibility" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-accessibility

  Background: 
    Given EPUB test files located at '/epub3/10-accessibility/files/'
    And EPUBCheck configured to check a Package Document

  
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
