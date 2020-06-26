Feature: EPUB 2 OPS Content Document
  
  Checks conformance to specification rules defined for EPUB 2 OPS Content Documents:
  http://idpf.org/epub/20/spec/OPS_2.0.1_draft.htm#Section2.0
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Note: Tests related to EPUB XHTML Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And test files located at '/epub2/files/content-document-ops/'


  #  2.0 OPS Content Document Vocabularies

  ##  2.1 Introduction

  Scenario: Report an unresolved entity reference in the doctype declaration
    When checking document 'ops-doctype-unresolved-entity-error.xhtml'
    Then error HTM-004 is reported
    And no other errors or warnings are reported


  ##  2.2 XHTML Modules in the OPS Preferred Vocabulary

  ### Class Attribute

  Scenario: Verify empty `class` attributes are allowed (issue 733)
    When checking document 'ops-class-empty-valid.xhtml'
    Then no errors or warnings are reported


  ### Edit Elements
  
  Scenario: Verify attributes allowed on `ins` and `del` are not restricted (issue 293)
    When checking document 'ops-edit-attributes-valid.xhtml'
    Then no errors or warnings are reported

  ### Identifiers
  
  Scenario: Report duplicate ID values
    When checking document 'ops-id-duplicate-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported


  ### Hyperlinks

  Scenario: Report nested `a` tags (issue 287)
    When checking document 'ops-hyperlinks-nested-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'The a element cannot contain any nested a elements'
    And no other errors or warnings are reported


  ### Language

  Scenario: Verify that `lang` attribute is allowed (issue 215)
    When checking document 'ops-lang-attr-valid.xhtml'
    Then no errors or warnings are reported


  ### Map

  Scenario: Verify `usemap` fragment reference is allowed (issue 696)
    When checking document 'ops-map-usemap-fragment-valid.xhtml'
    Then no errors or warnings are reported


  ### SVG

  Scenario: Verify HTML elements are allowed inside of `foreignObject` (issue 166)
    When checking document 'ops-svg-foreignObject-with-html-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `font-face-src` is allowed (issue 196)
    Given EPUBCheck configured to check an SVG Content Document
    When checking document 'ops-svg-font-face-src-valid.svg'
    Then no errors or warnings are reported

  Scenario: Verify `foreignObject` allowed outside `switch` and `body` allowed inside `foreignObject` (issues 222, 223, 20)
    When checking document 'ops-foreignObject-switch-valid.xhtml'
    Then no errors or warnings are reported
