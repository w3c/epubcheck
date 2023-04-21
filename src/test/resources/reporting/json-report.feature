 Feature: EPUBCheck - JSON Report tests
  
  Checks the JSON report format


  Background: 
    Given EPUB test files located at '/reporting/files/'
    And the reporting format is set to JSON
    And EPUBCheck with default settings

  Scenario: Basic well-formedness checks
    When checking EPUB 'minimal'
    Then the JSON report is valid
    And JSON at '$.items' contains 5 items
    And JSON at '$..checkSum' has no null values 
