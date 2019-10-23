Feature: EPUB 3 Navigation Document
  
  Checks conformance to specification rules defined for EPUB Navigation Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in `nav` mode to check
  single Navigation Documents (`.xhtml` files).
  
  Note: Tests related to EPUB navigation rules in a full EPUB publication are
        defined in the `navigation.feature` feature file.

  Background: 
    Given EPUBCheck configured to check a Navigation Document
    And test files located at '/epub3/files/navigation-document-xhtml/'

  Scenario: Minimal nav document
    # The mimeType of the nav document should be nav
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  Scenario: Nav document without epub:type
    When checking document 'invalid-nav-no-type.xhtml'
    Then warning RSC-017 is reported
    And no other errors or warnings are reported

  Scenario: Nav document nav001
    When checking document 'nav001.xhtml'
    Then no errors or warnings are reported

  Scenario: Nav document without a TOC nav, 1
    When checking document 'invalid-nav-no-toc-001.xhtml'
    Then error RSC-005 is reported
    And warning RSC-017 is reported
    And no other errors or warnings are reported

  Scenario: Nav document without a TOC nav, 2
    When checking document 'invalid-nav-no-toc-002.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document with invalid hidden attribute
    When checking document 'invalid-hidden-text.xhtml'
    Then the following errors are reported
      | RSC-005 | element "p" not allowed here; expected element "a" or "span" |
      | RSC-005 | element "ol" not allowed yet; expected element "a" or "span" |
      | RSC-005 | value of attribute "hidden" is invalid; must be equal to "" or "hidden" |
      | RSC-005 | element "li" incomplete; missing required element "ol" |
      | RSC-005 | Heading elements must contain text |
      | RSC-005 | Heading elements must contain text |
      | RSC-005 | Heading elements must contain text |
      | RSC-005 | Spans within nav elements must contain text |
    And no other errors or warnings are reported

  Scenario: Nav document has anchors that lack text
    When checking document 'invalid-nav-empty-anchor.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has anchors that lack text
    When checking document 'invalid-nav-empty-anchor-spans.xhtml'
    Then the following errors are reported
      | RSC-005 | Anchors within nav elements must contain text |
      | RSC-005 | Spans within nav elements must contain text |
    And no other errors or warnings are reported

  Scenario: Nav document has landmarks that lack epub:type
    When checking document 'invalid-nav-landmarks-missing-anchor-type.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has too many landmarks elements
    When checking document 'invalid-nav-landmarks-multiple.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has duplicate landmark elements
    When checking document 'invalid-nav-landmarks-duplicates.xhtml'
    Then error RSC-005 is reported
    And error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has anchors that lack text
    When checking document 'nav-landmarks-noduplicates.xhtml'
    Then no errors or warnings are reported

  Scenario: Nav document has multiple pagelist elements
    When checking document 'invalid-nav-multiple-pagelists.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has ordered list with no elements
    When checking document 'invalid-nav-empty-ol.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document lacks required headings
    When checking document 'invalid-nav-missing-required-headings.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Nav document has anchors that lack text
    When checking document 'invalid-nav-empty-anchor.xhtml'
    Then error RSC-005 is reported
    And no other errors or warnings are reported