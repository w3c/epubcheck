
  Scenario: testValidateXHTML_SVG001
    When checking document 'xhtml/invalid/svg-001.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_DupeID_EPUB3
    When checking document 'xhtml/invalid/duplicate-id.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_httpequiv
    When checking document 'http-equiv-1.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML_httpequiv_caseinsensitive
    When checking document 'http-equiv-2.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML_httpequiv_invalid
    When checking document 'xhtml/invalid/http-equiv-1.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_httpequivInvalidMetaSibling
    When checking document 'xhtml/invalid/http-equiv-2.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_SSMLemptyPh
    When checking document 'xhtml/invalid/ssml-empty-ph.xhtml'
    Then warning HTM_007 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_issue153_invalid
    When checking document 'xhtml/invalid/issue153.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_doctype1_obsolete
    When checking document 'xhtml/invalid/doctype-1.xhtml'
    Then error HTM_004 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTML_doctype1
    // <!DOCTYPE html>
    When checking document 'doctype-1.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML_doctype2
    // <!DOCTYPE html SYSTEM "about:legacy-compat">
    When checking document 'doctype-2.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML_SVGLinks
    When checking document 'svg-links.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML_SVGLinks_MisssingTitle
    When checking document 'xhtml/invalid/svg-links.xhtml'
    Then warning ACC_011 is reported
    And no other errors or warnings are reported

  Scenario: testValidateSVG_Links
    When checking document 'svg/valid/svg-links.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_ValidStyleWithoutType_issue688
    When checking document 'svg/valid/issue688.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithARIAAttributes
    When checking document 'svg/valid/aria-attributes.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithDataAttribute
    When checking document 'svg/valid/data-attribute.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_WithCustomNamespace
    When checking document 'svg/valid/custom-ns.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_Links_MisssingTitle
    When checking document 'svg/invalid/svg-links.svg'
    Then warning ACC_011 is reported
    And no other errors or warnings are reported

  Scenario: testValidateSVG_ForeignObject
    // tests that 'foreignObject' conforming to the rules is accepted
    When checking document 'svg/valid/foreignObject.svg'
    Then no errors or warnings are reported

  Scenario: testValidateSVG_ForeignObjectWithInvalidRequiredExtensions
    // tests that 'foreignObject' with a 'requiredExtensions' attribute other than the OPS NS is invalid 
    When checking document 'svg/invalid/foreignObject-invalid-requiredExtensions.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ForeignObjectWithNonXHTMLContent
    // tests that 'foreignObject' can't have children that are not HTML content 
    When checking document 'svg/invalid/foreignObject-non-html-content.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ForeignObjectWithTwoBodyElements
    // tests that 'foreignObject' can't have children that are not HTML content 
    When checking document 'svg/invalid/foreignObject-two-body.svg'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_DuplicateIds
    // tests that duplicate IDs are detected 
    When checking document 'svg/invalid/duplicate-ids.svg'
    Then error RSC_005 is reported  times
    And no other errors or warnings are reported
  
  Scenario: testValidateSVG_ImageHrefWithAFragment
    // tests that SVG 'image' elements can have an 'xlink:href' URL pointing to a fragment 
    When checking document 'svg/valid/svg-image-fragment.svg'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue204
    When checking document 'issue204.xhtml'
    Then error RSC_005 is reported
    And warning HTM_025 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLStyleAttr001
    When checking document 'styleAttr001.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLStyleAttr002
    When checking document 'xhtml/invalid/styleAttr001.xhtml'
    Then error CSS_008 is reported

  Scenario: testValidateSVGIssue219
    When checking document 'svg/valid/issue219.svg'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLSVGForeignObject
    // foreignObject allowed outside switch, and <body> allowed inside
    When checking document 'svg-foreignobject.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLSVGForeignObjectBody
    // foreignObject with disallowed flow content
    When checking document 'xhtml/invalid/svg-foreignobject-body.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLSVGForeignObjectNotFlow
    // foreignObject with disallowed flow content
    When checking document 'xhtml/invalid/svg-foreignobject-not-flow.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLSVGIssue769
    // allow aria attributes on SVG elements
    When checking document 'svg/valid/issue769.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue248
    When checking document 'issue248.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue282_ObjectTypemustmatch
    When checking document 'issue282-object-typemustmatch.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue288_InvalidURI
    When checking document 'xhtml/invalid/issue288-invalid-uri.xhtml'
    Then error RSC_020 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLIssue296
    When checking document 'issue296-irc-uri.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue340
    When checking document 'issue340.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue341
    When checking document 'issue341.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLIssue355
    When checking document 'issue355.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML301RDFaValid
    When checking document 'rdfa.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML301MDValid
    When checking document 'md.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTML301MDInvalid
    When checking document 'xhtml/invalid/md.xhtml'
    Then error RSC_005 is reported 3 times
    And no other errors or warnings are reported

  Scenario: testAttributesInCustomNS
    // test that attribute in a custom namespace are ignored
    When checking document 'attrs-custom-ns.xhtml'
    Then no errors or warnings are reported

  Scenario: testAttributesCaseInsensitive
    // test that the value of HTML boolean attributes and enumerated attributes are
    // parsed in a case-insensitive manner
    When checking document 'attrs-case-insensitive.xhtml'
    Then no errors or warnings are reported

  Scenario: testCustomElements
    // test that HTML custom elements are not rejected by the schema
    When checking document 'custom-elements.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupubSectioning_ExplicitBody
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'edupub-sectioning-explicit-body.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupubSectioning_ExplicitSections
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'edupub-sectioning-explicit-sections.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupubSectioning_ImplicitBody
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'edupub-sectioning-implicit-body.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupubSectioning_Subtitle
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'edupub-sectioning-subtitle.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupubSectioning_Invalid
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning.xhtml'
    Then error RSC_005 is reported 3 times
    And no other errors or warnings are reported

  Scenario: testEdupubSectioning_InvalidExplicitBody
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning-explicit-body.xhtml'
    Then error RSC_005 is reported 3 times
    And no other errors or warnings are reported

  Scenario: testEdupubSectioning_InvalidImplicitBody
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning-implicit-body.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testEdupubSectioning_InvalidImplicitBodyAriaHeading
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning-implicit-body-aria-heading.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testEdupubSectioning_InvalidSubtitle
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning-subtitle.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testEdupubSectioning_InvalidAriaLabel
    // aria-label MUST NOT be equal to heading content
    // 2 errors: one on body and one on sub-section
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-sectioning-arialabel-heading.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testEdupupHeaading_ImgWithAltText
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'edupub-heading-img.xhtml'
    Then no errors or warnings are reported

  Scenario: testEdupupHeaading_ImgWithEmptyAltText
    Given EPUBCheck configured with the EDUPUB profile
    When checking document 'xhtml/invalid/edupub-heading-imgnoalt.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testMathML_1
    When checking document 'mathml-01.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathML_2
    When checking document 'mathml-02.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLWithNoAlt
    When checking document 'mathml-noalt.xhtml'
    Then usage ACC_009 is reported
    And no other errors or warnings are reported

  Scenario: testMathMLWithContentMathML
    When checking document 'xhtml/invalid/mathml-contentmathml.xhtml'
    Then error RSC_005 is reported 2 times
    And no other errors or warnings are reported

  Scenario: testMathMLAnnotation
    When checking document 'mathml-annotation-tex.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testMathMLAnnotationXMLWithMathMLContent
    When checking document 'mathml-annotationxml-mathml-content.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testMathMLAnnotationXMLWithMathMLPresentation
    When checking document 'mathml-annotationxml-mathml-presentation.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithMathMLDescendant
    When checking document 'mathml-annotationxml-xhtml-with-mathml-descendants.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithMathMLAndNoNameAttr
    When checking document 'xhtml/invalid/mathml-annotationxml-mathml-noname.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithMathMLAndInvalidNameAttr
    When checking document 'xhtml/invalid/mathml-annotationxml-mathml-invalidname.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithMathMLAndInvalidEncodingAttr
    When checking document 'xhtml/invalid/mathml-annotationxml-mathml-invalidencoding.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithXHTML
    When checking document 'mathml-annotationxml-xhtml.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithXHTMLAndNoNameAttr
    When checking document 'mathml-annotationxml-xhtml-noname.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithXHTMLAndNameAttr
    When checking document 'mathml-annotationxml-xhtml-contentequiv.xhtml'
    Then no errors or warnings are reported

  Scenario: testMathMLAnnotationXMLWithXHTMLAndEncodingAttr
    When checking document 'mathml-annotationxml-xhtml-html-encoding.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testMathMLAnnotationXMLWithSVG
    When checking document 'mathml-annotationxml-svg.xhtml'
    Then no errors or warnings are reported

  Scenario: testIndex
    Given EPUBCheck configured with the idx profile
    When checking document 'index.xhtml'
    Then no errors or warnings are reported

  Scenario: testIndex_NoIndex
    Given EPUBCheck configured with the idx profile
    When checking document 'xhtml/invalid/index-noindex.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testIndex_IndexNotOnBody
    Given EPUBCheck configured with the idx profile
    When checking document 'xhtml/invalid/index-notonbody.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testValidateXHTMLImageMap_EPUB3_Valid
    When checking document 'imagemap-good_issue696.xhtml'
    Then no errors or warnings are reported

  Scenario: testValidateXHTMLImageMap_EPUB3_Invalid
    When checking document 'xhtml/invalid/imagemap-bad_issue696.xhtml'
    Then error RSC_005 is reported
    And no errors or warnings are reported

  Scenario: testValidateEmptyLangAttribute_EPUB3_Valid
    When checking document 'issue777-empty-lang.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testObsoleteContextMenuAttribute
    When checking document 'xhtml/invalid/obsolete-contextmenu.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testObsoleteDropzoneAttribute
    When checking document 'xhtml/invalid/obsolete-dropzone.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testObsoleteKeygenElement
    When checking document 'xhtml/invalid/obsolete-keygen.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testObsoleteMenus
    When checking document 'xhtml/invalid/obsolete-menus.xhtml'
    Then error RSC_005 is reported 3 times
    And no other errors or warnings are reported
  
  Scenario: testObsoletePubdateAttribute
    When checking document 'xhtml/invalid/obsolete-pubdate.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testObsoleteSeamessIframe
    When checking document 'xhtml/invalid/obsolete-seamless-iframe.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testContentModel_TimeInTime
    When checking document 'xhtml/invalid/time-in-time.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testValidTimes
    When checking document 'times.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testInvalidTimes
    // tests that one error is raised for each invalid time attribute in the test
    // file
    When checking document 'xhtml/invalid/times.xhtml'
    Then error RSC_005 is reported 25 times
    And no other errors or warnings are reported

  Scenario: testEntitiesValid
    // tests that known named character references are accepted
    // also tests that 'entity references' in comments or CDATA sections are ignored 
    When checking document 'entities.xhtml'
    Then no errors or warnings are reported

  Scenario: testEntitiesInternalDeclaration
    // tests that internal entity declarations are allowed
    When checking document 'entities-internal.xhtml'
    Then no errors or warnings are reported
  
  Scenario: testEntitiesMissingSemicolon
    // tests that entity references not ending with a semicolon cause a parsing error
    When checking document 'xhtml/invalid/entities-missing-semicolon.xhtml'
    Then fatal RSC_016 is reported
    And error RSC_005 is reported
    And no other errors or warnings are reported
  
  Scenario: testEntitiesUnknown
    // tests that unknown entity references are reported as errors
    When checking document 'xhtml/invalid/entities-unknown.xhtml'
    Then fatal RSC_016 is reported
    And error RSC_005 is reported
    And no other errors or warnings are reported

  Scenario: testIdRefNotNCName
    // tests that ID-referencing attributes can refer to non-NCName IDs
    When checking document 'id-ref-not-ncname.xhtml'
    Then no errors or warnings are reported

  Scenario: testIdRefNotFound
    // tests that ID-referencing attributes refer to existing IDs
    When checking document 'xhtml/invalid/id-ref-not-found.xhtml'
    Then error RSC_005 is reported
    And no other errors or warnings are reported
