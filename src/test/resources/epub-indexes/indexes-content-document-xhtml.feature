Feature: EPUB Indexes XHTML Content Document
  
  Checks conformance to rules for XHTML Content Documents defined in the
  EPUB Indexes specification:
    
    http://idpf.org/epub/idx/
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Background: 
    Given EPUB test files located at '/epub-indexes/files/content-document-xhtml/'
    And EPUBCheck configured to check an XHTML Content Document
    And EPUBCheck configured with the 'idx' profile

  Scenario: Verify a basic index
    When checking document 'index-basic-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report document without an index
    When checking document 'index-declaration-none-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: Report `index` semantic not declared on `body` element
    When checking document 'index-declaration-body-error.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

