 Feature: EPUB 3 — Publication Resources

  
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
    
  @spec @xref:sec-foreign-resources
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

  
  ### 3.4 Exempt resources

  Scenario: Verify that an unreferenced foreign resource can be included without fallback
    When checking EPUB 'resources-foreign-res-unused-valid'
    Then no errors or warnings are reported

  #### Links

  @spec @xref:sec-exempt-resources
  Scenario: Verify a linked resource without a fallback
    When checking EPUB 'content-xhtml-link-no-fallback-valid'
    And no errors or warnings are reported

  Scenario: Verify test that a foreign resource used in an HTML `link` can be included without fallback
    # FIXME #1118 this test does not match the specification
    When checking EPUB 'content-xhtml-foreign-res-in-link-valid'
    Then no errors or warnings are reported

  Scenario: Verify an xpgt style sheet with a manifest fallback to css
    See issues #271, #241
    When checking EPUB 'content-xhtml-xpgt-manifest-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Verify an xpgt style sheet with an implicit fallback to css in an xhtml document
    See issues #271, #241
    When checking EPUB 'content-xhtml-xpgt-implicit-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-exempt-resources
  Scenario: Verify an xpgt style sheet without a fallback
    See issues #271, #241
    When checking EPUB 'content-xhtml-xpgt-no-fallback-valid'
    Then no errors or warnings are reported
    

  ## 3.5 Resource fallbacks
  
  ### 3.5.1 Manifest fallbacks
  
  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow non-CMT file to be in the spine if they have an XHTML Content Document fallback
    Note: here an audio file is used in the spine
    When checking file 'fallback-to-xhtml-valid.opf'
    Then no errors or warnings are reported
    
  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow an SVG Content Document to be used as a fallback
    Note: here an image file is used in the spine
    When checking file 'fallback-to-svg-valid.opf'
    Then no errors or warnings are reported
    
  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow a deep fallback chain as long as it contains a Content Document
    Note: here a font file is used in the spine
    When checking file 'fallback-chain-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Report a cycle in the fallback chain
    When checking file 'fallback-cycle-error.opf'
    Then error OPF-045 is reported (circular reference)
    And error OPF-044 is reported (no Content Document fallback was found) 
    And no other errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Report files that aren't Content Documents (like audio) in spine when they don't have a fallback  
    Note: here an audio file is used in the spine
    When checking file 'fallback-missing-error.opf'
    Then error OPF-043 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Report a circular manifest fallback chain
    When checking EPUB 'resources-manifest-fallback-circular-error'
    Then error OPF-045 is reported 4 times
    And error MED-003 is reported
    And no other errors or warnings are reported


  ### 3.5.2 Intrinsic fallbacks
  
  #### 3.5.2.2 HTML img fallbacks

  @spec @xref:sec-fallbacks-img
  Scenario: Verify that an `img` element can reference a foreign resource so long as it has a manifest fallback (and is not in a `picture` element)
    When checking EPUB 'content-xhtml-img-manifest-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Verify that an `img srcset` can reference foreign resources when they have manifest fallbacks
    When checking EPUB 'content-xhtml-img-srcset-manifest-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report an `img src` with a foreign resource and no manifest fallback (when the `img` is not in a `picture` element)
    When checking EPUB 'content-xhtml-img-src-no-manifest-fallback-error'
    Then error MED-003 is reported
    And no other errors or warnings are reported
  
  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture` element with a foreign resource in its `img src` fallback  
    When checking EPUB 'content-xhtml-picture-fallback-img-foreign-src-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture` element with a foreign resource in its `img srcset` fallback
    When checking EPUB 'content-xhtml-picture-fallback-img-foreign-srcset-error'
    Then error MED-007 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Verify the `picture source` element can reference foreign resources so long as the `type` attribute is declared
    When checking EPUB 'content-xhtml-picture-source-foreign-with-type-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture source` element that does not include a `type` attribute for a foreign resource
    When checking EPUB 'content-xhtml-picture-source-foreign-no-type-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture source` element that references a foreign resource but incorrectly states a core media type in its `type` attribute
    When checking EPUB 'content-xhtml-picture-source-foreign-with-cmt-type-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported


  ## 3.6 Resources Locations
  
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
    
  @spec @xref:sec-resource-locations
  Scenario: Allow audio resources to be remote 
    When checking file 'resources-remote-audio-valid.opf'
    Then no errors or warnings are reported
    
  @spec @xref:sec-resource-locations
  Scenario: Verify that remote audio resources are allowed
    When checking EPUB 'resources-remote-audio-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote audio resources are allowed anywhere
    (not only in `audio` elements)
    When checking EPUB 'resources-remote-audio-object-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote audio resources defined in the `sources` element are allowed
    When checking EPUB 'resources-remote-audio-sources-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote audio resources with foreign media types are allowed with a fallback
    When checking EPUB 'resources-remote-audio-sources-foreign-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote video resources are allowed
    When checking EPUB 'resources-remote-video-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote video resources are allowed anywhere
    (not only in `video` elements)
    When checking EPUB 'resources-remote-audio-object-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a reference to a remote resource from an `iframe` element when the resource is declared in the package document (issue 852)
    When checking EPUB 'resources-remote-iframe-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a reference to a remote resource from an `iframe` element when the resource is not declared in package document
    When checking EPUB 'resources-remote-iframe-undeclared-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote image that is declared in the packaged document
    When checking EPUB 'resources-remote-img-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote image that is not declared in the package document
    When checking EPUB 'resources-remote-img-undeclared-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote image declared in the package document when it is referenced from an `img` tag and also retrieved by a script
    When checking EPUB 'resources-remote-img-in-script-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote fonts are allowed
    When checking EPUB 'resources-remote-font-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote SVG fonts are allowed
    When checking EPUB 'resources-remote-font-svg-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote fonts of unknown type declared in CSS `@font-face` are allowed
    When checking EPUB 'resources-remote-font-in-css-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that remote fonts of unknown type declared in SVG `font-face-uri` are allowed
    When checking EPUB 'resources-remote-font-in-svg-valid'
    Then no errors or warnings are reported
  
  @spec @xref:sec-resource-locations
  Scenario: Report a remote SVG font when it is referenced from an HTML `img` element
    When checking EPUB 'resources-remote-font-svg-also-used-as-img-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a reference to a remote resource from an `object` element when the resource is not declared in package document
    When checking EPUB 'resources-remote-object-undeclared-error'
    # FIXME the error should only be reported once
    Then error RSC-006 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote SVG Content Document
    When checking EPUB 'resources-remote-svg-contentdoc-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that a remote foreign resource is allowed when used by a script
    Given the reporting level set to usage
    When checking EPUB 'resources-remote-resource-for-script-foreign-valid'
    Then usage OPF-018b is reported (since the `remote-resources` property couldn't be verified)
    And usage RSC-006b is reported (to suggest checking scripts manually)
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Verify that a remote core media type resource is allowed when used by a script
    Given the reporting level set to usage
    When checking EPUB 'resources-remote-resource-for-script-cmt-valid'
    Then usage OPF-018b is reported (since the `remote-resources` property couldn't be verified)
    And usage RSC-006b is reported (to suggest checking scripts manually)
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote top-level Content Documents
    When checking EPUB 'resources-remote-spine-item-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote script
    When checking EPUB 'resources-remote-script-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote stylesheet
    When checking EPUB 'resources-remote-stylesheet-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Warn about a remote resource with a non `https` URL
    When checking EPUB 'resources-remote-not-https-warning'
    Then warning RSC-031 is reported 3 times
    And no other errors or warnings are reported

  ## 3.7 Data URLs

  @spec @xref:sec-data-urls
  Scenario: Allow a data URL in a manifest item not in the spine
    When checking file 'data-url-in-manifest-item-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in a manifest item referenced in the spine
    When checking file 'data-url-in-manifest-item-in-spine-error.opf'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in the `href` attribute of an HTML `a` element
    When checking file 'data-url-in-html-a-href-error.xhtml'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in the `href` attribute of an SVG `a` element
    When checking file 'data-url-in-svg-a-href-error.xhtml'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in the `href` attribute of an HTML `area` element
    When checking file 'data-url-in-html-area-href-error.xhtml'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Allow a data URL defining an exmpt resource (in an HTML `link` element)
    When checking EPUB 'data-url-in-html-link-exempt-valid'
    And no errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Allow a data URL defining a CMT resource (in an HTML `img` element)
    When checking EPUB 'data-url-in-html-img-cmt-valid'
    And no errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Allow a data URL defining a foreign resource with intrinsic fallback (in an HTML `img` element)
    When checking EPUB 'data-url-in-html-img-foreign-intrinsic-fallback-valid'
    And no errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Allow a data URL defining a foreign resource with a manifest fallback (in an HTML `img` element)
    When checking EPUB 'data-url-in-html-img-foreign-manifest-fallback-valid'
    And no errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL defining a foreign resource with no fallback (in an HTML `img` element)
    When checking EPUB 'data-url-in-html-img-foreign-no-fallback-error'
    Then error MED-003 is reported
    And no other errors or warnings are reported

  ## 3.8 File URLs

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in the package document
    When checking document 'file-url-in-package-document-error.opf'
    Then error RSC-030 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in a content document
    When checking document 'file-url-in-xhtml-content-error.xhtml'
    Then error RSC-030 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in a CSS document
    When checking EPUB 'file-url-in-css-error'
    Then error RSC-030 is reported 2 times (one in the package doc, one in the CSS)
    And no other errors or warnings are reported


  ## 3.9 XML conformance

  @spec @xref:sec-xml-constraints
  Scenario: an XML document encoded as UTF-8 with an encoding declaration is valid 
    When checking file 'xml-encoding-utf8-declared-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: an XML document encoded as UTF-8 with a BOM is valid 
    When checking file 'xml-encoding-utf8-BOM-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: an XML document encoded as UTF-8 with no encoding declaration is valid 
    When checking file 'xml-encoding-utf8-no-declaration-valid.opf'
    Then no errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Warn about an XML document encoded as UTF-16 (with an encoding declaration) 
    When checking file 'xml-encoding-utf16-declared-warning.opf'
    Then warning RSC-027 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Warn about an XML document encoded as UTF-16 (not declared but with a BOM) 
    When checking file 'xml-encoding-utf16-BOM-no-declaration-warning.opf'
    Then warning RSC-027 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Warn about an XML document encoded as UTF-16 (even with an UTF-8 declaration) 
    When checking file 'xml-encoding-utf16-BOM-and-utf8-declaration-warning.opf'
    Then warning RSC-027 is reported
    And fatal error RSC-016 is reported (by the XML parser)
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Report an XML document encoded as ISO-8859-1 (detected in the encoding declaration) 
    When checking file 'xml-encoding-latin1-declaration-error.opf'
    Then error RSC-028 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Report an XML document encoded as UCS-4 (detected with a BOM) 
    When checking file 'xml-encoding-utf32-BOM-error.opf'
    Then error RSC-028 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: Report an XML document declared with an unknown encoding name 
    When checking file 'xml-encoding-unknown-declared-error.opf'
    Then error RSC-028 is reported
    And fatal error RSC-016 is reported (by the XML parser)
    And no other errors or warnings are reported

  @spec @xref:sec-xml-constraints
  Scenario: a not well-formed Package Document is reported 
    When checking file 'conformance-xml-malformed-error.opf'
    Then fatal error RSC-016 is reported (parsing error)
    And no other errors or warnings are reported
    
  @spec @xref:sec-xml-constraints
  Scenario: using a not-declared namespace is not allowed 
    When checking file 'conformance-xml-undeclared-namespace-error.opf'
    Then fatal error RSC-016 is reported (parsing error)
    And no other errors or warnings are reported


  Scenario: Verify an attribute value with leading/trailing whitespace is allowed (issue 332)
    When checking EPUB 'conformance-xml-id-leading-trailing-spaces-valid'
    Then no errors or warnings are reported
