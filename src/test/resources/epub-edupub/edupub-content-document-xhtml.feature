Feature: EPUB for Education XHTML Content Documents
  
  Checks conformance to rules for XHTML Content Documents defined in the
  EPUB for Education specification (aka “EDUPUB”):
    
    http://idpf.org/epub/profiles/edu/spec/
  
  This feature file contains tests for EPUBCheck running in `xhtml` mode to check
  single XHTML Content Documents (`.xhtml` files).
  
  Background: 
    Given EPUB test files located at '/epub-edupub/files/content-document-xhtml/'
    And EPUBCheck configured to check an XHTML Content Document
    And EPUBCheck configured with the 'edupub' profile

  Scenario: Verify a minimal EDUPUB XHTML Content Document 
    When checking file 'edupub-minimal-valid.xhtml'
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

