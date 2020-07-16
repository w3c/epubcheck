Feature: EPUB Multiple-Rendition ▸ Full Publication Checks


  Checks conformance to the EPUB Multiple-Rendition Publications 1.0 specification:
    http://idpf.org/epub/renditions/multiple/

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub-multiple-renditions/files/epub/'
    And EPUBCheck with default settings


  ## 1. Overview
  
  Scenario: Verify basic multiple rendition publication
    When checking EPUB 'renditions-basic-valid'
    Then no errors or warnings are reported


  ## 2. Publication Metadata

  Scenario: Report a multiple-rendition publication with no 'metadata.xml' file
    When checking EPUB 'renditions-metadata-file-missing-warning'
    Then warning RSC-019 is reported
    And no other errors or warnings are reported

  Scenario: Report an incomplete identifier in multiple-rendition 'metadata.xml' file
    When checking EPUB 'renditions-metadata-identifier-incomplete-error'
    Then error RSC-005 is reported
    And the message contains 'dcterms:modified meta element must occur exactly once'
    And no other errors or warnings are reported

  ## 3. Rendition Selection

  Scenario: Report a media query syntax error in the 'rendition:media' rendition selection attribute
    When checking EPUB 'renditions-selection-mediaquery-syntax-error'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "rendition:media" is invalid'
    And no other errors or warnings are reported

  Scenario: Report an unknown rendition selection attribute
    When checking EPUB 'renditions-selection-attribute-unknown-error'
    Then error RSC-005 is reported
    And the message contains 'attribute "rendition:unknown" not allowed here'
    And no other errors or warnings are reported

  Scenario: Report a non-primary rootfile element without a rendition selection attribute
    When checking EPUB 'renditions-selection-attribute-missing-error'
    Then warning RSC-017 is reported
    And the message contains 'At least one rendition selection attribute should be specified'
    And no other errors or warnings are reported

  ## 4. Rendition Mapping

  ### 4.4 EPUB Rendition Mapping Document Definition

  Scenario: Verify a rendition mapping document with multiple nav elements
    When checking EPUB 'renditions-mapping-multiple-nav-valid'
    Then no errors or warnings are reported


  #### 4.4.4 Container Identification
  
  Scenario: Report a mapping document that is not identified as XHTML in the container document 
    When checking EPUB 'renditions-mapping-non-xhtml-error'
    Then error RSC-005 is reported
    And the message contains 'The media type of Rendition Mapping Documents must be "application/xhtml+xml"'
    And no other errors or warnings are reported

  Scenario: Report a container with more than one mapping document
    When checking EPUB 'renditions-mapping-multiple-docs-error'
    Then error RSC-005 is reported
    And the message contains 'The Container Document must not reference more than one mapping document.'
    # FIXME #1115 this warning probably shouldn't be raised as the document is declared
    And warning OPF-003 is reported
    And the message contains 'is not declared in the OPF manifest'
    And no other errors or warnings are reported

  Scenario: Report a container with multiple renditions but missing all the core identifying features
    When checking EPUB 'renditions-unmanifested-warning'
    Then warning RSC-019 is reported (for the missing metatada.xml file)
    And warning RSC-017 is reported (for the missing selection attributes)
    And the message contains 'At least one rendition selection attribute should be specified for each non-first rootfile element'
    And warning OPF-003 is reported (for a mapping document without a link in the container.xml file)
    And no other errors or warnings are reported


  #### 4.4.1 XHTML Content Document: Restrictions
  
  Scenario: Report a mapping document without a version identifier
    When checking EPUB 'renditions-mapping-no-version-error'
    Then error RSC-005 is reported
    And the message contains 'A meta tag with the name "epub.multiple.renditions.version" and value "1.0" is required'
    And no other errors or warnings are reported

  Scenario: Report a mapping document without a resource map
    When checking EPUB 'renditions-mapping-no-resourcemap-error'
    Then error RSC-005 is reported
    And the message contains 'A Rendition Mapping Document must contain exactly one "resource-map" nav element'
    And no other errors or warnings are reported

  Scenario: Report a mapping document with an unknown nav type
    When checking EPUB 'renditions-mapping-untyped-nav-error'
    Then error RSC-005 is reported
    And the message contains 'A nav element of a Rendition Mapping Document must identify its nature in an epub:type attribute'
    And no other errors or warnings are reported
