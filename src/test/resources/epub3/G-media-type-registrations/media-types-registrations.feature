Feature: EPUB 3 — Media Types Registrations


  Checks conformance to the "Media type registrations" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#app-media-type


  Background: 
    Given EPUB test files located at '/epub3/G-media-type-registrations/files/'
    And EPUBCheck with default settings
  
  # FIXME this should be at most a usage report, not a warning
  Scenario: Report when the '.epub' extension is not lower case
    When checking EPUB 'ocf-extension-not-lower-case-warning.ePub'
    Then warning PKG-016 is reported
    And no other errors or warnings are reported
