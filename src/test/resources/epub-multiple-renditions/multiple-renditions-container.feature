Feature: EPUB 3 Multiple Rendition Container
  
  Checks conformance to specification rules related to EPUB Multiple Rendition Container:
  http://idpf.org/epub/renditions/multiple/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Background: 
    Given EPUB test files located at '/epub-multiple-renditions/files/epub/'
    And EPUBCheck with default settings

  # 3. Rendition Selection
  
  Scenario: Verify basic multiple rendition publication
    When checking EPUB 'renditions-basic-valid'
    Then no errors or warnings are reported


  # 4. Rendition Mapping

  ## 4.4 EPUB Rendition Mapping Document Definition

  Scenario: Verify a rendition mapping document with multiple nav elements
    When checking EPUB 'renditions-mapping-multiple-nav-valid'
    Then no errors or warnings are reported


  ### 4.4.4 Container Identification
  
  Scenario: Report a mapping document that is not identified as XHTML in the container document 
    When checking EPUB 'renditions-mapping-non-xhtml-error'
    Then error RSC-005 is reported
    And the message contains 'The media type of Rendition Mapping Documents must be "application/xhtml+xml"'
    And no other errors or warnings are reported

  Scenario: Report a container with more than one mapping document
    When checking EPUB 'renditions-mapping-multiple-docs-error'
    Then error RSC-005 is reported
    And the message contains 'The Container Document must not reference more than one mapping document.'
    # this warning probably shouldn't be raised as the document is declared (see issue #1115)
    And warning OPF-003 is reported
    And the message contains 'is not declared in the OPF manifest'
    And no other errors or warnings are reported

  Scenario: Report a container with multiple renditions but missing all the core identifying features
    When checking EPUB 'renditions-unmanifested-error'
    # first error is for the missing metadata.xml file
    Then error RSC-019 is reported
    # second error is for the missing selection attributes
    And error RSC-017 is reported
    And the message contains 'At least one rendition selection attribute should be specified for each non-first rootfile element'
    # third error is for a mapping document without a link in the container.xml file
    And error OPF-003 is reported
    And no other errors or warnings are reported


  ### 4.4.1 XHTML Content Document: Restrictions
  
  Scenario: Report a mapping document without a version identifier
    When checking EPUB 'renditions-mapping-no-version-error'
    Then error RSC-005 is reported
    And the message contains "A meta tag with the name 'epub.multiple.renditions.version' and value '1.0' is required"
    And no other errors or warnings are reported

  Scenario: Report a mapping document without a resource map
    When checking EPUB 'renditions-mapping-no-resourcemap-error'
    Then error RSC-005 is reported
    And the message contains "A Rendition Mapping Document must contain exactly one 'resource-map' nav element"
    And no other errors or warnings are reported

  Scenario: Report a mapping document with an unknown nav type
    When checking EPUB 'renditions-mapping-untyped-nav-error'
    Then error RSC-005 is reported
    And the message contains 'A nav element of a Rendition Mapping Document must identify its nature in an epub:type attribute'
    And no other errors or warnings are reported
