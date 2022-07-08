Feature: EPUB 3 ▸ Content Documents ▸ CSS


  Checks conformance to the "Cascading Style Sheets" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-css


  Background: 
    Given EPUB test files located at '/epub3/06-content-document/files/'
    And EPUBCheck with default settings


  ###  6.3.1 CSS Style Sheets
  
  Scenario: Verify a minimal publication with a stylesheet 
    When checking EPUB 'content-css-minimal-valid'
    Then no errors or warnings are reported
    
  #### 6.3.1.2 CSS requirements
    
  ##### Properties not allowed in EPUB

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

  ##### Encoding

  Scenario: Verify a CSS file with a `@charset` declaration and UTF8 encoding
    See also issue #262
    When checking EPUB 'content-css-charset-utf8-valid'
    Then no errors or warnings are reported

  Scenario: Report a CSS file with a `@charset` declaration that is not utf-8
    When checking EPUB 'content-css-charset-enc-error'
    Then error CSS-003 is reported
    And no other errors or warnings are reported

  ##### Resources and imports

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

  #### CSS syntax

  Scenario: Verify valid CSS Selectors syntax
    When checking EPUB 'content-css-selectors-valid'
    Then no errors or warnings are reported

  Scenario: Report CSS syntax errors
    When checking EPUB 'content-css-syntax-error'
    Then error CSS-008 is reported 2 times
    Then no errors or warnings are reported

  Scenario: Report an empty `@font-face` declaration
    When checking EPUB 'content-css-font-face-empty-error'
    Then warning CSS-019 is reported
    Then no errors or warnings are reported

  Scenario: Report a `@font-face` declaration with an empty URL reference
    When checking EPUB 'content-css-font-face-url-empty-error'
    Then error CSS-002 is reported
    Then no errors or warnings are reported

  Scenario: Do not check invalid CSS `font-size` values
    Note: this is out of scope for EPUBCheck, 
          until we integrate proper CSS validation
    When checking EPUB 'content-css-font-size-value-error'
    Then no errors or warnings are reported

  Scenario: Verify that CSS `font-size: 0` declaration is allowed (issue 922)
    When checking EPUB 'content-css-font-size-zero-valid'
    Then no errors or warnings are reported

  Scenario: Verify a fragment-only URL does not trigger a "fragment not defined" error 
    When checking EPUB 'content-css-url-fragment-valid'
    Then no errors or warnings are reported


