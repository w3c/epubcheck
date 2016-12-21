<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">

	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	<ns uri="http://www.w3.org/ns/SMIL" prefix="s"/>

	<include href="./mod/id-unique.sch"/>

	<pattern id="clip-attribute-checks">
		<rule context="s:audio[@clipBegin and @clipEnd]">
			<!-- #568 check @clipBegin==@clipEnd -->
			<assert test="@clipBegin != @clipEnd">Attributes 'clipBegin' and 'clipEnd' must not be equal!</assert>
		</rule>
	</pattern>

</schema>
