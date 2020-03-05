Feature: EPUB 2 OPS Content Document
  
  Checks conformance to specification rules defined for EPUB 2 OPS Content Documents:
  http://idpf.org/epub/20/spec/OPS_2.0.1_draft.htm#Section2.0
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Note: Tests related to EPUB XHTML Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And test files located at '/epub2/files/content-document-ops/'

  Scenario: Minimal Content Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported


  Scenario: testValidateXHTML_UnresolvedDTD
    When checking document 'ops/invalid/unresolved-entity.xhtml'
    Then error HTM_004 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_DupeID
    When checking document 'ops/invalid/dupe-id.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_issue166_valid
    When checking document 'ops/valid/svg-foreignObject.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateSVGIssue196
    When checking document 'ops/valid/svg-font-face.svg'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue215
    When checking document 'ops/valid/issue215.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue222_223_20
    // foreignObject allowed outside switch, and <body> allowed inside
    When checking document 'ops/valid/issue222.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue287_NestedHyperlink
    When checking document 'ops/invalid/issue287-nested-hyperlink.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLIssue293
    When checking document 'ops/valid/issue293-edits-elem-attributes.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLImageMap_EPUB2_Valid
    When checking document 'imagemap-good_issue696.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLEmptyClass_EPUB2_Valid
    When checking document 'empty-class-attribute-is-valid_issue733.xhtml'
    Then no errors or warnings are reported

