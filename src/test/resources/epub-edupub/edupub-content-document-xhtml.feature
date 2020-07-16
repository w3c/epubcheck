Feature: EPUB for Education ▸ XHTML Content Document Checks


  Checks conformance to the EPUB for Education specification:
    http://idpf.org/epub/profiles/edu/spec/

  In the scenarios below, checks are run against single XHTML Content Documents.
  EPUBCheck is launched in 'xhtml' mode.


  Background: 
    Given EPUB test files located at '/epub-edupub/files/content-document-xhtml/'
    And EPUBCheck configured to check an XHTML Content Document
    And EPUBCheck configured with the 'edupub' profile


  #  4. Content Structure

  Scenario: Minimal Content Document
    When checking document 'edupub-minimal-valid.xhtml'
    Then no errors or warnings are reported


  ##  4.2 Sectioning

  Scenario: Verify `body` used as an explicit section of content
    When checking document 'edupub-body-explicit-section-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `body` only contains sectioning elements when no heading (remove this invalid duplicate? see issue 1109)
    When checking document 'edupub-body-implicit-section-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Verify `body` only contains sectioning elements when no heading
    When checking document 'edupub-body-sectioning-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report `body` used as an explicit section without a heading
    When checking document 'edupub-body-explicit-section-no-heading-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'The body element requires a heading when it is used as an implied section'
    And no other errors or warnings are reported


  ##  4.3 Titles and Headings
  
  ###  4.3.1 Titled Sections

  Scenario: Verify section title and subtitle are in a `header` element
    When checking document 'edupub-titles-subtitle-header-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report section subtitle not in a `header` element
    When checking document 'edupub-titles-subtitle-header-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Section subtitles must be wrapped in a header element'
    And no other errors or warnings are reported

  Scenario: Report invalid and missing headings in sectioning elements
    When checking document 'edupub-titles-invalid-missing-error.xhtml'
    Then error RSC-005 is reported 3 times
    And the message contains ''
    And no other errors or warnings are reported

  Scenario: Report invalid subheadings within a `body` used as an explicit section
    When checking document 'edupub-titles-explicit-body-error.xhtml'
    Then error RSC-005 is reported 3 times
    And the message contains 'does not match the current nesting level'
    And no other errors or warnings are reported

  Scenario: Report `aria-label` on `body` and `section` with headings
    When checking document 'edupub-titles-aria-label-matches-heading-error.xhtml'
    Then error RSC-005 is reported 2 times
    And the message contains 'The value of the "aria-label" attribute must not be the same as the content of the heading'
    And no other errors or warnings are reported

  
  ###  4.3.2 Untitled Sections

  Scenario: Report incorrect heading level following `aria-label` on `body`
    When checking document 'edupub-untitled-heading-level-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'does not match the current nesting level'
    And no other errors or warnings are reported


  #  No matching section

  Scenario: Verify a heading with only an `img` that has alternative text
    When checking document 'edupub-heading-img-alt-valid.xhtml'
    Then no errors or warnings are reported

  Scenario: Report a heading with only an `img` without alternative text
    When checking document 'edupub-heading-img-no-alt-error.xhtml'
    Then error RSC-005 is reported
    And the message contains 'Empty ranked heading detected'
    And no other errors or warnings are reported

