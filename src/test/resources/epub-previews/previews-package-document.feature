Feature: EPUB Previews ▸ Package Document Checks


  Checks conformance to the EPUB Previews 1.0 specification:
    http://idpf.org/epub/previews/

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub-previews/files/package-document/'
    And EPUBCheck configured to check a Package Document

    
  # 3. Embedded Previews
  
  Scenario: an embedded EPUB preview collection 
    When checking file 'preview-embedded-valid.opf'
    Then no errors or warnings are reported
