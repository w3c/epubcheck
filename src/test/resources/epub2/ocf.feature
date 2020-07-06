Feature: EPUB 2.0.1 Open Container Format
  
  Checks conformance to rules to the Open Container Format (OCF) 2.0.1 specification:
  
    http://matt.garrish.ca/res/OCF_2.0.1.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications
  
  Note:   
  - Tests related to EPUB 3 Packages are defined in the `epub3` directory.

  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules
    
  ## 3. OCF Container Contents

  Scenario: Verify a minimal EPUB 2.0.1 publication
    When checking EPUB 'minimal'
    Then no errors or warnings are reported
