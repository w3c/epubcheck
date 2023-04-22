Feature: EPUB 3 — Open Container Format


  Checks conformance to the "Open Container Format (OCF)" section of the EPUB 3.3 specification:
    https://www.w3.org/TR/epub-33/#sec-ocf


  Background: 
    Given EPUB test files located at '/epub3/04-ocf/files/'
    And EPUBCheck with default settings


	## 4.1 OCF Abstract Container

  ###  4.1.2 File and Directory Structure

  @spec @xref:sec-container-file-and-dir-structure
  Scenario: Report publication resources found in META-INF
    When checking EPUB 'ocf-meta-inf-with-publication-resource-error'
    Then error PKG-025 is reported
    And no other errors or warnings are reported


  ###  4.1.3 File paths and file names

  @spec @xref:sec-container-filenames
  Scenario: Verify a file name containing a `+` character is allowed (issue 188)
    When checking EPUB 'ocf-container-filename-character-plus-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report a duplicate filename after common case folding
    When checking EPUB 'ocf-filename-duplicate-after-common-case-folding-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report a duplicate filename after full case folding
    When checking EPUB 'ocf-filename-duplicate-after-full-case-folding-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report a duplicate filename after Unicode canonical normalization (NFC)
    When checking EPUB 'ocf-filename-duplicate-after-canonical-normalization-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Allow a duplicate filename after Unicode compatibility normalization (NFKC)
    When checking EPUB 'ocf-filename-duplicate-after-compatibility-normalization-valid.epub'
    Then no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Allow Unicode emoji tag set in file name
    When checking EPUB 'ocf-filename-character-emoji-tag-sequence-valid'
    Then no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report forbidden characters in file names
    When checking EPUB 'ocf-filename-character-forbidden-error.epub'
    Then error PKG-009 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report forbidden characters in file names even for non-publication resources 
    When checking EPUB 'ocf-filename-character-forbidden-non-publication-resource-error.epub'
    Then error PKG-009 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Allow forbidden characters in remote resource URLs 
    When checking EPUB 'ocf-filename-character-forbidden-in-remote-URL-valid.opf'
    Then no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Report forbidden characters in file names (single package doc check)
    When checking EPUB 'ocf-filename-character-forbidden-error.opf'
    Then error PKG-009 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Inform about non-ASCII characters in file names (full-publication check)
    Given the reporting level is set to USAGE
    When checking EPUB 'ocf-filename-character-non-ascii-usage'
    Then usage PKG-012 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-filenames
  Scenario: Inform about non-ASCII characters in file names (single package doc check)
    Given the reporting level is set to USAGE
    When checking EPUB 'ocf-filename-character-non-ascii-usage.opf'
    Then usage PKG-012 is reported
    And no other errors or warnings are reported
    
  @spec @xref:sec-container-filenames
  Scenario: Warn about spaces in file names (full-publication check)
    When checking file 'ocf-filename-character-space-warning'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported
    
  @spec @xref:sec-container-filenames
  Scenario: Warn about spaces in file names (single package doc check)
    When checking file 'ocf-filename-character-space-warning.opf'
    Then warning PKG-010 is reported
    And no other errors or warnings are reported
    
  ###  4.1.5 URLs in the OCF abstract container

  #### Valid container URLs

  @spec @xref:sec-container-iri
  Scenario: Allow valid container URLs in XHTML
    When checking EPUB 'url-in-xhtml-valid.xhtml'
    And no errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Allow percent-encoded URLs
    When checking EPUB 'url-percent-encoded-valid'
    And no errors or warnings are reported


  #### Invalid container URLs

  @spec @xref:sec-container-iri
  Scenario: Report leaking URLs in the package document
    When checking EPUB 'ocf-url-leaking-in-opf-error'
    Then error RSC-026 is reported 2 times
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report path-absolute URLs in the package document
    When checking EPUB 'ocf-url-path-absolute-error'
    Then error RSC-026 is reported
    And no other errors or warnings are reported

  #### URL query checks:

  @spec @xref:sec-container-iri
  Scenario: Report a URL query string found in a manifest item
    When checking EPUB 'url-query-in-package-item-error.opf'
    Then error RSC-033 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a URL query string found in a package link
    When checking EPUB 'url-query-in-package-link-error.opf'
    Then error RSC-033 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a URL query string found in a manifest item
    When checking EPUB 'url-query-in-xhtml-a-error.xhtml'
    Then error RSC-033 is reported
    And no other errors or warnings are reported

  #### resource existence checks:

  Scenario: Allow an absolute `cite` URL
    When checking EPUB 'url-xhtml-cite-absolute-valid'
    Then no errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a relative `cite` URL when the resource is not found in the manifest
    When checking EPUB 'url-xhtml-cite-missing-resource-error'
    Then error RSC-007 is reported 4 times
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a reference from an XHTML `iframe` not declared in the manifest
    When checking EPUB 'url-xhtml-iframe-missing-resource-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-container-iri
  Scenario: Report a reference from an XHTML `track` not declared in the manifest
    When checking EPUB 'url-xhtml-track-missing-resource-error'
    Then error RSC-007 is reported
    And no other errors or warnings are reported


  ###  4.1.6 META-INF Directory

  ####  4.1.6.3.1 Container File (container.xml)

  @spec @xref:sec-container-metainf-container.xml
  Scenario: Report an unknown element in the `container.xml` file
    When checking EPUB 'ocf-container-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'element "foo" not allowed anywhere'
    And no other errors or warnings are reported

  @spec @xref:sec-container-metainf-container.xml
  Scenario: Report a missing 'container.xml' file
    When checking EPUB 'ocf-container-file-missing-fatal'
    Then fatal error RSC-002 is reported
    Then no errors or warnings are reported

  @spec @xref:sec-container-metainf-container.xml
  Scenario: Report a fatal error when checking an archive that is not an OCF
    When checking EPUB 'ocf-container-not-ocf-error.epub'
    Then fatal error RSC-002 is reported (container.xml not found)
    And error PKG-006 is reported (missing mimetype)
    Then no errors or warnings are reported

  ### 4.1.6.3.1.3 The rootfile element

  @spec @xref:sec-container.xml-rootfile-elem
  Scenario: Report a missing OPF document
    When checking EPUB 'ocf-package-document-missing-fatal'
    Then fatal error OPF-002 is reported
    And no other errors or warnings are reported


  ####  Encryption File (encryption.xml)
  
  #### 4.1.6.3.2.1 The encryption element

  @spec @xref:sec-encryption.xml-encryption
  Scenario: Report an `encryption.xml` file with invalid markup
    When checking EPUB 'ocf-encryption-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "encryption"'
    And no other errors or warnings are reported

  Scenario: Verify encryption can be used
    (but file will not be parsed)
    Given the reporting level is set to INFO
    When checking EPUB 'ocf-encryption-unknown-valid'
    Then info RSC-004 is reported
    And no other errors or warnings are reported

	#FIXME !!! test that RSC-007 is reported when resource referenced in encryption.xml was not found
	   
  @spec @xref:sec-encryption.xml-encryption
  Scenario: Report an `encryption.xml` file with duplicate IDs
    When checking EPUB 'ocf-encryption-duplicate-ids-error'
    Then error RSC-005 is reported 2 times
    And the message contains 'Duplicate'
    And no other errors or warnings are reported

  #### 4.1.6.3.2.2 Order of compression and encryption
  
  @spec @xref:sec-enc-compression
  Scenario: Report an `encryption.xml` file with invalid compression metadata
    When checking EPUB 'ocf-encryption-compression-attributes-invalid-error'
    Then the following errors are reported
      | RSC-005 | value of attribute "Method" is invalid         |
      | RSC-005 | value of attribute "OriginalLength" is invalid |


  #### 4.1.6.3.6 Digital Signatures File (signatures.xml)
  
  ##### 4.1.6.3.6.1 The signatures element

  @spec @xref:sec-signatures.xml-signatures
  Scenario: Report a `signature.xml` file with invald markup
    When checking EPUB 'ocf-signatures-content-model-error'
    Then error RSC-005 is reported
    And the message contains 'expected element "signatures"'
    And no other errors or warnings are reported

  
  
  ## 4.2 OCF ZIP container  
  
  ### 4.2.2 ZIP file requirements

  @spec @xref:sec-epub-conf @xref:sec-zip-container-zipreqs
  Scenario: Verify a minimal packaged EPUB
    When checking EPUB 'ocf-zip-valid.epub'
    Then no errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report an unreadable ZIP file (empty file)
    When checking EPUB 'ocf-zip-unreadable-empty-fatal.epub'
    Then error PKG-003 is reported
    Then fatal error PKG-008 is reported
    And the message contains 'zip file is empty'
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report an unreadable ZIP file (no end header)
    When checking EPUB 'ocf-zip-unreadable-no-end-header-fatal.epub'
    Then fatal error PKG-008 is reported
    And the message contains 'zip'
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report an unreadable ZIP file (image file with an '.epub' extension)
    When checking EPUB 'ocf-zip-unreadable-image-with-epub-extension-fatal.epub'
    Then fatal error PKG-004 is reported (corrupted ZIP header)
    Then fatal error PKG-008 is reported (error in opening ZIP file)
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report a duplicate ZIP entry for the same file
    When checking EPUB 'ocf-filename-duplicate-zip-entry-error.epub'
    Then error OPF-060 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Verify file names with non-ASCII UTF-8-encoded character are allowed
    When checking EPUB 'ocf-filename-utf8-valid.epub'
    Then no errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report file names that are not encoded as UTF-8
    When checking EPUB 'ocf-filename-not-utf8-error.epub'
    Then fatal error PKG-027 is reported
    Then no errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Verify path names with non-ASCII UTF-8-encoded character are allowed
    When checking EPUB 'ocf-filepath-utf8-valid.epub'
    Then no errors or warnings are reported

  @spec @xref:sec-zip-container-zipreqs
  Scenario: Report file names that are not encoded as UTF-8
    When checking EPUB 'ocf-filepath-not-utf8-error.epub'
    Then fatal error PKG-027 is reported
    Then no errors or warnings are reported


  ### 4.2.3 OCF ZIP container media type idenfication

  @spec @xref:sec-zip-container-mime
  Scenario: Report a mimetype file with an incorrect value
    When checking EPUB 'ocf-mimetype-file-incorrect-value-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-mime
  Scenario: Report a mimetype file with leading spaces
    When checking EPUB 'ocf-mimetype-file-leading-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-mime
  Scenario: Report a missing mimetype file
    When checking EPUB 'ocf-mimetype-file-missing-error'
    Then error PKG-006 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-mime
  Scenario: Report a mimetype file with a trailing newline
    When checking EPUB 'ocf-mimetype-file-trailing-newline-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-mime
  Scenario: Report a mimetype file with trailing spaces
    When checking EPUB 'ocf-mimetype-file-trailing-spaces-error'
    Then error PKG-007 is reported
    And no other errors or warnings are reported

  @spec @xref:sec-zip-container-mime
  Scenario: Report when the 'mimetype' entry has an extra field in its ZIP header
    When checking EPUB 'ocf-zip-mimetype-entry-extra-field-error.epub'
    Then error PKG-005 is reported
    And no other errors or warnings are reported


  ## 4.3 Font obfuscation

  ### 4.3.5 Specifying obfuscated fonts
  
  @spec @xref:obfus-specifying
  Scenario: Verify a publication with obfuscated font
    When checking EPUB 'ocf-obfuscation-valid'
    Then no errors or warnings are reported

  @spec @xref:obfus-specifying
  Scenario: Report an obfuscated font that is not a Core Media Type
    When checking EPUB 'ocf-obfuscation-not-cmt-error'
    Then error PKG-026 is reported
    And no errors or warnings are reported

  @spec @xref:obfus-specifying
  Scenario: Report an obfuscated font that is not a font
    When checking EPUB 'ocf-obfuscation-not-font-error'
    Then error PKG-026 is reported
    And no errors or warnings are reported
