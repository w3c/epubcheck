Feature: EPUB 2 ▸ Open Packaging Format ▸ Full Publication Checks


  Checks conformance to the Open Packaging Format (OPF) 2.0.1 specification:
    http://idpf.org/epub/20/spec/OPF_2.0_latest.htm

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules

    
  ## 1.0; Overview

	# FIXME the current API doesn’t allow the version to be explicitly set
	# PKG-001 should either be removed, or made a fatal error
  Scenario: Report when checking an EPUB 2.0.1 explicitly against EPUB 3.x
    Given EPUBCheck configured to check EPUB 3 rules
    When checking EPUB 'minimal'
    #Then error PKG-001 is reported
    And no other errors or warnings are reported


  ###  1.3.2: Relationship to XML Namespaces

  Scenario: Report a missing version attribute
    When checking EPUB 'opf-version-missing-error'
    Then error OPF-001 is reported
    And no other errors or warnings are reported


  ###  1.4.1: Package Conformance

  ####  1.4.1.2: Publication Conformance

  Scenario: Verify that the 'clr' MARC code is allowed in the `opf:role` attribute (issue 205)
    When checking EPUB 'opf-metadata-creator-role-clr-valid'
    Then no errors or warnings are reported


  #  2.0: The OPF Package Document
  
  ## 2.1: Package Identity

  Scenario: Verify that package IDs with leading/trailing spaces are allowed (issue 332)
    When checking EPUB 'opf-package-id-spaces-valid'
    Then no errors or warnings are reported

  Scenario: the unique identifier must not be empty
    When checking EPUB 'opf-unique-identifier-not-found-error'
    Then error OPF-030 is reported
    And no other errors or warnings are reported


  ##  2.3: Manifest

  Scenario: Report a reference to a resource that is not listed in the manifest
    When checking EPUB 'opf-manifest-item-missing-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  Scenario: Report a resource declared in the manifest but missing from the container
    When checking EPUB 'opf-manifest-item-resource-missing-error'
    Then error RSC-001 is reported
    And no other errors or warnings are reported

  Scenario: Report an XHTML OPS document declard as 'text/html'
    When checking EPUB 'opf-manifest-item-xhtml-mediatype-html-warning'
    Then warning OPF-035 is reported
    And no other errors or warnings are reported

  Scenario: Report a reference to a resource that is not listed in the manifest
    When checking EPUB 'opf-manifest-resource-undeclared-warning'
    Then warning OPF-003 is reported
    And no other errors or warnings are reported

  Scenario: Verify that operating system files (`.DS_STORE`, `thumbs.db`) are ignored (issue 256)
    When checking EPUB 'opf-manifest-os-files-ignore-valid'
    Then no errors or warnings are reported
    
  Scenario: Report a reference to a remote resource from an `object` element when the resource is not declared in package document
    When checking EPUB 'opf-remote-object-undeclared-error'
    Then error RSC-006 is reported
    And no other errors or warnings are reported


  ###  2.3.1: Fallback Items
  
  ####  2.3.1.1: Items That Are Not OPS Core Media Types

  Scenario: Report a manifest fallback that does not resolve to a resource in the publication
    When checking EPUB 'opf-fallback-non-resolving-error'
    Then error OPF-040 is reported (missing resource)
    And error MED-003 is reported (fallback isn't provided)
    And no other errors or warnings are reported


  ##  2.4: Spine

  Scenario: Report a missing spine
    When checking EPUB 'opf-spine-missing-error'
    Then the following errors are reported
      | RSC-005 | missing required element "spine" |
      | NCX-002 | toc attribute was not found      | # side effect
    And fatal error OPF-019 is reported
    And no other errors or warnings are reported

  Scenario: Report a spine with no 'toc' attribute
    When checking EPUB 'opf-spine-toc-attribute-missing-error'
    Then error RSC-005 is reported
    And the message contains 'missing required attribute "toc"'
    And error NCX-002 is reported (publication-level check)
    And no other errors or warnings are reported

  Scenario: Report a toc attribute pointing to something else than the NCX
    When checking EPUB 'opf-spine-toc-attribute-to-non-ncx-error'
    Then error OPF-050 is reported (toc references a non-NCX resource)
    And error CHK-008 is reported (skipping checks for the item)
    And no other errors or warnings are reported

  Scenario: Report repeated spine items (issue 182)
    When checking EPUB 'opf-spine-itemref-repeated-error'
    Then error OPF-034 is reported
    And no other errors or warnings are reported

  Scenario: Report a 'page-map' attribute (invalid Adobe extension)
    When checking EPUB 'opf-pagemap-error'
    Then error RSC-005 is reported
    And the message contains 'attribute "page-map" not allowed here'
    And no other errors or warnings are reported

  # FIXME OPF-063 is probably useless as 'page-map' are not supported
  Scenario: Report a 'page-map' attribute (invalid Adobe extension) pointing to nowhere
    When checking EPUB 'opf-pagemap-ref-not-found-warning'
    Then error RSC-005 is reported ("page-map" not allowed)
    And warning OPF-063 is reported
    And no other errors or warnings are reported


  ## 2.6: Guide

	# FIXME this should be detected in single-file mode
  Scenario: Report 'guide' references that are not in the manifest 
    When checking EPUB 'opf-guide-reference-undeclared-error'
    Then error OPF-031 is reported (guide reference not declared)
    And error RSC-007 is reported (referenced resource not found in the EPUB)
    And no other errors or warnings are reported

	# FIXME this should be detected in single-file mode
  Scenario: Report 'guide' references to non-OPS resources
    When checking EPUB 'opf-guide-reference-to-image-error'
    Then error OPF-032 is reported
    And no other errors or warnings are reported

  ## Legacy

  Scenario: Report a legacy OEBPS 1.2 publication
    When checking EPUB 'opf-legacy-oebps12-error'
    Then error OPF-001 is reported (version not found)
    And fatal error OPF-019 is reported (spine not found)
    And no other errors or warnings are reported

  # FIXME there’s no real point in reporting these since OEBPS 1.2 is not fully supported
  Scenario: Report a bad content document media type on legacy OEBPS 1.2 publications
    When checking EPUB 'opf-legacy-oebps12-mediatype-html-warning'
    Then warning OPF-039 is reported

  # FIXME there’s no real point in reporting these since OEBPS 1.2 is not fully supported
  Scenario: Report a bad CSS media type on legacy OEBPS 1.2 publications
    When checking EPUB 'opf-legacy-oebps12-mediatype-css-warning'
    Then warning OPF-038 is reported
