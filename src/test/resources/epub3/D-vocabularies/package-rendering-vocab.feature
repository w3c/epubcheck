Feature: EPUB 3 — Vocabularies — Package rendering vocabulary


  Checks conformance to the "Package rendering vocabulary" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#app-rendering-vocab


  Background: 
    Given EPUB test files located at '/epub3/D-vocabularies/files/'
    And EPUBCheck with default settings
    
  # Note: 
  # The properties themselves are tested in the "layout.feature" file,
  # since all the properties are defined in that section and not in the
  # vocabulary appendix.

  ## D.4 Package rendering vocabulary

  ### D.4.1 Layout control

  ### D.4.1.1 rendition:layout


  Rule: The 'rendition:layout' property MUST be one of 'reflowable', 'pre-paginated', or 'roll'

	  @spec @xref:layout-def
    Example: 'rendition:layout' 'reflowable' can be used to declare a reflowable publication
      When checking file 'rendition-layout-reflowable-valid.opf'
      Then no errors or warnings are reported

	  @spec @xref:layout-def
    Example: 'rendition:layout' 'pre-paginated' can be used to declare a pre-paginated publication
      When checking file 'rendition-layout-pre-paginated-valid.opf'
      Then no errors or warnings are reported

	  @spec @xref:layout-def
    Example: 'rendition:layout' 'roll' can be used to declare a roll publication
      When checking file 'rendition-layout-roll-valid.opf'
      Then no errors or warnings are reported

    @spec @xref:layout-def
    Example: A 'rendition:layout' property with no value is reported
      See also issue #727
      When checking file 'rendition-layout-empty-error.opf'
      Then the following errors are reported (one for the empty element, one for the consequently unexpected value)
        | RSC-005 | character content of element "meta" invalid          |
        | RSC-005 | The value of the "rendition:layout" property must be |
      And no other errors or warnings are reported

    @spec @xref:layout-def
    Example: A 'rendition:layout' property with an unknown value is reported
      When checking file 'rendition-layout-unknown-value-error.opf'
      Then error RSC-005 is reported
      And the message contains 'The value of the "rendition:layout" property must be'
      And no other errors or warnings are reported

  Rule: The 'rendition:layout' cardinality is zero or one

    @spec @xref:layout-def
    Example: A 'rendition:layout' property declared more than once is reported
      When checking file 'rendition-layout-duplicate-error.opf'
      Then error RSC-005 is reported
      And the message contains 'The "rendition:layout" property must not occur more than one time'
      And no other errors or warnings are reported

  Rule: The 'rendition:layout' MUST NOT be used when the 'refines' attribute is present

    @spec @xref:layout-def
    Example: A 'rendition:layout' property on a refining 'meta' element is reported
      When checking file 'rendition-layout-refines-error.opf'
      Then error RSC-005 is reported
      And the message contains "refines"
      And no other errors or warnings are reported


  ### D.4.1.2 Layout overrides

  Rule: 'rendition:layout-pre-paginated' MAY be specified only for spine itemref elements.

    @spec @xref:sec-layout-pre-paginated
    Example: A 'rendition:layout-pre-paginated' property can be used as a spine override
      When checking file 'rendition-layout-pre-paginated-override-valid.opf'
      Then no errors or warnings are reported

  Rule: 'rendition:layout-pre-paginated' MUST NOT be specified with roll layouts.

    @spec @xref:sec-layout-pre-paginated
    Example: A 'rendition:layout-pre-paginated' override in a roll publication is reported
      When checking file 'rendition-layout-pre-paginated-override-roll-error.opf'
      Then error RSC-005 is reported
      And the message contains '"rendition:layout-pre-paginated" must not be used in roll publications'
      And no other errors or warnings are reported

  Rule: Layout overrides MUST NOT be paired with another layout override property in the same properties attribute

    @spec @xref:sec-layout-pre-paginated @xref:sec-layout-reflowable
    Example: two 'rendition:layout' overrides in the same 'itemref' element are reported
      When checking file 'rendition-layout-override-conflict-error.opf'
      Then error RSC-005 is reported
      And the message contains "are mutually exclusive"
      And no other errors or warnings are reported

  Rule: 'rendition:layout-pre-paginated' MAY be specified only for spine itemref elements.

    @spec @xref:sec-layout-reflowable
    Example: A 'rendition:layout-reflowable' property can be used as a spine override
      When checking file 'rendition-layout-reflowable-override-valid.opf'
      Then no errors or warnings are reported

  Rule: 'rendition:layout-reflowable' MUST NOT be specified with roll layouts.

    @spec @xref:sec-layout-reflowable
    Example: A 'rendition:layout-reflowable' override in a roll publication is reported
      When checking file 'rendition-layout-reflowable-override-roll-error.opf'
      Then error RSC-005 is reported
      And the message contains '"rendition:layout-reflowable" must not be used in roll publications'
      And no other errors or warnings are reported


  ### D.4.2 Synthetic spread placement

    @spec @xref:page-spread
    Example: The 'rendition:page-spread-*' properties can be used without the prefix
      When checking file 'rendition-page-spread-itemref-unprefixed-valid.opf'
      Then no errors or warnings are reported

    @spec @xref:page-spread
    Example: The 'rendition:page-spread-*' properties values are mutually exclusive
      When checking file 'rendition-page-spread-itemref-conflict-error.opf'
      Then error RSC-005 is reported
      And the message contains "are mutually exclusive"
      And no other errors or warnings are reported


  ## D.5 Custom rendering properties

  Rule: Custom properties MUST NOT be defined with a rendition: prefix 

    @spec @xref:sec-rendering-custom-properties
    Example: A custom rendition property using the 'rendition' prefix is reported
      When checking file 'rendition-property-unknown-error.opf'
      Then error OPF-027 is reported
      And no other errors or warnings are reported
  
