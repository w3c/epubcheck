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

  ##  2. Indexes Definition
  
  Scenario: Verify a minimal index
    When checking document 'index-minimal-valid.xhtml'
    Then no errors or warnings are reported


  ##  2.2.1 Index, Index Head Notes

  Scenario: Report document without an index declaration
    When checking document 'index-declaration-none-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'At least one "index" element must be present'
    And no other errors or warnings are reported

  Scenario: Report `index` semantic not declared on `body` element
    When checking document 'index-declaration-body-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'its "body" element must have the epub:type "index"'
    And no other errors or warnings are reported
