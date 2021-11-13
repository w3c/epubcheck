Feature: EPUB 3 ▸ Content Documents ▸ SVG Document Checks


  Checks conformance to the EPUB Content Documents 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-contentdocs.html

  In the scenarios below, checks are run against single SVG Content Documents.
  EPUBCheck is launched in 'svg' mode.


  Background: 
    Given EPUB test files located at '/epub3/files/content-document-svg/'
    And EPUBCheck configured to check an SVG Content Document
    And EPUBCheck with default settings


  #  3. SVG Content Documents
  
  ##  3.2 Content Conformance

  ###  ARIA attributes

  Scenario: Verify ARIA attributes are allowed
    When checking document 'aria-attributes-valid.svg'
    Then no errors or warnings are reported

  ### Custom Namespaces

  Scenario: Verify elements from custom namespaces are allowed
    When checking document 'ns-custom-valid.svg'
    Then no errors or warnings are reported

  ###  Data Attributes

  Scenario: Verify `data-*` attributes are allowed
    When checking document 'data-attribute-valid.svg'
    Then no errors or warnings are reported

  ###  Fonts

  Scenario: Verify that empty `font-face` declarations are allowed
    When checking document 'font-face-empty-valid.svg'
    Then no errors or warnings are reported

  ###  Hyperlinks

  Scenario: Verify links are allowed
    When checking document 'link-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report SVG link without a title
    When checking document 'link-no-title-error.svg'
    Then warning ACC-011 is reported
    And no other errors or warnings are reported

  Scenario: Verify that `image` elements can have an `xlink:href` URL pointing to a fragment 
    When checking document 'image-fragment-valid.svg'
    Then no errors or warnings are reported

  ### Identifiers

  Scenario: Report duplicate `id` attribute values 
    When checking document 'id-duplicate-error.svg'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported

  Scenario: Report invalid `id` attribute values 
    When checking document 'id-invalid-error.svg'
    Then error RSC-005 is reported
    And the message contains '"id" is invalid'
    And no other errors or warnings are reported

  ### Style Attribute

  Scenario: Verify `style` element without explicit `type` (issue 688)
    When checking document 'style-no-type-valid.svg'
    Then no errors or warnings are reported
  
  ###  RDF

  Scenario: Verify RDF elements can be embedded in SVG
    When checking document 'rdf-valid.svg'
    Then no errors or warnings are reported


  ##  3.3 Restrictions on SVG

  Scenario: Verify that `foreignObject` conforming to the rules is allowed
    When checking document 'foreignObject-valid.svg'
    Then no errors or warnings are reported

  Scenario: Verify that the `requiredExtensions` attribute can have any value
    Note: 'requiredExensions' was required to be set to 'http://www.idpf.org/2007/ops' in EPUB 3.2 and earlier versions  
    When checking document 'foreignObject-requiredExtensions-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report `foreignObject` with non-HTML child content 
    When checking document 'foreignObject-not-html-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "foo" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report `foreignObject` with non-flow content
    When checking document 'foreignObject-not-flow-content-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "title" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report `foreignObject` with multiple HTML `body` elements
    When checking document 'foreignObject-multiple-body-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "body" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report HTML validation errors within `foreignObject` content
    When checking document 'foreignObject-html-invalid-error.svg'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify `title` can contain text
    When checking document 'title-text-valid.svg'
    Then no errors or warnings are reported

  Scenario: Verify `title` can contain HTML phrasing content
    When checking document 'title-phrasing-content-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report `title` with non-phrasing content
    When checking document 'title-not-phrasing-content-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "h1" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report HTML validation errors within `title` content
    When checking document 'title-html-invalid-error.svg'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported
