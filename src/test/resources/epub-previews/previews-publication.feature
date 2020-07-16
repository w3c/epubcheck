Feature: EPUB Previews ▸ Full Publication Checks


  Checks conformance to the EPUB Previews 1.0 specification:
    http://idpf.org/epub/previews/

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub-previews/files/epub/'
    And EPUBCheck with default settings


  #  2. Preview Publications

  Scenario: Verify a preview publication
    When checking EPUB 'preview-pub-valid'
    Then no errors or warnings are reported


  ##  2.4 Preview Identification

  Scenario: Report a preview publication that does not identify itself in a `dc:type` element
    Given EPUBCheck configured with the 'preview' profile
    When checking EPUB 'preview-pub-dc-type-missing-error'
    Then error RSC-005 is reported
    And the message contains 'An EPUB Preview publication must have a "preview" dc:type'
    And no other errors or warnings are reported


  ##  2.5 Identifiers

  Scenario: Report a preview pubication that does not identify its source publication
    Given EPUBCheck configured with the 'preview' profile
    When checking EPUB 'preview-pub-source-missing-warning'
    Then warning RSC-017 is reported
    And the message contains 'An EPUB Preview publication should link back to its source Publication'
    And no other errors or warnings are reported

  Scenario: Report a preview publication that uses its own identifier as the source publication
    Given EPUBCheck configured with the 'preview' profile
    When checking EPUB 'preview-pub-self-as-source-error'
    Then error RSC-005 is reported
    And the message contains 'A Preview Publication must not use the same package identifier as its source Publication'
    And no other errors or warnings are reported


  #  3. Embedded Previews

  Scenario: Verify an embedded preview
    When checking EPUB 'preview-embedded-valid'
    Then no errors or warnings are reported


  ##  3.4 Preview Collections

  Scenario: Report an embedded preview that does not have a manifest
    When checking EPUB 'preview-embedded-no-manifest-error'
    Then error RSC-005 is reported
    And the message contains 'must include exactly one child "manifest" collection'
    And no other errors or warnings are reported

  Scenario: Report an embedded preview without any links to preview content
    When checking EPUB 'preview-embedded-no-links-error'
    Then error RSC-005 is reported
    And the message contains 'must include at least one child "link" element'
    And no other errors or warnings are reported

  Scenario: Report an embedded preview without that links to non-xhtml content documents
    When checking EPUB 'preview-embedded-no-content-doc-link-error'
    Then error OPF-075 is reported
    And no other errors or warnings are reported

  Scenario: Report an embedded preview that uses EPUB CFIs in the links to xhtml content documents
    When checking EPUB 'preview-embedded-link-cfi-error'
    Then error OPF-076 is reported
    And no other errors or warnings are reported
