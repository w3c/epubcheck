Feature: EPUB 3 Media Overlays Document
  
  Checks conformance to specification rules defined for EPUB Media Overlays Documents:
  https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav
  
  This feature file contains tests for EPUBCheck running in `mo` mode to check
  single Media Overlays Documents (`.smil` files).
  
  Note: Tests related to EPUB Media Overlays rules in a full EPUB publication
        are defined in the `mediaoverlays.feature` feature file.

  Background: 
    Given test files located at '/epub3/files/mediaoverlays-document/'
    And EPUBCheck configured to check a Media Overlays Document


  #Section 2.4.1 The `smil` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-smil-elem
  Scenario: SMIL Document with additional prefixes declared
    When checking document 'valid-prefixes.smil'
    Then no errors or warnings are reported

  Scenario: The `body` element of the SMIL document includes a reference to a prefix that has not been defined
    When checking document 'undeclared-prefix.smil'
    Then error OPF-028 is reported
    And the message contains "Undeclared prefix: 'foof'"
    And no other errors or warnings are reported

  Scenario: The `body`, `seq` and `par` elements contain properties that are not defined by a prefix or the spec
    When checking document 'undeclared-properties.smil'
    Then the following errors are reported
      | OPF-027 | Undefined property: 'not' |
      | OPF-027 | Undefined property: 'exists' |
      | OPF-027 | Undefined property: 'seq_prop' |
      | OPF-027 | Undefined property: 'par_prop' |
    And no other errors or warnings are reported

  #Section 2.4.2 The `head` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-head-elem
  Scenario: SMIL document that contains a head element with an invalid child element
    When checking document 'incorrect-meta-data.smil'
    Then error RSC-005 is reported
    And the message contains 'element "meta" not allowed here'
    And no other errors or warnings are reported


  #Section 2.4.3 The `metadata` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-metadata-elem
  Scenario: SMIL Document that includes an optional `metadata` element
    When checking document 'valid-metadata.smil'
    Then no errors or warnings are reported


  #Section 2.4.4 The `body` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-body-elem
  Scenario: SMIL Document with epub:Type value of "aside"
    When checking document 'valid-epubtype-aside.smil'
    Then no errors or warnings are reported



  #Section 2.4.5 The `seq` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-seq-elem
  Scenario: SMIL Document with `audio` and `text` elements in a seq element
    When checking document 'audio-nested-in-seq.smil'
    Then the following errors are reported
      | RSC-005 | element "audio" not allowed here |
      | RSC-005 | element "text" not allowed here |
    And no other errors or warnings are reported



  #Section 2.4.6 The `par` element
  #Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-body-elem
  Scenario: PAR Element that contains more than one `text` element is not allowed
    When checking document 'multiple-text-elements.smil'
    Then error RSC-005 is reported
    And the message contains 'element "text" not allowed here'
    And no other errors or warnings are reported

  Scenario: PAR Element can only contain text and audio elements
    When checking document 'seq-nested-in-par.smil'
    Then error RSC-005 is reported
    And the message contains 'element "seq" not allowed here'
    And no other errors or warnings are reported

  # Section 2.4.8 The `audio` element
  # Spec Reference: https://w3c.github.io/publ-epub-revision/epub32/spec/epub-mediaoverlays.html#sec-smil-audio-elem
  Scenario: Clock values with the full clock syntax `hh:mm:ss.milli` are allowed
    When checking document 'valid-full-clock-format.smil'
    Then no errors or warnings are reported

  Scenario: Clock values with the timecount syntax `XXmin` are allowed
    When checking document 'valid-timecount-format.smil'
    Then no errors or warnings are reported

  Scenario: A variety of invalid Clock Values are tested
    The first audio element has a clipBegin attribute that references a seconds value over one minute (334 seconds)
    The first audio element has a clipEnd attribute that references a minutes value over one hour (223 minutes)
    The second audio element has a clipBegin attribute that includes an invalid clock value metric (m)
    The second audio element has a clipEnd attribute that uses the 'partial clock value' format with a minutes value over one hour (456)
    The third audio element has a clipBegin attribute that does not include the leading portion of the TimeCount syntax (.5s) - a correct value should be (0.5s)
    The third audio element has a clipEnd attribute that uses an incorrect metric (hrs) to identify the offset
    The fourth audio element has a clipBegin attribute that mixes 'Full Clock' syntax with 'Timecount' syntax (00:00:23.93ms) - 'Full Clock' values do not use metrics (ms)
    When checking document 'incorrect-clock-value-formats.smil'
    Then the following errors are reported
      | RSC-005 | value of attribute "clipBegin" is invalid |
      | RSC-005 | value of attribute "clipEnd" is invalid |
      | RSC-005 | value of attribute "clipBegin" is invalid |
      | RSC-005 | value of attribute "clipEnd" is invalid |
      | RSC-005 | value of attribute "clipBegin" is invalid |
      | RSC-005 | value of attribute "clipEnd" is invalid |
      | RSC-005 | value of attribute "clipBegin" is invalid |
    And no other errors or warnings are reported

  Scenario: The `clipEnd` MUST be after the starting offset in 'clipBegin'
    When checking document 'matching-clip-times.smil'
    Then error RSC-005 is reported
    And the message contains 'Attributes \'clipBegin\' and \'clipEnd\' must not be equal'
    And no other errors or warnings are reported


  #Testing invalid prefix and property values on the
  Scenario: The `body`, `seq` and `par` elements of the SMIL document include undefined
    When checking document 'undeclared-attributes.smil'
    Then the following errors are reported
         | OPF-027 | Undefined property: 'not' |
         | OPF-027 | Undefined property: 'exists' |
         | OPF-028 | Undeclared prefix: 'foof' |
         | OPF-027 | Undefined property: 'asfas' |
         | OPF-027 | Undefined property: 'asfar' |
    And no other errors or warnings are reported

