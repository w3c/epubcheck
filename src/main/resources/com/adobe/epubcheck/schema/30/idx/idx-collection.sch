<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="idx.collection">
		<rule context="opf:collection[tokenize(@role,'\s+')='index']">
			<report test="exists(opf:collection[not(tokenize(@role,'\s+')='index-group')])">An
				'index' collection must not have sub-collections other than 'index-group'
				collections.</report>
		</rule>
		<rule context="opf:collection[tokenize(@role,'\s+')='index-group']">
			<assert test="parent::opf:collection[tokenize(@role,'\s+')='index']">An 'index-group'
				collection must be a child of an 'index' collection.</assert>
			<assert test="empty(opf:collection)">An 'index-group' collection must not have child
				collections.</assert>
		</rule>
	</pattern>

</schema>
