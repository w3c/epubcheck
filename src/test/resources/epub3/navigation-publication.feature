Feature: EPUB 3 ▸ Navigation Document ▸ Full Publication Checks


  Checks conformance to the "EPUB Navigation Document" section of the
  EPUB Packages 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ##  5.4 EPUB Navigation Document Definition

  Scenario: Report schema errors when checking a Navigation Document in a full publication
    When checking EPUB 'nav-toc-missing-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  ###  5.4.1 The nav Element: Restrictions

  Scenario: Report a `toc nav` that links to documents not in the spine
    When checking EPUB 'nav-links-out-of-spine-error'
    Then error RSC-011 is reported
    And no other errors or warnings are reported

  Scenario: Report external links in the `toc`, `page-list` and `landmarks` `nav` elements
    When checking EPUB 'nav-links-remote-error'
    Then error NAV-010 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` that links to resource that is not a Content Document
    When checking EPUB 'nav-links-to-non-content-document-type-error'
    Then error RSC-010 is reported
    And no other errors or warnings are reported


  ###  5.4.2 The nav Element: Types

  ####  5.4.2.2 The toc nav Element 

  Scenario: Verify a `toc nav` with links that match the reading order
    When checking EPUB 'nav-toc-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Report a `toc nav` whose links do not match the spine order 
    When checking EPUB 'nav-toc-unordered-spine-warning'
    Then warning NAV-011 is reported
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` whose link fragments do match the document order
    When checking EPUB 'nav-toc-unordered-fragments-warning'
    Then warning NAV-011 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report as a USAGE a `toc nav` which does not link to all spine items
    Given the reporting level set to USAGE
    When checking EPUB 'nav-toc-missing-references-to-spine-valid'
    Then no errors or warnings are reported
    And usage OPF-059 is reported



  ####  5.4.2.3 The page-list nav Element 

  Scenario: Verify that a `page-list nav` with links that match the reading order
    When checking EPUB 'nav-page-list-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Verify a `page-list nav` whose links do not match the spine order 
    When checking EPUB 'nav-page-list-unordered-spine-warning'
    And no other errors or warnings are reported

  Scenario: Verify a `page-list nav` whose links do match the document order
    When checking EPUB 'nav-page-list-unordered-fragments-warning'
    And no other errors or warnings are reported


  ## Other

  Scenario: Verify a Navigation Document using EPUB CFI
    When checking EPUB 'nav-cfi-valid'
    Then no errors or warnings are reported
