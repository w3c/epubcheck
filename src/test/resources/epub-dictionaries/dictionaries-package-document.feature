Feature: EPUB Dictionaries and Glossaries ▸ Package Document Checks


  Checks conformance to the EPUB Dictionaries and Glossaries 1.0 specification:
    http://idpf.org/epub/dict/

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub-dictionaries/files/package-document/'
    And EPUBCheck configured to check a Package Document
    And EPUBCheck configured with the 'dict' profile


  ## 2.5 Identification of the Dictionary or Glossary in the Package Document
  
  ## 2.5.1.1 Identification with dc:type

  Scenario: An EPUB Dictionary publication must have a 'dictionary' `dc:type` property
    When checking file 'dictionary-metadata-type-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'dc:type identifier "dictionary" is required'
    Then no other errors or warnings are reported
  
  ## 2.5.1.2 Publication Consisting of a Single EPUB Dictionary

  Scenario: A publication with single EPUB Dictionary is valid
    When checking file 'dictionary-single-valid.opf'
    Then no errors or warnings are reported

  Scenario: A single EPUB Dictionary must declare a search key map
    When checking file 'dictionary-single-skm-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "must contain exactly one Search Key Map document"
    Then no other errors or warnings are reported

  Scenario: A single EPUB Dictionary search key map must have the 'dictionary' property
    When checking file 'dictionary-single-skm-property-dictionary-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains '"dictionary"'
    Then no other errors or warnings are reported

  ## 2.5.1.3 Publication Consisting of Multiple EPUB Dictionaries

  Scenario: A dictionary collection can be used to define multiple EPUB Dictionaries
    When checking file 'dictionary-collection-valid.opf'
    Then no errors or warnings are reported

  Scenario: A dictionary collection must contain a search key map
    When checking file 'dictionary-collection-skm-missing-error.opf'
    Then error OPF-083 is reported
    And the message contains "no Search Key Map Document"
    Then no other errors or warnings are reported

  Scenario: A dictionary collection must not contain a search key map used by another collection
    When checking file 'dictionary-collection-skm-shared-error.opf'
    Then error RSC-005 is reported
    And the message contains "is referenced in more than one dictionary collection"
    Then no other errors or warnings are reported

  Scenario: A dictionary collection must not contain more than one search key map
    When checking file 'dictionary-collection-skm-multiple-error.opf'
    Then error OPF-082 is reported
    And the message contains "more than one Search Key Map Document"
    Then no other errors or warnings are reported

  Scenario: A dictionary collection must not contain child collections
    When checking file 'dictionary-collection-subcollection-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not have sub-collections"
    Then no other errors or warnings are reported

  Scenario: A dictionary collection must not contain resources not in the manifest
    When checking file 'dictionary-collection-resource-missing-error.opf'
    Then error OPF-081 is reported
    And the message contains "was not found"
    Then no other errors or warnings are reported

  Scenario: A dictionary collection must not contain resources other than XHTML Content Documents and Search Key Map
    When checking file 'dictionary-collection-resource-not-xhtml-error.opf'
    Then error OPF-084 is reported
    And the message contains "neither a Search Key Map Document nor an XHTML Content Document"
    Then no other errors or warnings are reported
    
  ## 2.5.1.4 Additional Dictionary Metadata
  
  ## 2.5.1.4.1 Identifying the Dictionary Type
  
  Scenario: A dictionary-type property `monolingual` is valid
    When checking file 'dictionary-metadata-dictionary-type-monolingual-valid.opf'
    Then no errors or warnings are reported

  Scenario: A dictionary-type property with an unknown value is reported
    When checking file 'dictionary-metadata-dictionary-type-unknown-error.opf'
    Then error RSC-005 is reported
    And the message contains '"dictionary-type" metadata must be one of'
    And no other errors or warnings are reported

  ## 2.5.1.4.3 Identifying Source and Target Languages

  Scenario: The source language of a single-dictionary publication must be defined
    When checking file 'dictionary-metadata-languages-missing-source-error.opf'
    Then error RSC-005 is reported
    And the message contains "must declare its source language"
    And no other errors or warnings are reported

  Scenario: The source language of a single-dictionary publication must not be defined more than once
    When checking file 'dictionary-metadata-languages-multiple-source-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not declare more than one source language"
    And no other errors or warnings are reported

  Scenario: The target language of a single-dictionary publication must one of the declared 'dc:language' values 
    When checking file 'dictionary-metadata-languages-undeclared-lang-target-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must also be declared as "dc:language"'
    And no other errors or warnings are reported

  Scenario: The source/target languages of a multiple-dictionary publication can be defined in the collections
    When checking file 'dictionary-metadata-languages-collection-valid.opf'
    Then no errors or warnings are reported

  Scenario: The target language of a dictionary collection must be defined
    When checking file 'dictionary-metadata-languages-collection-missing-target-error.opf'
    Then error RSC-005 is reported
    And the message contains "must declare its source language"
    And no other errors or warnings are reported

  Scenario: The source language of a dictionary collection must not be defined more than once
    When checking file 'dictionary-metadata-languages-collection-multiple-source-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not declare more than one source language"
    And no other errors or warnings are reported

  Scenario: The target language of a dictionary collection must one of the declared 'dc:language' values
    When checking file 'dictionary-metadata-languages-collection-undeclared-lang-target-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must also be declared as "dc:language"'
    And no other errors or warnings are reported
    
  ## 2.5.3 Manifest item Properties

  Scenario: A Search Key Map document must have the correct media type
    When checking file 'dictionary-item-property-skm-mediatype-unknown-error.opf'
    Then error OPF-012 is reported
    And the message contains 'property "search-key-map" is not defined for media type'
    Then no other errors or warnings are reported
