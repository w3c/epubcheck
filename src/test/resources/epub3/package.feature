Feature: EPUB 3 Packages
  
  Checks conformance to specification rules related to EPUB Packages:
  https://www.w3.org/publishing/epub32/epub-packages.html
  
  This feature file contains tests for EPUBCheck running in default mode to check
  full EPUB publications.
  
  Note:
  - Tests that do not require a full publication but a single Package Document
    are defined in the `package-document.feature` feature file.   
  - Tests related to EPUB Navigation Documents are defined in the `navigation.feature`
    and `navigation-document.feature` feature files.

  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  #  3. Package Document

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

  Scenario: Report a reference a remote resource when the `remote-resources` property is not set in the manifest
    When checking EPUB 'package-manifest-prop-remote-resource-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report the incorrect use of the `remote-resources` property for a resource defined in an `object` `param` element (issue 249)
    When checking EPUB 'package-manifest-prop-remote-resource-object-param-warning'
    Then warning OPF-018 is reported
    And no other errors or warnings are reported


  ###  E.2.5 scripted

  Scenario: Report a scripted document without the `scripted` property declared in the package document
    When checking EPUB 'package-manifest-prop-scripted-undeclared-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Verify that script data blocks do not require the `scripted` property to be defined in the manifest
    When checking EPUB 'package-manifest-prop-scripted-not-required-for-script-data-block-valid'
    Then no errors or warnings are reported

  ###  E.2.6 svg

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
    And the message contains "The 'epub:switch' element is deprecated"
    And no other errors or warnings are reported
