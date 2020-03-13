Feature: EPUB 3 SVG Content Document
  
  Checks conformance to specification rules defined for EPUB SVG Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html#sec-svg
  
  This feature file contains tests for EPUBCheck running in `svg` mode to check
  single SVG Content Documents (`.svg` files).
  
  Note: Tests related to EPUB SVG Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/content-document-svg/'
    And EPUBCheck configured to check an SVG Content Document
    And EPUBCheck with default settings

  #  3. SVG Content Documents
  
  ##  3.2 Content Conformance

  ###  ARIA attributes
 
  Scenario: Verify ARIA attributes are allowed
    When checking document 'core-aria-attributes-valid.svg'
    Then no errors or warnings are reported

  ### Custom Namespaces

  Scenario: Verify elements from custom namespaces are allowed
    When checking document 'core-ns-custom-valid.svg'
    Then no errors or warnings are reported

  ###  Data Attributes

  Scenario: Verify `data-*` attributes are allowed
    When checking document 'core-data-attribute-valid.svg'
    Then no errors or warnings are reported

  ###  Fonts

  Scenario: Verify that empty `font-face` declarations are allowed
    When checking document 'core-font-face-empty-valid.svg'
    Then no errors or warnings are reported

  ###  Hyperlinks

  Scenario: Verify links are allowed
    When checking document 'core-link-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report SVG link without a title
    When checking document 'core-link-no-title-error.svg'
    Then warning ACC-011 is reported
    And no other errors or warnings are reported

  Scenario: Verify that `image` elements can have an `xlink:href` URL pointing to a fragment 
    When checking document 'core-image-fragment-valid.svg'
    Then no errors or warnings are reported

  ### Identifiers

  Scenario: Report duplicate `id` attribute values 
    When checking document 'core-id-duplicate-error.svg'
    Then error RSC-005 is reported 2 times
    And no other errors or warnings are reported

  ### Style Attribute

  Scenario: Verify `style` element without explicit `type` (issue 688)
    When checking document 'core-style-no-type-valid.svg'
    Then no errors or warnings are reported
  
  ###  RDF

  Scenario: Verify RDF elements can be embedded in SVG
    When checking document 'core-rdf-valid.svg'
    Then no errors or warnings are reported


  ##  3.3 Restrictions on SVG

  Scenario: Verify that `foreignObject` conforming to the rules is allowed
    When checking document 'core-foreignObject-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report `foreignObject` with a `requiredExtensions` attribute with a non-OPS namespace 
    When checking document 'core-foreignObject-requiredExtensions-ns-error.svg'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
  
  Scenario: Report `foreignObject` with non-HTML child content 
    When checking document 'core-foreignObject-non-html-content-error.svg'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
  
  Scenario: Report `foreignObject` with multiple HTML `body` elements
    When checking document 'core-foreignObject-multiple-body-error.svg'
    Then error RSC-005 is reported
    And no other errors or warnings are reported
