<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

	<ns uri="http://www.idpf.org/2013/metadata" prefix="ocf"/>
	<ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

	<pattern id="ocf.uid">
		<rule context="ocf:metadata[@unique-identifier]">
			<let name="uid" value="./@unique-identifier"/>
			<assert test="dc:identifier[@id = $uid]"> unique-identifier attribute does not resolve to a dc:identifier element (given reference was '<value-of select="$uid"/>')</assert>
		</rule>
	</pattern>

	<pattern id="ocf.dcterms.modified">
		<rule context="ocf:metadata">
			<assert test="count(ocf:meta[@property='dcterms:modified' and not(@refines)]) = 1">dcterms:modified meta element must occur exactly once</assert>
		</rule>
	</pattern>

	<pattern id="ocf.dcterms.modified.syntax">
		<rule context="ocf:meta[@property='dcterms:modified']">
			<assert test="matches(normalize-space(.), '^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})Z$')">dcterms:modified illegal syntax (expecting: 'CCYY-MM-DDThh:mm:ssZ')</assert>
		</rule>
	</pattern>

	<pattern id="ocf.refines.relative">
		<rule context="*[@refines and starts-with(@refines,'#')]">
			<let name="refines-target-id" value="substring(@refines, 2)"/>
			<assert test="//*[@id=$refines-target-id]">@refines missing target id: '<value-of select="$refines-target-id"/>'</assert>
		</rule>
	</pattern>

	<pattern id="ocf.meta.source-of">
		<rule context="ocf:meta[@property='source-of']">
			<assert test="normalize-space(.) eq 'pagination'">The 'source-of' property must have the value 'pagination'</assert>
			<assert test="exists(@refines) and exists(../dc:source[@id=substring(current()/@refines,2)])">The 'source-of' property must refine a 'dc:source' element.</assert>
		</rule>
	</pattern>

	<pattern id="ocf.link.record">
		<rule context="ocf:link[tokenize(@rel,'\s+')='record']">
			<assert test="exists(@media-type)">The type of 'record' references must be identifiable from the link element's 'media-type' attribute.</assert>
			<assert test="empty(@refines)">'record' links only applies to the Publication (must not have a 'refines' attribute).</assert>
		</rule>
	</pattern>

	<pattern id="ocf.meta.belongs-to-collection">
		<rule context="ocf:meta[@property='belongs-to-collection']">
			<assert test="empty(@refines) or exists(../ocf:meta[@id=substring(current()/@refines,2)][@property='belongs-to-collection'])">Property 'belongs-to-collection' can only refine other 'belongs-to-collection' properties.</assert>
		</rule>
	</pattern>

	<pattern id="ocf.meta.collection-type">
		<rule context="ocf:meta[@property='collection-type']">
			<assert test="exists(../ocf:meta[@id=substring(current()/@refines,2)][@property='belongs-to-collection'])">Property 'collection-type' must refine a 'belongs-to-collection' property.</assert>
		</rule>
	</pattern>


	<pattern id="ocf.rendition.globals">
		<rule context="ocf:metadata">
			<assert test="count(ocf:meta[@property='rendition:flow']) le 1">The 'rendition:flow' property must not occur more than one time in the metadata.</assert>
			<assert test="count(ocf:meta[@property='rendition:layout']) le 1">The 'rendition:layout' property must not occur more than one time in the metadata.</assert>
			<assert test="count(ocf:meta[@property='rendition:orientation']) le 1">The 'rendition:orientation' property must not occur more than one time in the metadata.</assert>
			<assert test="count(ocf:meta[@property='rendition:spread']) le 1">The 'rendition:spread' property must not occur more than one time in the metadata.</assert>
			<assert test="count(ocf:meta[@property='rendition:viewport'][empty(@refines)]) le 1">The 'rendition:viewport' property must not occur more than one time as a global value in the metadata.</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:flow')]">
			<assert test="empty(@refines)">The 'rendition:flow' property must not be set on elements with a 'refines' attribute</assert>
			<assert test="normalize-space()=('paginated','scrolled-continuous','scrolled-doc','auto')">The value of the 'rendition:flow' property must be either 'paginated', 'scrolled-continuous', 'scrolled-doc', or 'auto'</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:layout')]">
			<assert test="empty(@refines)">The 'rendition:layout' property must not be set on elements with a 'refines' attribute</assert>
			<assert test="normalize-space()=('reflowable','pre-paginated')">The value of the 'rendition:layout' property must be either 'reflowable' or 'pre-paginated'</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:orientation')]">
			<assert test="empty(@refines)">The 'rendition:orientation' property must not be set on elements with a 'refines' attribute</assert>
			<assert test="normalize-space()=('landscape','portrait','auto')">The value of the 'rendition:orientation' property must be either 'landscape', 'portrait' or 'auto'</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:spread')]">
			<assert test="empty(@refines)">The 'rendition:spread' property must not be set on elements with a 'refines' attribute</assert>
			<assert test="normalize-space()=('none','landscape','portrait','both','auto')">The value of the 'rendition:spread' property must be either 'none', 'landscape', 'portrait', 'both' or 'auto'</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:spread')]">
			<assert test="empty(@refines)">The 'rendition:spread' property must not be set on elements with a 'refines' attribute</assert>
			<assert test="normalize-space()=('none','landscape','portrait','both','auto')">The value of the 'rendition:spread' property must be either 'none', 'landscape', 'portrait', 'both' or 'auto'</assert>
		</rule>
		<rule context="ocf:meta[@property=('rendition:viewport')]">
			<assert test="matches(normalize-space(),'^((width=\d+,\s*height=\d+)|(height=\d+,\s*width=\d+))$')">The value of the 'rendition:viewport' property must be of the form 'width=x, height=y'</assert>
		</rule>
	</pattern>

	<include href="./mod/id-unique.sch"/>

</schema>
