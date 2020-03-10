Feature: EPUB Previews Package Document
  
  Checks conformance to rules for Package Documents defined in the
  EPUB Previews 1.0 specification:
    
    http://idpf.org/epub/previews/
  
  This feature file contains tests for EPUBCheck running in `opf` mode to check
  single Package Documents (`.opf` files).
  
  Note: 
  - Tests related to EPUB Previews package rules in a full EPUB publication
    are defined in the `previews-package.feature` feature file.
  - Tests related to general conformance of Package Documents are defined
    in the `epub3/package-document.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub-previews/files/package-document/'
    And EPUBCheck configured to check a Package Document
    
  # 3. Embedded Previews
  
  Scenario: an embedded EPUB preview collection 
    When checking file 'preview-embedded-valid.opf'
    Then no errors or warnings are reported
