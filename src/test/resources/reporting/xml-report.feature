 Feature: EPUBCheck - XML Report tests
  
  Checks the XML (JHove) report format


  Background: 
    Given EPUB test files located at '/reporting/files/'
    And the reporting format is set to XML
    And EPUBCheck with default settings
    Given the default namespace is 'http://schema.openpreservation.org/ois/xml/ns/jhove'



  Rule: The XML report contains basic information

    Example: the report is well-formed and has content
      When checking EPUB 'minimal'
      Then the XML report is well-formed
      And XPath '//repInfo' exists

    Example: the creation date is set
      When checking EPUB 'minimal'
      Then XPath '//repInfo/created' exists


  ## Maximum number of each message

  Rule: The maximum number of each message reported can be configured

    Example: the default maximum number of each message is 25
      When checking EPUB 'messages-maxcount'
      Then XPath 'count(//message)' is 25

    Example: the maximum number can be configured to a limit
      Given the maximum number of each message is set to 10
      When checking EPUB 'messages-maxcount'
      Then XPath 'count(//message)' is 10

    Example: the maximum number can be configured to 0
      Given the maximum number of each message is set to 0
      When checking EPUB 'messages-maxcount'
      Then XPath 'count(//message)' is 0

    Example: the maximum number can be unlimited
      Given the maximum number of each message is set to -1
      When checking EPUB 'messages-maxcount'
      Then XPath 'count(//message)' is 30


