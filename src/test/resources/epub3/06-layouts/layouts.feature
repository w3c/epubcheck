Feature: EPUB 3 — Layouts


  Checks conformance to the "Layouts" section of the EPUB 3.4 specification:
    https://www.w3.org/TR/epub-34/#sec-layouts


  Background: 
    Given EPUB test files located at '/epub3/06-layouts/files/'
    And EPUBCheck with default settings


  ## 6.2 Reflowable layouts

	Rule: Reflowable layouts are the default for EPUB 3.

    @spec @xref:sec-reflowable
    Example: Verify a minimal reflowable publication
      When checking EPUB 'layout-reflowable-valid'
      Then no errors or warnings are reported

	Rule: It is possible to override a pre-paginated fixed layout to specify that some spine items are reflowable.

    @spec @xref:sec-reflowable
    Example: Verify a reflowable overriden item in a pre-paginated publication
      When checking EPUB 'layout-reflowable-override-valid'
      Then no errors or warnings are reported

  ## 6.3 Fixed layouts

  ### 6.3.1 Pre-paginated

  Rule: Pre-paginated fixed layout publications are allowed in EPUB 3.

    @spec @xref:sec-pre-paginated
    Example: Verify a minimal pre-paginated publication
      When checking EPUB 'layout-pre-paginated-valid'
      Then no errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Verify that fixed layout constraints apply to pre-paginated publication content
      When checking EPUB 'layout-pre-paginated-content-invalid-fxl-error'
      Then error HTM-047 is reported
      And no other errors or warnings are reported

  Rule:  it is possible to override a reflowable layout to specify that some spine items are pre-paginated.

    @spec @xref:sec-pre-paginated
    Example: Verify a pre-paginated overriden item in a reflowable publication
      When checking EPUB 'layout-pre-paginated-override-valid'
      Then no errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Verify that fixed layout constraints apply to pre-paginated overridden content
      When checking EPUB 'layout-pre-paginated-override-content-invalid-fxl-error'
      Then error HTM-047 is reported
      And no other errors or warnings are reported

  Rule: Pre-paginated spine items MUST reference a fixed layout document or there MUST be one in the manifest fallback chain.

    @spec @xref:sec-pre-paginated
    Example: Report a pre-paginated publication item that is not a fixed layout document
      When checking EPUB 'layout-pre-paginated-content-reflowable-error'
      Then error HTM-046 is reported
      And no other errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Report a pre-paginated overridden item that is not a fixed layout document
      When checking EPUB 'layout-pre-paginated-override-content-reflowable-error'
      Then error HTM-046 is reported
      And no other errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Verify a pre-paginated publication item with a fixed layout document in its fallback chain  
      When checking EPUB 'layout-pre-paginated-fallback-valid'
      Then no errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Verify a pre-paginated overridden item with a fixed layout document in its fallback chain
      When checking EPUB 'layout-pre-paginated-override-fallback-valid'
      Then no errors or warnings are reported

    @spec @xref:sec-pre-paginated
    Example: Report a pre-paginated item that has no fixed layout document in its fallback chain
      When checking EPUB 'layout-pre-paginated-fallback-reflowable-error'
      Then error HTM-046 is reported
      And no other errors or warnings are reported

  ### 6.3.1.1 Synthetic spreads

  Rule: The automatic population behavior MAY be overridden by specifying [synthetic spread placement properties]

    @spec @xref:page-spread
    Example: 'page-spread-*' properties can be used on content of a pre-paginated publication
      Given the reporting level is set to USAGE
      When checking EPUB 'layout-page-spread-pre-paginated-valid'
      Then no errors or warnings are reported
      And no usages are reported

    @spec @xref:page-spread
    Example: 'page-spread-*' properties can be used on pre-paginated content of a reflowable publication
      Given the reporting level is set to USAGE
      When checking EPUB 'layout-page-spread-pre-paginated-override-valid'
      Then no errors or warnings are reported
      And no usages are reported

  Rule: Synthetic spread placement properties only apply to pre-paginated content

    @spec @xref:page-spread
    Example: 'page-spread-*' properties used on content of reflowable publications are reported
      Given the reporting level is set to USAGE
      When checking EPUB 'layout-page-spread-reflowable-usage'
      Then usage OPF-100 is reported 5 times (3 prefixed properties and 2 unprefixed properties)
      But no errors or warnings are reported

    @spec @xref:page-spread
    Example: 'page-spread-*' properties used on reflowable content of a pre-paginated publications is reported
      Given the reporting level is set to USAGE
      When checking EPUB 'layout-page-spread-reflowable-override-usage'
      Then usage OPF-100 is reported
      But no errors or warnings are reported

    @spec @xref:page-spread
    Example: 'page-spread-*' properties used on content of reflowable publications are reported
      Given the reporting level is set to USAGE
      When checking EPUB 'layout-page-spread-roll-usage'
      Then usage OPF-100 is reported
      But no errors or warnings are reported


  ### 6.3.2 Roll

  Rule: Roll layout publications are allowed in EPUB 3.

    @spec @xref:sec-roll
    Example: Verify a minimal roll publication
      When checking EPUB 'layout-roll-valid'
      Then no errors or warnings are reported

  Rule: When a roll layout is declared, each spine item MUST reference a fixed-layout document.

    @spec @xref:sec-roll
    Example: Report a roll publication item that is not a fixed layout document
      When checking EPUB 'layout-roll-content-reflowable-error'
      Then error HTM-046 is reported
      And no other errors or warnings are reported

    @spec @xref:sec-roll
    Example: Verify a roll publication item with a fixed layout document in its fallback chain  
      When checking EPUB 'layout-roll-fallback-valid'
      Then no errors or warnings are reported

    @spec @xref:sec-roll
    Example: Report a roll item that has no fixed layout document in its fallback chain
      When checking EPUB 'layout-roll-fallback-reflowable-error'
      Then error HTM-046 is reported
      And no other errors or warnings are reported
