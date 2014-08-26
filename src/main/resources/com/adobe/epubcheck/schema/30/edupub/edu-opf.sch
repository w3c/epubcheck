<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	
	<pattern id="edu.a11y">
		<rule context="opf:metadata">
			<assert test="child::opf:meta[@property='schema:accessibilityFeature']">At least one
				schema:accessibilityFeature declaration is required.</assert>
		</rule>
	</pattern>

</schema>
