Feature: EPUB 3 Publication Resources
  
  Checks conformance to rules related to Publication Resources:
  https://www.w3.org/publishing/epub32/epub-spec.html#sec-publication-resources

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings

  ## 3.1 Core Media Types
  
  # Note: Core Media Types support on the Package Document `item` elements
  #       is tested in the Package Document feature.   
  
  ### 3.1.2 Supported Media Types
  
  ### 3.1.3 Foreign Resources
  
  ## 3.2 Resources Locations