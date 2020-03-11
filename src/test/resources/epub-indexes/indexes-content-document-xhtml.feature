Feature: EPUB Indexes XHTML Content Document
  
  Checks conformance to rules for XHTML Content Documents defined in the
  EPUB Indexes specification:
    
    http://idpf.org/epub/idx/
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Background: 
    Given EPUB test files located at '/epub-indexes/files/content-document-xhtml/'
    And EPUBCheck configured to check an XHTML Content Document

  Scenario: testIndex
    Given EPUBCheck configured with the idx profile
    When checking document 'index.xhtml'
    Then no errors or warnings are reported

  Scenario: testIndex_NoIndex
    Given EPUBCheck configured with the idx profile
    When checking document 'xhtml/invalid/index-noindex.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testIndex_IndexNotOnBody
    Given EPUBCheck configured with the idx profile
    When checking document 'xhtml/invalid/index-notonbody.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

