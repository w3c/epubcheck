Feature: EPUB Scriptable Components ▸ Package Document Checks


  Checks conformance to the EPUB Scriptable Components Packaging and
  Integration 1.0 specification:
    http://idpf.org/epub/sc/pkg/

  In the scenarios below, checks are run against single Pacakge Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub-scriptable-components/files/package-document/'
    And EPUBCheck configured to check a Package Document


  ## 3.3.3 Embedded Component

  Scenario: A minimal embedded scriptable component is reported as valid
    When checking file 'sc-embedded-valid.opf'
    Then no errors or warnings are reported

  Scenario: The 'epubsc' prefix must be declared
    When checking file 'sc-prefix-declaration-missing-error.opf'
    Then error OPF-028 is reported
    And the message contains 'Undeclared prefix: "epubsc"'
    And no other errors or warnings are reported
