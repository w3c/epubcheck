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

  ## 3.3 Foreign resources
    
  @spec @xref:sec-foreign-resources
  Scenario: Allow a foreign resource in HTML `audio` with a manifest fallback
    When checking EPUB 'foreign-xhtml-audio-manifest-fallback-valid'
    And no other errors or warnings are reported
    
  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in HTML `audio` with no fallbacks
    When checking EPUB 'foreign-xhtml-audio-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported
    
  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in HTML `audio` `source` with no fallback
    When checking EPUB 'foreign-xhtml-audio-source-no-fallback-error'
    Then error RSC-032 is reported
    And no errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Allow a foreign resource in HTML `embed` with a manifest fallback
    When checking EPUB 'foreign-xhtml-embed-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in HTML `embed` with no fallback
    When checking EPUB 'foreign-xhtml-embed-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in HTML video `input` with no fallack
    When checking EPUB 'foreign-xhtml-input-image-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Allow a foreign resource in HTML video `poster` with a manifest fallback
    When checking EPUB 'foreign-xhtml-video-poster-fallback-valid'
    And no errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in HTML video `poster` with no fallack
    When checking EPUB 'foreign-xhtml-video-poster-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Report a foreign resource in MathML `altimg` attribute with no fallack
    When checking EPUB 'foreign-xhtml-math-altimg-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

    
  ## 3.4 Exempt resources

  ### Fonts

  @spec @xref:sec-exempt-resources
  Scenario: Allow foreign font media types without fallbacks 
    When checking EPUB 'foreign-exempt-font-valid'
    Then info CSS-007 is reported
    And no other errors or warnings are reported

  ### Linked resources

  @spec @xref:sec-exempt-resources
  Scenario: Allow foreign linked resources without fallbacks
    When checking EPUB 'foreign-exempt-xhtml-link-valid'
    And no errors or warnings are reported

  @spec @xref:sec-exempt-resources
  Scenario: Allow XPGT style sheets without fallbacks
    See issues #271, #241
    When checking EPUB 'foreign-exempt-xhtml-link-xpgt-no-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Allow an xpgt style sheet to have an explicit manifest fallback
    See issues #271, #241
    When checking EPUB 'foreign-exempt-xhtml-link-xpgt-manifest-fallback-valid'
    Then no errors or warnings are reported

  ### Tracks

  @spec @xref:sec-exempt-resources
  Scenario: Allow foreign text tracks without fallbacks
    When checking EPUB 'foreign-exempt-xhtml-track-valid'
    And no errors or warnings are reported

  ### Video

  @spec @xref:sec-foreign-resources
  Scenario: Allow foreign video in a HTML `video` element without a fallback
    When checking EPUB 'foreign-exempt-xhtml-video-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-foreign-resources
  Scenario: Allow foreign video in a HTML `img` element without a fallback
    When checking EPUB 'foreign-exempt-xhtml-video-in-img-valid'
    Then no errors or warnings are reported

	#### Other

  @spec @xref:sec-exempt-resources
  Scenario: Allow unreferenced foreign resources without fallbacks
    When checking EPUB 'foreign-exempt-unused-valid'
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
  Scenario: Report files that aren't Content Documents (like audio) in spine when they don't have a fallback  
    Note: here an audio file is used in the spine
    When checking file 'fallback-missing-error.opf'
    Then error OPF-043 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow valid manifest fallback chain (single doc)
    When checking file 'fallback-chain-valid.opf'
    And no errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow valid manifest fallback chain (waterfall)
    When checking file 'fallback-chain-waterfall-valid'
    And no errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Allow valid manifest fallback chain (n-to-1)
    When checking file 'fallback-chain-n-to-1-valid'
    And no errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Report a circular manifest fallback chain (single doc)
    When checking file 'fallback-chain-circular-error.opf'
    Then error OPF-045 is reported (circular reference)
    And no other errors or warnings are reported

  @spec @xref:sec-manifest-fallbacks
  Scenario: Report a circular manifest fallback chain (full publication)
    When checking EPUB 'fallback-chain-circular-error'
    Then error OPF-045 is reported
    And no other errors or warnings are reported


  ### 3.5.2 Intrinsic fallbacks
  
  #### 3.5.2.2 HTML `audio` fallbacks
  
  @spec @xref:sec-fallbacks-audio
  Scenario: Report foreign HTML `audio` without fallbacks even with inner flow content
    When checking EPUB 'foreign-xhtml-audio-no-fallback-with-flow-content-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported
    
  @spec @xref:sec-fallbacks-audio
  Scenario: Allow foreign HTML `audio` with a `source` fallback
    When checking EPUB 'foreign-xhtml-audio-source-fallback-valid'
    And no errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Allow remote foreign HTML `audio` with a remote `source` fallback
    When checking EPUB 'foreign-xhtml-audio-source-remote-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-audio
  Scenario: Report foreign audio in HTML `video` element without fallback
    When checking EPUB 'foreign-xhtml-audio-in-video-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported
  
  #### 3.5.2.2 HTML `img` fallbacks

  @spec @xref:sec-fallbacks-img
  Scenario: Verify that an `img` element can reference a foreign resource so long as it has a manifest fallback (and is not in a `picture` element)
    When checking EPUB 'foreign-xhtml-img-manifest-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Verify that an `img srcset` can reference foreign resources when they have manifest fallbacks
    When checking EPUB 'foreign-xhtml-img-srcset-manifest-fallback-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report an `img src` with a foreign resource and no manifest fallback (when the `img` is not in a `picture` element)
    When checking EPUB 'foreign-xhtml-img-src-no-manifest-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture` element with a foreign resource in its `img src` fallback  
    When checking EPUB 'foreign-xhtml-picture-img-src-error'
    Then error MED-003 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture` element with a foreign resource in its `img srcset` fallback
    When checking EPUB 'foreign-xhtml-picture-img-srcset-error'
    Then error MED-003 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Verify the `picture source` element can reference foreign resources so long as the `type` attribute is declared
    When checking EPUB 'foreign-xhtml-picture-source-with-type-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-fallbacks-img
  Scenario: Report a `picture source` element that does not include a `type` attribute for a foreign resource
    When checking EPUB 'foreign-xhtml-picture-source-no-type-error'
    Then error MED-007 is reported
    And no other errors or warnings are reported
  
  #### 3.5.2.3 HTML `script` element

  @spec @xref:html-script-element
  Scenario: Verify a script data block does not require a fallback
    When checking EPUB 'foreign-xhtml-script-datablock-valid'
    Then no errors or warnings are reported


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
    Then error RSC-006 is reported
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
  Scenario: Report a remote stylesheet declared in SVG XML processing instruction
    When checking EPUB 'resources-remote-stylesheet-svg-xmlpi-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Report a remote stylesheet declared in SVG inline style import
    When checking EPUB 'resources-remote-stylesheet-svg-import-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-resource-locations
  Scenario: Warn about a remote resource with a non `https` URL
    When checking EPUB 'resources-remote-not-https-warning'
    Then warning RSC-031 is reported 3 times
    And no other errors or warnings are reported

  ## 3.7 Data URLs

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in a manifest item href (not in the spine)
    When checking file 'data-url-in-manifest-item-error.opf'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in a manifest item href (referenced in the spine)
    When checking file 'data-url-in-manifest-item-in-spine-error.opf'
    Then error RSC-029 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-data-urls
  Scenario: Report a data URL in a package link href
    When checking file 'data-url-in-package-link-error.opf'
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
  Scenario: Report a data URL defining a foreign resource with no fallback (in an HTML `img` element)
    When checking EPUB 'data-url-in-html-img-foreign-no-fallback-error'
    Then error RSC-032 is reported
    And no other errors or warnings are reported

  ## 3.8 File URLs

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in the package document
    When checking document 'file-url-in-package-document-error.opf'
    Then error RSC-030 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in an XHTML content document
    When checking document 'file-url-in-xhtml-content-error.xhtml'
    Then error RSC-030 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-file-urls
  Scenario: Report a file URL used in an SVG content document
    When checking document 'file-url-in-svg-content-error.svg'
    Then error RSC-030 is reported 2 times
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
    
  ## Other: non EPUB-defined checks
  
  ### MIME type mismatch warning

  Scenario: Report an `object` element with a `type` attribute not matching the publication resource type
    When checking EPUB 'type-mismatch-in-object-warning'
    Then warning OPF-013 is reported
    And no other errors or warnings are reported

  Scenario: Report a picture `source` element `type` attribute not matching the publication resource type
    When checking EPUB 'type-mismatch-in-picture-source-warning'
    Then warning OPF-013 is reported
    And no other errors or warnings are reported

  Scenario: Report an audio `source` element `type` attribute not matching the publication resource type
    When checking EPUB 'type-mismatch-in-picture-source-warning'
    Then warning OPF-013 is reported
    And no other errors or warnings are reported

  Scenario: Report an `embed` element with a `type` attribute not matching the publication resource type
    When checking EPUB 'type-mismatch-in-embed-warning'
    Then warning OPF-013 is reported
    And no other errors or warnings are reported
