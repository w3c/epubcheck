Feature: EPUB 3 ▸ Packages ▸ Package Document Checks


  Checks conformance to the EPUB Packages 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-packages.html

  In the scenarios below, checks are run against single Package Documents.
  EPUBCheck is launched in 'opf' mode.


  Background: 
    Given test files located at '/epub3/files/package-document/'
    And EPUBCheck configured to check a Package Document


  # 3.2 Content Conformance

  Scenario: the minimal Package Document is reported as valid 
    When checking file 'minimal.opf'
    Then no errors or warnings are reported
    
  Scenario: a not well-formed Package Document is reported 
    When checking file 'conformance-xml-malformed-error.opf'
    Then fatal error RSC-016 is reported (parsing error)
    And error RSC-005 is reported (schema error)
    And no other errors or warnings are reported
    
  Scenario: using a not-declared namespace is not allowed 
    When checking file 'conformance-xml-undeclared-namespace-error.opf'
    Then fatal error RSC-016 is reported (parsing error)
    And error RSC-005 is reported (schema error)
    And no other errors or warnings are reported


  # 3.4 Pacakge Document Definition
  
  ## 3.4.1 The package element

  Scenario: the unique identifier must not be empty
    When checking EPUB 'package-unique-identifier-attribute-missing-error.opf'
    # FIXME OPF-048 could be removed, as it is already reported as RSC-005
    Then the following errors are reported
      | RSC-005 | missing required attribute                       |
      | OPF-048 | missing its required unique-identifier attribute |
    And no other errors or warnings are reported
  
  Scenario: the 'package' 'unique-identifier' attribute must be a known ID
    When checking file 'package-unique-identifier-unknown-error.opf'
    Then error RSC-005 is reported
    And the message contains "does not resolve to a dc:identifier element"
    And no other errors or warnings are reported 
    
  Scenario: the 'package' 'unique-identifier' attribute must point to a 'dc:identifier' element
    When checking file 'package-unique-identifier-not-targeting-identifier-error.opf'
    Then error RSC-005 is reported
    And the message contains "does not resolve to a dc:identifier element"
    And no other errors or warnings are reported 
  
  Scenario: the 'package' element must have a 'metadata' child element  
    When checking file 'package-no-metadata-element-error.opf'
    Then error RSC-005 is reported (missing metadata element)
    And the message contains 'missing required element "metadata"'
    And error RSC-005 is reported (side effect: missing unique-identifier target) 
    And no other errors or warnings are reported
    
  Scenario: the 'package' element’s 'metadata' child must be before the 'manifest' child  
    When checking file 'package-manifest-before-metadata-error.opf'
    Then error RSC-005 is reported
    And the message contains 'element "manifest" not allowed yet'
    And error RSC-005 is reported
    And the message contains 'element "metadata" not allowed here' 
    And no other errors or warnings are reported
  
  ## 3.4.2 Shared attributes
  
  Scenario: 'id' attributes can have leading or trailing space 
    When checking file 'attr-id-with-spaces-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: 'id' attributes must be unique 
    When checking file 'attr-id-duplicate-error.opf'
    Then error RSC-005 is reported 2 times (once for each ID)
    And no other errors or warnings are reported
  
  Scenario: 'id' attributes must be unique after whitespace normalization 
    When checking file 'attr-id-duplicate-with-spaces-error.opf'
    Then error RSC-005 is reported 2 times (once for each ID)
    And no other errors or warnings are reported

  Scenario: the 'xml:lang' attribute can be empty
    When checking file 'attr-lang-empty-valid.opf'
    Then no other errors or warnings are reported
  
  ## 3.4.3 Metadata
  ### 3.4.3 The metadata element
  #### 3.4.3.2 DCMES Required Elements
  
    
  Scenario: 'dc:identifier' must not be empty 
    When checking file 'metadata-identifier-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains "must be a string with length at least 1" 
    And no other errors or warnings are reported
    
 Scenario: 'dc:identifier' starting with "urn:uuid:" should be a valid UUID  
    When checking file 'metadata-identifier-uuid-invalid-warning.opf'
    Then warning OPF-085 is reported
    And no other errors or warnings are reported
    
  Scenario: 'dc:modified' must be defined 
    When checking file 'metadata-modified-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "dcterms:modified"
    And no other errors or warnings are reported

  Scenario: 'dc:modified' must be of the form 'CCYY-MM-DDThh:mm:ssZ' 
    When checking file 'metadata-modified-syntax-error.opf'
    Then error RSC-005 is reported
    And the message contains "CCYY-MM-DDThh:mm:ssZ"
    And no other errors or warnings are reported

  Scenario: 'dc:title' must be specified
    When checking file 'metadata-title-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'missing required element "dc:title"' 
    And no other errors or warnings are reported

  Scenario: 'dc:title' must not be empty 
    When checking file 'metadata-title-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains "must be a string with length at least 1" 
    And no other errors or warnings are reported
    
  #### 3.4.3.3 DCMES Optional Elements
  
  Scenario: 'dc:date' can be specified as an ISO 8601:2004 value 
    When checking file 'metadata-date-single-year-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'dc:date' can be specified as a single year 
    When checking file 'metadata-date-single-year-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'dc:date' value can have leading/trailng whitespace 
    When checking file 'metadata-date-with-whitespace-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'dc:date' with an invalid ISO 8601:2004 syntax is reported
    When checking file 'metadata-date-iso-syntax-error-warning.opf'
    Then warning OPF-053 is reported
    And the message contains "does not follow recommended syntax"
    And no errors or warnings are reported

  Scenario: 'dc:date' with an unknown format is reported
    When checking file 'metadata-date-unknown-format-warning.opf'
    Then warning OPF-053 is reported
    And the message contains "does not follow recommended syntax"
    And no errors or warnings are reported
    
  Scenario: 'dc:source' valid values are allowed 
    When checking file 'metadata-source-valid.opf'
    Then no errors or warnings are reported
  
  Scenario: 'dc:type' valid values are allowed 
    When checking file 'metadata-type-valid.opf'
    Then no errors or warnings are reported
    
  
  #### 3.4.3.4 The meta Element
  
  Scenario: a metadata’s property name must be defined 
    When checking file 'metadata-meta-property-empty-error.opf'
    Then error RSC-005 is reported 2 times
    And the message contains 'value of attribute "property" is invalid' 
    And no other errors or warnings are reported
    
  Scenario: a metadata’s property name must not be a list of values 
    When checking file 'metadata-meta-property-list-error.opf'
    Then error RSC-005 is reported (value is not an NMTOKEN)
    Then error OPF-025 is reported
    And the message contains "only one value must be specified"
    And no other errors or warnings are reported

  Scenario: a metadata’s property name must be well-formed
    When checking file 'metadata-meta-property-malformed-error.opf'
    Then error OPF-026 is reported
    And no other errors or warnings are reported
    
  Scenario: a metadata’s value must be defined 
    When checking file 'metadata-meta-value-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains "must be a string with length at least 1" 
    And no other errors or warnings are reported

  Scenario: 'refines' attribute MUST be a relative URL 
    When checking file 'metadata-refines-not-relative-error.opf'
    Then error RSC-005 is reported
    And the message contains "@refines must be a relative URL"
    And no other errors or warnings are reported

  Scenario: 'refines' attribute should use a fragment ID if refering to a Publication Resource 
    When checking file 'metadata-refines-not-a-fragment-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "using a fragment identifier pointing to its manifest item"
    And no other errors or warnings are reported

  Scenario: 'refines' attribute, when using fragment ID, must target an existing ID
    When checking file 'metadata-refines-unknown-id-error.opf'
    Then error RSC-005 is reported
    And the message contains "@refines missing target id"
    And no other errors or warnings are reported
    
  Scenario: 'refines' references cycles are not allowed
    When checking file 'metadata-refines-cycle-error.opf'
    Then error OPF-065 is reported
    And no other errors or warnings are reported
    
  Scenario: 'scheme' can be used to identify the value system
    When checking file 'metadata-meta-scheme-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'scheme' must not be list of values
    When checking file 'metadata-meta-scheme-list-error.opf'
    Then error RSC-005 is reported (value is not an NMTOKEN)
    And error OPF-025 is reported
    And no other errors or warnings are reported
    
  Scenario: 'scheme' must not be an unknown value with no prefix
    When checking file 'metadata-meta-scheme-unknown-error.opf'
    Then error OPF-027 is reported
    And no other errors or warnings are reported
  

  #### 3.4.3.5 The link Element
  
  Scenario: 'link' targets must not be manifest items 
    When checking file 'link-to-publication-resource-error.opf'
    Then error OPF-067 is reported
    And no other errors or warnings are reported
  
  ### 3.4.4 Manifest
  
  #### 3.4.4.1 The manifest Element
  
  #### 3.4.4.2 The item Element
  
  Scenario: a manifest item must declare a media type  
    When checking file 'item-media-type-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'missing required attribute "media-type"'
    And no other errors or warnings are reported

  Scenario: item URLs should not contain spaces
    When checking file 'item-href-contains-spaces-warning.opf'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported

  Scenario: item URLs must not have a fragment identifier
    When checking file 'item-href-with-fragment-error.opf'
    Then error OPF-091 is reported
    And no other errors or warnings are reported

  Scenario: two manifest items cannot represent the same resource 
    When checking file 'item-duplicate-resource-error.opf'
    Then error OPF-074 is reported
    And no other errors or warnings are reported
  
  Scenario: one item must have the 'nav' property  
    When checking file 'item-nav-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Exactly one manifest item must declare the "nav" property'
    And no other errors or warnings are reported
    
  Scenario: at most one item must have the 'nav' property  
    When checking file 'item-nav-multiple-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Exactly one manifest item must declare the "nav" property'
    And no other errors or warnings are reported
    
  Scenario: the 'nav' property must be on an XHTML Content Document  
    When checking file 'item-nav-not-xhtml-error.opf'
    Then error RSC-005 is reported
    And the message contains 'the Navigation Document must be of the "application/xhtml+xml" type'
    And error OPF-012 is reported ('nav' undefined for 'application/x+dtbncx+xml')
    And no other errors or warnings are reported
  
  #### 3.4.4.3 Manifest Fallbacks
  
  Scenario: Allow non-CMT file to be in the spine if they have an XHTML Content Document fallback
    Note: here an audio file is used in the spine
    When checking file 'fallback-to-xhtml-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: Allow an SVG Content Document to be used as a fallback
    Note: here an image file is used in the spine
    When checking file 'fallback-to-svg-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: Allow a deep fallback chain as long as it contains a Content Document
    Note: here a font file is used in the spine
    When checking file 'fallback-chain-valid.opf'
    Then no errors or warnings are reported

  Scenario: Report a cycle in the fallback chain
    When checking file 'fallback-cycle-error.opf'
    Then error OPF-045 is reported (circular reference)
    And error OPF-044 is reported (no Content Document fallback was found) 
    And no other errors or warnings are reported

  Scenario: Report files that aren’t Content Documents (like audio) in spine when they don’t have a fallback  
    Note: here an audio file is used in the spine
    When checking file 'fallback-missing-error.opf'
    Then error OPF-043 is reported
    And no other errors or warnings are reported
    
  Scenario: Manifest fallback must point to an existing item ID 
    When checking file 'fallback-to-unknown-id-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must resolve to another manifest item'
    And no other errors or warnings are reported
    
  Scenario: Manifest fallback must point to an existing item ID 
    When checking file 'fallback-to-self-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must resolve to another manifest item'
    And no other errors or warnings are reported

  Scenario: Report usage of the EPUB 2 'fallback-style' attribute
    When checking file 'fallback-style-error.opf'
    Then error RSC-005 is reported
    And the message contains 'fallback-style'
    And no other errors or warnings are reported

  #### 3.4.4.4 The bindings Element
  
  Scenario: Report usage of the 'bindings' element as deprecated 
    When checking file 'bindings-deprecated-warning.opf'
    Then warning RSC-017 is reported
    And no other errors or warnings are reported

  Scenario: Report a binding declared for an image resource
    When checking file 'bindings-for-image-error.opf'
    Then warning RSC-017 is reported (since bindings is deprecated)
    And error OPF-008 is reported (to report handler for an image)
    And no other errors or warnings are reported

  Scenario: Report a bindings handler that is not an XHTML Content Document
    When checking file 'bindings-handler-not-xhtml-error.opf'
    Then warning RSC-017 is reported (since bindings is deprecated)
    And error RSC-005 is reported (to report the non-XHTML handler)
    And no other errors or warnings are reported
  
  ### 3.4.5 Spine
  #### 3.4.5.1 The spine Element
  #### 3.4.5.2 The itemref Element
  
  Scenario: An SVG Content Document is allowed in the spine 
    When checking file 'spine-item-svg-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: An unknown 'itemref' ID is reported  
    When checking file 'spine-item-unknown-error.opf'
    Then error OPF-049 is reported (ID not found)
    Then error RSC-005 is reported (schema error)
    And no other errors or warnings are reported

  Scenario: Two spine 'itemref' elements cannot reference the same manifest item  
    When checking file 'spine-item-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains "Itemref refers to the same manifest entry as a previous itemref"
    And no other errors or warnings are reported

  ### 3.4.6 Collections

  Scenario: a collection role can be an absolute URL
    When checking file 'collection-role-url-valid.opf'
    Then no errors or warnings are reported

  Scenario: a collection role must not be an invalid URL
    Spec mismatch: this should be reported as an error 
    When checking file 'collection-role-url-invalid-error.opf'
    Then warning OPF-070 is reported
    And no other errors or warnings are reported

  Scenario: a collection role URL cannot contain 'idpf.org' in its host 
    When checking file 'collection-role-url-idpf.org-error.opf'
    Then error OPF-069 is reported
    And no other errors or warnings are reported

  Scenario: a collection role must not be an unknown token value
    When checking file 'collection-role-unknown-error.opf'
    Then error OPF-068 is reported
    And no other errors or warnings are reported

  Scenario: a 'manifest' collection must be the child of another collection
    See http://idpf.org/epub/vocab/package/roles/manifest/
    When checking file 'collection-role-manifest-toplevel-error.opf'
    Then error RSC-005 is reported
    And the message contains "A manifest collection must be the child of another collection"
    And no other errors or warnings are reported

  ### 3.4.7 Legacy
  #### 3.4.7.1 The meta Element
  #### 3.4.7.2 The guide Element

  Scenario: 'guide' should not contain two entries of the same type pointing to the same resource
    When checking EPUB 'legacy-guide-duplicates-warning.opf'
    Then warning RSC-017 is reported 2 times (once for each entry)
    And the message contains 'Duplicate "reference" elements with the same "type" and "href" attributes'
    And no other errors or warnings are reported
    
  #### 3.4.7.3 NCX

  Scenario: When an NCX document is present, it must be identified in the 'toc' attribute of the spine  
    When checking file 'legacy-ncx-toc-attribute-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "toc attribute must be set"
    And no other errors or warnings are reported
    
  Scenario: The 'toc' attribute of the spine must point to an NCX document  
    When checking file 'legacy-ncx-toc-attribute-not-ncx-error.opf'
    Then error OPF-050 is reported
    Then error RSC-005 is reported (duplicate schema error)
    And no other errors or warnings are reported
    
  # 4. Package Metadata
  
  ## 4.1 Publication Identifiers
  
  ## 4.2 Vocabulary Association Mechanisms

  Scenario: the 'prefix' attribute can be used to define new prefix mappings 
    When checking file 'property-prefix-declaration-valid.opf'
    Then no errors or warnings are reported

  Scenario: reserved prefixes can be explicitly declared
    When checking file 'property-prefix-declaration-reserved-explicit-valid.opf'
    Then no errors or warnings are reported

  Scenario: syntax errors in the 'prefix' attribute are reported
    When checking file 'property-prefix-declaration-syntax-error.opf'
    Then error OPF-004c is reported 2 times (the test file contains 2 syntax errors)
    And no other errors or warnings are reported

  Scenario: reserved prefixes should not be overridden to other vocabularies 
    When checking file 'property-prefix-declaration-reserved-overridden-warning.opf'
    Then warning OPF-007 is reported 8 times (once for each reserved prefix)
    And no other errors or warnings are reported

  Scenario: default vocabularies must not be assigned a prefix
  	Note: This should be an error, but is currently reported as a warning
  	See issue 522: https://github.com/w3c/epubcheck/issues/522
    When checking file 'property-prefix-declaration-default-vocabs-error.opf'
    Then warning OPF-007b is reported 4 times (once for each default vocabulary)
    And no other errors or warnings are reported

  Scenario: A metadata property with an unknown prefix is reported
    When checking file 'property-prefix-declaration-missing-error.opf'
    Then error OPF-028 is reported
    And no errors or warnings are reported

  Scenario: The 'schema' prefix can be used in metadata properties without being declared
    When checking file 'property-prefix-schema-not-declared-valid.opf'
    Then no errors or warnings are reported
      
  ## 4.3 Package Rendering Metadata
  ### 4.3.3 General Properties

  Scenario: the 'rendition:flow' property can be used to define the global flow preference
    When checking file 'rendition-flow-global-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'rendition:flow' property with an unknown value is reported
    When checking file 'rendition-flow-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:flow" property must be'
    And no other errors or warnings are reported

  Scenario: the 'rendition:flow' property cannot be declared more than once
    When checking file 'rendition-flow-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:flow" property must not occur more than one time'
    And no other errors or warnings are reported

  Scenario: the 'rendition:flow' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-flow-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  Scenario: the 'rendition:flow' property can be used as a spine override
    When checking file 'rendition-flow-itemref-valid.opf'
    Then no errors or warnings are reported

  Scenario: the 'rendition:flow' spine overrides values are mutually exclusive
    When checking file 'rendition-flow-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported
  
  
  ### 4.3.4 Fixed-Layout Properties

  Scenario: the 'rendition:layout' property can be used to define the global layout preference
    When checking file 'rendition-layout-global-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'rendition:layout' property with no value is reported
    See also issue #727
    When checking file 'rendition-layout-global-empty-error.opf'
    Then the following errors are reported (one for the empty element, one for the consequently unexpected value)
      | RSC-005 | character content of element "meta" invalid          |
      | RSC-005 | The value of the "rendition:layout" property must be |
    And no other errors or warnings are reported

  Scenario: a 'rendition:layout' property with an unknown value is reported
    When checking file 'rendition-layout-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:layout" property must be'
    And no other errors or warnings are reported

  Scenario: the 'rendition:layout' property cannot be declared more than once
    When checking file 'rendition-layout-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:layout" property must not occur more than one time'
    And no other errors or warnings are reported

  Scenario: the 'rendition:layout' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-layout-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  Scenario: the 'rendition:layout' property can be used as a spine override
    When checking file 'rendition-layout-itemref-valid.opf'
    Then no errors or warnings are reported

  Scenario: the 'rendition:layout' spine overrides values are mutually exclusive
    When checking file 'rendition-layout-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  Scenario: the 'rendition:page-spread-*' properties can be used without the prefix
    When checking file 'rendition-page-spread-itemref-unprefixed-valid.opf'
    Then no errors or warnings are reported

  Scenario: the 'rendition:page-spread-*' properties values are mutually exclusive
    When checking file 'rendition-page-spread-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  Scenario: the 'rendition:orientation' property can be used to define the global orientation preference
    When checking file 'rendition-orientation-global-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'rendition:orientation' property with an unknown value is reported
    When checking file 'rendition-orientation-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:orientation" property must be'
    And no other errors or warnings are reported

  Scenario: the 'rendition:orientation' property cannot be declared more than once
    When checking file 'rendition-orientation-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:orientation" property must not occur more than one time'
    And no other errors or warnings are reported

  Scenario: the 'rendition:orientation' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-orientation-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  Scenario: the 'rendition:orientation' property can be used as a spine override
    When checking file 'rendition-orientation-itemref-valid.opf'
    Then no errors or warnings are reported

  Scenario: the 'rendition:orientation' spine overrides values are mutually exclusive
    When checking file 'rendition-orientation-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' property can be used to define the global spread preference
    When checking file 'rendition-spread-global-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'rendition:spread' property with an unknown value is reported
    When checking file 'rendition-spread-global-unknown-value-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The value of the "rendition:spread" property must be'
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' property cannot be declared more than once
    When checking file 'rendition-spread-global-duplicate-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "rendition:spread" property must not occur more than one time'
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' property cannot be used in a 'meta' element to refine a publication resource
    When checking file 'rendition-spread-global-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains "refines"
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' property can be used as a spine override
    When checking file 'rendition-spread-itemref-valid.opf'
    Then no errors or warnings are reported

  Scenario: the 'rendition:spread' spine overrides values are mutually exclusive
    When checking file 'rendition-spread-itemref-conflict-error.opf'
    Then error RSC-005 is reported
    And the message contains "are mutually exclusive"
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' 'portrait' value is deprecated as a global value
    When checking file 'rendition-spread-portrait-global-deprecated-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "is deprecated"
    And no other errors or warnings are reported

  Scenario: the 'rendition:spread' 'spread-portrait' value is deprecated as a spine override
    When checking file 'rendition-spread-portrait-itemref-deprecated-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "is deprecated"
    And no other errors or warnings are reported

  Scenario: the 'rendition:viewport' property is deprecated
    When checking file 'rendition-viewport-deprecated-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "is deprecated"
    And no other errors or warnings are reported

  Scenario: the 'rendition:viewport' property syntax errors are reported
    When checking file 'rendition-viewport-syntax-error.opf'
    Then warning RSC-017 is reported (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The value of the "rendition:viewport" property must be of the form'
    And no other errors or warnings are reported

  Scenario: the 'rendition:viewport' property cannot be declared more than once
    When checking file 'rendition-viewport-duplicate-error.opf'
    Then warning RSC-017 is reported 2 times (since 'viewport' is deprecated)
    And error RSC-005 is reported
    And the message contains 'The "rendition:viewport" property must not occur more than one time as a global value'
    And no other errors or warnings are reported

  # C. Meta Properties Vocabulary

  Scenario: 'authority' metadata can refine a subject expression
  	When checking file 'metadata-meta-authority-valid.opf'
  	Then no errors or warnings are reported

  Scenario: 'authority' metadata can only refine a subject expression
  	When checking file 'metadata-meta-authority-refines-disallowed-error.opf'
  	Then error RSC-005 is reported
    And the message contains 'Property "authority" must refine a "subject" property'
  	Then no errors or warnings are reported

  Scenario: 'authority' metadata must be associated to a term
  	When checking file 'metadata-meta-authority-no-term-error.opf'
  	Then error RSC-005 is reported
  	And the message contains "A term property must be associated"
  	And no other errors or warnings are reported

  Scenario: 'authority' metadata must not be defined more than once
  	When checking file 'metadata-meta-authority-cardinality-error.opf'
  	Then error RSC-005 is reported
  	And the message contains "Only one pair of authority and term properties"
  	And no other errors or warnings are reported

  Scenario: 'belongs-to-collection' metadata can identify the publication’s collection
    When checking file 'metadata-meta-collection-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'belongs-to-collection' metadata can only refine other 'belongs-to-collection' metadata
    When checking file 'metadata-meta-collection-refines-non-collection-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Property "belongs-to-collection" can only refine other "belongs-to-collection" properties'
    And no other errors or warnings are reported

  Scenario: 'collection-type' cannot be used as a primary metadata
    When checking file 'metadata-meta-collection-type-refines-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Property "collection-type" must refine a "belongs-to-collection" property'
    And no other errors or warnings are reported

  Scenario: 'collection-type' metadata can only refine a 'belongs-to-collection' property
    When checking file 'metadata-meta-collection-type-refines-non-collection-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Property "collection-type" must refine a "belongs-to-collection" property'
    And no other errors or warnings are reported

  Scenario: 'collection-type' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-collection-type-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"collection-type" cannot be declared more than once'
    And no other errors or warnings are reported
  
  Scenario: 'display-seq' metadata is allowed 
    When checking file 'metadata-meta-display-seq-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'display-seq' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-display-seq-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"display-seq" cannot be declared more than once'
    And no other errors or warnings are reported
  
  Scenario: 'file-as' metadata is allowed 
    When checking file 'metadata-meta-file-as-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'file-as' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-file-as-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"file-as" cannot be declared more than once'
    And no other errors or warnings are reported

  Scenario: 'group-position' metadata is allowed 
    When checking file 'metadata-meta-group-position-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'group-position' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-group-position-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"group-position" cannot be declared more than once'
    And no other errors or warnings are reported

	Scenario: 'identifier-type' metadata can only refine a 'source' or 'identifier' property
    When checking file 'metadata-meta-identifier-type-refines-disallowed-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Property "identifier-type" must refine an "identifier" or "source" property'
    And no other errors or warnings are reported
    
  Scenario: 'identifier-type' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-identifier-type-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"identifier-type" cannot be declared more than once'
    And no other errors or warnings are reported
    
  Scenario: 'meta-auth' metadata is deprecated 
    When checking file 'metadata-meta-meta-auth-deprecated-warning.opf'
    Then warning RSC-017 is reported
    And the message contains "the meta-auth property is deprecated"
    And no other errors or warnings are reported

  Scenario: 'role' metadata can be used once or more to refine a creator, contributor, or publisher 
    When checking file 'metadata-meta-role-valid.opf'
    Then no errors or warnings are reported

  Scenario: 'role' metadata cannot be used to refine properties other than creator, contributor, or publisher  
    When checking file 'metadata-meta-role-refines-disallowed-error.opf'
    Then error RSC-005 is reported
    And the message contains '"role" must refine a "creator", "contributor", or "publisher" property'
    And no other errors or warnings are reported
  
  Scenario: 'source-of' metadata can be used to refine the pagination source 
    When checking file 'metadata-meta-source-of-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: 'source-of' metadata value must be "pagination" 
    When checking file 'metadata-meta-source-of-value-unknown-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "source-of" property must have the value "pagination"'
    And no other errors or warnings are reported
  
  Scenario: 'source-of' metadata cannot be used as a primary metadata 
    When checking file 'metadata-meta-source-of-refines-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "source-of" property must refine a "source" property'
    And no other errors or warnings are reported
  
  Scenario: 'source-of' metadata must refine a 'dc:source' metadata entry
    When checking file 'metadata-meta-source-of-refines-not-dcsource-error.opf'
    Then error RSC-005 is reported
    And the message contains 'The "source-of" property must refine a "source" property'
    And no other errors or warnings are reported

  Scenario: 'source-of' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-source-of-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"source-of" cannot be declared more than once'
    And no other errors or warnings are reported

  Scenario: 'term' metadata can refine a subject expression
  	When checking file 'metadata-meta-term-valid.opf'
  	Then no errors or warnings are reported

  Scenario: 'term' metadata can only refine a subject expression
  	When checking file 'metadata-meta-term-refines-disallowed-error.opf'
  	Then error RSC-005 is reported
    And the message contains 'Property "term" must refine a "subject" property'
  	Then no errors or warnings are reported

  Scenario: 'term' metadata must be associated to an authority
  	When checking file 'metadata-meta-term-no-authority-error.opf'
  	Then error RSC-005 is reported
  	And the message contains "An authority property must be associated"
  	And no other errors or warnings are reported

  Scenario: 'term' metadata must not be defined more than once
  	When checking file 'metadata-meta-term-cardinality-error.opf'
  	Then error RSC-005 is reported
  	And the message contains "Only one pair of authority and term properties"
  	And no other errors or warnings are reported

  Scenario: 'title-type' metadata can be used to refine a title expression 
    When checking file 'metadata-meta-title-type-valid.opf'
    Then no errors or warnings are reported

	Scenario: 'title-type' metadata can only refine a 'title' expression
    When checking file 'metadata-meta-title-type-refines-disallowed-error.opf'
    Then error RSC-005 is reported
    And the message contains 'Property "title-type" must refine a "title" property'
    And no other errors or warnings are reported

  Scenario: 'title-type' metadata cannot be defined more than once to refine the same expression 
    When checking file 'metadata-meta-title-type-cardinality-error.opf'
    Then error RSC-005 is reported
    And the message contains '"title-type" cannot be declared more than once'
    And no other errors or warnings are reported
  
  # D. Metadata Link Vocabulary
  
  Scenario: the link 'rel' attribute can have multiple properties
    When checking file 'link-rel-multiple-properties-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: an 'acquire' link can identify the full version of the publication
    When checking file 'link-rel-acquire-valid.opf'
    Then no errors or warnings are reported

  Scenario: an 'alternate' link can identify an alternate version of the Package Document
    When checking file 'link-rel-alternate-valid.opf'
    Then no errors or warnings are reported

  Scenario: an 'alternate' link must not be paired with other keywords
    When checking file 'link-rel-alternate-with-other-keyword-error.opf'
    Then error OPF-089 is reported
    And no other errors or warnings are reported

  Scenario: a 'record' link can point to a local record
    When checking file 'link-rel-record-local-valid.opf'
    Then no errors or warnings are reported

  Scenario: a 'record' link can point to a remote record
    When checking file 'link-rel-record-remote-valid.opf'
    Then no errors or warnings are reported

    
  Scenario: 'record' link can be paired with other keywords
    When checking file 'link-rel-record-with-other-keyword-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: a 'record' link must have a 'media-type' attribute 
    When checking file 'link-rel-record-mediatype-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains "media-type"
    And no other errors or warnings are reported

  Scenario: a 'record' link type can be further identified with a 'properties' attribute
    When checking file 'link-rel-record-properties-valid.opf'
    And no errors or warnings are reported

  Scenario: a 'record' link with an unknown identifier property is reported
    When checking file 'link-rel-record-properties-undefined-error.opf'
    Then error OPF-027 is reported
    And no other errors or warnings are reported
    
  Scenario: a 'record' link with an empty identifier property is reported
    When checking file 'link-rel-record-properties-empty-error.opf'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "properties" is invalid'
    And no other errors or warnings are reported

  Scenario: a 'record' link cannot refine another property or resource
    When checking file 'link-rel-record-refines-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must not have a "refines" attribute'
    And no other errors or warnings are reported

  Scenario: '*-record' links are deprecated 
    When checking file 'link-rel-record-deprecated-warning.opf'
    Then the following warnings are reported
      | OPF-086 | "marc21xml-record" is deprecated |
      | OPF-086 | "mods-record" is deprecated      |
      | OPF-086 | "onix-record" is deprecated      |
      | OPF-086 | "xmp-record" is deprecated       |
    And no other errors or warnings are reported
    
  Scenario: a 'voicing' link can identify the aural representation of metadata
    When checking file 'link-rel-voicing-valid.opf'
    Then no errors or warnings are reported
    
  Scenario: a 'voicing' link must refine another property or resource
    When checking file 'link-rel-voicing-as-publication-metadata-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must have a "refines" attribute'
    And no other errors or warnings are reported

  Scenario: a 'voicing' link must have a 'media-type' attribute
    When checking file 'link-rel-voicing-mediatype-missing-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must have a "media-type" attribute'
    And no other errors or warnings are reported

  Scenario: a 'voicing' link resource must have an audio media type
    When checking file 'link-rel-voicing-mediatype-not-audio-error.opf'
    Then error RSC-005 is reported
    And the message contains 'must have a "media-type" attribute identifying an audio MIME type'
    And no other errors or warnings are reported
    
  Scenario: 'xml-signature' links are deprecated 
    When checking file 'link-rel-xml-signature-deprecated-warning.opf'
    Then warning OPF-086 is reported
    And the message contains '"xml-signature" is deprecated'
    And no other errors or warnings are reported

  
  # E. Manifest Properties Vocabulary
  
  Scenario: An unknown item property in the default vocab is reported
    When checking file 'item-property-unknown-error.opf'
    Then error OPF-027 is reported
    And no errors or warnings are reported
  
  Scenario: The 'cover-image' item property must occur at most once 
    When checking file 'item-property-cover-image-multiple-error.opf'
    Then error RSC-005 is reported
    And the message contains "cover-image"
    And no other errors or warnings are reported

  Scenario: The 'cover-image' item property must only be used on images 
    When checking file 'item-property-cover-image-wrongtype-error.opf'
    Then error OPF-012 is reported
    And no other errors or warnings are reported  

  # F. Spine Properties Vocabulary
  