Feature: EPUB 3 — EPUB publication conformance


  Checks conformance to the "EPUB publication conformance" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-epub-conf


  Background:
    Given test files located at '/epub3/02-epub-publication-conformance/files/'
    And EPUBCheck with default settings


  ##  2. EPUB publication conformance

  @spec @xref:sec-epub-conf
  Scenario: the minimal Package Document is reported as valid 
    When checking file 'minimal.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-epub-conf
  Scenario: Verify a minimal packaged EPUB
    When checking EPUB 'minimal.epub'
    Then no errors or warnings are reported

