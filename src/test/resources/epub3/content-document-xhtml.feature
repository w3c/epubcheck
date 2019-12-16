Feature: EPUB 3 XHTML Content Document
  
  Checks conformance to specification rules defined for EPUB XHTML Content Documents:
  https://www.w3.org/publishing/epub32/epub-contentdocs.html#sec-xhtml
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Note: Tests related to EPUB XHTML Content Document rules in a full EPUB publication
        are defined in the `content.feature` feature file.

  Background: 
    Given EPUBCheck configured to check an XHTML Content Document
    And test files located at '/epub3/files/content-document-xhtml/'

  Scenario: Minimal Content Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  #############################################################################
  ###  ARIA																																	###
  #############################################################################
  #
  Scenario: ARIA role on an `a` element with no `href` attribute
    When checking document 'aria-role-a-nohref.xhtml'
    Then no errors or warnings are reported

  Scenario: ARIA `describedat` attribute
    When checking document 'aria-describedAt-invalid.xhtml'
    Then error RSC-005 is reported
    And the message contains 'attribute "aria-describedat" not allowed here'
    And no other errors or warnings are reported
    
  #############################################################################
  ###  epub:type																														###
  #############################################################################
  #
