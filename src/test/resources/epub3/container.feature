Feature: EPUB 3 Open Container Format
  
  Checks conformance to specification rules related to EPUB Open Container Format:
  https://www.w3.org/publishing/epub32/epub-ocf.html
  
  This feature file contains tests for EPUBCheck running in defaut mode to check
  full EPUB publications.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings

  Scenario: Encryption with invalid compression metadata
    When checking EPUB 'ocf-encryption-compression-invalid'
    Then the following errors are reported
      | RSC-005 | value of attribute "Method" is invalid         |
      | RSC-005 | value of attribute "OriginalLength" is invalid |
