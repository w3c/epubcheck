Feature: EPUB for Education ▸ Full Publication Checks


  Checks conformance to the EPUB for Education specification:
    http://idpf.org/epub/profiles/edu/spec/

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub-edupub/files/epub/'
    And EPUBCheck configured with the 'edupub' profile


  ##  3. Education Document Models

  ###  3.1 Reflowable Publications

  Scenario: Verify a basic edupub publication
    When checking EPUB 'edupub-basic-valid'
    Then no errors or warnings are reported


  ###  3.2 Fixed-Layout Publications

  Scenario: Verify an edupub publication with fixed-layout documents
    When checking EPUB 'edupub-fxl-valid'
    Then no errors or warnings are reported

  ### 3.3 Multiple-Rendition Publications

  Scenario: Verify an edupub publication with multiple renditions
    When checking EPUB 'edupub-multiple-renditions-valid'
    Then no errors or warnings are reported

  Scenario: Report a missing publication-level 'dc:type' for edupub publication with multiple renditions
    When checking EPUB 'edupub-multiple-renditions-dctype-missing-for-publication-error'
    Then error RSC-005 is reported
    And the message contains 'A dc:type element with the value "edupub" is required'
    And no other errors or warnings are reported

  Scenario: Report a missing rendition-level 'dc:type' for edupub publication with multiple renditions
    When checking EPUB 'edupub-multiple-renditions-dctype-missing-for-rendition-error'
    Then error RSC-005 is reported
    And the message contains 'The dc:type identifier "edupub" is required'
    And no other errors or warnings are reported


  #  4. Content Structure

  ##  4.2 Sectioning

  Scenario: Verify an non-linear content does not have to follow the sectioning rules
    When checking EPUB 'edupub-non-linear-valid'
    Then no errors or warnings are reported


  ##  4.5 Semantic Enrichment

  Scenario: Report an edupub publication with microdata attributes
    When checking EPUB 'edupub-microdata-warning'
    Then warning HTM-051 is reported
    And no other errors or warnings are reported


  ##  4.6 Pagination

  Scenario: Verify an edupub publication with a page list
    When checking EPUB 'edupub-pagelist-valid'
    Then no errors or warnings are reported

  Scenario: Report an edupub publication missing a page list
    When checking EPUB 'edupub-pagelist-missing-error'
    Then error NAV-003 is reported
    And no other errors or warnings are reported

  Scenario: Report an edupub publication with a page list but the source of the pagination is not identified
    When checking EPUB 'edupub-pagelist-no-source-error'
    Then error OPF-066 is reported
    And no other errors or warnings are reported
