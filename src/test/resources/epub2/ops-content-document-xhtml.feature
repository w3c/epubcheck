Feature: EPUB 2 ▸ Open Publication Structure ▸ XHTML Document Checks


  Checks conformance to the Open Publication Structure (OPS) 2.0.1 specification:
    http://idpf.org/epub/20/spec/OPS_2.0_latest.htm

  In the scenarios below, checks are run against single XHTML Content Documents.
  EPUBCheck is launched in 'xhtml' mode.


  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And test files located at '/epub2/files/ops-document-xhtml/'


  #  2.0 OPS Content Document Vocabularies

  Scenario: Verify a minimal XHTML OPS Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  ##  2.1 Introduction

  Scenario: Report an unresolved entity reference in the doctype declaration
    When checking document 'doctype-unresolved-entity-error.xhtml'
    Then error HTM-004 is reported
    And no other errors or warnings are reported

  Scenario: Report a DOCTYPE declaration with an invalid public identifier
    When checking file 'doctype-public-id-error.xhtml'
    Then error HTM-004 is reported
    And no other errors or warnings are reported
  
  Scenario: Report an HTML5 DOCTYPE declaration
    When checking file 'doctype-html5-error.xhtml'
    Then error HTM-004 is reported
    And no other errors or warnings are reported

  Scenario: Verify valid character references
    When checking document 'entities-character-references-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report HTML5 elements used in OPS XHTML Content Documents
    When checking document 'html5-elements-error.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  ##  2.2 XHTML Modules in the OPS Preferred Vocabulary

  ### Class Attribute

  Scenario: Verify empty `class` attributes are allowed (issue 733)
    When checking document 'class-empty-valid.xhtml'
    Then no errors or warnings are reported


  ### Custom Attributes

  Scenario: Report the use of a custom namespaced attribute
    When checking document 'custom-ns-attr-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "foo:bar" not allowed here'
    And no other errors or warnings are reported

  ### Edit Elements
  
  Scenario: Verify attributes allowed on `ins` and `del` are not restricted (issue 293)
    When checking document 'edit-attributes-valid.xhtml'
    Then no errors or warnings are reported

  ### Identifiers
  
  Scenario: Report duplicate ID values
    When checking document 'id-duplicate-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported


  ### Hyperlinks

  Scenario: Report nested `a` tags (issue 287)
    When checking document 'hyperlinks-nested-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'The "a" element cannot contain any nested "a" elements'
    And no other errors or warnings are reported


  ### Language

  Scenario: Verify that `lang` attribute is allowed (issue 215)
    When checking document 'lang-attr-valid.xhtml'
    Then no errors or warnings are reported


  ### Map

  Scenario: Verify `usemap` fragment reference is allowed (issue 696)
    When checking document 'map-usemap-fragment-valid.xhtml'
    Then no errors or warnings are reported


  ### SVG

  Scenario: Verify HTML elements are allowed inside of `foreignObject` (issue 166)
    When checking document 'svg-foreignObject-with-html-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `foreignObject` allowed outside `switch` and `body` allowed inside `foreignObject` (issues 222, 223, 20)
    When checking document 'svg-foreignObject-switch-valid.xhtml'
    Then no errors or warnings are reported
