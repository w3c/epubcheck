<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

	<ns uri="urn:oasis:names:tc:opendocument:xmlns:container" prefix="ocf"/>
	<ns uri="http://www.idpf.org/2013/rendition" prefix="rendition"/>

	<pattern id="selection.accessModes">
		<rule context="ocf:rootfile[@rendition:accessMode]">
			<report
				test="some $mode in tokenize(@rendition:accessMode,'\s+') satisfies (count(tokenize(@rendition:accessMode, $mode))=3)"
				>The accessMode attribute contains a duplicate value.</report>
		</rule>
	</pattern>

	<pattern id="selection.mandatingOneSelectionAttribute">
		<rule context="ocf:rootfile[position() > 1]">
			<assert test="count(@rendition:*) > 0">WARNING: At least one rendition selection
				attribute should be specified for each non-first rootfile element, which represents
				a non-default rendition."</assert>
		</rule>
	</pattern>

	<pattern id="mapping.atMostOne">
		<rule context="ocf:links">
			<report test="count(ocf:link[normalize-space(@rel)='mapping'])>1">The Container Document
				must not reference more than one mapping document.</report>
		</rule>
	</pattern>
	<pattern id="mapping.mediaType">
		<rule context="ocf:link[normalize-space(@rel) = 'mapping']">
			<assert test="normalize-space(@media-type) = 'application/xhtml+xml'">The media type of
				Rendition Mapping Documents must be "application/xhtml+xml".</assert>
		</rule>
	</pattern>
</schema>
