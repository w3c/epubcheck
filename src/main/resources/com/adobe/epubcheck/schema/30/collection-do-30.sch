<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
	
	<pattern id="do.collection">
		<rule context="opf:collection[tokenize(@role,'\s+')='distributable-object']">
			<assert test="child::opf:metadata">A
				distributable-object collection must include a child metadata element.</assert>
			<report test="count(child::opf:collection[@role='manifest']) &gt; 1">A
				distributable-object collection must not contain more than one child manifest
				collection.</report>
			<assert test="count(child::opf:link) &gt; 0">A
				distributable-object collection must include at least one child link
				element.</assert>
		</rule>
		<rule context="opf:collection[tokenize(@role,'\s+')='distributable-object']/opf:metadata">
			<assert test="count(child::dc:identifier)=1">The distributable-object metadata must
				include exactly one identifier (dc:identifier).</assert>
			<assert test="child::dc:title">The distributable-object metadata must include at least
				one title (dc:title).</assert>
			<assert test="child::dc:language">The distributable-object metadata must include at
				least one language declaration (dc:language).</assert>
		</rule>
	</pattern>

</schema>
