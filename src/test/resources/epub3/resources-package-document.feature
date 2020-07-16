Feature: EPUB 3 ▸ Publication Resources ▸ Package Document Checks


  Checks conformance to the "Publication Resources" section of the EPUB 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-spec.html#sec-publication-resources

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given EPUB test files located at '/epub3/files/package-document/'
    And EPUBCheck configured to check a Package Document


  ## 3.1 Core Media Types
    
  ### 3.1.2 Supported Media Types
  
  Scenario: items with Core Media Types do not require fallbacks
      The test document contains one item of each supported core media types
    When checking file 'resources-core-media-types-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: items with Core Media Types that are not preferred types are reported as usage
      The test document contains one item of each non-preferred core media types
    When the reporting level is set to USAGE
    And checking EPUB 'resources-core-media-types-not-preferred-valid.opf'
    Then Usage OPF-090 is reported 5 times
    And no errors or warnings are reported   
  
  
  ### 3.1.3 Foreign Resources
  
  ## 3.2 Resources Locations
  
  Scenario: Allow audio resources to be remote 
    When checking file 'resources-remote-audio-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: remote XHTML document is not detected in single-document mode
  	Remote resources checks depend on publication-wide validation
    (e.g. to check if the resource is used a font)
    When checking file 'resources-remote-xhtml-error.opf'
    Then no errors or warnings are reported
    
  Scenario: remote SVG document is not detected in single-document mode
    Remote resources checks depend on publication-wide validation
    (e.g. to check if the resource is used a font)
    When checking file 'resources-remote-svg-font-valid.opf'
    Then no errors or warnings are reported
