Feature: EPUB 3 Open Container Format
  
  Checks conformance to specification rules related to EPUB Open Container Format:
  https://www.w3.org/publishing/epub32/epub-ocf.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ##  3.4 File Names

  Scenario: Verify a file name containing a `+` character is allowed (issue 188)
    When checking EPUB 'container-filename-character-plus-valid'
    Then no errors or warnings are reported


  ####  3.5.2.1 Container File (container.xml)

  Scenario: Report an unknown element in the `container.xml` file
    When checking EPUB 'container-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'element "foo" not allowed anywhere'
    And no other errors or warnings are reported


  ####  3.5.2.2 Encryption File (encryption.xml)

  Scenario: Report an `encryption.xml` file with invalid markup
    When checking EPUB 'encryption-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "encryption"'
    And no other errors or warnings are reported

  Scenario: Report an unknown encryption scheme
    When checking EPUB 'encryption-unknown-error'
    Then error RSC-004 is reported
    And no other errors or warnings are reported

  Scenario: Report an `encryption.xml` file with duplicate IDs
    When checking EPUB 'encryption-duplicate-ids-error'
    Then error RSC_005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported


  ####  3.5.2.6 Digital Signatures File (signatures.xml)  

  Scenario: Report a `signature.xml` file with invald markup
    When checking EPUB 'signatures-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "signatures"'
    And no other errors or warnings are reported



  ##  3.2 File and Directory Structure

  Scenario: Report a mimetype file with an incorrect value
    When checking EPUB 'mimetype-file-incorrect-value-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with leading spaces
    When checking EPUB 'mimetype-file-leading-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with a trailing newline
    When checking EPUB 'mimetype-file-trailing-newline-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with trailing spaces
    When checking EPUB 'mimetype-file-trailing-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported


  ####  3.5.2.2 Encryption File (encryption.xml)

  Scenario: Encryption with invalid compression metadata
    When checking EPUB 'ocf-encryption-compression-invalid'
    Then the following errors are reported
      | RSC-005 | value of attribute "Method" is invalid         |
      | RSC-005 | value of attribute "OriginalLength" is invalid |
