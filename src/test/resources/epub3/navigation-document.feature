Feature: EPUB 3 ▸ Navigation Document ▸ Navigation Document Checks


  Checks conformance to the "EPUB Navigation Document" section of the
  EPUB Packages 3.2 specification:
    https://www.w3.org/publishing/epub32/epub-packages.html#sec-package-nav

  In the scenarios below, checks are run against single Navigation Documents.
  EPUBCheck is launched in 'nav' mode.


  Background: 
    Given EPUBCheck configured to check a Navigation Document
    And test files located at '/epub3/files/navigation-document/'


  ## 5.4 EPUB Navigation Document Definition

  Scenario: Verify a minimal Navigation Document
    When checking document 'minimal.xhtml'
    Then no errors or warnings are reported
    

  ### 5.4.1 The nav Element: Restrictions
      
  Scenario: Report an empty nav heading 
    When checking document 'content-model-heading-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Heading elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a `p` element used as a nav heading 
    When checking document 'content-model-heading-p-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "p" not allowed here'
    And no other errors or warnings are reported
    
  Scenario: Report a missing list item label
    When checking document 'content-model-li-label-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "ol" not allowed yet; expected element "a" or "span"'
    And no other errors or warnings are reported
    
  Scenario: Report an empty list item label
    When checking document 'content-model-li-label-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Spans within nav elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a leaf list item with no link (just a span label)
    When checking document 'content-model-li-leaf-with-no-link-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "li" incomplete; missing required element "ol"'
    And no other errors or warnings are reported

  Scenario: Report a nav hyperlink without content
    When checking document 'content-model-a-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Anchors within nav elements must contain text'
    And no other errors or warnings are reported

  Scenario: Report a nav hyperlink without content (but an empty nested span)
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
    When checking document 'content-model-ol-empty-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'element "ol" incomplete'
    And no other errors or warnings are reported    


  ### 5.4.2 The nav Element: Types

  #### 5.4.2.2 The toc nav Element

  Scenario: Allow a nested `toc` nav
    When checking document 'nav-toc-nested-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a missing `toc` nav
    When checking document 'nav-toc-missing-error.xhtml'
    Then error RSC-005 is reported (toc nav missing)
    And no other errors or warnings are reported

  #### 5.4.2.3 The page-list nav Element  

  Scenario: Allow a `page-list` nav
    When checking document 'nav-page-list-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report multiple occurences of `page-list` nav
    When checking document 'nav-page-list-multiple-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Multiple occurrences of the "page-list" nav element'
    And no other errors or warnings are reported

  Scenario: Report nested `ol` in `page-list` nav
    When checking document 'nav-page-list-nested-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains "page-list"
    And the message contains "no nested sublists"
    And no other errors or warnings are reported

  #### 5.4.2.4 The landmarks nav Element

  Scenario: Allow a `landmarks` nav
    When checking document 'nav-landmarks-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a link without an epub:type in a `landmarks` nav
    When checking document 'nav-landmarks-link-type-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Missing epub:type attribute on anchor inside "landmarks" nav'
    And no other errors or warnings are reported

  Scenario: Report multiple occurences of `landmarks` nav
    When checking document 'nav-landmarks-multiple-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Multiple occurrences of the "landmarks" nav element'
    And no other errors or warnings are reported

  Scenario: Allow multiple entries with the same epub:type in a `landmarks` nav when pointing to different resources
    When checking document 'nav-landmarks-type-twice-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report multiple entries with the same epub:type in a `landmarks` nav
    When checking document 'nav-landmarks-type-twice-same-resource-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'Another landmark was found with the same epub:type and same reference'
    And no other errors or warnings are reported

  Scenario: Report nested `ol` in `landmarks` nav
    When checking document 'nav-landmarks-nested-warning.xhtml'
    Then warning RSC-017 is reported
    And the message contains "landmarks"
    And the message contains "no nested sublists"
    And no other errors or warnings are reported
    
  #### 5.4.2.5 Other nav Elements

  Scenario: Allow a `lot` nav
    When checking document 'nav-other-lot-valid.xhtml'
    Then no errors or warnings are reported

    
  Scenario: Report a nav other than 'toc'/'page-list'/'landmarks' without a heading
    When checking document 'nav-other-heading-missing-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'must have a heading'
    And no other errors or warnings are reported

  Scenario: Verify a nav without a declared epub:type is not restricted
    When checking document 'nav-type-missing-not-restricted-valid.xhtml'
    Then no errors or warnings are reported

  ### 5.4.3 The hidden attribute

  Scenario: Allow a hidden nav (set on a `page-list` nav)
    When checking document 'hidden-nav-valid.xhtml'
    Then no errors or warnings are reported
    
  Scenario: Report a hidden attribute with a wrong value
    When checking document 'hidden-attribute-invalid-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'value of attribute "hidden" is invalid'
    And no other errors or warnings are reported