Feature: EPUB 3 ▸ Navigation Document


  Checks conformance to the "EPUB navigation document" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-nav


  Background: 
    Given EPUB test files located at '/epub3/07-navigation-document/files/'
    And EPUBCheck with default settings

  Scenario: Verify a minimal Navigation Document
  	Given EPUBCheck configured to check a navigation document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported

  Scenario: Report schema errors when checking a Navigation Document in a full publication
    When checking EPUB 'nav-toc-missing-error'
    Then error RSC-005 is reported
    And no other errors or warnings are reported

  ##  7.2 The nav element: restrictions
      
  Scenario: Report an empty nav heading 
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-heading-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Heading elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a `p` element used as a nav heading
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-heading-p-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "p" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report a missing list item label
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-li-label-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "ol" not allowed yet; expected element "a" or "span"'
    And no other errors or warnings are reported
    
  Scenario: Report an empty list item label
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-li-label-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Spans within nav elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a leaf list item with no link (just a span label)
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-li-leaf-with-no-link-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "li" incomplete; missing required element "ol"'
    And no other errors or warnings are reported

  Scenario: Report a nav hyperlink without content
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-a-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Anchors within nav elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a nav hyperlink without content (but an empty nested span)
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-a-span-empty-error.xhtml'
    Then the following errors are reported (two errors as a side effect)
       | RSC-005 | Anchors within nav elements must contain text |
       | RSC-005 | Spans within nav elements must contain text |
    And no other errors or warnings are reported

  Scenario: Allow nav hyperlinks to have leading and trailig spaces
    See issue #156
    Given the reporting level is set to usage
    When checking document 'content-model-a-with-leading-trailing-spaces-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a nav list without content
  	Given EPUBCheck configured to check a navigation document
    When checking document 'content-model-ol-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "ol" incomplete'
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` that links to documents not in the spine
    When checking EPUB 'nav-links-out-of-spine-error'
    Then error RSC-011 is reported
    And no other errors or warnings are reported

  Scenario: Report external links in the `toc`, `page-list` and `landmarks` `nav` elements
    When checking EPUB 'nav-links-remote-error'
    Then error NAV-010 is reported 3 times
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` that links to resource that is not a Content Document
    When checking EPUB 'nav-links-to-non-content-document-type-error'
    Then error RSC-010 is reported
    And no other errors or warnings are reported

  Scenario: Verify a Navigation Document using EPUB CFI
    When checking EPUB 'nav-cfi-valid'
    Then no errors or warnings are reported
    


  ##  7.3 The nav element: types

  ###  7.3.2 The `toc nav` element

  Scenario: Allow a nested `toc` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-toc-nested-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a missing `toc` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-toc-missing-error.xhtml'
    Then error RSC-005 is reported (toc nav missing)
    And no other errors or warnings are reported

  Scenario: Verify a `toc nav` with links that match the reading order
    When checking EPUB 'nav-toc-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Report a `toc nav` whose links do not match the spine order 
    When checking EPUB 'nav-toc-unordered-spine-warning'
    Then warning NAV-011 is reported
    And no other errors or warnings are reported

  Scenario: Report a `toc nav` whose link fragments do match the document order
    When checking EPUB 'nav-toc-unordered-fragments-warning'
    Then warning NAV-011 is reported 2 times
    And no other errors or warnings are reported

  Scenario: Verify a `toc nav` which does not link to all spine items
    Given the reporting level set to USAGE
    When checking EPUB 'nav-toc-missing-references-to-spine-valid'
    Then no errors or warnings are reported



  ###  7.3.3 The `page-list nav` element  

  Scenario: Allow a `page-list` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-page-list-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report multiple occurences of `page-list` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-page-list-multiple-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Multiple occurrences of the "page-list" nav element'
    And no other errors or warnings are reported

  Scenario: Report nested `ol` in `page-list` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-page-list-nested-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains "page-list"
    And the message contains "no nested sublists"
    And no other errors or warnings are reported

  Scenario: Verify that a `page-list nav` with links that match the reading order
    When checking EPUB 'nav-page-list-reading-order-valid'
    Then no errors or warnings are reported

  Scenario: Verify a `page-list nav` whose links do not match the spine order 
    When checking EPUB 'nav-page-list-unordered-spine-warning'
    And no other errors or warnings are reported

  Scenario: Verify a `page-list nav` whose links do match the document order
    When checking EPUB 'nav-page-list-unordered-fragments-warning'
    And no other errors or warnings are reported


  ### 7.3.4 The `landmarks nav` element

  Scenario: Allow a `landmarks` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a link without an epub:type in a `landmarks` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-link-type-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Missing epub:type attribute on anchor inside "landmarks" nav'
    And no other errors or warnings are reported

  Scenario: Report multiple occurences of `landmarks` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-multiple-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Multiple occurrences of the "landmarks" nav element'
    And no other errors or warnings are reported

  Scenario: Allow multiple entries with the same epub:type in a `landmarks` nav when pointing to different resources
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-type-twice-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report multiple entries with the same epub:type in a `landmarks` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-type-twice-same-resource-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'Another landmark was found with the same epub:type and same reference'
    And no other errors or warnings are reported

  Scenario: Report nested `ol` in `landmarks` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-landmarks-nested-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains "landmarks"
    And the message contains "no nested sublists"
    And no other errors or warnings are reported
    
  ### 7.3.5 Other `nav` elements

  Scenario: Allow a `lot` nav
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-other-lot-valid.xhtml'
    Then no errors or warnings are reported

    
  Scenario: Report a nav other than 'toc'/'page-list'/'landmarks' without a heading
  	Given EPUBCheck configured to check a navigation document
    When checking document 'nav-other-heading-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'must have a heading'
    And no other errors or warnings are reported

  Scenario: Verify a nav without a declared epub:type is not restricted
    When checking document 'nav-type-missing-not-restricted-valid.xhtml'
    Then no errors or warnings are reported


  ## 7.4 Use in the spine

  Scenario: Allow a hidden nav (set on a `page-list` nav)
  	Given EPUBCheck configured to check a navigation document
    When checking document 'hidden-nav-valid.xhtml'
    Then no errors or warnings are reported
    
  Scenario: Report a hidden attribute with a wrong value
  	Given EPUBCheck configured to check a navigation document
    When checking document 'hidden-attribute-invalid-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "hidden" is invalid'
    And no other errors or warnings are reported
