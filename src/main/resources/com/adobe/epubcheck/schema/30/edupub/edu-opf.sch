<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
	
	<pattern id="edu.dctype">
		<rule context="opf:metadata">
			<assert test="child::dc:type[normalize-space(.)='edupub']">The dc:type identifier 'edupub' is required.</assert>
		</rule>
	</pattern>
	
	<pattern id="edu.a11y">
		<rule context="opf:metadata">
			<assert test="child::opf:meta[normalize-space(@property)='schema:accessibilityFeature']">At least one
				schema:accessibilityFeature declaration is required.</assert>
			<report test="child::opf:meta[normalize-space(@property)='schema:accessibilityFeature'][normalize-space(.)='none']">The 
				schema:accessibilityFeature property value 'none' is not valid in edupub. Use 'tableOfContents'
				if no other values are applicable.</report>
		</rule>
	</pattern>
	
	<pattern id="edu.teacher.edition">
		<rule context="dc:type[normalize-space(.)='teacher-edition']">
			<report test="parent::opf:metadata/not(child::dc:source)">WARNING: A teacher's edition
				should identify the corresponding student edition in a dc:source element.</report>
		</rule>
	</pattern>
	
</schema>
