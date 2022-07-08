 Feature: EPUB 3 ▸ Publication Resources

  
  Checks conformance to the "Publication resources" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-publication-resources


  Background: 
    Given EPUB test files located at '/epub3/03-resources/files/'
    And EPUBCheck with default settings


  ## 3.2 Core Media Types
  

  ####  Audio core media types

  Scenario: items with Core Media Types do not require fallbacks
      The test document contains one item of each supported core media types
    When checking file 'resources-core-media-types-valid.opf'
    Then no errors or warnings are reported

  Scenario: items with Core Media Types that are not preferred types are reported as usage
      The test document contains one item of each non-preferred core media types
    When the reporting level is set to USAGE
    And checking EPUB 'resources-core-media-types-not-preferred-valid.opf'
    Then Usage OPF-090 is reported 6 times
    And no errors or warnings are reported   
    

  Scenario: Verify MP3 audio is allowed
    When checking EPUB 'resources-cmt-audio-mp3-valid'
    Then no errors or warnings are reported

  Scenario: Verify AAC/MP4 audio is allowed
    When checking EPUB 'resources-cmt-audio-mp4-valid'
    Then no errors or warnings are reported

  Scenario: Verify OPUS audio is allowed
    When checking EPUB 'resources-cmt-audio-opus-valid'
    Then no errors or warnings are reported
    
  Scenario: Report foreign audio used with no available fallback
    When checking EPUB 'resources-cmt-audio-foreign-error'
    Then error MED-002 is reported
    And no other errors or warnings are reported
  

  ####  Image core media types

  Scenario: Verify GIF images are allowed
    When checking EPUB 'resources-cmt-image-gif-valid'
    Then no errors or warnings are reported

  Scenario: Verify JPEG images are allowed
    When checking EPUB 'resources-cmt-image-jpg-valid'
    Then no errors or warnings are reported

  Scenario: Verify PNG images are allowed
    When checking EPUB 'resources-cmt-image-png-valid'
    Then no errors or warnings are reported

  Scenario: Verify WebP images are allowed
    When checking EPUB 'resources-cmt-image-webp-valid'
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


  ### 3.3 Foreign Resources
  
  Scenario: Verify that an unreferenced foreign resource can be included without fallback
    When checking EPUB 'resources-foreign-res-unused-valid'
    Then no errors or warnings are reported
    

  Scenario: Report a `picture` element with a foreign resource in its `img src` fallback  
    When checking EPUB 'content-xhtml-picture-fallback-img-foreign-src-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a `picture` element with a foreign resource in its `img srcset` fallback
    When checking EPUB 'content-xhtml-picture-fallback-img-foreign-srcset-error'
    Then error MED-007 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Verify the `picture source` element can reference foreign resources so long as the `type` attribute is declared
    When checking EPUB 'content-xhtml-picture-source-foreign-with-type-valid'
    Then no errors or warnings are reported

  Scenario: Report a `picture source` element that does not include a `type` attribute for a foreign resource
    When checking EPUB 'content-xhtml-picture-source-foreign-no-type-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a `picture source` element that references a foreign resource but incorrectly states a core media type in its `type` attribute
    When checking EPUB 'content-xhtml-picture-source-foreign-with-cmt-type-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported

  Scenario: Verify test that a foreign resource used in an HTML `link` can be included without fallback
    # FIXME #1118 this test does not match the specification
    When checking EPUB 'content-xhtml-foreign-res-in-link-valid'
    Then no errors or warnings are reported


  ## 3.6 Resources Locations
  
  
  
  Scenario: Allow audio resources to be remote 
    When checking file 'resources-remote-audio-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: remote XHTML document is not detected in single-document mode
  	Remote resources checks depend on publication-wide validation
    (e.g. to check if the resource is used a font)
    When checking file 'resources-remote-xhtml-error.opf'
    Then no errors or warnings are reported
    
  Scenario: remote SVG document is not detected in single-document mode
    Remote resources checks depend on publication-wide validation
    (e.g. to check if the resource is used a font)
    When checking file 'resources-remote-svg-font-valid.opf'
    Then no errors or warnings are reported
    

  Scenario: Verify that remote audio resources are allowed
    When checking EPUB 'resources-remote-audio-valid'
    Then no errors or warnings are reported

  Scenario: Verify that remote audio resources are allowed anywhere
    (not only in `audio` elements)
    When checking EPUB 'resources-remote-audio-object-valid'
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

  Scenario: Verify that remote video resources are allowed anywhere
    (not only in `video` elements)
    When checking EPUB 'resources-remote-audio-object-valid'
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
    And no other errors or warnings are reported

  Scenario: Verify that a remote core media type resource is allowed when used by a script
    Given the reporting level set to usage
    When checking EPUB 'resources-remote-resource-for-script-cmt-valid'
    Then usage OPF-018b is reported (since the `remote-resources` property couldn't be verified)
    And usage RSC-006b is reported (to suggest checking scripts manually)
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
