Feature: EPUB 3 Media Overlays Document
  
  Checks conformance to specification rules defined for EPUB Media Overlays Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in `mo` mode to check
  single Media Overlays Documents (`.smil` files).
  
  Note: Tests related to EPUB Media Overlays rules in a full EPUB publication
        are defined in the `mediaoverlays.feature` feature file.

  Background: 
    Given EPUB test files located at '/epub3/files/mediaoverlays-document/'
    And EPUBCheck configured to check a Media Overlays Document


  Scenario: Valid SMIL Document with hh:mm:ss.milli format
    When checking document 'valid-timestamp-format.smil'
    Then no errors or warnings are reported


  Scenario: Valid SMIL Document with XX min format
    When checking document 'valid-word-format.smil'
    Then no errors or warnings are reported

  Scenario: Valid SMIL Document with a head section and meta data
    When checking document 'valid-head-metadata.smil'
    Then no errors or warnings are reported

  Scenario: Valid SMIL Document with epub:Type value of "aside"
    When checking document 'valid-epubtype-aside.smil'
    Then no errors or warnings are reported

  Scenario: Valid SMIL Document with additional prefixes declared
    When checking document 'valid-prefixes.smil'
    Then no errors or warnings are reported

  Scenario: SMIL Document with deprecated head syntax
    When checking document 'incorrect-meta-data.smil'
    Then error RSC-005 is reported
    And the message contains 'element "meta" not allowed here'
    And no other errors or warnings are reported

  Scenario: SMIL Document with duplicate siblings
    When checking document 'duplicate-siblings.smil'
    Then error RSC-005 is reported
    And the message contains 'element "text" not allowed here'
    And no other errors or warnings are reported

  Scenario: SMIL Document with `seq` nested in a `par` element.
    When checking document 'seq-nested-in-par.smil'
    Then the following errors are reported
        | RSC-005 | element "seq" not allowed here |
        | RSC-005 | element "seq" missing required attribute "epub:textref" |
    And no other errors or warnings are reported

  Scenario: SMIL Document with `audio` and `text` elements in a seq element
    When checking document 'audio-nested-in-seq.smil'
    Then the following errors are reported
        | RSC-005 | element "audio" not allowed here |
        | RSC-005 | element "text" not allowed here |
    And no other errors or warnings are reported

  Scenario: SMIL Document with invalid clip-begin and clip-end formats
    When checking document 'incorrect-clip-formats.smil'
    Then the following errors are reported
        | RSC-005 | value of attribute "clipBegin" is invalid |
        | RSC-005 | value of attribute "clipEnd" is invalid |
        | RSC-005 | value of attribute "clipBegin" is invalid |
        | RSC-005 | value of attribute "clipEnd" is invalid |
        | RSC-005 | value of attribute "clipBegin" is invalid |
        | RSC-005 | value of attribute "clipEnd" is invalid |
        | RSC-005 | value of attribute "clipBegin" is invalid |
    And no other errors or warnings are reported

  Scenario: SMIL Document with undeclared property and prefix values
    When checking document 'undeclared-attributes.smil'
    Then the following errors are reported
         | OPF-027 | Undefined property: 'not' |
         | OPF-027 | Undefined property: 'exists' |
         | OPF-028 | Undeclared prefix: 'foof' |
         | OPF-027 | Undefined property: 'asfas' |
         | OPF-027 | Undefined property: 'asfas' |
    And no other errors or warnings are reported

  Scenario: SMIL Document with matching clipBegin and clipEnd times
    When checking document 'matching-clip-times.smil'
    Then error RSC-005 is reported
    And the message contains 'Attributes \'clipBegin\' and \'clipEnd\' must not be equal'
    And no other errors or warnings are reported

