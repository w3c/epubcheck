Feature: EPUB 2 ▸ Open Publication Structure ▸ SVG Document Checks


  Checks conformance to the Open Publication Structure (OPS) 2.0.1 specification:
    http://idpf.org/epub/20/spec/OPS_2.0_latest.htm

  In the scenarios below, checks are run against single SVG Content Documents.
  EPUBCheck is launched in 'svg' mode.


  Background: 
    Given EPUBCheck configured to check an SVG Content Document
    And EPUBCheck configured to check EPUB 2.0.1 rules
    And test files located at '/epub2/files/ops-document-svg/'


  #  2.0 OPS Content Document Vocabularies

  ### SVG

  Scenario: Verify that namespaced extensions are allowed
    See issue #1147
    When checking document 'namespace-extension-valid.svg'
    Then no errors or warnings are reported

  Scenario: Verify `font-face-src` is allowed (issue 196)
    When checking document 'font-face-src-valid.svg'
    Then no errors or warnings are reported