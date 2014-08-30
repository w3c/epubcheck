<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="manifest.collection">
		<rule context="opf:collection[tokenize(@role,'\s+')='manifest']">
			<assert test="empty(opf:metadata|opf:collection)">A manifest collection must only
				contain child link elements.</assert>
			<assert test="count(child::opf:link) &gt; 0">A manifest collection must include at least
				one child link element.</assert>
			<assert test="parent::opf:collection">A manifest collection must be the child of another
				collection.</assert>
		</rule>
	</pattern>

</schema>
