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

  Scenario: test that a toc nav in reading order is conforming
    When checking EPUB 'valid/nav-toc-reading-order'
    Then no errors or warnings are reported

  Scenario: test that toc nav links MUST be in spine order 
    When checking EPUB 'invalid/nav-toc-unordered-spine'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: test that toc nav links MUST be in document order
    When checking EPUB 'invalid/nav-toc-unordered-fragments'
    Then warning NAV_011 is reported 2 times
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: test that a page-list nav in reading order is conforming
    When checking EPUB 'valid/nav-page-list-reading-order'
    Then no errors or warnings are reported

  Scenario: test that page-list nav links MUST be in spine order 
    When checking EPUB 'invalid/nav-page-list-unordered-spine'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: test that page-list nav links MUST be in document order
    When checking EPUB 'invalid/nav-page-list-unordered-fragments'
    Then warning NAV-011 is reported
    And info INF-001 is reported
    And no other errors or warnings are reported

  Scenario: testValidateNav_TocMissing
    When checking EPUB 'invalid/nav-toc-missing/'
    Then error RSC-005 is reported
    And the message contains ''
    And no other errors or warnings are reported

  Scenario: testValidateNav_LinksOutOfSpine
    When checking EPUB 'invalid/nav-links-out-of-spine/'
    Then error RSC-011 is reported
    And no other errors or warnings are reported

  Scenario: testValidateNav_LinksRemote
    When checking EPUB 'invalid/nav-links-remote/'
    Then error NAV_010 is reported 3 times
    And no other errors or warnings are reported

