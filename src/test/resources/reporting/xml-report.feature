 Feature: EPUBCheck - XML Report tests
  
  Checks the XML (JHove) report format


  Background: 
    Given EPUB test files located at '/reporting/files/'
    And the reporting format is set to XML
    And EPUBCheck with default settings
    Given the default namespace is 'http://schema.openpreservation.org/ois/xml/ns/jhove'

  Scenario: Basic well-formedness checks
    When checking EPUB 'minimal'
    Then the XML report is well-formed
    And XPath '//repInfo' exists

  Scenario: Creation date is set
    When checking EPUB 'minimal'
    And XPath '//repInfo/created' exists


