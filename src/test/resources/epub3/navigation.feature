Feature: EPUB 3 Navigation
  
  Checks conformance to specification rules related to EPUB 3 navigation:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note: Tests that do not require a full publication but a single Navigation
        Document are defined in the `navigation-document.feature` feature file.   
  
  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ##  5.4 EPUB Navigation Document Definition

  ###  5.4.1 The nav Element: Restrictions

  Scenario: Report a `toc nav` that links to documents not in the spine
    When checking EPUB 'nav-links-out-of-spine-error'
    Then error RSC-011 is reported
    And no other errors or warnings are reported

  Scenario: Report external links in the `toc`, `page-list` and `landmarks` `nav` elements
    When checking EPUB 'nav-links-remote-error'
    Then error NAV-010 is reported 3 times
    And no other errors or warnings are reported


  ###  5.4.2 The nav Element: Types

  ####  5.4.2.2 The toc nav Element 

  Scenario: Verify a `toc nav` with links that match the reading order
    When checking EPUB 'nav-toc-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Report a `toc nav` whose links do not match the spine order 
    When checking EPUB 'nav-toc-unordered-spine-warning'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` whose link fragments do match the document order
    When checking EPUB 'nav-toc-unordered-fragments-warning'
    Then warning NAV-011 is reported 2 times
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: Report a publication without a `toc nav` in its navigation document
    When checking EPUB 'nav-toc-missing-error'
    Then error RSC-005 is reported
    And the message contains ''
    And no other errors or warnings are reported


  ####  5.4.2.3 The page-list nav Element 

  Scenario: Verify that a `page-list nav` with links that match the reading order
    When checking EPUB 'nav-page-list-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Report a `page-list nav` whose links do not match the spine order 
    When checking EPUB 'nav-page-list-unordered-spine-warning'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: Report a `page-list nav` whose links do match the document order
    When checking EPUB 'nav-page-list-unordered-fragments-warning'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported
