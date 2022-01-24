Feature: EPUB 3 ▸ Content Documents ▸ Full Publication Checks


  Checks conformance to the EPUB Content Documents 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-contentdocs.html

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given EPUB test files located at '/epub3/files/epub/'
    And EPUBCheck with default settings


  ##  2. XHTML Content Documents

  Scenario: Verify that an XHTML Content Document can have any extension
    When checking EPUB 'content-xhtml-file-extension-unusual-valid'
    Then no errors or warnings are reported

  ###  2.2 Content Conformance

  Scenario: Report RelaxNG schema errors when checking a Content Document in a full publication
    When checking EPUB 'content-xhtml-relaxng-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  Scenario: Report Schematron schema errors when checking a Content Document in a full publication
    When checking EPUB 'content-xhtml-schematron-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  ####  Document Properties - HTML Conformance

  #####  base

  Scenario: Verify that a base url can be set
    When checking EPUB 'content-xhtml-base-url-valid'
    Then warning HTM-055 is reported (side effect of `base` being discouraged)
    Then no errors or warnings are reported

  Scenario: Report relative paths as remote resources when HTML `base` is set to an extenal URL (issue 155)
    When checking EPUB 'content-xhtml-base-url-remote-relative-path-error'
    Then warning HTM-055 is reported (side effect of `base` being discouraged)
    Then error RSC-006 is reported
    And no other errors or warnings are reported

  Scenario: Report relative paths as remote resources when `xml:base` is set to an extenal URL (issue 155)
    When checking EPUB 'content-xhtml-xml-base-url-remote-relative-path-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported


  #####  data attributes

  Scenario: Report invalid elements after a `data-*` attribute (issue 189 - was allowed by stripping of `data-*` attributes)
    When checking EPUB 'content-xhtml-data-attr-removal-markup-error'
    Then error RSC-005 is reported
    And the message contains 'element "somebadxhtmlformatting" not allowed here'
    And no other errors or warnings are reported

  Scenario: Verify fragment identifiers are allowed in attributes after a `data-*` declaration (issue 198 - caused error from stripping of `data-*` attributes)
    When checking EPUB 'content-xhtml-data-attr-removal-fragments-valid'
    Then no errors or warnings are reported


  #####  hyperlinks

  Scenario: Report as an INFO a hyperlink to a resource in the local file system
    See issue #289
    When checking EPUB 'content-xhtml-link-to-local-file-valid'
    Then info HTM-053 is reported
    And no errors or warnings are reported

  Scenario: Do not report escaped hyperlinks to resources in the local file system
    See issue #1182
    When checking EPUB 'content-xhtml-link-to-local-file-escaped-valid'
    Then info HTM-053 is reported 0 times
    And no errors or warnings are reported
    
  Scenario: Report a hyperlink to a resource missing from the publication
    When checking EPUB 'content-xhtml-link-to-missing-doc-error'
    Then error RSC-007 is reported
    And no errors or warnings are reported

  Scenario: Report a hyperlink to a missing identifier
    When checking EPUB 'content-xhtml-link-to-missing-id-error'
    Then error RSC-012 is reported
    And no errors or warnings are reported

  Scenario: Report a hyperlink to a mising identifier in another document
    When checking EPUB 'content-xhtml-link-to-missing-id-xref-error'
    Then error RSC-012 is reported
    And no errors or warnings are reported

  Scenario: Verify that href values that only contain whitepace are allowed (issue 225 asked for a warning, but an empty string is a valid URL)
    When checking EPUB 'content-xhtml-link-href-empty-valid'
    Then no errors or warnings are reported

  Scenario: Verify `object` element does not cause issues with fragment references (issue 226)
    When checking EPUB 'content-xhtml-link-fragment-after-object-valid'
    Then no errors or warnings are reported

  Scenario: Verify that relative paths starting with a single dot are resolved properly (issue 270)
    When checking EPUB 'content-xhtml-link-rel-path-dot-valid'
    Then no errors or warnings are reported

  Scenario: Report a link to a resource that is not in the spine
    When checking EPUB 'content-xhtml-link-out-of-spine-error'
    Then error RSC-011 is reported
    And no other errors or warnings are reported

  Scenario: Report a reference from an XHTML doc to a resource not declared in the manifest
    When checking EPUB 'content-xhtml-referenced-resource-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report fragment identifiers used in stylesheet URLs
    When checking document 'content-xhtml-link-stylesheet-fragment-id-error'
    Then error RSC-013 is reported
    Then no other errors or warnings are reported

  # FIXME not sure this error is legit
  Scenario: Report a hyperlink to SVG symbol ("incompatible resource type")
    When checking document 'content-xhtml-link-to-svg-fragment-error'
    Then error RSC-014 is reported
    Then no other errors or warnings are reported

  Scenario: Verify a linked resource without a fallback
    When checking EPUB 'content-xhtml-link-no-fallback-valid'
    And no errors or warnings are reported
    
  #####  iframes
  
  Scenario: Verify that an `iframe` can reference another XHTML document
    When checking EPUB 'content-xhtml-iframe-basic-valid'
    Then no errors or warnings are reported


  #####  img

  Scenario: Verify that an `img` element can reference a foreign resource so long as it has a manifest fallback (and is not in a `picture` element)
    When checking EPUB 'content-xhtml-img-manifest-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Verify that an `img srcset` can reference foreign resources when they have manifest fallbacks
    When checking EPUB 'content-xhtml-img-srcset-manifest-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Report an `img src` with a foreign resource and no manifest fallback (when the `img` is not in a `picture` element)
    When checking EPUB 'content-xhtml-img-src-no-manifest-fallback-error'
    Then error MED-003 is reported
    And no other errors or warnings are reported

  Scenario: Verify that `img` element can reference SVG fragments
    When checking EPUB 'content-xhtml-img-fragment-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report non-SVG images referenced as fragments
    When checking EPUB 'content-xhtml-img-fragment-non-svg-warning'
    Then warning RSC-009 is reported 2 times (1 for an HTML `img` element, 1 for an SVG `image` element)
    And no other errors or warnings are reported

  Scenario: Report references to undeclared resources in `img srcset`
    When checking EPUB 'content-xhtml-img-srcset-undeclared-error'
    Then error RSC-008 is reported (undeclared resource in srcset)
    And warning OPF-003 is reported (undeclared resource in container)
    And no other errors or warnings are reported


  #####  lang

  # FIXME HTM-017 seems to be unnecessary
  Scenario: Report a mismatch in `lang` and `xml:lang` attributes
    When checking document 'content-xhtml-lang-xml-lang-mismatch-error'
    Then the following errors are reported
      | RSC-005 | lang and xml:lang attributes must have the same value    |
      | HTM-017 | different language value in attributes xml:lang and lang |
    And no other errors or warnings are reported


  #####  MathML

  Scenario: Report a MathML formula with an alternative image that cannot be found
    When checking EPUB 'content-xhtml-mathml-altimg-not-found-warning'
    Then warning RSC-018 is reported
    And no other errors or warnings are reported


  #####  meta

  Scenario: Verify that `viewport meta` declaration is not checked for non-fixed layout documents (issue 419)
    When checking EPUB 'content-xhtml-meta-viewport-non-fxl-valid'
    Then no errors or warnings are reported


  #####  object

  Scenario: Report an `object` element without a fallback
    When checking EPUB 'content-xhtml-object-no-fallback-error'
    Then error MED-002 is reported
    And no other errors or warnings are reported

  Scenario: Report an `object` element with a media type not matching the Package Document declaration
    When checking EPUB 'content-xhtml-object-mediatype-mismatch-error'
    Then error OPF-013 is reported
    And no other errors or warnings are reported

  #####  script
  
  #//TODO verify script core media types

  #####  SVG

  Scenario: Verify that an SVG image can be referenced from `img`, `object` and `iframe` elements
    When checking EPUB 'content-xhtml-svg-reference-valid'
    And no errors or warnings are reported

  Scenario: Verify that `svg:switch` doesn't trigger the package document `switch` property check
    When checking EPUB 'content-xhtml-svg-switch-valid'
    Then no errors or warnings are reported

  Scenario: Verify that `svgView` fragments are allowed when associated to SVG documents
    When checking EPUB 'content-xhtml-svg-fragment-svgview-valid'
    Then no errors or warnings are reported


  #####  video
  
  Scenario: Report a `poster` attribute that references an invalid media type 
    When checking EPUB 'content-xhtml-video-poster-media-type-error'
    Then error MED-001 is reported
    And no other errors or warnings are reported


  #####  xpgt

  Scenario: Verify an xpgt style sheet with a manifest fallback to css
    See issues #271, #241
    When checking EPUB 'content-xhtml-xpgt-manifest-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Verify an xpgt style sheet with an implicit fallback to css in an xhtml document
    See issues #271, #241
    When checking EPUB 'content-xhtml-xpgt-implicit-fallback-valid'
    Then no errors or warnings are reported

  Scenario: Report an xpgt style sheet without a fallback
    See issues #271, #241
    When checking EPUB 'content-xhtml-link-no-fallback-valid'
    And no other errors or warnings are reported


  ###  2.5 HTML Deviations and Constraints
  
  ####  2.5.5 Foreign Resource Restrictions

  #####  picture

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


  ##### link

  Scenario: Verify test that a foreign resource used in an HTML `link` can be included without fallback
    # FIXME #1118 this test does not match the specification
    When checking EPUB 'content-xhtml-foreign-res-in-link-valid'
    Then no errors or warnings are reported


  ##  3. SVG Content Documents

  Scenario: Verify that SVG Content Documents can be referenced in the spine
    When checking EPUB 'content-svg-in-spine-valid'
    Then no errors or warnings are reported

  Scenario: Verify that the need for a `viewbox` declaration does not apply to non-fixed layout SVGs
    When checking EPUB 'content-svg-no-viewbox-not-fxl-valid'
    Then no errors or warnings are reported

  Scenario: Report SVG `use` elements that don’t point to a document fragment
    When checking EPUB 'content-svg-use-href-no-fragment-error'
    Then error RSC-015 is reported
    And no other errors or warnings are reported

  Scenario: Verify that an SVG Content Document can have any extension
    When checking EPUB 'content-svg-file-extension-unusual-valid'
    Then no errors or warnings are reported


  ##  4. CSS Style Sheets

  Scenario: Verify a minimal publication with a stylesheet 
    When checking EPUB 'content-css-minimal-valid'
    Then no errors or warnings are reported
    
  ### Properties not allowed in EPUB

  Scenario: Report the use of the CSS 'direction' property 
    When checking EPUB 'content-css-property-direction-error'
    Then error CSS-001 is reported
    And the message contains 'direction'
    And no other errors or warnings are reported

  Scenario: Report the use of the CSS 'unicode-bidi' property 
    When checking EPUB 'content-css-property-unicode-bidi-error'
    Then error CSS-001 is reported
    And the message contains 'unicode-bidi'
    And no other errors or warnings are reported

  ## Encoding

  Scenario: Verify a CSS file with a `@charset` declaration and UTF8 encoding
    See also issue #262
    When checking EPUB 'content-css-charset-utf8-valid'
    Then no errors or warnings are reported

  Scenario: Report a CSS file with a `@charset` declaration that is not utf-8
    When checking EPUB 'content-css-charset-enc-error'
    Then error CSS-003 is reported
    And no other errors or warnings are reported

  ## Resources and imports

  Scenario: Verify that namespace URIs in CSS are not recognized as remote resources (issue 237)
    When checking EPUB 'content-css-namespace-uri-not-resource-valid'
    Then no errors or warnings are reported

  Scenario: Report an attempt to `@import` a CSS file that declared in the package document but not present in the container
    When checking EPUB 'content-css-import-not-present-error'
    Then error RSC-001 is reported
    And no other errors or warnings are reported

  Scenario: Report an attempt to `@import` a CSS file that is not declared in the manifest but is present in the container
    When checking EPUB 'content-css-import-not-declared-error'
    Then error RSC-008 is reported
    And warning OPF-003 is reported
    And no other errors or warnings are reported

  Scenario: Report a CSS `url` that is not declared in the package document or present in the container
    When checking EPUB 'content-css-url-not-present-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a CSS `url` error even when preceded by a syntax error
    When checking EPUB 'content-css-url-not-present-preceded-by-invalid-syntax-error'
    Then error CSS-008 is reported (syntax error)
    And  error RSC-007 is reported (resource not found)
    Then no errors or warnings are reported

  ## CSS syntax

  Scenario: Verify valid CSS Selectors syntax
    When checking EPUB 'content-css-selectors-valid'
    Then no errors or warnings are reported

  Scenario: Report CSS syntax errors
    When checking EPUB 'content-css-syntax-error'
    Then error CSS-008 is reported 2 times
    Then no errors or warnings are reported

  Scenario: Report an empty `@font-face` declaration
    When checking EPUB 'content-css-font-face-empty-error'
    #FIXME should only be reported once
    Then warning CSS-019 is reported 2 times
    Then no errors or warnings are reported

  Scenario: Report a `@font-face` declaration with an empty URL reference
    When checking EPUB 'content-css-font-face-url-empty-error'
    #FIXME should only be reported once
    Then error CSS-002 is reported
    Then no errors or warnings are reported

  Scenario: Report an invalid CSS `font-size` value
    When checking EPUB 'content-css-font-size-value-error'
    Then error CSS-020 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Report a CSS `font-size` value without a unit specified
    When checking EPUB 'content-css-font-size-no-unit-error'
    Then error CSS-020 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report a CSS `font-size` unknown value even when preceded by a syntax error
    When checking EPUB 'content-css-font-size-error-preceded-by-invalid-syntax-error'
    Then error CSS-008 is reported (syntax error)
    And  error CSS-020 is reported (unknown font-size value)
    Then no errors or warnings are reported

  Scenario: Verify that CSS `font-size: 0` declaration is allowed (issue 922)
    When checking EPUB 'content-css-font-size-zero-valid'
    Then no errors or warnings are reported

  Scenario: Verify a fragment-only URL does not trigger a "fragment not defined" error 
    When checking EPUB 'content-css-url-fragment-valid'
    Then no errors or warnings are reported


  ##  6.  Fixed Layouts

  ###  6.2 Content Conformance

  Scenario: Verify a fixed-layout SVG
    When checking EPUB 'content-fxl-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report a fixed-layout XHTML document with no viewport
    When checking EPUB 'content-fxl-xhtml-viewport-missing-error'
    Then error HTM-046 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout XHTML document with an invalid viewport
    When checking EPUB 'content-fxl-xhtml-viewport-invalid-error'
    Then error HTM-047 is reported
    And no other errors or warnings are reported


  ###  6.5 Initial Containing Block Dimensions

  ####  6.5.2 Expressing in SVG

  Scenario: Verify that the initial containing block rules are not checked on embedded svg elements
    When checking EPUB 'content-fxl-svg-no-viewbox-on-inner-svg-valid'
    Then no errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration
    When checking EPUB 'content-fxl-svg-no-viewbox-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in units)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-units-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported

  Scenario: Report a fixed-layout SVG without a `viewbox` declaration (only `width`/`height` in percent)
    When checking EPUB 'content-fxl-svg-no-viewbox-width-height-percent-error'
    Then error HTM-048 is reported
    And no other errors or warnings are reported
