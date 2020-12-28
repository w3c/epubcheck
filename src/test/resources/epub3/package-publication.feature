Feature: EPUB 3 ▸ Packages ▸ Full Publication Checks


  Checks conformance to the EPUB Packages 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-packages.html

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  Scenario: Verify a minimal EPUB
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  ##  3. Package Document

  ### 3.4.1 The package Element

	# FIXME the current API doesn’t allow the version to be explicitly set
	# PKG-001 should either be removed, or made a fatal error
  Scenario: Report when checking an EPUB 3 explicitly against EPUB 2.0.1
    Given EPUBCheck configured to check EPUB 2 rules
    When checking EPUB 'minimal'
    #Then error PKG-001 is reported
    And no other errors or warnings are reported

  ###  3.4.2 Shared Attributes

  ####  xml:lang

  # FIXME does this test really requires a full EPUB?
  Scenario: Verify that three-character language codes are allowed (issue 615)
    When checking EPUB 'package-lang-three-char-code-valid'
    Then no errors or warnings are reported


  ###  3.4.3 Metadata
  
  ####  3.4.3.5 The link Element

  Scenario: Report a package metadata link to a missing resource
    When checking EPUB 'package-link-missing-resource-error'
    Then warning RSC-007w is reported
    And no other errors or warnings are reported


  ###  3.4.4 Manifest

  ####  3.4.4.2 The item Element

  Scenario: Report duplicate declarations of a resource in the package document manifest
    When checking EPUB 'package-manifest-duplicate-resource-error'
    Then error OPF-074 is reported
    And no other errors or warnings are reported

  Scenario: Report a resource declared in the package document but missing from the container
    When checking EPUB 'package-manifest-item-missing-error'
    Then error RSC-001 is reported
    And no other errors or warnings are reported

  Scenario: Report a manifest item path with unencoded spaces
    See issue #239 for why this needs to also be checked at the publication level
    When checking file 'package-manifest-item-with-spaces-warning'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported

  Scenario: Report fonts declared in the package document but missing from the container
    When checking EPUB 'package-manifest-fonts-missing-error'
    Then error RSC-001 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report a CSS file declared with an invalid media type and no fallback
    When checking EPUB 'package-manifest-css-wrong-media-type-error'
    Then error CSS-010 is reported
    And no errors or warnings are reported


  ####  3.4.4.3 Manifest Fallbacks

  Scenario: Report a circular manifest fallback chain
    When checking EPUB 'package-manifest-fallback-circular-error'
    Then error OPF-045 is reported 4 times
    And error MED-003 is reported
    And no other errors or warnings are reported

  Scenario: Report a manifest fallback that references a non-existent resource
    When checking EPUB 'package-manifest-fallback-non-resolving-error'
    Then error RSC-005 is reported
    And the message contains 'manifest item element fallback attribute must resolve to another manifest item'
    And error MED-003 is reported
    And no other errors or warnings are reported

  #### 3.4.4.4 The bindings Element

  Scenario: Report a bindings handler for a type that already has a handler
    When checking file 'package-bindings-handler-duplicate-error'
    Then warning RSC-017 is reported (since bindings is deprecated)
    And error OPF-009 is reported (to report the duplicate handler)
    And no other errors or warnings are reported

  Scenario: Report a bindings handler that is not a scripted document
    When checking file 'package-bindings-handler-not-scripted-error'
    Then warning RSC-017 is reported (since bindings is deprecated)
    And error OPF-046 is reported (to report the duplicate handler)
    And no other errors or warnings are reported
 
 
  ### 3.4.7 Leegacy
  
  #### 3.4.7.3

  Scenario: Verify a publication featuring a legacy NCX navigation document
    When checking EPUB 'package-ncx-valid'
    Then no errors or warnings are reported

  Scenario: Report validation errors in legacy NCX documents
    When checking EPUB 'package-ncx-invalid-error'
    Then error RSC-012 is reported
    And the message contains 'Fragment identifier is not defined'
    And no other errors or warnings are reported

  Scenario: Report as a USAGE a NCX which does not link to all spine items
    Given the reporting level set to USAGE
    When checking EPUB 'package-ncx-missing-references-to-spine-valid'
    Then no errors or warnings are reported
    And usage OPF-059 is reported

  ### 3.4.5 Spine

  Scenario: Report a missing spine
    When checking EPUB 'package-spine-missing-error'
    Then the following errors are reported
      | RSC-005 | missing required element "spine"                 |
      | RSC-011 | reference to a resource that is not a spine item | # in the Nav Doc
    And fatal error OPF-019 is reported
    And no other errors or warnings are reported

  #  E. Manifest Properties

  ##  E.2 Manifest item Properies
  
  Scenario: Report an unknown manifest item property
    When checking EPUB 'package-manifest-prop-unknown-error'
    Then error OPF-027 is reported
    And no other errors or warnings are reported


  ###  E.2.2 mathml

  Scenario: Verify content documents are identified as containing mathml
    When checking EPUB 'package-mathml-valid'
    Then no errors or warnings are reported


  ###  E.2.4 remote-resources

  Scenario: Report the declaration of the `remote-resources` property when the content has no script
    When checking EPUB 'package-manifest-prop-remote-resource-declared-but-unnecessary-error'
    Then warning OPF-018 is reported
    And no other errors or warnings are reported

  Scenario: Report a reference a remote resource when the `remote-resources` property is not set in the manifest
    When checking EPUB 'package-manifest-prop-remote-resource-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report the incorrect use of the `remote-resources` property for a resource defined in an `object` `param` element (issue 249)
    When checking EPUB 'package-manifest-prop-remote-resource-object-param-warning'
    Then warning OPF-018 is reported
    And no other errors or warnings are reported

  Scenario: Report a media overlay document with remote resources but missing the `remote-resources` property
    When checking EPUB 'resources-remote-audio-in-overlays-missing-property-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported


  ###  E.2.5 scripted

  Scenario: Report the declaration of the `scripted` property when the content has no script
    When checking EPUB 'package-manifest-prop-scripted-declared-but-unnecessary-error'
    Then error OPF-015 is reported
    And no other errors or warnings are reported

  Scenario: Report a scripted document without the `scripted` property declared in the package document
    When checking EPUB 'package-manifest-prop-scripted-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Verify that script data blocks do not require the `scripted` property to be defined in the manifest
    When checking EPUB 'package-manifest-prop-scripted-not-required-for-script-data-block-valid'
    Then no errors or warnings are reported

  ###  E.2.6 svg

  Scenario: Report the declaration of the `svg` property when the content has no embedded SVG
    When checking EPUB 'package-manifest-prop-svg-declared-but-unnecessary-error'
    Then error OPF-015 is reported
    And no other errors or warnings are reported

  Scenario: Report references to embedded SVG when the `svg` property is not set in the manifest 
    When checking EPUB 'package-manifest-prop-svg-undeclared-error'
    Then error OPF-014 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report reference to an embedded SVG when the `svg` property is not set in the manifest (one reference is set properly)
    When checking EPUB 'package-manifest-prop-svg-undeclared-partial-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported


  ###  E.2.7 switch

  Scenario: Report a content document without the `switch` property declared in the manifest
    When checking EPUB 'package-manifest-prop-switch-not-declared-error'
    Then error OPF-014 is reported
    And warning RSC-017 is reported
    And the message contains 'The "epub:switch" element is deprecated'
    And no other errors or warnings are reported
