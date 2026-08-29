Feature: EPUB 3 — Vocabularies — Metadata link vocabulary


  Checks conformance to the "Metadata link vocabulary" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#app-link-vocab


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings
  
  # D.3 Metadata Link Vocabulary

  ### D.3.1 Link relationships

  #### D.3.1.1 alternate

  @spec @xref:sec-alternate
  Scenario: an 'alternate' link can identify an alternate version of the Package Document
    When checking file 'link-rel-alternate-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-alternate
  Scenario: an 'alternate' link must not be paired with other keywords
    When checking file 'link-rel-alternate-with-other-keyword-error.opf'
    Then error OPF-089 is reported
    And no other errors or warnings are reported

  #### D.3.1.2 record
  
  Scenario: a 'record' link can point to a local record
    When checking file 'link-rel-record-local-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'record' link can point to a remote record
    When checking file 'link-rel-record-remote-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'record' link can be paired with other keywords
    When checking file 'link-rel-record-with-other-keyword-valid.opf'
    Then no errors or warnings are reported
  
  @spec @xref:sec-record
  Scenario: a 'record' link must have a 'media-type' attribute even when remote 
    When checking file 'link-rel-record-mediatype-missing-error.opf'
    Then error OPF-094 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-record
  Scenario: a 'record' link cannot refine another property or resource
    When checking file 'link-rel-record-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must not have a "refines" attribute'
    And no other errors or warnings are reported


  #### D.3.1.3 voicing
  
  @spec @xref:sec-voicing
  Scenario: a 'voicing' link can identify the aural representation of metadata
    When checking file 'link-rel-voicing-valid.opf'
    Then no errors or warnings are reported
    
  @spec @xref:sec-voicing
  Scenario: a 'voicing' link must refine another property or resource
    When checking file 'link-rel-voicing-as-publication-metadata-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must have a "refines" attribute'
    And no other errors or warnings are reported

  @spec @xref:sec-voicing
  Scenario: a 'voicing' link must have a 'media-type' attribute even when remote
    When checking file 'link-rel-voicing-mediatype-missing-error.opf'
    Then error OPF-094 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-voicing
  Scenario: a 'voicing' link resource must have an audio media type
    When checking file 'link-rel-voicing-mediatype-not-audio-error.opf'
    Then error OPF-095 is reported
    And no other errors or warnings are reported


  ### D.3.2 Link properties

  @spec @xref:sec-link-properties
  Scenario: a 'record' link type can be further identified with a 'properties' attribute
    When checking file 'link-rel-record-properties-valid.opf'
    And no errors or warnings are reported
