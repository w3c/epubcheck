Feature: EPUB 3 — Content Documents — SVG


  Checks conformance to the "SVG content documents" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-svg


  Background: 
    Given EPUB test files located at '/epub3/06-content-document/files/'
    And EPUBCheck with default settings


  ##  6.2 SVG Content Documents

  @spec @xref:sec-spine-elem
  Scenario: Verify that SVG Content Documents can be referenced in the spine
    When checking EPUB 'content-svg-in-spine-valid'
    Then no errors or warnings are reported

  Scenario: Verify that the need for a `viewbox` declaration does not apply to non-fixed layout SVGs
    When checking EPUB 'content-svg-no-viewbox-not-fxl-valid'
    Then no errors or warnings are reported

  Scenario: Report SVG `use` elements that don't point to a document fragment
    When checking EPUB 'content-svg-use-href-no-fragment-error'
    Then error RSC-015 is reported
    And no other errors or warnings are reported

  Scenario: Verify that an SVG Content Document can have any extension
    When checking EPUB 'content-svg-file-extension-unusual-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-svg-req
  Scenario: Verify that SVG validation erors are reported as USAGE
    Given the reporting level set to usage
    When checking document 'svg-invalid-usage.svg'
    Then usage RSC-025 is reported
    And the message contains 'element "foo" not allowed here'
    And no other errors or warnings are reported

	@spec @xref:sec-svg-req
  Scenario: Allow `epub:type` on structural, shape, and text elements
    Given the reporting level is set to USAGE
    When checking document 'epubtype-valid.svg'
    Then no other errors or warnings are reported

	@spec @xref:sec-svg-req
  Scenario: Report an `epub:type` attribute used on an element where it is not allowed
    When checking document 'epubtype-not-allowed-error.svg'
    Then error RSC-005 is reported 5 times
    And the message contains '"epub:type" not allowed'
    And no other errors or warnings are reported

  
  ### 6.2.2 SVG requirements

  ####  ARIA attributes

  Scenario: Verify ARIA attributes are allowed
    When checking document 'aria-attributes-valid.svg'
    Then no errors or warnings are reported

  #### Custom Namespaces

  Scenario: Verify elements from custom namespaces are allowed
    When checking document 'ns-custom-valid.svg'
    Then no errors or warnings are reported

  ####  Data Attributes

  Scenario: Verify `data-*` attributes are allowed
    When checking document 'data-attribute-valid.svg'
    Then no errors or warnings are reported

  ####  Fonts

  Scenario: Verify that empty `font-face` declarations are allowed
    When checking document 'font-face-empty-valid.svg'
    Then no errors or warnings are reported

  ####  Hyperlinks

  Scenario: Verify links are allowed
    When checking document 'link-valid.svg'
    Then no errors or warnings are reported

  Scenario: Report SVG link without a label as usage
    Given the reporting level is set to usage
    When checking document 'link-label-valid.svg'
    Then usage ACC-011 is reported
    And no other usages are reported
    And no other errors or warnings are reported

  Scenario: Verify that `image` elements can have an `xlink:href` URL pointing to a fragment 
    When checking document 'image-fragment-valid.svg'
    Then no errors or warnings are reported

  #### Identifiers

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

  #### Style Attribute

  Scenario: Verify `style` element without explicit `type` (issue 688)
    When checking document 'style-no-type-valid.svg'
    Then no errors or warnings are reported
  
  ####  RDF

  Scenario: Verify RDF elements can be embedded in SVG
    When checking document 'rdf-valid.svg'
    Then no errors or warnings are reported


  ###  6.2.3 Restrictions on SVG

	#### `foreignObject` element

  @spec @xref:sec-svg-restrictions
  Scenario: Verify that `foreignObject` conforming to the rules is allowed
    When checking document 'foreignObject-valid.svg'
    Then no errors or warnings are reported

  Scenario: Verify that the `requiredExtensions` attribute can have any value
    Note: 'requiredExensions' was required to be set to 'http://www.idpf.org/2007/ops' in EPUB 3.2 and earlier versions  
    When checking document 'foreignObject-requiredExtensions-valid.svg'
    Then no errors or warnings are reported

  @spec @xref:sec-svg-restrictions
  Scenario: Report `foreignObject` with non-HTML child content 
    When checking document 'foreignObject-not-html-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "foo" not allowed here'
    And no other errors or warnings are reported

  @spec @xref:sec-svg-restrictions
  Scenario: Report `foreignObject` with non-flow content
    When checking document 'foreignObject-not-flow-content-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "title" not allowed here'
    And no other errors or warnings are reported

  @spec @xref:sec-svg-restrictions
  Scenario: Report `foreignObject` with multiple HTML `body` elements
    When checking document 'foreignObject-multiple-body-error.svg'
    Then error RSC-005 is reported
    And the message contains 'element "body" not allowed here'
    And no other errors or warnings are reported
    
  @spec @xref:sec-svg-restrictions
  Scenario: Report HTML validation errors within `foreignObject` content
    When checking document 'foreignObject-html-invalid-error.svg'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    And no other errors or warnings are reported

	#### `title` element

  @spec @xref:sec-svg-restrictions
  Scenario: Verify `title` valid content model
    When checking document 'title-content-valid.svg'
    Then no errors or warnings are reported

  @spec @xref:sec-svg-restrictions
  Scenario: Report `title` with non-HTML elements
    When checking document 'title-content-not-html-error.svg'
    Then error RSC-005 is reported
    And the message contains 'elements from namespace "https://example.org" are not allowed'
    Then error RSC-005 is reported
    And the message contains 'elements from namespace "http://www.w3.org/2000/svg" are not allowed'
    And no other errors or warnings are reported
    
  @spec @xref:sec-svg-restrictions
  Scenario: Report HTML validation errors within `title` content
    When checking document 'title-content-invalid-html-error.svg'
    Then error RSC-005 is reported
    And the message contains 'attribute "href" not allowed here'
    Then error RSC-005 is reported
    And the message contains 'element "body" not allowed here'
    And no other errors or warnings are reported

