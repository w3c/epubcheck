Feature: EPUB 2 OPS Content Document (SVG)
  
  Checks conformance to specification rules defined for EPUB 2 OPS Content Documents:
  http://idpf.org/epub/20/spec/OPS_2.0.1_draft.htm#Section2.0
  
  This feature file contains tests for EPUBCheck running in `svg` mode to check
  single SVG Documents (`.svg` files).
  
  Note:
  - Tests related to EPUB 2 OPS rules in a full EPUB publication are defined in
    the `ops.feature` feature file.
  - Tests related to EPUB 2 OPS XHTML content are defined in the `ops-document-xhtml`
    feature file.

  Background: 
    Given EPUBCheck configured to check an SVG Content Document
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And test files located at '/epub2/files/ops-document-svg/'


  #  2.0 OPS Content Document Vocabularies

  ### SVG

  Scenario: Verify `font-face-src` is allowed (issue 196)
    When checking document 'font-face-src-valid.svg'
    Then no errors or warnings are reported