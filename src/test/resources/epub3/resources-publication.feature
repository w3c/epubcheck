Feature: EPUB 3 ▸ Publication Resources ▸ Full Publication Checks

  
  Checks conformance to the "Publication Resources" section of the EPUB 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-spec.html#sec-publication-resources

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ## 3.1 Core Media Types
  
  # Note: Core Media Types support on the Package Document `item` elements
  #       is tested in the Package Document feature.   
  
  ###  3.1.2 Supported Media Types
  
  ####  Image core media types
  
  Scenario: Verify PNG images are allowed
    When checking EPUB 'resources-cmt-image-png-valid'
    Then no errors or warnings are reported

  Scenario: Verify JPEG images are allowed
    When checking EPUB 'resources-cmt-image-jpg-valid'
    Then no errors or warnings are reported

  Scenario: Verify that JPEG file is not corrupt (issue 567)
    When checking EPUB 'resources-cmt-image-jpg-not-corrupt-valid'
    Then no errors or warnings are reported

  Scenario: Report a corrupt image
    When checking EPUB 'resources-cmt-image-corrupt-error'
    Then error MED-004 is reported
    And error PKG-021 is reported
    And no other errors or warnings are reported

  Scenario: Report a JPEG image declared as 'image/gif'
    When checking EPUB 'resources-cmt-image-jpeg-declared-as-gif-error'
    Then error OPF-029 is reported
    And no other errors or warnings are reported

  Scenario: Report a JPEG image with a '.gif' extension
    When checking EPUB 'resources-cmt-image-wrong-extension-warning'
    Then warning PKG-022 is reported
    And no errors or warnings are reported


  ####  Font core media types

  Scenario: Verify Open Type fonts are allowed
    When checking EPUB 'resources-cmt-font-opentype-valid'
    Then no errors or warnings are reported

  Scenario: Verify SVG fonts are allowed
    When checking EPUB 'resources-cmt-font-svg-valid'
    Then no errors or warnings are reported

  Scenario: Verify font media types not listed in the specification are allowed
    When checking EPUB 'resources-cmt-font-other-mediatype-valid'
    Then info CSS-007 is reported
    And no other errors or warnings are reported


  ### 3.1.3 Foreign Resources
  
  Scenario: Verify that an unreferenced foreign resource can be included without fallback
    When checking EPUB 'resources-foreign-res-unused-valid'
    Then no errors or warnings are reported


  ## 3.2 Resources Locations

  Scenario: Verify that remote audio resources are allowed
    When checking EPUB 'resources-remote-audio-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote audio resources defined in the `sources` element are allowed
    When checking EPUB 'resources-remote-audio-sources-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote audio resources with foreign media types are allowed with a fallback
    When checking EPUB 'resources-remote-audio-sources-foreign-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote video resources are allowed
    When checking EPUB 'resources-remote-video-valid'
    Then no errors or warnings are reported

  Scenario: Report a reference to a remote resource from an `iframe` element when the resource is declared in the package document (issue 852)
    When checking EPUB 'resources-remote-iframe-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a reference to a remote resource from an `iframe` element when the resource is not declared in package document
    When checking EPUB 'resources-remote-iframe-undeclared-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote image that is declared in the packaged document
    When checking EPUB 'resources-remote-img-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote image that is not declared in the package document
    When checking EPUB 'resources-remote-img-undeclared-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote image declared in the package document even when only retrieved by a script
    When checking EPUB 'resources-remote-img-in-script-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote image declared in the package document even when only referenced from an HTML `link` element
    When checking EPUB 'resources-remote-img-in-link-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report an XHTML document with remote audio but without the `remote-resources` property set in the package document
    When checking EPUB 'resources-remote-audio-missing-property-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report remote audio resources not declared in the package document
    When checking EPUB 'resources-remote-audio-undeclared-error'
    Then error RSC-008 is reported
    And error MED-002 is reported (side-effect error about the audio missing a fallback, since its type cannot be known from the OPF declaration)
    And no other errors or warnings are reported

  Scenario: Report remote audio resources defined in `sources` elements but not declared in the package document
    When checking EPUB 'resources-remote-audio-sources-undeclared-error'
    Then error RSC-008 is reported
    And no other errors or warnings are reported

  Scenario: Verify that remote fonts are allowed
    When checking EPUB 'resources-remote-font-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote SVG fonts are allowed
    When checking EPUB 'resources-remote-font-svg-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote fonts of unknown type declared in CSS `@font-face` are allowed
    When checking EPUB 'resources-remote-font-in-css-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote fonts of unknown type declared in SVG `font-face-uri` are allowed
    When checking EPUB 'resources-remote-font-in-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report a remote font not declared in the package document
    When checking EPUB 'resources-remote-font-undeclared-error'
    Then error RSC-008 is reported
    And no other errors or warnings are reported

  Scenario: Report remote fonts in CSS without the `remote-resource` property set in the package document
    When checking EPUB 'resources-remote-font-in-css-missing-property-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report an SVG using remote fonts without the `remote-resource` property set in the package document
    When checking EPUB 'resources-remote-font-in-svg-missing-property-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report an XHTML document using remote fonts in `style` without the `remote-resource` property set in the package document
    When checking EPUB 'resources-remote-font-in-xhtml-missing-property-error'
    Then error OPF-014 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote SVG font when it is referenced from an HTML `img` element
    When checking EPUB 'resources-remote-font-svg-also-used-as-img-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a reference to a remote resource from an `object` element when the resource is not declared in package document
    When checking EPUB 'resources-remote-object-undeclared-error'
    # FIXME the error should only be reported once
    Then error RSC-006 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Reprot a remote SVG Content Document
    When checking EPUB 'resources-remote-svg-contentdoc-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Verify that a remote foreign resource is allowed when used by a script
    Given the reporting level set to usage
    When checking EPUB 'resources-remote-resource-for-script-foreign-valid'
    Then usage OPF-018b is reported (since the `remote-resources` property couldn't be verified)
    And usage RSC-006b is reported (to suggest checking scripts manually)
    And usage SCP-002 is reported (since xmlhttprequest is a secrity risk)
    And usage SCP-010 is reported (since `script` is used)
    And no other errors or warnings are reported

  Scenario: Verify that a remote core media type resource is allowed when used by a script
    Given the reporting level set to usage
    When checking EPUB 'resources-remote-resource-for-script-cmt-valid'
    Then usage OPF-018b is reported (since the `remote-resources` property couldn't be verified)
    And usage RSC-006b is reported (to suggest checking scripts manually)
    And usage SCP-002 is reported (since xmlhttprequest is a secrity risk)
    And usage SCP-010 is reported (since `script` is used)
    And no other errors or warnings are reported

  Scenario: Report a remote top-level Content Documents
    When checking EPUB 'resources-remote-spine-item-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote script
    When checking EPUB 'resources-remote-script-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report a remote stylesheet
    When checking EPUB 'resources-remote-stylesheet-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported


  ##  3.3 XML Conformance

  Scenario: Report an NCX file with a DOCTYPE declaration including the external identifier (issue 305)
    When checking EPUB 'xml-ncx-doctype-external-identifier-error'
    Then error OPF-073 is reported
    And no other errors or warnings are reported

  Scenario: Verify an attribute value with leading/trailing whitespace is allowed (issue 332)
    When checking EPUB 'xml-id-leading-trailing-spaces-valid'
    Then no errors or warnings are reported
