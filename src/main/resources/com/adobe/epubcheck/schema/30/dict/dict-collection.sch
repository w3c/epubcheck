<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="dict.collection">
		<rule context="opf:collection[tokenize(@role, '\s+') = 'dictionary']">
			<report test="exists(opf:collection)">An EPUB Dictionary collection must not have
				sub-collections.</report>
		</rule>
	</pattern>

	<pattern id="dict.collection.lang">
		<rule context="opf:collection[tokenize(@role, '\s+') = 'dictionary']">
			<assert
				test="
					exists(/opf:package/opf:metadata/opf:meta[normalize-space(@property) = 'source-language']
					| opf:metadata/opf:meta[normalize-space(@property) = 'source-language'])"
				>An EPUB Dictionary must declare its source language using a "source-language"
				metadata.</assert>
			<assert
				test="
					exists(/opf:package/opf:metadata/opf:meta[normalize-space(@property) = 'target-language']
					| opf:metadata/opf:meta[normalize-space(@property) = 'target-language'])"
				>An EPUB Dictionary must declare its source language using a "target-language"
				metadata.</assert>
			<report
				test="count(opf:metadata/opf:meta[normalize-space(@property)='source-language'])>1"
				>An EPUB Dictionary must not declare more than one source language.</report>
		</rule>
	</pattern>

</schema>
