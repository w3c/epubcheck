<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    
    <ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
    <ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
            
    <pattern id="opf.uid">
        <rule context="opf:package[@unique-identifier]">
            <let name="uid" value="./@unique-identifier" />
            <assert test="/opf:package/opf:metadata/dc:identifier[@id = $uid]"
                >package element unique-identifier attribute does not resolve to a dc:identifier element (given reference was '<value-of select="$uid"/>')</assert>
        </rule>    
    </pattern>
    
    <pattern id="opf.dcterms.modified">
        <rule context="opf:metadata">            
            <assert test="count(opf:meta[@property='dcterms:modified' and not(@refines)]) = 1"
                >package dcterms:modified meta element must occur exactly once</assert>            
        </rule>        
    </pattern>
    
    <pattern id="opf.dcterms.modified.syntax">
        <rule context="opf:meta[@property='dcterms:modified']">            
            <assert test="matches(normalize-space(.), '^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})Z$')"
                >dcterms:modified illegal syntax (expecting: 'CCYY-MM-DDThh:mm:ssZ')</assert>            
        </rule>
    </pattern>    
    
    <pattern id="opf.refines.relative">
        <rule context="*[@refines and starts-with(@refines,'#')]">
            <let name="refines-target-id" value="substring(@refines, 2)" />
            <assert test="//*[@id=$refines-target-id]"
                >@refines missing target id: '<value-of select="$refines-target-id"/>'</assert>
        </rule>
    </pattern>
    
    <pattern id="opf.itemref">        
        <rule context="opf:spine/opf:itemref[@idref]">    
            <let name="ref" value="./@idref" />            
            <let name="item" value="//opf:manifest/opf:item[@id = $ref]"/>
            <let name="item-media-type" value="$item/@media-type" />                                   
            <assert test="$item"
                >itemref element idref attribute does not resolve to a manifest item element</assert>
        </rule>    
    </pattern>
    
    <pattern id="opf.fallback.ref"> 
        <rule context="opf:item[@fallback]">
            <let name="ref" value="./@fallback" />
            <let name="item" value="/opf:package/opf:manifest/opf:item[@id = $ref]"/>
            <assert test="$item and $item/@id != ./@id"
                >manifest item element fallback attribute must resolve to another manifest item (given reference was '<value-of select="$ref"/>')</assert>
        </rule>            
    </pattern>
        
    <pattern id="opf.media.overlay"> 
        <rule context="opf:item[@media-overlay]">
            <let name="ref" value="./@media-overlay" />
            <let name="item" value="//opf:manifest/opf:item[@id = $ref]" />
            <let name="item-media-type" value="$item/@media-type" />
            <assert test="$item-media-type = 'application/smil+xml'"
                >media overlay items must be of the 'application/smil+xml' type (given type was '<value-of select="$item-media-type"/>')</assert>
        </rule>            
    </pattern>
    
    <pattern id="opf.media.overlay.metadata.global"> 
        <rule context="opf:manifest[opf:item[@media-overlay]]">
            <assert test="//opf:meta[@property='media:duration' and not (@refines)]"
                >global media:duration meta element not set</assert>
        </rule>        
    </pattern>
    
    <pattern id="opf.media.overlay.metadata.item">
        <rule context="opf:manifest/opf:item[@media-overlay]">
            <let name="mo-idref" value="@media-overlay"/>
            <let name="mo-item" value="//opf:item[@id = $mo-idref]"/>
            <let name="mo-item-id" value="$mo-item/@id"/>
            <let name="mo-item-uri" value="concat('#', $mo-item-id)"/>
            <assert test="//opf:meta[@property='media:duration' and @refines = $mo-item-uri ]"
                >item media:duration meta element not set (expecting: meta property='media:duration' refines='<value-of select="$mo-item-uri"/>')</assert>
        </rule>
    </pattern>
          
    <pattern id="opf.bindings.handler"> 
        <rule context="opf:bindings/opf:mediaType">
            <let name="ref" value="./@handler" />
            <let name="item" value="//opf:manifest/opf:item[@id = $ref]" />
            <let name="item-media-type" value="$item/@media-type" />
            <assert test="$item-media-type = 'application/xhtml+xml'"
                >manifest items referenced from the handler attribute of a bindings mediaType element must be of the 'application/xhtml+xml' type (given type was '<value-of select="$item-media-type"/>')</assert>
        </rule>            
    </pattern>
          
    <pattern id="opf.toc.ncx"> 
        <rule context="opf:spine[@toc]">
            <let name="ref" value="./@toc" />            
            <let name="item" value="/opf:package/opf:manifest/opf:item[@id = $ref]"/>
            <let name="item-media-type" value="$item/@media-type" />
            <assert test="$item-media-type = 'application/x-dtbncx+xml'"
                >spine element toc attribute must reference the NCX manifest item (referenced media type was '<value-of select="$item-media-type"/>')</assert>
        </rule>            
    </pattern>    
    
    <pattern id="opf.toc.ncx.2">     
        <rule context="opf:item[@media-type='application/x-dtbncx+xml']">
            <assert test="//opf:spine[@toc]"
                >spine element toc attribute must be set when an NCX is included in the publication</assert>
        </rule>    
    </pattern>    
        
    <pattern id="opf.nav.prop"> 
        <rule context="opf:manifest">            
            <let name="item" value="//opf:manifest/opf:item[@properties and (some $token in tokenize(@properties,' ') satisfies (normalize-space($token) eq 'nav'))]" />            
            <assert test="count($item) = 1"
                >Exactly one manifest item must declare the 'nav' property (number of 'nav' items: <value-of select="count($item)"/>).</assert>                            
        </rule> 
    </pattern>
    
    <pattern id="opf.nav.type"> 
        <rule context="opf:manifest/opf:item[@properties and (some $token in tokenize(@properties,' ') satisfies (normalize-space($token) eq 'nav'))]">
            <assert test="@media-type = 'application/xhtml+xml'"
                >The manifest item representing the Navigation Document must be of the 'application/xhtml+xml' type (given type was '<value-of select="@media-type"/>')</assert>
        </rule>    
    </pattern>
    
    <pattern id="opf.cover-image"> 
        <rule context="opf:manifest">            
            <let name="item" value="//opf:manifest/opf:item[@properties and (some $token in tokenize(@properties,' ') satisfies (normalize-space($token) eq 'cover-image'))]" />            
            <assert test="count($item) &lt; 2"
                >Multiple occurrences of the 'cover-image' property (number of 'cover-image' items: <value-of select="count($item)"/>).</assert>                            
        </rule> 
    </pattern>
    
    <include href="./mod/id-unique.sch"/>     
         
             
</schema>