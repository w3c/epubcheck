<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

    <ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
    <ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>

    <pattern id="opf.uid">
        <rule context="opf:package[@unique-identifier]">
            <let name="uid" value="./normalize-space(@unique-identifier)"/>
            <assert test="/opf:package/opf:metadata/dc:identifier[normalize-space(@id) = $uid]">package element
                unique-identifier attribute does not resolve to a dc:identifier element (given
                reference was "<value-of select="$uid"/>")</assert>
        </rule>
    </pattern>

    <pattern id="opf.dcterms.modified">
        <rule context="opf:package/opf:metadata">
            <assert test="count(opf:meta[normalize-space(@property)='dcterms:modified' and not(@refines)]) = 1"
                >package dcterms:modified meta element must occur exactly once</assert>
        </rule>
    </pattern>

    <pattern id="opf.dcterms.modified.syntax">
        <rule context="opf:meta[normalize-space(@property)='dcterms:modified'][not(ancestor::opf:collection)]">
            <assert
                test="matches(normalize-space(.), '^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})Z$')"
                >dcterms:modified illegal syntax (expecting: "CCYY-MM-DDThh:mm:ssZ")</assert>
        </rule>
    </pattern>

    <pattern id="opf.refines.relative">
        <rule context="*[@refines and starts-with(normalize-space(@refines),'#')][not(ancestor::opf:collection)]">
        	<let name="refines-target-id" value="substring(normalize-space(@refines), 2)"/>
            <assert test="//*[normalize-space(@id)=$refines-target-id]">@refines missing target id: "<value-of
                    select="$refines-target-id"/>"</assert>
        </rule>
    </pattern>

    <pattern id="opf.meta.source-of">
        <rule context="opf:meta[normalize-space(@property)='source-of']">
            <assert test="normalize-space(.) eq 'pagination'">The "source-of" property must have the
                value "pagination"</assert>
            <assert
            	test="exists(@refines) and exists(../dc:source[normalize-space(@id)=substring(normalize-space(current()/@refines),2)])"
                >The "source-of" property must refine a "dc:source" element.</assert>
        </rule>
    </pattern>

    <pattern id="opf.link.record">
        <rule context="opf:link[tokenize(@rel,'\s+')='record']">
            <assert test="exists(@media-type)">The type of "record" references must be identifiable
                from the link element’s "media-type" attribute.</assert>
            <assert test="empty(@refines)">"record" links only applies to the Publication (must not
                have a "refines" attribute).</assert>
        </rule>
    </pattern>
    
    <pattern id="opf.link.voicing">
        <rule context="opf:link[tokenize(@rel,'\s+')='voicing']">
            <assert test="starts-with(normalize-space(@media-type),'audio/')">"voicing" links must have a "media-type" attribute identifying an audio MIME type.</assert>
            <assert test="exists(@refines)">"voicing" links must have a "refines" attribute.</assert>
        </rule>
    </pattern>

    <pattern id="opf.meta.belongs-to-collection">
    	<rule context="opf:meta[normalize-space(@property)='belongs-to-collection']">
            <assert
            	test="empty(@refines) or exists(../opf:meta[normalize-space(@id)=substring(normalize-space(current()/@refines),2)][normalize-space(@property)='belongs-to-collection'])"
                >Property "belongs-to-collection" can only refine other "belongs-to-collection"
                properties.</assert>
        </rule>
    </pattern>

    <pattern id="opf.meta.collection-type">
        <rule context="opf:meta[normalize-space(@property)='collection-type']">
            <assert
            	test="exists(../opf:meta[normalize-space(@id)=substring(normalize-space(current()/@refines),2)][normalize-space(@property)='belongs-to-collection'])"
                >Property "collection-type" must refine a "belongs-to-collection" property.</assert>
        </rule>
    </pattern>


    <pattern id="opf.itemref">
        <rule context="opf:spine/opf:itemref[@idref]">
            <let name="ref" value="./normalize-space(@idref)"/>
            <let name="item" value="//opf:manifest/opf:item[normalize-space(@id) = $ref]"/>
            <assert test="$item">itemref element idref attribute does not resolve to a manifest item
                element</assert>
        </rule>
    </pattern>

    <pattern id="opf.fallback.ref">
        <rule context="opf:item[@fallback]">
            <let name="ref" value="./normalize-space(@fallback)"/>
            <let name="item" value="/opf:package/opf:manifest/opf:item[normalize-space(@id) = $ref]"/>
        	<assert test="$item and normalize-space($item/@id) != normalize-space(./@id)">manifest item element fallback attribute
                must resolve to another manifest item (given reference was "<value-of select="$ref"
                />")</assert>
        </rule>
    </pattern>

    <pattern id="opf.media.overlay">
        <rule context="opf:item[@media-overlay]">
            <let name="ref" value="./normalize-space(@media-overlay)"/>
            <let name="item" value="//opf:manifest/opf:item[normalize-space(@id) = $ref]"/>
        	<let name="item-media-type" value="normalize-space($item/@media-type)"/>
        	<let name="media-type" value="normalize-space(@media-type)"/>
            <assert test="$item-media-type = 'application/smil+xml'">media overlay items must be of
                the "application/smil+xml" type (given type was "<value-of select="$item-media-type"
                />")</assert>
        	<assert test="$media-type='application/xhtml+xml' or $media-type='image/svg+xml'"
        		>The media-overlay attribute is only allowed on XHTML and SVG content documents.</assert>
        </rule>
    </pattern>

    <pattern id="opf.media.overlay.metadata.global">
        <rule context="opf:manifest[opf:item[@media-overlay]]">
        	<assert test="//opf:meta[normalize-space(@property)='media:duration' and not (@refines)]">global
                media:duration meta element not set</assert>
        </rule>
    </pattern>

    <pattern id="opf.media.overlay.metadata.item">
        <rule context="opf:manifest/opf:item[@media-overlay]">
            <let name="mo-idref" value="normalize-space(@media-overlay)"/>
            <let name="mo-item" value="//opf:item[normalize-space(@id) = $mo-idref]"/>
            <let name="mo-item-id" value="$mo-item/normalize-space(@id)"/>
            <let name="mo-item-uri" value="concat('#', $mo-item-id)"/>
        	<assert test="//opf:meta[normalize-space(@property)='media:duration' and normalize-space(@refines) = $mo-item-uri ]">item
                media:duration meta element not set (expecting: meta property='media:duration'
                    refines='<value-of select="$mo-item-uri"/>')</assert>
        </rule>
    </pattern>

    <pattern id="opf.bindings.handler">
        <rule context="opf:bindings/opf:mediaType">
            <let name="ref" value="./normalize-space(@handler)"/>
            <let name="item" value="//opf:manifest/opf:item[normalize-space(@id) = $ref]"/>
        	<let name="item-media-type" value="normalize-space($item/@media-type)"/>
            <assert test="$item-media-type = 'application/xhtml+xml'">manifest items referenced from
                the handler attribute of a bindings mediaType element must be of the
                "application/xhtml+xml" type (given type was "<value-of select="$item-media-type"
                />")</assert>
        </rule>
    </pattern>

    <pattern id="opf.toc.ncx">
        <rule context="opf:spine[@toc]">
            <let name="ref" value="./normalize-space(@toc)"/>
            <let name="item" value="/opf:package/opf:manifest/opf:item[normalize-space(@id) = $ref]"/>
        	<let name="item-media-type" value="normalize-space($item/@media-type)"/>
            <assert test="$item-media-type = 'application/x-dtbncx+xml'">spine element toc attribute
                must reference the NCX manifest item (referenced media type was "<value-of
                    select="$item-media-type"/>")</assert>
        </rule>
    </pattern>

    <pattern id="opf.toc.ncx.2">
    	<rule context="opf:item[normalize-space(@media-type)='application/x-dtbncx+xml']">
            <assert test="//opf:spine[@toc]">spine element toc attribute must be set when an NCX is
                included in the publication</assert>
        </rule>
    </pattern>

    <pattern id="opf.nav.prop">
        <rule context="opf:manifest">
            <let name="item"
                value="//opf:manifest/opf:item[@properties and tokenize(@properties,'\s+') = 'nav']"/>
            <assert test="count($item) = 1">Exactly one manifest item must declare the "nav"
                property (number of "nav" items: <value-of select="count($item)"/>).</assert>
        </rule>
    </pattern>

    <pattern id="opf.nav.type">
        <rule
            context="opf:manifest/opf:item[@properties and tokenize(@properties,'\s+') = 'nav']">
            <assert test="@media-type = 'application/xhtml+xml'">The manifest item representing the
                Navigation Document must be of the "application/xhtml+xml" type (given type was
                    "<value-of select="@media-type"/>")</assert>
        </rule>
    </pattern>
    
    <pattern id="opf.datanav.prop">
        <rule context="opf:manifest">
            <let name="item" value="opf:item[tokenize(@properties, '\s+') = 'data-nav']"/>
            <assert test="count($item) le 1">Found <value-of select="count($item)"/> "data-nav" items. The manifest must not include more than one Data Navigation Document.</assert>
        </rule>
    </pattern>
    
    <pattern id="opf.cover-image">
        <rule context="opf:manifest">
            <let name="item"
                value="//opf:manifest/opf:item[@properties and tokenize(@properties,'\s+')='cover-image']"/>
            <assert test="count($item) &lt; 2">Multiple occurrences of the "cover-image" property
                (number of "cover-image" items: <value-of select="count($item)"/>).</assert>
        </rule>
    </pattern>

    <pattern id="opf.rendition.globals">
        <rule context="opf:package/opf:metadata">
        	<assert test="count(opf:meta[normalize-space(@property)='rendition:flow']) le 1">The "rendition:flow"
                property must not occur more than one time in the package metadata.</assert>
        	<assert test="count(opf:meta[normalize-space(@property)='rendition:layout']) le 1">The "rendition:layout"
                property must not occur more than one time in the package metadata.</assert>
        	<assert test="count(opf:meta[normalize-space(@property)='rendition:orientation']) le 1">The
                "rendition:orientation" property must not occur more than one time in the package
                metadata.</assert>
        	<assert test="count(opf:meta[normalize-space(@property)='rendition:spread']) le 1">The "rendition:spread"
                property must not occur more than one time in the package metadata.</assert>
        	<assert test="count(opf:meta[normalize-space(@property)='rendition:viewport'][empty(@refines)]) le 1">The
                "rendition:viewport" property must not occur more than one time as a global value in
                the package metadata.</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)='rendition:flow']">
            <assert test="empty(@refines)">The "rendition:flow" property must not be set on elements
                with a "refines" attribute</assert>
            <assert
                test="normalize-space()=('paginated','scrolled-continuous','scrolled-doc','auto')"
                >The value of the "rendition:flow" property must be either "paginated",
                "scrolled-continuous", "scrolled-doc", or "auto"</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)=('rendition:layout')]">
            <assert test="empty(@refines)">The "rendition:layout" property must not be set on
                elements with a "refines" attribute</assert>
            <assert test="normalize-space()=('reflowable','pre-paginated')">The value of the
                "rendition:layout" property must be either "reflowable" or "pre-paginated"</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)='rendition:orientation']">
            <assert test="empty(@refines)">The "rendition:orientation" property must not be set on
                elements with a "refines" attribute</assert>
            <assert test="normalize-space()=('landscape','portrait','auto')">The value of the
                "rendition:orientation" property must be either "landscape", "portrait" or
                "auto"</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)='rendition:spread']">
            <assert test="empty(@refines)">The "rendition:spread" property must not be set on
                elements with a "refines" attribute</assert>
            <assert test="normalize-space()=('none','landscape','portrait','both','auto')">The value
                of the "rendition:spread" property must be either "none", "landscape", "portrait",
                "both" or "auto"</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)='rendition:spread']">
            <assert test="empty(@refines)">The "rendition:spread" property must not be set on
                elements with a "refines" attribute</assert>
            <assert test="normalize-space()=('none','landscape','portrait','both','auto')">The value
                of the "rendition:spread" property must be either "none", "landscape", "portrait",
                "both" or "auto"</assert>
        </rule>
    	<rule context="opf:meta[not(ancestor::opf:collection)][normalize-space(@property)='rendition:viewport']">
            <assert
                test="matches(normalize-space(),'^((width=\d+,\s*height=\d+)|(height=\d+,\s*width=\d+))$')"
                >The value of the "rendition:viewport" property must be of the form "width=x,
                height=y"</assert>
        </rule>
    </pattern>

    <pattern id="opf.rendition.overrides">
        <rule context="opf:itemref">
            <assert
                test="count(tokenize(@properties,'\s+')[.=('rendition:flow-paginated','rendition:flow-scrolled-continuous','rendition:flow-scrolled-doc','rendition:flow-auto')]) le 1"
                >Properties "rendition:flow-paginated", "rendition:flow-scrolled-continuous",
                "rendition:flow-scrolled-doc" and "rendition:flow-auto" are mutually
                exclusive</assert>
            <assert
                test="count(tokenize(@properties,'\s+')[.=('rendition:layout-reflowable','rendition:layout-pre-paginated')]) le 1"
                >Properties "rendition:layout-reflowable" and "rendition:layout-pre-paginated" are
                mutually exclusive</assert>
            <assert
                test="count(tokenize(@properties,'\s+')[.=('rendition:orientation-landscape','rendition:orientation-portrait','rendition:orientation-auto')]) le 1"
                >Properties "rendition:orientation-landscape", "rendition:orientation-portrait" and
                "rendition:orientation-auto" are mutually exclusive</assert>
            <assert
                test="count(tokenize(@properties,'\s+')[.=('page-spread-right','page-spread-left','rendition:page-spread-center')]) le 1"
                >Properties "page-spread-right", "page-spread-left" and
                "rendition:page-spread-center" are mutually exclusive</assert>
            <assert
                test="count(tokenize(@properties,'\s+')[.=('rendition:spread-portrait','rendition:spread-landscape','rendition:spread-both','rendition:spread-none','rendition:spread-auto')]) le 1"
                >Properties "rendition:spread-portrait", "rendition:spread-landscape",
                "rendition:spread-both", "rendition:spread-none" and "rendition:spread-auto" are
                mutually exclusive</assert>
        </rule>
    </pattern>

    <pattern id="opf.collection.refines-restriction">
        <rule context="opf:collection/opf:metadata/*[@refines]">
        	<let name="refines-target-id" value="substring(normalize-space(@refines), 2)"/>
            <assert
            	test="starts-with(normalize-space(@refines),'#') and ancestor::opf:collection[not(ancestor::opf:collection)]//*[normalize-space(@id)=$refines-target-id]"
                > @refines must point to an element within the current collection </assert>
        </rule>
    </pattern>
    
    <pattern id="opf_guideReferenceUnique">
        <!-- guide/reference element should be unique (#493) -->
        <rule context="opf:reference">
            <let name="current_type_normalized" value="normalize-space(lower-case(@type))"/>
            <let name="current_href_normalized" value="normalize-space(lower-case(@href))"/>
            <assert test="
                count(//opf:reference[
                    normalize-space(lower-case(@type)) = $current_type_normalized and
                    normalize-space(lower-case(@href)) = $current_href_normalized
                ]) le 1">WARNING: Duplicate "reference" elements with the same "type" and "href" attributes</assert>
        </rule>
    </pattern>
    
    <include href="./mod/id-unique.sch"/>
	
	<!-- EPUB 3.2 New Checks -->
	
	<pattern id="opf.spine.duplicate.refs">
		<rule context="opf:itemref">
			<report test="normalize-space(./@idref) = preceding-sibling::opf:itemref/normalize-space(@idref)">Itemref refers to the same manifest entry as a previous itemref</report>
		</rule>
	</pattern>
	
	<pattern id="opf.subject.authority-term">
		<rule context="opf:metadata/dc:subject">
			<let name="id" value="normalize-space(./@id)"/>
			<let name="authority" value="//opf:meta[normalize-space(@property)='authority'][substring(normalize-space(@refines), 2) = $id]"/>
			<let name="term" value="//opf:meta[normalize-space(@property)='term'][substring(normalize-space(@refines), 2) = $id]"/>
			<report test="(count($authority) = 1 and count($term) = 0)">A term property must be associated with a dc:subject when an authority is specified</report>
			<report test="(count($authority) = 0 and count($term) = 1)">An authority property must be associated with a dc:subject when a term is specified</report>
			<report test="(count($authority) &gt; 1 or count($term) &gt; 1)">Only one pair of authority and term properties can be associated with a dc:subject</report>
		</rule>
	</pattern>
	
	<!-- EPUB 3.2 Deprecated Features -->
	
	<pattern id="opf.bindings.deprecated">
		<rule context="opf:package/opf:bindings">
			<report test=".">WARNING: Use of the bindings element is deprecated</report>
		</rule>
	</pattern>
	
	<pattern id="opf.meta.viewport.deprecated">
		<rule context="opf:metadata/opf:meta[normalize-space(@property)='rendition:viewport']">
			<report test=".">WARNING: Use of the rendition:viewport property is deprecated</report>
		</rule>
	</pattern>
	
	<pattern id="opf.meta.spread.portrait.deprecated">
		<rule context="opf:metadata/opf:meta[normalize-space(@property)='rendition:spread']">
			<report test=". = 'portrait'">WARNING: Use of the rendition:spread value "portrait" is deprecated in favour of the value "both"</report>
		</rule>
	</pattern>
	
	<pattern id="opf.itemref.spread.portrait.deprecated">
		<rule context="opf:spine/opf:itemref[@properties]">
			<report test="tokenize(@properties,'\s+')='rendition:spread-portrait'">WARNING: Use of the "rendition:spread-portrait" spine override is deprecated in favour of "rendition:spread-both"</report>
		</rule>
	</pattern>
	
	<pattern id="opf.meta.meta-auth.deprecated">
		<rule context="opf:metadata/opf:meta[normalize-space(@property)='meta-auth']">
			<report test=".">WARNING: Use of the meta-auth property is deprecated</report>
		</rule>
	</pattern>
	
</schema>