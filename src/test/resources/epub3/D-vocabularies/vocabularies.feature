Feature: EPUB 3 — Vocabularies — Vocabulary association


	Checks conformance to the "Vocabulary association" section of the EPUB 3.3 specification:
	https://www.w3.org/TR/epub-33/#sec-vocab-assoc


	Background:
		Given EPUB test files located at '/epub3/D-vocabularies/files/'
		And EPUBCheck with default settings


	# D.1 Vocabulary association mechanisms

	## D.1.3 Default vocabularies

	Rule: EPUB creators MUST NOT assign a prefix to the URLs associated with default vocabularies using the `prefix` attribute.

		@spec @xref:sec-default-vocab
		Example: Report `prefix` re-mapping default vocabularies in a package document
			When checking file 'prefix-mapping-default-vocabs-error.opf'
			Then error OPF-007b is reported 4 times (once for each default vocabulary)
			And no other errors or warnings are reported

		@spec @xref:sec-default-vocab
		Example: Report `prefix` re-mapping default vocabularies in XHTML
			When checking file 'prefix-mapping-default-vocabs-error.opf'
			Then error OPF-007b is reported 4 times (once for each default vocabulary)
			And no errors or warnings are reported


	## D.1.4 The prefix attribute

	Rule: The value of the `prefix` attribute is a whitespace-separated list of one or more prefix-to-URL mappings

		@spec @xref:sec-prefix-attr
		Example: Allow valid `prefix` attribute mappings in package documents
			When checking file 'prefix-attribute-valid.opf'
			Then no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Allow valid `prefix` attribute mappings in XHTML content documents
			When checking file 'prefix-attribute-valid.xhtml'
			Then no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Allow valid `prefix` attribute mappings in media overlays
			When checking file 'prefix-attribute-valid.smil'
			Then no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Allow valid `prefix` attribute mappings in SVG content documents
			When checking file 'prefix-attribute-valid.svg'
			Then no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Report syntax errors in the `prefix` attribute value
			When checking file 'prefix-attribute-syntax-error.opf'
			Then error OPF-004c is reported 2 times (the test file contains 2 syntax errors)
			And no other errors or warnings are reported
			Then no errors or warnings are reported


	Rule: With the exception of reserved prefixes, EPUB creators MUST declare all prefixes used in a document

		@spec @xref:sec-prefix-attr
		Example: Allow declared prefixes used in the XHTML `epub:type` attribute
			When checking document 'prefix-in-epub-type-valid.xhtml'
			Then no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Scenario: Report undeclared prefixes used in the XHTML `epub:type` attribute
			When checking document 'prefix-undeclared-error.xhtml'
			Then error OPF-028 is reported
			And no other errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Report undeclared prefixes used in package metadata properties
			When checking file 'prefix-undeclared-error.opf'
			Then error OPF-028 is reported
			And no errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Scenario: Report undeclared prefixes used in a Media Overlays `epub:type` attribute
			When checking document 'prefix-undeclared-error.smil'
			Then error OPF-028 is reported
			And no other errors or warnings are reported


	Rule: EPUB creators MUST declare the `prefix` attribute in the namespace `http://www.idpf.org/2007/ops` in EPUB content documents and media overlay documents.

		@spec @xref:sec-prefix-attr
		Example: Report a `epub:type` using a prefix declared in a `prefix` attribute with no namespace, in an XHTML content document.
			When checking file 'prefix-attribute-no-namespace-unrecognized-error.xhtml'
			Then error OPF-028 is reported (undeclared prefix)
			And no other errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Report an `epub:type` using a prefix declared in a `prefix` attribute with no namespace, in a media overlay.
			When checking file 'prefix-attribute-no-namespace-unrecognized-error.smil'
			Then error RSC-005 is reported
			And the message contains 'attribute "prefix" not allowed here'
			Then error OPF-028 is reported (undeclared prefix)
			And no other errors or warnings are reported


	Rule: EPUB creators MUST only specify the `prefix` attribute on the root element of the respective format.

		Note that for SVG embedded by inclusion, prefixes MUST be declared on the root html element.

		@spec @xref:sec-prefix-attr
		Example: Report a `prefix` attribute on an XHTML `head` element
			When checking file 'prefix-attribute-on-head-error.xhtml'
			Then error RSC-005 is reported
			And the message contains 'attribute "epub:prefix" not allowed here'
			And no other errors or warnings are reported

		@spec @xref:sec-prefix-attr
		Example: Report a `prefix` attribute on an XHTML embedded `svg` element
			When checking file 'prefix-attribute-on-svg-error.xhtml'
			Then error RSC-005 is reported
		  And the message contains 'attribute "epub:prefix" not allowed here'
			And no other errors or warnings are reported


	Rule: EPUB creators MUST NOT declare the prefix '_'.

		@spec @xref:sec-prefix-attr
		Example: Report a declaration of the prefix '_' in XHTML content documents
			When checking file 'prefix-underscore-error.xhtml'
			Then error OPF-007a is reported
			And no other errors or warnings are reported


	Rule: EPUB creators MUST NOT declare a prefix for the Dublin Core `/elements/1.1/` namespace.

		@spec @xref:sec-prefix-attr
		Example: Report a prefix mapping to the Dublic Core elements namespace
			When checking file 'prefix-mapping-dc-elements-error.opf'
			Then error OPF-007c is reported
			And no other errors or warnings are reported


	## D.1.5 Reserved prefixes

	Rule: (IMPLIED) EPUB creators can explicitly declare reserved prefixes.

		@spec @xref:sec-reserved-prefixes
		Example: Allow explicit declarations of reserved prefixes in package documents
			When checking file 'prefix-mapping-reserved-valid.opf'
			Then no errors or warnings are reported

		@spec @xref:sec-reserved-prefixes
		Example: Allow explicit declarations of reserved prefixes in content documents
			When checking file 'prefix-mapping-reserved-valid.xhtml'
			Then no errors or warnings are reported


	Rule: EPUB creators MAY use reserved prefixes in attributes that expect a property value without declaring them in a `prefix` attribute.

		@spec @xref:sec-reserved-prefixes
		Example: Allow the reserved 'schema' prefix in package metadata properties
			When checking file 'prefix-reserved-schema-undeclared-valid.opf'
			Then no errors or warnings are reported

		@spec @xref:sec-reserved-prefixes
		Example: Allow the reserved 'prism' prefix in XHTML `epub:type` attributes
			When checking file 'prefix-reserved-prism-undeclared-valid.xhtml'
			Then no errors or warnings are reported


	Rule: EPUB creators SHOULD NOT override reserved prefixes in the `prefix` attribute.

		@spec @xref:sec-reserved-prefixes
		Example: Report overriding of reserved prefixes in package documents
			When checking file 'prefix-reserved-overridden-warning.opf'
			Then warning OPF-007 is reported 8 times (once for each reserved prefix)
			And no other errors or warnings are reported

		@spec @xref:sec-reserved-prefixes
		Example: Report overriding of reserved prefixes in XHTML content documents
			When checking file 'prefix-reserved-overridden-warning.xhtml'
			Then warning OPF-007 is reported 2 times (once for each reserved prefix)
			And no other errors or warnings are reported
