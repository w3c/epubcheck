<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>


	<pattern id="dict.dctype">
		<rule context="/opf:package/opf:metadata">
			<assert test="child::dc:type[normalize-space(.) = 'dictionary']">The dc:type identifier
				"dictionary" is required.</assert>
		</rule>
	</pattern>

	<pattern id="dict.single-dict">
		<rule
			context="opf:package[count(opf:collection[tokenize(@role, '\s+') = 'dictionary']) lt 2]">
			<assert
				test="
					count(opf:manifest/opf:item[
					tokenize(@properties, '\s+') = 'search-key-map'
					and tokenize(@properties, '\s+') = 'dictionary'
					]) = 1"
				>An EPUB Publication consisting of a single EPUB Dictionary must contain exactly one
				Search Key Map document for this dictionary (i.e. exactly one item with properties
				"search-key-map" and "dictionary").</assert>
		</rule>
	</pattern>
	<pattern id="dict.single-dict.lang">
		<rule context="opf:package[empty(opf:collection[tokenize(@role, '\s+') = 'dictionary'])]">
			<assert
				test="exists(opf:metadata/opf:meta[normalize-space(@property) = 'source-language'])"
				>An EPUB Dictionary must declare its source language using a "source-language"
				metadata.</assert>
			<assert
				test="exists(opf:metadata/opf:meta[normalize-space(@property) = 'target-language'])"
				>An EPUB Dictionary must declare its source language using a "target-language"
				metadata.</assert>
			<report
				test="count(opf:metadata/opf:meta[normalize-space(@property)='source-language'])>1"
				>An EPUB Dictionary must not declare more than one source language.</report>
		</rule>
	</pattern>
	<pattern id="dict.skm">
		<rule context="opf:item[tokenize(@properties, '\s+') = 'search-key-map']">
			<let name="skm" value="normalize-space(@href)"/>
			<report
				test="count(/opf:package/opf:collection[tokenize(@role,'\s+')='dictionary']
				            /opf:link[normalize-space(@href)=$skm]) > 1"
				>Search Key Map Document "<value-of select="$skm"/>" is referenced in more than one
				dictionary collection.</report>
		</rule>
	</pattern>
	<pattern id="dict.lang">
		<rule
			context="
				opf:meta[normalize-space(@property) = ('source-language',
				'target-language')]">
			<let name="lang" value="normalize-space(.)"/>
			<assert test="exists(/opf:package/opf:metadata/dc:language[normalize-space(.) = $lang])"
				>EPUB Dictionaries "source-language" and "target-language" must also be declared as
				"dc:language" in package-level metadata.</assert>
		</rule>
	</pattern>
	<pattern id="dict.type">
		<rule context="
				opf:meta[normalize-space(@property) = 'dictionary-type']">
			<assert
				test="
					normalize-space(.) = ('monolingual',
					'bilingual',
					'multilingual',
					'thesaurus',
					'encyclopedia',
					'spelling',
					'pronouncing',
					'etymological')"
				>EPUB Dictionaries "dictionary-type" metadata must be one of "monolingual",
				"bilingual", "multilingual", "thesaurus", "encyclopedia", "spelling", "pronouncing",
				or "etymological".</assert>
		</rule>
	</pattern>



</schema>
