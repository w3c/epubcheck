<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">

	<pattern id="id-unique">
		<let name="id-set" value="//*[@Id]"/>
		<rule context="*[@Id]">
			<assert test="count($id-set[normalize-space(@Id) = normalize-space(current()/@Id)]) = 1"
				>Duplicate "<value-of select="normalize-space(current()/@Id)"/>"</assert>
		</rule>
	</pattern>

</schema>
