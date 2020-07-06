Feature: EPUB 3 Open Container Format
  
  Checks conformance to specification rules related to EPUB Open Container Format:
  https://www.w3.org/publishing/epub32/epub-ocf.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings
    
	## 3. OCF Abstract Container

  ###  3.2 File and Directory Structure

  Scenario: Report a mimetype file with an incorrect value
    When checking EPUB 'ocf-mimetype-file-incorrect-value-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with leading spaces
    When checking EPUB 'ocf-mimetype-file-leading-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with a trailing newline
    When checking EPUB 'ocf-mimetype-file-trailing-newline-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a mimetype file with trailing spaces
    When checking EPUB 'ocf-mimetype-file-trailing-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported


  ###  3.4 File Names

  Scenario: Verify a file name containing a `+` character is allowed (issue 188)
    When checking EPUB 'ocf-container-filename-character-plus-valid'
    Then no errors or warnings are reported

  Scenario: Report a duplicate filename if two files only differ by case
    When checking EPUB 'ocf-filename-duplicate-after-case-normalization-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  Scenario: Report a duplicate ZIP entry for the same file
    When checking EPUB 'ocf-filename-duplicate-zip-entry-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  Scenario: Report a duplicate filename if two files have the same name after Unicode normalization
    When checking EPUB 'ocf-filename-duplicate-after-unicode-normalization-warning.epub'
    Then warning OPF-061 is reported
    And warning PKG-012 is reported 2 times (side effects of having non-ASCII characters in the file name)
    And no other errors or warnings are reported


  ###  3.5 META-INF Directory

  #####  3.5.2.1 Container File (container.xml)

  Scenario: Report an unknown element in the `container.xml` file
    When checking EPUB 'ocf-container-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'element "foo" not allowed anywhere'
    And no other errors or warnings are reported


  #####  3.5.2.2 Encryption File (encryption.xml)

  Scenario: Report an `encryption.xml` file with invalid markup
    When checking EPUB 'ocf-encryption-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "encryption"'
    And no other errors or warnings are reported

  Scenario: Report an unknown encryption scheme
    When checking EPUB 'ocf-encryption-unknown-error'
    Then error RSC-004 is reported
    And no other errors or warnings are reported

  Scenario: Report an `encryption.xml` file with duplicate IDs
    When checking EPUB 'ocf-encryption-duplicate-ids-error'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported

  Scenario: Report an `encryption.xml` file with invalid compression metadata
    When checking EPUB 'ocf-encryption-compression-invalid'
    Then the following errors are reported
      | RSC-005 | value of attribute "Method" is invalid         |
      | RSC-005 | value of attribute "OriginalLength" is invalid |


  #####  3.5.2.6 Digital Signatures File (signatures.xml)  

  Scenario: Report a `signature.xml` file with invald markup
    When checking EPUB 'ocf-signatures-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "signatures"'
    And no other errors or warnings are reported

  ## 4. OCF ZIP Container  

  Scenario: Verify a minimal packaged EPUB
    When checking EPUB 'minimal.epub'
    Then no errors or warnings are reported

  ### 4.3 OCF ZIP Container Media Type Idenfication

  Scenario: Report when the 'mimetype' entry has an extra field in its ZIP header
    When checking EPUB 'ocf-zip-mimetype-entry-extra-field-error.epub'
    Then error PKG-005 is reported
    And no other errors or warnings are reported


  ## 5. Resource Obfuscation
  
  Scenario: Verify a publication with obfuscated resource (here a font file)
    When checking EPUB 'ocf-obfuscation-valid'
    Then no errors or warnings are reported
    

  ## C. the 'application/epub+zip' Media Type
  
  Scenario: Report when the '.epub' extension is not lower case
    When checking EPUB 'ocf-extension-not-lower-case-warning.ePub'
    Then warning PKG-016 is reported
    And no other errors or warnings are reported