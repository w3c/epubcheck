Feature: EPUB 3 Dictionaries and Glossaries
  
  Checks conformance to specification rules related to EPUB Dictionaries and Glossaries:
  http://idpf.org/epub/dict/
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Background: 
    Given EPUB test files located at '/epub-dictionaries/files/epub/'
    And EPUBCheck configured with the 'dict' profile

  ##  2.2 Content Documents - Dictionaries

  Scenario: Report a dictionary that does not meet the content model requirements
    When checking EPUB 'dictionary-content-model-error'
    Then error RSC-005 is reported
    And the message contains "A 'dictionary' must have at least one article child"
    And error RSC-005 is reported
    And the message contains "A dictionary entry must have at least one 'dfn' descendant"
    And no other errors or warnings are reported

  
  ###  2.4.4 Search Key Map Document Definition
  
  ####  2.4.4.1 The search-key-map Element 

  Scenario: Report a dictionary search key map with an invalid content model
    When checking EPUB 'dictionary-search-key-map-content-error'
    Then error RSC-005 is reported
    And the message contains 'element "search-key-map" incomplete'
    And no other errors or warnings are reported


  ####  2.4.4.2 The search-key-group Element 

  Scenario: Reprot a  link to a missing resource
    When checking EPUB 'dictionary-search-key-map-link-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a link to a CSS file instead of a content document
    When checking EPUB 'dictionary-search-key-map-link-css-error'
    Then error RSC-021 is reported
    And no other errors or warnings are reported
