<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
                        
    <ns uri="http://www.w3.org/1999/xhtml" prefix="h"/>
    <ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
    <ns uri="http://www.w3.org/1998/Math/MathML" prefix="math"/>
    <ns uri="http://www.w3.org/2001/10/synthesis" prefix="ssml"/>
    <ns uri="http://www.w3.org/2001/xml-events" prefix="ev"/>
    <ns uri="http://www.w3.org/2000/svg" prefix="svg"/>
    
    <let name="id-set" value="//*[@id]"/>
    
    <pattern id="ancestor-area-map">
        <rule context="h:area">            
            <assert test="ancestor::h:map">The <value-of select="replace('h:area','h:','')"/> element must have an ancestor <value-of select="replace('h:map','h:','')"/> element.</assert>
        </rule>
    </pattern>

    <pattern id="ancestor-imgismap-ahref">
        <rule context="h:img[@ismap]">            
            <assert test="ancestor::h:a[@href]">The <value-of select="replace('h:img[@ismap]','h:','')"/> element must have an ancestor <value-of select="replace('h:a[@href]','h:','')"/> element.</assert>
        </rule>
    </pattern>
    
    <pattern id="descendant-a-interactive">
        <rule context="h:a|h:audio[@controls]|h:button|h:details|h:embed|h:iframe|h:img[@usemap]|h:input[not(@type='hidden')]             |h:keygen|h:label|h:menu[@type='toolbar']|h:object[@usemap]|h:select|h:textarea|h:video[@controls]">
            <report test="ancestor::h:a">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:a)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-button-interactive">
        <rule context="h:a|h:audio[@controls]|h:button|h:details|h:embed|h:iframe|h:img[@usemap]|h:input[not(@type='hidden')]             |h:keygen|h:label|h:menu[@type='toolbar']|h:object[@usemap]|h:select|h:textarea|h:video[@controls]">
            <report test="ancestor::h:button">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:button)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-audio-audio">
        <rule context="h:audio">
            <report test="ancestor::h:audio">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:audio)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-audio-video">
        <rule context="h:video">
            <report test="ancestor::h:audio">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:audio)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-video-video">
        <rule context="h:video">
            <report test="ancestor::h:video">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:video)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-video-audio">
        <rule context="h:audio">
            <report test="ancestor::h:video">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:video)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-address-address">
        <rule context="h:address">
            <report test="ancestor::h:address">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:address)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-address-header">
        <rule context="h:header">
            <report test="ancestor::h:address">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:address)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-address-footer">
        <rule context="h:footer">
            <report test="ancestor::h:address">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:address)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-form-form">
        <rule context="h:form">
            <report test="ancestor::h:form">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:form)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-progress-progress">
        <rule context="h:progress">
            <report test="ancestor::h:progress">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:progress)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-meter-meter">
        <rule context="h:meter">
            <report test="ancestor::h:meter">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:meter)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-dfn-dfn">
        <rule context="h:dfn">
            <report test="ancestor::h:dfn">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:dfn)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-time-time">
        <rule context="h:time">
            <report test="ancestor::h:time">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:time)"/> elements.</report>            
        </rule>
    </pattern>

    <pattern id="descendant-caption-table">
        <rule context="h:table">
            <report test="ancestor::h:caption">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:caption)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-header-header">
        <rule context="h:header">
            <report test="ancestor::h:header">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:header)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-header-footer">
        <rule context="h:footer">
            <report test="ancestor::h:header">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:header)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-footer-footer">
        <rule context="h:footer">
            <report test="ancestor::h:footer">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:footer)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-footer-header">
        <rule context="h:header">
            <report test="ancestor::h:footer">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:footer)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-label-label">
        <rule context="h:label">
            <report test="ancestor::h:label">The <name/> element must not appear inside <value-of select="local-name(ancestor::h:label)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-annotation-xml-math">
        <rule context="math:*">
            <report test="ancestor::math:annotation-xml[@encoding='application/xhtml+xml' and @name='alternate-representation']">The <name/> element must not appear inside <value-of select="local-name(ancestor::math:annotation-xml[@encoding='application/xhtml+xml' and @name='alternate-representation'])"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="descendant-svgtitle-svg">
        <rule context="svg:*">
            <report test="ancestor::svg:title">The <name/> element must not appear inside <value-of select="local-name(ancestor::svg:title)"/> elements.</report>            
        </rule>
    </pattern>
    
    <pattern id="bdo-dir">
        <rule context="h:bdo">
            <assert test="@dir">The <name/> element must have a <value-of select="'dir'"/> attribute.</assert>            
        </rule>
    </pattern>

    <pattern id="idrefs-aria-describedby">
        <rule context="*[@aria-describedby]">            
            <assert test="every $idref in tokenize(@aria-describedby,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@aria-describedby"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>

    <pattern id="idrefs-output-for">
        <rule context="h:output[@for]">            
            <assert test="every $idref in tokenize(@for,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@for"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>

    <pattern id="idrefs-aria-flowto">
        <rule context="*[@aria-flowto]">            
            <assert test="every $idref in tokenize(@aria-flowto,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@aria-flowto"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>

    <pattern id="idrefs-aria-labelledby">
        <rule context="*[@aria-labelledby]">            
            <assert test="every $idref in tokenize(@aria-labelledby,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@aria-labelledby"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>

    <pattern id="idrefs-aria-owns">
        <rule context="*[@aria-owns]">            
            <assert test="every $idref in tokenize(@aria-owns,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@aria-owns"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>

    <pattern id="idrefs-aria-controls">
        <rule context="*[@aria-controls]">            
            <assert test="every $idref in tokenize(@aria-controls,'\s+') satisfies (some $elem in $id-set satisfies ($elem/@id eq $idref))">The <name path="@aria-controls"/> attribute must refer to elements in the same document (target ID missing)</assert>
        </rule>
    </pattern>
    
    <pattern id="idref-mathml-xref">
        <rule context="math:*[@xref]">
            <assert test="some $elem in $id-set satisfies $elem/@id eq current()/@xref">The <name path="@xref"/> attribute must refer to an element in the same document (the ID '<value-of select="current()/@xref"/>' does not exist).</assert>
        </rule>
    </pattern>

    <pattern id="idref-mathml-indenttarget">
        <rule context="math:*[@indenttarget]">
            <assert test="some $elem in $id-set satisfies $elem/@id eq current()/@indenttarget">The <name path="@indenttarget"/> attribute must refer to an element in the same document (the ID '<value-of select="current()/@indenttarget"/>' does not exist).</assert>
        </rule>
    </pattern>

    <pattern id="idref-contextmenu">
        <rule context="h:*[@contextmenu]">            
            <assert test="//h:menu[@id = current()/@contextmenu]">The <name path="@contextmenu"/> attribute does not refer to an allowed target element (expecting: <value-of select="replace('h:menu','h:','')"/>).</assert>
        </rule>
    </pattern>
    
    <pattern id="idref-input-list">
        <rule context="h:input[@list]">            
            <assert test="//h:datalist[@id = current()/@list]">The <name path="@list"/> attribute does not refer to an allowed target element (expecting: <value-of select="replace('h:datalist','h:','')"/>).</assert>
        </rule>
    </pattern>
    
    <pattern id="idref-forms-form">
        <rule context="h:*[@form]">            
            <assert test="//h:form[@id = current()/@form]">The <name path="@form"/> attribute does not refer to an allowed target element (expecting: <value-of select="replace('h:form','h:','')"/>).</assert>
        </rule>
    </pattern>

    <pattern id="idref-aria-activedescendant">
        <rule context="*[@aria-activedescendant]">
            <assert test="descendant::*[@id = current()/@aria-activedescendant]">The aria-activedescendant attribute must refer to a descendant element.</assert>
        </rule>
    </pattern>

    <pattern id="idref-label-for">        
        <rule context="h:label[@for]">
            <assert test="some $elem in $id-set satisfies $elem/@id eq current()/@for and                     (local-name($elem) eq 'button'                   or (local-name($elem) eq 'input' and not($elem/@type='hidden'))                  or local-name($elem) eq 'keygen'                   or local-name($elem) eq 'meter'                  or local-name($elem) eq 'output'                   or local-name($elem) eq 'progress'                   or local-name($elem) eq 'select'                   or local-name($elem) eq 'textarea')">The for attribute does not refer to an allowed target element (expecting: button|keygen|meter|output|progress|select|textarea|input[not(@type='hidden')]).</assert>
        </rule>
    </pattern>
            
    <pattern id="idrefs-headers">        
        <rule context="h:*[@headers]">    
            <let name="table" value="ancestor::h:table"/>
            <assert test="every $idref in tokenize(@headers, '\s+') satisfies (some $elem in $table//h:th satisfies ($elem/@id eq $idref))">The headers attribute must refer to th elements in the same table.</assert>            
        </rule>
    </pattern> 
    
    <pattern id="idref-trigger-observer">
        <rule context="epub:trigger[@ev:observer]">
            <assert test="some $elem in $id-set satisfies $elem/@id eq current()/@ev:observer">The <name path="@ev:observer"/> attribute must refer to an element in the same document (the ID '<value-of select="current()/@ev:observer"/>' does not exist).</assert>
        </rule>
    </pattern>
    
    <pattern id="idref-trigger-ref">
        <rule context="epub:trigger[@ref]">
            <assert test="some $elem in $id-set satisfies $elem/@id eq current()/@ref">The <name path="@ref"/> attribute must refer to an element in the same document (the ID '<value-of select="current()/@ref"/>' does not exist).</assert>
        </rule>
    </pattern>
    
    <pattern id="map.name">
        <rule context="h:map[@name]">   
            <let name="name-set" value="//h:map[@name]"/>
            <assert test="count($name-set[@name = current()/@name]) = 1">Duplicate map name '<value-of select="current()/@name"/>'</assert>
        </rule>
    </pattern>
    
    <pattern id="map.id">
        <rule context="h:map[@id and @name]">
            <assert test="@id = @name">The id attribute on the map element must have the same value as the name attribute.</assert>
        </rule>        
    </pattern>
    
    <pattern id="lang-xmllang">
        <rule context="h:*[@lang and @xml:lang]">
            <assert test="lower-case(@xml:lang) = lower-case(@lang)">The lang and xml:lang attributes must have the same value.</assert>                
        </rule>
    </pattern>
    
    <pattern id="id-unique">
        <rule context="*[@id]">
            <assert test="count($id-set[@id = current()/@id]) = 1">Duplicate ID '<value-of select="current()/@id"/>'</assert>
        </rule>
    </pattern>
    
    <pattern id="select-multiple">
        <rule context="h:select[not(@multiple)]">
            <report test="count(descendant::h:option[@selected]) &gt; 1">A select element whose multiple attribute is not specified must not have more than one descendant option element with its selected attribute set.</report>
        </rule>
    </pattern> 
    
    <pattern id="track">                
        <rule context="h:track">
            <report test="@label and normalize-space(@label) = ''">The track element label attribute value must not be the empty string.</report>
            <report test="@default and preceding-sibling::h:track[@default]">There must not be more than one track child of a media element element with the default attribute specified.</report>            
        </rule>
    </pattern>
    
    <pattern id="ssml-ph">        
        <rule context="*[@ssml:ph]">
            <report test="ancestor::*[@ssml:ph]">The ssml:ph attribute must not be specified on a descendant of an element that also carries this attribute.</report>
        </rule>            
    </pattern>
            
    <pattern id="style-scoped">
        <rule context="h:style[ancestor::h:body]">
            <assert test="every $elem in preceding-sibling::* satisfies local-name($elem) eq 'style'">The scoped style element must occur before any other flow content other than other style elements and inter-element whitespace.</assert>
        </rule>
    </pattern>
        
    <pattern id="link-sizes">
        <rule context="h:link[@sizes]">
            <assert test="@rel='icon'">The sizes attribute must not be specified on link elements that do not have a rel attribute that specifies the icon keyword.</assert>
        </rule>
    </pattern>   
    
    <pattern id="meta-charset">
        <rule context="h:meta[@charset]">
            <assert test="count(preceding-sibling::h:meta[@charset]) = 0">There must not be more than one meta element with a charset attribute per document.</assert>
        </rule>
    </pattern>
    
    <pattern id="article-pubdate">
        <rule context="h:article[h:time]">
            <assert test="count(./h:time[@pubdate]) &lt; 2">For each article element, there must be no more than one time element child with a pubdate attribute</assert>
        </rule>
    </pattern>
    
    <pattern id="document-pubdate">
        <rule context="h:time[not (ancestor::h:article)]">
            <assert test="count(//h:time[@pubdate and not (ancestor::h:article)]) &lt; 2">For each Document, there must be no more than one time element with a pubdate 
                attribute that does not have an ancestor article element.</assert>
        </rule>
    </pattern>

    

    

    

    

    
    
    
    
    
    
    <pattern id="svg-fo-re">    
    <rule context="svg:foreignObject[@requiredExtensions]">
        <assert test="@requiredExtensions = 'http://www.idpf.org/2007/ops'">Invalid value (expecting: 'http://www.idpf.org/2007/ops')</assert>
    </rule>
</pattern>
    
</schema>