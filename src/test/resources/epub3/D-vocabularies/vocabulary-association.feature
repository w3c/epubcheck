Feature: EPUB 3 — Vocabularies — Vocabulary association


  Checks conformance to the "Vocabulary association" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-vocab-assoc


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings


  # D.1 Vocabulary association mechanisms

  ## D.1.4 The prefix attribute
  
  @spec @xref:sec-prefix-attr
  Scenario: the 'prefix' attribute can be used to define new prefix mappings 
    When checking file 'property-prefix-declaration-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-prefix-attr
  Scenario: syntax errors in the 'prefix' attribute are reported
    When checking file 'property-prefix-declaration-syntax-error.opf'
    Then error OPF-004c is reported 2 times (the test file contains 2 syntax errors)
    And no other errors or warnings are reported

  @spec @xref:sec-prefix-attr
  Scenario: default vocabularies must not be assigned a prefix
    When checking file 'property-prefix-declaration-default-vocabs-error.opf'
    Then error OPF-007b is reported 4 times (once for each default vocabulary)
    And no other errors or warnings are reported

  @spec @xref:sec-prefix-attr
  Scenario: A metadata property with an unknown prefix is reported
    When checking file 'property-prefix-declaration-missing-error.opf'
    Then error OPF-028 is reported
    And no errors or warnings are reported


  ## D.1.5 Reserved prefixes
  
  Scenario: reserved prefixes can be explicitly declared
    When checking file 'property-prefix-declaration-reserved-explicit-valid.opf'
    Then no errors or warnings are reported

  Scenario: reserved prefixes should not be overridden to other vocabularies 
    When checking file 'property-prefix-declaration-reserved-overridden-warning.opf'
    Then warning OPF-007 is reported 8 times (once for each reserved prefix)
    And no other errors or warnings are reported

  Scenario: The 'schema' prefix can be used in metadata properties without being declared
    When checking file 'property-prefix-schema-not-declared-valid.opf'
    Then no errors or warnings are reported
