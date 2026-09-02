Feature: EPUB 3 — Deprecated Features


  Checks conformance to the "Deprecated features" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#sec-obs-deprecated


  Background: 
    Given EPUB test files located at '/epub3/E-obsolete-features/files/'
    And EPUBCheck with default settings


  ## Package document

  ### bindings wlement
  
  @spec @xref:sec-opf-bindings
  Scenario: Report usage of the 'bindings' element as deprecated 
    When checking file 'deprecated-bindings-warning.opf'
    Then warning RSC-017 is reported
    And no other errors or warnings are reported


  ## XHTML content documents

  ### switch element

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report `epub:switch` is deprecated
    When checking document 'deprecated-switch-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported
  
  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report `epub:switch` with invalid mathml
    When checking document 'deprecated-switch-mathml-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "math" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report an `epub:switch` with a `default` before any `case` elements 
    When checking document 'deprecated-switch-default-before-case-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed yet'
    And error RSC-005 is reported
    And the message contains 'element "epub:case" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report an `epub:switch` with multiple `default` elements
    When checking document 'deprecated-switch-multiple-default-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed here'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report `epub:switch` without any `case` elements
    When checking document 'deprecated-switch-no-case-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:default" not allowed yet'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report `epub:switch` element without a `default`
    When checking document 'deprecated-switch-no-default-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:switch" incomplete'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-switch
  Scenario: Report `epub:case` without a `required-namespace` attribute
    When checking document 'deprecated-switch-no-case-namespace-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "epub:case" missing required attribute "required-namespace"'
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported


  ### epub:trigger element

  @spec @xref:sec-xhtml-epub-trigger
  Scenario: Report `epub:trigger` is deprecated
    When checking document 'deprecated-trigger-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains 'The "epub:trigger" element is deprecated'
    And no other errors or warnings are reported

  @spec @xref:sec-xhtml-epub-trigger
  Scenario: Report `epub:trigger` that references non-existent IDs
    When checking document 'deprecated-trigger-badrefs-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'The ref attribute must refer to an element in the same document'
    And error RSC-005 is reported
    And the message contains 'The ev:observer attribute must refer to an element in the same document'
    And warning RSC-017 is reported 2 times
    And the message contains 'The "epub:trigger" element is deprecated'
    And no other errors or warnings are reported


  ## Meta properties vocabulary

  ### meta-auth property
  
  @spec @xref:sec-meta-auth
  Scenario: 'meta-auth' metadata is deprecated 
    When checking file 'deprecated-metadata-meta-meta-auth-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "the meta-auth property is deprecated"
    And no other errors or warnings are reported


  ## Link relationships vocabulary

  ### acquire property

  Scenario: an 'acquire' link can identify the full version of the publication
    Note:
      the 'acquire' relationship was removed in EPUB 3.3:
      see https://github.com/w3c/epub-specs/issues/2489
      it is still accepted, for backward compatibility. 
    When checking file 'link-rel-acquire-valid.opf'
    Then no errors or warnings are reported

  ### *-record properties

  @spec @xref:sec-marc21xml-record @xref:sec-mods-record @xref:sec-onix-record @xref:sec-xmp-record
  Scenario: '*-record' links are deprecated 
    When checking file 'deprecated-link-rel-record-warning.opf'
    Then the following warnings are reported
      | OPF-086 | "marc21xml-record" is deprecated |
      | OPF-086 | "mods-record" is deprecated      |
      | OPF-086 | "onix-record" is deprecated      |
      | OPF-086 | "xmp-record" is deprecated       |
    And error OPF-093 is reported 4 times
      # note: 'media-type' is now required, even on deprecated properties
    And no other errors or warnings are reported

  ### xml-signature property

  @spec @xref:sec-xml-signature
  Scenario: 'xml-signature' links are deprecated 
    When checking file 'deprecated-link-rel-xml-signature-warning.opf'
    Then warning OPF-086 is reported
    And the message contains '"xml-signature" is deprecated'
    And error OPF-093 is reported
      # note: 'media-type' is now required, even on deprecated properties
    And no other errors or warnings are reported


  ## Package rendering vocabulary

  ### rendition:spread portrait value

  Scenario: the 'rendition:spread' 'portrait' property value is deprecated
    When checking file 'deprecated-rendition-spread-portrait-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread-portrait' spine override is deprecated
    When checking file 'deprecated-rendition-spread-portrait-override-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported

  ### rendition:viewport property

	@spec @xref:viewport
  Scenario: the 'rendition:viewport' property is deprecated
    When checking file 'deprecated-rendition-viewport-warning.opf'
    Then warning OPF-086 is reported
    And no other errors or warnings are reported

  @spec @xref:viewport
  Scenario: the 'rendition:viewport' property syntax errors are reported
    When checking file 'deprecated-rendition-viewport-syntax-error.opf'
    Then warning OPF-086 is reported (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The value of the "rendition:viewport" property must be of the form'
    And no other errors or warnings are reported

  @spec @xref:viewport
  Scenario: the 'rendition:viewport' property cannot be declared more than once
    When checking file 'deprecated-rendition-viewport-duplicate-error.opf'
    Then warning OPF-086 is reported 2 times (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The "rendition:viewport" property must not occur more than one time as a global value'
    And no other errors or warnings are reported
