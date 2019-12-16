Feature: EPUB Scriptable Components
  
  Checks conformance to rules for Package Documents defined in the
  EPUB Scriptable Components Packaging and Integration 1.0:
    
    http://idpf.org/epub/sc/pkg/
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note: 
  - Tests related to EPUB Scriptable Components package rules in a full EPUB publication
    are defined in the `scriptable-components-package.feature` feature file.
  - Tests related to general conformance of Package Documents are defined
    in the `epub3/package-document.feature` feature file.

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
    And the message contains "Undeclared prefix: 'epubsc'"
    And no other errors or warnings are reported
