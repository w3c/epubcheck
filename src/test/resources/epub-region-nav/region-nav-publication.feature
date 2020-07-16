Feature: EPUB Region-Based Navigation ▸ Full Publication Checks


  Checks conformance to the EPUB Region-Based Navigation 1.0 specification:
    http://idpf.org/epub/renditions/region-nav/

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub-region-nav/files/epub/'
    And EPUBCheck with default settings


  #  2. The Data Navigation Document

  Scenario: Verify a basic data nav file
    When checking EPUB 'data-nav-valid'
    Then no errors or warnings are reported


  ##  2.2 Content Conformance

  Scenario: Report a data nav file that is not encoded as `application/xhtml+xml`
    When checking EPUB 'data-nav-not-xhtml-error'
    Then error OPF-012 is reported
    And no other errors or warnings are reported


  ##  2.4 Data Navigation Document Definition

  Scenario: Report a data nav file included in the spine
    When checking EPUB 'data-nav-in-spine-warning'
    Then warning OPF-077 is reported
    And no other errors or warnings are reported

  Scenario: Report a data nav with an unidentified `nav` element in it
    When checking EPUB 'data-nav-missing-type-error'
    Then error RSC-005 is reported
    And the message contains 'A "nav" element in a Data Navigation Document must have an "epub:type" attribute'
    And no other errors or warnings are reported


  ##  2.5 Identification

  Scenario: Report the inclusion of more than one data nav file
    When checking EPUB 'data-nav-multiple-error'
    Then error RSC-005 is reported
    And the message contains 'The manifest must not include more than one Data Navigation Document'
    And no other errors or warnings are reported


  #  3. Region-based Navigation

  Scenario: Verify a data nav that defines region-based navgiation 
    When checking EPUB 'region-based-nav-valid'
    Then no errors or warnings are reported


  ##  3.4 The region-based nav Element

  ### 3.4.2 Content Model  

  Scenario: Report region-based navigation not defined on a `nav` element
    When checking EPUB 'region-based-nav-wrong-element-error'
    Then error HTM-052 is reported
    And no other errors or warnings are reported

  Scenario: Report a region-based `nav` element that does not point to fixed-layout documents
    When checking EPUB 'region-based-nav-not-fxl-error'
    Then error NAV-009 is reported
    And no other errors or warnings are reported

  Scenario: Report a region-based `nav` element with an invalid content model
    When checking EPUB 'region-based-nav-content-model-error'
    Then warning RSC-017 is reported
    And the message contains '"a" elements in region-based navs should not contain text labels'
    And the following errors are reported
    | RSC-005 | A region-based nav element must contain exactly one child ol element |
    | RSC-005 | The first child of a region-based nav list item must be either an "a" or "span" element |
    | RSC-005 | "span" elements in region-base navs must contain exactly two "a" elements |
    | RSC-005 | "span" elements in region-base navs must contain exactly two "a" elements |
    | RSC-005 | The first child of a region-based nav list item can only be followed by a single "ol" element |
    | RSC-005 | The first child of a region-based nav list item can only be followed by a single "ol" element |
    | RSC-005 | The first child of a region-based nav list item must be either an "a" or "span" element |
    And no other errors or warnings are reported


  ###  3.4.4 Subregion Navigation

  Scenario: Verify subregion navigation using comics semantics
    When checking EPUB 'region-based-nav-comics-valid'
    Then no errors or warnings are reported
