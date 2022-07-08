 Feature: EPUB 3 ▸ External Identifiers


  Checks conformance to the "Allowed external identifiers" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#app-identifiers-allowed


  
  Background: 
    Given EPUB test files located at '/epub3/B-external-identifiers/files/'
    And EPUBCheck with default settings


  Scenario: Verify DOCTYPE declarations with allowed external identifiers
    When checking EPUB 'xml-external-identifier-allowed-valid'
    Then no errors or warnings are reported

  Scenario: Report a DOCTYPE declaration with an allowed external identifier but not on the expected media type
    When checking EPUB 'xml-external-identifier-bad-mediatype-error'
    Then error OPF-073 is reported
    And no other errors or warnings are reported

  Scenario: Report a DOCTYPE declaration with an external identifier that is not allowed
    When checking EPUB 'xml-external-identifier-disallowed-error'
    Then error OPF-073 is reported
    And no other errors or warnings are reported

  Scenario: Verify an attribute value with leading/trailing whitespace is allowed (issue 332)
    When checking EPUB 'xml-id-leading-trailing-spaces-valid'
    Then no errors or warnings are reported
