<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
	
	<let name="audienceType" value="opf:package/opf:metadata/opf:meta[normalize-space(@property)='schema:audienceType'][lower-case(normalize-space(.))='schools']"/>
	
	<pattern id="edu.dctype">
		<rule context="opf:metadata">
			<assert test="child::dc:type[normalize-space(.)='edupub']">The dc:type identifier "edupub" is required.</assert>
		</rule>
	</pattern>
	
	<pattern id="edu.a11y">
		<rule context="opf:metadata">
			<assert test="child::opf:meta[normalize-space(@property)='schema:accessibilityFeature']">At least one
				schema:accessibilityFeature declaration is required.</assert>
			<report test="child::opf:meta[normalize-space(@property)='schema:accessibilityFeature'][normalize-space(.)='none']">The 
				schema:accessibilityFeature property value "none" is not valid in edupub. Use "tableOfContents"
				if no other values are applicable.</report>
		</rule>
	</pattern>
	
	<pattern id="edu.teacher.edition">
		<rule context="dc:type[normalize-space(.)='teacher-edition']">
			<report test="parent::opf:metadata/not(child::dc:source)">WARNING: A teacher’s edition
				should identify the corresponding student edition in a dc:source element.</report>
		</rule>
	</pattern>
	
	<pattern id="edu.schools">
		<rule context="opf:metadata[$audienceType='schools']">
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalAlignment'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalAlignment property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalRole'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalRole property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalUse'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalUse property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:interactivityType'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:interactivityType property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:isBasedOnUrl'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:isBasedOnUrlUse property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:learningResourceType'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:learningResourceType property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:typicalAgeRange'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:typicalAgeRange property is recommended.</report>
		</rule>
	</pattern>
	
	<pattern id="edu.highered">
		<rule context="opf:metadata[$audienceType='higher-ed']">
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalAlignment'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalAlignment property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalRole'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalRole property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalUse'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalUse property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:interactivityType'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:interactivityType property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:isBasedOnUrl'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:isBasedOnUrlUse property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:learningResourceType'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:learningResourceType property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:typicalAgeRange'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:typicalAgeRange property is recommended.</report>
		</rule>
	</pattern>
	
	<pattern id="edu.pro-corp">
		<rule context="opf:metadata[$audienceType='professional' or $audienceType='corporate']">
			<report test="not(child::opf:meta[normalize-space(@property)='schema:educationalRole'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:educationalRole property is strongly recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:interactivityType'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:interactivityType property is recommended.</report>
			<report test="not(child::opf:meta[normalize-space(@property)='schema:isBasedOnUrl'])">WARNING: When targeting <value-of select="$audienceType"/>, including a schema:isBasedOnUrlUse property is recommended.</report>
		</rule>
	</pattern>
	
</schema>
