Feature: EPUB 3 — Media Overlays


  Checks conformance to the "Media overlays" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-media-overlays


  Background: 
    Given EPUB test files located at '/epub3/09-media-overlays/files/'
    And EPUBCheck with default settings


  ## 9.2 Media overlay documents
  
  Scenario: Verify that a Media Overlays Document can have any extension
    When checking EPUB 'mediaoverlays-file-extension-unusual-valid'
    Then no errors or warnings are reported

  ### 9.2.1 Media overlay documents requiremements
  
  @spec @xref:sec-overlay-req
  Scenario: Verify a minimal EPUB 3 publication with Media Overlays
    When checking EPUB 'mediaoverlays-minimal-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-overlay-req
  Scenario: Verify a minimal Media Overlay document
    When checking document 'minimal.smil'
    Then no other errors or warnings are reported

  @spec @xref:sec-overlay-req
  Scenario: Report an EPUB content document that is declared in more than one overlay
    When checking EPUB 'mediaoverlays-multiple-overlay-ref-error'
    Then error MED-011 is reported
    And no other errors or warnings are reported


  ### 9.2.2 Media overlay document definition
  
  #### 9.2.2.2 The `head` element

  @spec @xref:sec-smil-head-elem
  Scenario: Report a `meta` element used in the `head` container 
    When checking document 'metadata-syntax-invalid-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "meta" not allowed here'
    And no other errors or warnings are reported


  #### 9.2.2.3 The `metadata` element

  Scenario: Allow a `metadata` element with custom metadata properties
    When checking document 'metadata-properties-valid.smil'
    Then no errors or warnings are reported


  #### 9.2.2.5 The `seq` element

  @spec @xref:sec-smil-seq-elem
  Scenario: Report media clips used as direct children of a `seq` element
    When checking document 'seq-with-direct-media-children-error.smil'
    Then the following errors are reported
      | RSC-005 | element "text" not allowed here |
      | RSC-005 | element "audio" not allowed here |
    And no other errors or warnings are reported

  @spec @xref:sec-smil-seq-elem
  Scenario: Report a fragment identifier that does not resolve to an element
    When checking EPUB 'mediaoverlays-fragid-resolve-error'
    Then error RSC-012 is reported
    And no other errors or warnings are reported


  #### 9.2.2.6 The `par` element

  @spec @xref:sec-smil-par-elem
  Scenario: Report a `par` element with more than one `text` child
    When checking document 'par-with-multiple-text-elements-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "text" not allowed here'
    And no other errors or warnings are reported

  @spec @xref:sec-smil-par-elem
  Scenario: Report a `par` element with a `seq` child
    When checking document 'par-with-seq-child-error.smil'
    Then error RSC-005 is reported
    And the message contains 'element "seq" not allowed here'
    And no other errors or warnings are reported


  #### 9.2.2.7 The `text` element
  
  @spec @xref:sec-smil-text-elem @xref:sec-smil-body-elem
  Scenario: Report empty fragment identifiers
    When checking EPUB 'mediaoverlays-fragid-invalid-error'
    Then error MED-014 is reported 2 times
    And no other errors or warnings are reported

  
  #### 9.2.2.8 The `audio` element

  @spec @xref:sec-smil-audio-elem
  Scenario: Allow clock values with the full clock syntax (`hh:mm:ss.milli`)
    When checking document 'clock-value-full-syntax-valid.smil'
    Then no errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Allow clock values with the partial clock syntax (`mm:ss.milli`)
    When checking document 'clock-value-partial-syntax-valid.smil'
    Then no errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Allow clock values with the timecount syntax (`XXmin`)
    When checking document 'clock-value-timecount-syntax-valid.smil'
    Then no errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Report invalid clock values
    When checking document 'clock-value-syntax-invalid-error.smil'
    Then error RSC-005 is reported 6 times
    And no other errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Report if the `clipBegin` value is after the `clipEnd` value
    When checking document 'clipBegin-after-clipEnd-error.smil'
    Then error MED-008 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Report if the `clipEnd` value equals the `clipBegin` value
    When checking document 'clip-times-equal-error.smil'
    Then error MED-009 is reported 4 times
    And no other errors or warnings are reported

  @spec @xref:sec-smil-audio-elem
  Scenario: Report an audio clip that is not a Core Media Type
    When checking EPUB 'mediaoverlays-audio-non-cmt-error'
    Then error MED-005 is reported
    And no other errors or warnings are reported


  ## 9.3 Creating Media Overlays
  
  ### 9.3.2 Relationship to the EPUB content document

  Scenario: Report an overlay document whose text elements do not match the dom order of the corresponding content document
    Given the reporting level is set to USAGE
    When checking EPUB 'mediaoverlays-text-reading-order-error'
    Then usage MED-015 is reported
    And no other errors or warnings are reported


  ### 9.3.3 Structural semantics

  Scenario: Allow epub:type properties in the default vocabulary
    When checking document 'epubtype-valid.smil'
    Then no errors or warnings are reported
    
  Scenario: Allow custom epub:type properties with a declared prefix
    When checking document 'epubtype-prefix-declared-valid.smil'
    Then no errors or warnings are reported

  Scenario: Report an epub:type property with an undeclared prefix
    When checking document 'epubtype-prefix-undeclared-error.smil'
    Then error OPF-028 is reported
    And the message contains 'Undeclared prefix: "my"'
    And no other errors or warnings are reported

  Scenario: Allow unknown epub:type properties in the default vocabulary
    Given the reporting level set to usage
    When checking document 'epubtype-unknown-usage.smil'
    Then usage OPF-088 is reported
    And no other errors or warnings are reported


  ### 9.3.4 Associating style information

  @spec @xref:sec-docs-assoc-style
  Scenario: Report a 'media:active-class' property with a refines attribute
    When checking file 'mediaoverlays-active-class-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not be used with the media:active-class property"
    And no other errors or warnings are reported
  
  @spec @xref:sec-docs-assoc-style
  Scenario: Report a 'media:playback-active-class' property with a refines attribute
    When checking file 'mediaoverlays-playback-active-class-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "must not be used with the media:playback-active-class property"
    And no other errors or warnings are reported

    
  ### 9.3.5 Media overlays packaging

  #### 9.3.5.1 Including Media Overlays

  @spec @xref:sec-package-including
  Scenario: Report an EPUB content document referenced from an overlay that is missing its media-overlay attribute
    When checking EPUB 'mediaoverlays-missing-mo-attr-error'
    Then error MED-010 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-package-including
  Scenario: Report an EPUB content document that references the wrong overlay
    When checking EPUB 'mediaoverlays-incorrect-overlay-ref-error'
    Then error MED-012 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-package-including
  Scenario: Report an EPUB content document that declares a media-overlay attribute but is not referenced from the overlay
    When checking EPUB 'mediaoverlays-no-overlay-ref-error'
    Then error MED-013 is reported
    And no other errors or warnings are reported
  
  @spec @xref:sec-package-including
  Scenario: a Media Overlay must have the media type 'application/smil+xml'
    When checking file 'mediaoverlays-type-invalid-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must be of the "application/smil+xml" type'
    And no other errors or warnings are reported

  @spec @xref:sec-package-including
  Scenario: Report use of the media-overlay attribute on non-EPUB Content Documents
    When checking file 'mediaoverlays-non-contentdoc-error.opf'
    Then error RSC-005 is reported
    And the message contains 'media-overlay attribute is only allowed'
    And no other errors or warnings are reported


  ### 9.3.5.2 Overlays package metadata
  
  @spec @xref:sec-mo-package-metadata
  Scenario: the entire publication duration must be defined
    When checking file 'mediaoverlays-duration-global-not-defined-error.opf'
    Then error RSC-005 is reported
    And the message contains "global media:duration meta element not set"
    And no other errors or warnings are reported
    
  @spec @xref:sec-mo-package-metadata
  Scenario: the duration of each Media Overlay must be defined
    When checking file 'mediaoverlays-duration-single-not-defined-error.opf'
    Then error RSC-005 is reported
    And the message contains "item media:duration meta element not set"
    And no other errors or warnings are reported

  Scenario: the total duration should be the sum of all Media Overlay durations
    When checking file 'mediaoverlays-duration-total-not-sum-warning.opf'
    Then warning MED-016 is reported
    And no other errors or warnings are reported
