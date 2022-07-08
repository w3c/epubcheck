 Feature: EPUB 3 ▸ Minimal Publications
  
  Checks minimal publications against the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/


  Background: 
    Given EPUB test files located at '/epub3/00-minimal/files/'
    And EPUBCheck with default settings


  Scenario: Verify a minimal EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported
    
  Scenario: Verify a minimal packaged EPUB
    When checking EPUB 'minimal.epub'
    Then no errors or warnings are reported

	# FIXME the current API doesn’t allow the version to be explicitly set
	# PKG-001 should either be removed, or made a fatal error
  Scenario: Report when checking an EPUB 3 explicitly against EPUB 2.0.1
    Given EPUBCheck configured to check EPUB 2 rules
    When checking EPUB 'minimal'
    #Then error PKG-001 is reported
    And no other errors or warnings are reported