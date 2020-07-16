Feature: EPUB 2 ▸ Open Container Format ▸ Full Publication Checks


  Checks conformance to the Open Container Format (OCF) 2.0.1 specification:
    http://matt.garrish.ca/res/OCF_2.0.1.html

  In the scenarios below, checks are run against full EPUB publications.
  EPUBCheck is launched in default mode.


  Background: 
    Given test files located at '/epub2/files/epub/'
    And EPUBCheck configured to check EPUB 2.0.1 rules

    
  ## 3. OCF Container Contents

  Scenario: Verify a minimal EPUB 2.0.1 publication
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  Scenario: Verify a minimal EPUB 2.0.1 publication even when a 3.0 profile is specified
    Given EPUBCheck configured with the 'dict' profile
    When checking EPUB 'minimal'
    Then no errors or warnings are reported

  ### 3.3 File Names

  Scenario: Report a file name with spaces
    See issue #239 for why this needs to also be tested at the publication level
    When checking EPUB 'ocf-filename-with-space-warning'
    Then warning PKG-010 is reported
    Then no errors or warnings are reported

  ### 3.4 Container media type identification

  Scenario: Report an empty directory in the OCF structure
    When checking EPUB 'ocf-directory-empty-warning.epub'
    Then warning PKG-014 is reported
    And no other errors or warnings are reported


  ### 3.4 Container media type identification

  Scenario: Report a missing mimetype file
    When checking EPUB 'ocf-mimetype-missing-error'
    Then error PKG-006 is reported
    And no other errors or warnings are reported

  Scenario: Report trailing spaces in the mimetype file
    When checking EPUB 'ocf-mimetype-with-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported


  ### 3.5 META-INF

  Scenario: Ignore unknown files in the META-INF directory
    When checking EPUB 'ocf-metainf-file-unknown-valid'
    Then no errors or warnings are reported

  #### 3.5.1 Container (META-INF/container.xml)

  Scenario: Allow alternative rootfiles in the 'container.xml' file
    When checking EPUB 'ocf-metainf-container-alternative-valid'
    Then no errors or warnings are reported

  Scenario: Report a missing 'container.xml' file
    When checking EPUB 'ocf-metainf-container-file-missing-fatal'
    Then fatal error RSC-002 is reported
    And error RSC-001 is reported (unnecessary, but generic error for missing resources)
    Then no errors or warnings are reported

  Scenario: Report multiple OPF rootfiles in the 'container.xml' file
    When checking EPUB 'ocf-metainf-container-multiple-opf-error'
    Then error PKG-013 is reported
    And no other errors or warnings are reported

  Scenario: Report a wrong media type on the 'rootfile' element of the 'container.xml' file
    When checking EPUB 'ocf-metainf-container-mediatype-invalid-error'
    Then error RSC-003 is reported
    And no other errors or warnings are reported

  Scenario: Report a missing 'full-path' attribute on the rootfile element of the 'container.xml' file
    When checking EPUB 'ocf-metainf-container-rootfile-full-path-missing-error'
    Then error OPF-016 is reported
    And no other errors or warnings are reported

  Scenario: Report an empty 'full-path' attribute on the rootfile element of the 'container.xml' file
    When checking EPUB 'ocf-metainf-container-rootfile-full-path-empty-error'
    Then error OPF-017 is reported
    And no other errors or warnings are reported

  Scenario: Report a missing OPF document
    When checking EPUB 'ocf-opf-missing-fatal'
    Then fatal error OPF-002 is reported
    And error RSC-001 is reported (unnecessary, but generic error for missing resources)
    And no other errors or warnings are reported

  ## 4. ZIP Container

  Scenario: Verify a minimal packaged EPUB 2.0.1 publication
    When checking EPUB 'ocf-minimal-valid.epub'
    Then no errors or warnings are reported
