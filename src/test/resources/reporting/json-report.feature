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
    
  ## Fonts

  Scenario: Font remote
    When checking EPUB 'fonts-remote'
    Then the JSON report is valid
    And JSON at '$.publication.refFonts' is:
     | Open Sans |
    And JSON at '$.publication.embeddedFonts' is empty

  Scenario: Font embedded
    When checking EPUB 'fonts-embedded'
    Then the JSON report is valid
    And JSON at '$.publication.embeddedFonts' is:
     | Open Sans |
    And JSON at '$.publication.refFonts' is empty
    
  ## References
  
  Scenario: cross-HTML references
    When checking EPUB 'minimal'
    Then the JSON report is valid
    And JSON at '$..items[?(@.id=="nav")].referencedItems[0]' is:
      | EPUB/content_001.xhtml |

  ## Layout

  Scenario: layout field defaults
    When checking EPUB 'minimal'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "reflowable"
    And JSON at '$.publication.renditionOrientation' is "auto"
    And JSON at '$.publication.renditionSpread' is "auto"
    And JSON at '$.publication.hasFixedFormat' is false
    And JSON at '$..items[?(@.isSpineItem==true)].renditionLayout' are all "reflowable"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionOrientation' are all "auto"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionSpread' are all "auto"
    And JSON at '$..items[?(@.isSpineItem==true)].isFixedFormat' are all false

  Scenario: `renditionLayout` "pre-paginated" on publication
    When checking EPUB 'layout-paginated'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.hasFixedFormat' is true
    And JSON at '$..items[?(@.isSpineItem==true)].renditionLayout' are all "pre-paginated"
    And JSON at '$..items[?(@.isSpineItem==true)].isFixedFormat' are all true

  Scenario: `renditionLayout` "pre-paginated" on item
    When checking EPUB 'layout-paginated-item'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "reflowable"
    And JSON at '$.publication.hasFixedFormat' is true
    And JSON at '$..items[?(@.isSpineItem==true)].renditionLayout' is:
      | reflowable |
      | pre-paginated |

  Scenario: `renditionLayout` "reflowable" on publication
    When checking EPUB 'layout-reflowable'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "reflowable"
    And JSON at '$.publication.hasFixedFormat' is false
    And JSON at '$..items[?(@.isSpineItem==true)].renditionLayout' are all "reflowable"
    And JSON at '$..items[?(@.isSpineItem==true)].isFixedFormat' are all false

  Scenario: `renditionLayout` "reflowable" on item
    When checking EPUB 'layout-reflowable-item'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.hasFixedFormat' is true
    And JSON at '$..items[?(@.isSpineItem==true)].renditionLayout' is:
      | pre-paginated |
      | reflowable |

  Scenario: `renditionOrientation` on publication
    When checking EPUB 'layout-orientation'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.renditionOrientation' is "portrait"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionOrientation' are all "portrait"

  Scenario: `renditionOrientation` on item
    When checking EPUB 'layout-orientation-item'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.renditionOrientation' is "auto"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionOrientation' is:
      | auto |
      | landscape |

  Scenario: `renditionSpread` on publication
    When checking EPUB 'layout-spread'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.renditionSpread' is "none"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionSpread' are all "none"

  Scenario: `renditionSpread` on item
    When checking EPUB 'layout-spread-item'
    Then the JSON report is valid
    And JSON at '$.publication.renditionLayout' is "pre-paginated"
    And JSON at '$.publication.renditionSpread' is "auto"
    And JSON at '$..items[?(@.isSpineItem==true)].renditionSpread' is:
      | auto |
      | both |
