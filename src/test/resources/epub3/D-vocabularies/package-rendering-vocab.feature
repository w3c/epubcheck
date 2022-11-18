Feature: EPUB 3 — Vocabularies — Package rendering vocabulary


  Checks conformance to the "Package rendering vocabulary" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#app-rendering-vocab


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings
    
  # Note: 
  # The properties themselves are tested in the "layout.feature" file,
  # since all the properties are defined in that section and not in the
  # vocabulary appendix.

  # D.5.1 Package rendering vocabulary

  @spec @xref:sec-rendering-custom-properties
  Scenario: Report a custom rendition property using the 'rendition' prefix
    When checking file 'rendition-property-unknown-error.opf'
    Then error OPF-027 is reported
    And no other errors or warnings are reported
  
