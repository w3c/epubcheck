Feature: EPUB Accessibility Package Document
  
  Checks conformance to rules for Package Documents defined in the
  EPUB Accessibility 1.0 specification:
    
    http://www.idpf.org/epub/latest/accessibility
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note: 
  - Tests related to general conformance of Package Documents are defined
    in the `package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/package-document/'
    And EPUBCheck configured to check a Package Document
    
  ## 3.2 Package Metadata
  
  Scenario: The 'a11y' prefix can be used in metadata properties without being declared
    When checking file 'property-prefix-a11y-not-declared-valid.opf'
    Then no errors or warnings are reported

  Scenario: unknown 'a11y' metadata is reported
    When checking file 'property-prefix-a11y-unknown-value-error.opf'
    Then error OPF-027 is reported 2 times (1 unknown 'meta', 1 unknown 'link' property) 
    And no other errors or warnings are reported
