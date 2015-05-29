<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

	<ns uri="http://www.idpf.org/2013/metadata" prefix="ocf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="dc.type">
		<rule context="ocf:metadata">
			<assert test="dc:type[normalize-space(.)='edupub']">A dc:type element with the value 'edupub' is required.</assert>
		</rule>
	</pattern>

</schema>
