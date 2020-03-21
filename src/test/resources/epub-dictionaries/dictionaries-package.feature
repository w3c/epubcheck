Feature: EPUB 3 Dictionaries and Glossaries
  
  Checks conformance to specification rules related to EPUB Dictionaries and Glossaries:
  http://idpf.org/epub/dict/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note:
  - Tests that do not require a full publication but a single Package Document
    are defined in the `dictionaries-package-document.feature` feature file.   

  Background: 
    Given EPUB test files located at '/epub-dictionaries/files/epub/'
    And EPUBCheck configured with the 'dict' profile

  ##  2.4 Search Key Map Documents

  ###  2.4.2 Content Conformance

  Scenario: Report a search key map file that does not have an `.xml` extension
    When checking EPUB 'dictionary-search-key-map-extension-error'
    Then warning OPF-080 is reported
    And no other errors or warnings are reported


  ##  2.5 Identification of the Dictionary or Glossary in the Package Document

  ####  2.5.1.1 Identification with dc:type

  Scenario: Report a dictionary that does not identify itself in a `dc:type` element
    When checking EPUB 'dictionary-dc-type-missing-error'
    Then error RSC-005 is reported
    And the message contains "The dc:type identifier 'dictionary' is required."
    Then warning OPF-079 is reported
    And no other errors or warnings are reported

  Scenario: Report a dictionary that is not processed using the 'dict' profile but is detected as a dictionary from a content document's `epub:type` value
    Given EPUBCheck configured with default settings
    When checking EPUB 'dictionary-no-profile-dc-type-missing-warning'
    Then warning OPF-079 is reported
    And no other errors or warnings are reported


  ####  2.5.1.2 Publication Consisting of a Single EPUB Dictionary 

  Scenario: Verify a publication with a single dictionary
    When checking EPUB 'dictionary-single-valid'
    Then no errors or warnings are reported

  Scenario: Report a single dictionary without any content documents with dictionary content
    When checking EPUB 'dictionary-single-no-content-error'
    Then error OPF-078 is reported
    And no other errors or warnings are reported


  ####  2.5.1.3 Publication Consisting of Multiple EPUB Dictionaries

  Scenario: Verify a publication with multiple dictionaries
    When checking EPUB 'dictionary-multiple-valid'
    Then no errors or warnings are reported

  Scenario: Report multiple dictionaries without any content documents with dictionary content
    When checking EPUB 'dictionary-multiple-no-content-error'
    Then error OPF-078 is reported
    And no other errors or warnings are reported
