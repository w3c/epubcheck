Feature: EPUB 3 SVG Content Document
  
  Checks conformance to specification rules defined for EPUB SVG Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html#sec-svg
  
  This feature file contains tests for EPUBCheck running in `svg` mode to check
  single SVG Content Documents (`.svg` files).
  
  Note: Tests related to EPUB SVG Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  Scenario: testValidateSVG_Links
    When checking document 'svg/valid/svg-links.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_ValidStyleWithoutType_issue688
    When checking document 'svg/valid/issue688.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithARIAAttributes
    When checking document 'svg/valid/aria-attributes.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithDataAttribute
    When checking document 'svg/valid/data-attribute.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithCustomNamespace
    When checking document 'svg/valid/custom-ns.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_Links_MisssingTitle
    When checking document 'svg/invalid/svg-links.svg'
    Then warning ACC_011 is reported
    And no other errors or warnings are reported

  Scenario: testValidateSVG_ForeignObject
    // tests that 'foreignObject' conforming to the rules is accepted
    When checking document 'svg/valid/foreignObject.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_ForeignObjectWithInvalidRequiredExtensions
    // tests that 'foreignObject' with a 'requiredExtensions' attribute other than the OPS NS is invalid 
    When checking document 'svg/invalid/foreignObject-invalid-requiredExtensions.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ForeignObjectWithNonXHTMLContent
    // tests that 'foreignObject' can't have children that are not HTML content 
    When checking document 'svg/invalid/foreignObject-non-html-content.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ForeignObjectWithTwoBodyElements
    // tests that 'foreignObject' can't have children that are not HTML content 
    When checking document 'svg/invalid/foreignObject-two-body.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_DuplicateIds
    // tests that duplicate IDs are detected 
    When checking document 'svg/invalid/duplicate-ids.svg'
    Then error RSC_005 is reported  times
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ImageHrefWithAFragment
    // tests that SVG 'image' elements can have an 'xlink:href' URL pointing to a fragment 
    When checking document 'svg/valid/svg-image-fragment.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVGIssue219
    When checking document 'svg/valid/issue219.svg'
    Then no errors or warnings are reported

