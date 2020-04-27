<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="preview.collection">
		<rule context="opf:collection[tokenize(@role, '\s+') = 'preview']">
			<assert test="count(child::opf:collection[@role = 'manifest']) eq 1">A "preview"
				collection must include exactly one child "manifest" collection</assert>
			<assert test="count(child::opf:link) > 0">A "preview" collection must include at least
				one child "link" element, pointing to an EPUB Content Document.</assert>
		</rule>
	</pattern>

</schema>
