Feature: EPUB 3 — Outdated Features


  Checks conformance to the "Outdated features" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#sec-obs-conform


  Background: 
    Given EPUB test files located at '/epub3/E-obsolete-features/files/'
    And EPUBCheck with default settings


  ## Publication resources

  ### Content fallbacks

  @spec @xref:content-fallbacks @xref:sec-foreign-resources
  Scenario: Report content fallback in HTML `audio` as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-manifest-fallback-xhtml-audio-valid'
    Then no other errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains "content fallback"

  @spec @xref:content-fallbacks @xref:sec-foreign-resources
  Scenario: Allow a foreign resource in HTML `embed` with a manifest fallback
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-manifest-fallback-xhtml-embed-valid'
    Then no other errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains "content fallback"


  @spec @xref:content-fallbacks @xref:sec-foreign-resources
  Scenario: Report content fallback in HTML `img` as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-manifest-fallback-xhtml-audio-valid'
    Then no other errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains "content fallback"

  @spec @xref:sec-foreign-resources
  Scenario: Allow a foreign resource in HTML video `poster` with a manifest fallback
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-manifest-fallback-xhtml-video-poster-valid'
    Then no other errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains "content fallback"


  ## Open Container Format (OCF)

  ### Font obfuscation

  @spec @xref:sec-font-obfuscation
  Scenario: Report font obfuscation as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-ocf-obfuscation-valid'
    Then no errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains "font obfuscation"

  @spec @xref:sec-font-obfuscation
  Scenario: Verify duplicating encryption declaration is allowed
    When checking EPUB 'outdated-ocf-obfuscation-duplicate-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-font-obfuscation
  Scenario: Report an obfuscated font that is not a Core Media Type
    When checking EPUB 'outdated-ocf-obfuscation-not-cmt-error'
    Then error PKG-026 is reported
    And no errors or warnings are reported

  @spec @xref:sec-font-obfuscation
  Scenario: Report an obfuscated font that is not a font
    When checking EPUB 'outdated-ocf-obfuscation-not-font-error'
    Then error PKG-026 is reported
    And no errors or warnings are reported


  ## Collections
  
  ### collection element

  @spec @xref:sec-collection-elem
  Scenario: Report a collection element as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking file 'outdated-collection-role-url-valid.opf'
    Then no errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains 'the "collection" element'

  @spec @xref:sec-collection-elem
  Scenario: a collection role must not be an invalid URL
    Spec mismatch: this should be reported as an error 
    When checking file 'outdated-collection-role-url-invalid-error.opf'
    Then warning OPF-070 is reported
    And no other errors or warnings are reported

  #! FIXME this should probably not raise an RSC-005
  # see https://www.w3.org/TR/epub-33/#sec-collection-elem
  # see https://idpf.org/epub/vocab/package/roles/manifest/
  Scenario: a 'manifest' collection must be the child of another collection
    See http://idpf.org/epub/vocab/package/roles/manifest/
    When checking file 'outdated-collection-role-manifest-toplevel-error.opf'
    Then error RSC-005 is reported
    And the message contains "A manifest collection must be the child of another collection"
    And no other errors or warnings are reported


  ## Legacy features

  ### OPF 2 meta element

  @spec @xref:sec-opf2-meta
  Scenario: Report the OPF 2 meta element as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-legacy-meta-element-valid.opf'
    Then no errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains 'OPF 2 "meta" element'

  ### OPF 2 guide element

  @spec @xref:sec-opf2-guide
  Scenario: Report the OPF 2 guide element as outdated (USAGE)
    Given the reporting level is set to USAGE
    When checking EPUB 'outdated-legacy-guide-valid.opf'
    Then no errors or warnings are reported
    But usage OBS-001 is reported
    And the message contains 'OPF 2 "guide" element'

  @spec @xref:sec-opf2-guide
  Scenario: 'guide' should not contain two entries of the same type pointing to the same resource
    When checking EPUB 'outdated-legacy-guide-duplicates-warning.opf'
    Then warning RSC-017 is reported 2 times (once for each entry)
    And the message contains 'Duplicate "reference" elements with the same "type" and "href" attributes'
    And no other errors or warnings are reported
    
  ### OPF 2 NCX

  @spec @xref:sec-opf2-ncx
  Scenario: When an NCX document is present, it must be identified in the 'toc' attribute of the spine  
    When checking file 'outdated-legacy-ncx-toc-attribute-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "toc attribute must be set"
    And no other errors or warnings are reported

  @spec @xref:sec-opf2-ncx
  Scenario: The 'toc' attribute of the spine must point to an NCX document  
    When checking file 'outdated-legacy-ncx-toc-attribute-not-ncx-error.opf'
    Then error OPF-050 is reported
    Then error RSC-005 is reported (duplicate schema error)
    And no other errors or warnings are reported

  @spec @xref:sec-opf2-ncx
  Scenario: Verify a publication featuring a legacy NCX navigation document
    When checking EPUB 'outdated-package-ncx-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-opf2-ncx
  Scenario: Report validation errors in legacy NCX documents
    When checking EPUB 'outdated-package-ncx-invalid-error'
    Then error RSC-012 is reported
    And the message contains 'Fragment identifier is not defined'
    And no other errors or warnings are reported

  @spec @xref:sec-opf2-ncx
  Scenario: Verify an NCX which does not link to all spine items
    Given the reporting level set to USAGE
    When checking EPUB 'outdated-package-ncx-missing-references-to-spine-valid'
    Then no errors or warnings are reported


  ## Package rendering vocabulary
 
  ### rendition:flow property
  
  @spec @xref:ref-for-index-term-rendition-flow-property-1
  Scenario: the 'rendition:flow' property can be used to define the global flow preference
    When checking file 'outdated-rendition-flow-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-flow-property-1
  Scenario: a 'rendition:flow' property with an unknown value is reported
    When checking file 'outdated-rendition-flow-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:flow" property must be'
    And no other errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-flow-property-1
  Scenario: the 'rendition:flow' property cannot be declared more than once
    When checking file 'outdated-rendition-flow-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:flow" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-flow-property-1
  Scenario: the 'rendition:flow' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'outdated-rendition-flow-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  ### rendition:flow property spine overrides
  
  @spec @xref:ref-for-index-term-spine-overrides-1
  Scenario: the 'rendition:flow' property can be used as a spine override
    When checking file 'outdated-rendition-flow-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:ref-for-index-term-spine-overrides-1
  Scenario: the 'rendition:flow' spine overrides values are mutually exclusive
    When checking file 'outdated-rendition-flow-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  ### rendition:orientation property

  @spec @xref:ref-for-index-term-rendition-orientation-property-1
  Scenario: the 'rendition:orientation' property can be used to define the global orientation preference
    When checking file 'outdated-rendition-orientation-global-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-orientation-property-1
  Scenario: a 'rendition:orientation' property with an unknown value is reported
    When checking file 'outdated-rendition-orientation-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:orientation" property must be'
    And no other errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-orientation-property-1
  Scenario: the 'rendition:orientation' property cannot be declared more than once
    When checking file 'outdated-rendition-orientation-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:orientation" property must not occur more than one time'
    And no other errors or warnings are reported

  @spec @xref:ref-for-index-term-rendition-orientation-property-1
  Scenario: the 'rendition:orientation' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'outdated-rendition-orientation-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  ### rendition:orientation spine overrides
  
  @spec @xref:ref-for-index-term-spine-overrides-0-1
  Scenario: the 'rendition:orientation' property can be used as a spine override
    When checking file 'outdated-rendition-orientation-itemref-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:ref-for-index-term-spine-overrides-0-1
  Scenario: the 'rendition:orientation' spine overrides values are mutually exclusive
    When checking file 'outdated-rendition-orientation-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported
