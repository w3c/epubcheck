Feature: EPUB Dictionaries and Glossaries ▸ Full Publication Checks


  Checks conformance to the EPUB Dictionaries and Glossaries 1.0 specification:
    http://idpf.org/epub/dict/

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub-dictionaries/files/epub/'
    And EPUBCheck configured with the 'dict' profile


  ##  2.2 Content Documents - Dictionaries

  Scenario: Report a dictionary that does not meet the content model requirements
    When checking EPUB 'dictionary-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'A "dictionary" must have at least one article child'
    And error RSC-005 is reported
    And the message contains 'A dictionary entry must have at least one "dfn" descendant'
    And no other errors or warnings are reported

  ##  2.4 Search Key Map Documents

  ###  2.4.2 Content Conformance

  Scenario: Report a search key map file that does not have an `.xml` extension
    When checking EPUB 'dictionary-search-key-map-extension-error'
    Then warning OPF-080 is reported
    And no other errors or warnings are reported


  ###  2.4.4 Search Key Map Document Definition
  
  ####  2.4.4.1 The search-key-map Element 

  Scenario: Report a dictionary search key map with an invalid content model
    When checking EPUB 'dictionary-search-key-map-content-error'
    Then error RSC-005 is reported
    And the message contains 'element "search-key-map" incomplete'
    And no other errors or warnings are reported


  ####  2.4.4.2 The search-key-group Element 

  Scenario: Report a link to a missing resource
    When checking EPUB 'dictionary-search-key-map-link-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a link to a CSS file instead of a content document
    When checking EPUB 'dictionary-search-key-map-link-css-error'
    Then error RSC-021 is reported
    And no other errors or warnings are reported


  ##  2.5 Identification of the Dictionary or Glossary in the Package Document

  ####  2.5.1.1 Identification with dc:type

  Scenario: Report a dictionary that does not identify itself in a `dc:type` element
    When checking EPUB 'dictionary-dc-type-missing-error'
    Then error RSC-005 is reported
    And the message contains 'The dc:type identifier "dictionary" is required.'
    Then warning OPF-079 is reported
    And no other errors or warnings are reported

  Scenario: Report a dictionary that is not processed using the 'dict' profile but is detected as a dictionary from a content document's `epub:type` value
    Given EPUBCheck configured with the default profile
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

  ### 2.5.2 Glossary Identification

  Scenario: Verify a publication with a single glossary
    Given EPUBCheck configured with the 'default' profile
    When checking EPUB 'glossary-single-valid'
    Then no errors or warnings are reported

  Scenario: Verify the 'glossary' manifest item property is not mandatory in the default checking profile
    Note: we cannot check that the property is mandatory in EPUB Glossaries, as there is no dedicated profile for these
    Given EPUBCheck configured with the 'default' profile
    When checking EPUB 'glossary-single-package-property-not-defined-error'
    Then no errors or warnings are reported
