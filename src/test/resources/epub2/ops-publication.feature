Feature: EPUB 2 ▸ Open Publication Structure ▸ Full Publication Checks


  Checks conformance to the Open Publication Structure (OPS) 2.0.1 specification:
    http://idpf.org/epub/20/spec/OPS_2.0_latest.htm

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules


  ## 1.0: Overview

  ### 1.4: Conformance

  ####  1.4.1.2: XHTML Content Document Requirements

  Scenario: Verify an XHTML content document without an `.xhtml` extension
    When checking EPUB 'ops-xhtml-unusual-extension-valid'
    Then no errors or warnings are reported

  Scenario: Report a broken internal link in XHTML
    When checking EPUB 'ops-xhtml-hyperlink-to-missing-fragment-error'
    Then error RSC-012 is reported
    And no other errors or warnings are reported

  Scenario: Report a missing stylesheet referenced in a link element
    See issue #316 about link/@rel being case-insensitive
    When checking EPUB 'ops-xhtml-link-to-missing-stylesheet-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  #### 1.4.1.3: DTBook Content Document Requirements

  Scenario: Verify a publication using a DTBook Content Document
    See issue #316 about link/@rel being case-insensitive
    When checking EPUB 'ops-dtbook-valid'
    Then no errors or warnings are reported

  ## 2.0 OPS Content Document Vocabularies

  ### 2.3 Element and and Attribute Semantic Differences from and Restrictions beyond XHTML 1.1

  #FIXME this shouldn't be an error, a warning at most
  Scenario: Report usage of Javascript in XHTML
    When checking EPUB 'ops-xhtml-script-error'
    Then error SCP-004 is reported
    And no other errors or warnings are reported