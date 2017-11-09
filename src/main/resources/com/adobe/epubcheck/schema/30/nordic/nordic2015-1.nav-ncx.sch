<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

    <title>Nordic EPUB3 Navigation Document and NCX rules</title>

    <ns prefix="html" uri="http://www.w3.org/1999/xhtml"/>
    <ns prefix="epub" uri="http://www.idpf.org/2007/ops"/>
    <ns prefix="nordic" uri="http://www.mtm.se/epub/"/>
    <ns prefix="ncx" uri="http://www.daisy.org/z3986/2005/ncx/"/>

    <!-- navdoc toc items must exist in the ncx -->
    <pattern>
        <rule context="html:a[ancestor::html:nav/tokenize(@epub:type,'\s+')='toc']">
            <let name="navPoint" value="/*/ncx:*[1]/ncx:navMap//ncx:navPoint[ncx:content/@src=current()/@href]"/>
            <report test="count($navPoint) = 0">[nordic_nav_ncx_1_a] toc items in the navigation document must also occur in the NCX. <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
        </rule>
    </pattern>

    <!-- navdoc page-list items must exist in the ncx -->
    <pattern>
        <rule context="html:a[ancestor::html:nav/tokenize(@epub:type,'\s+')='page-list']">
            <let name="pageTarget" value="/*/ncx:*[1]/ncx:pageList/ncx:pageTarget[ncx:content/@src=current()/@href]"/>
            <report test="count($pageTarget) = 0">[nordic_nav_ncx_2_a] page references in the navigation document must also occur as pageTarget items in the NCX. <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
        </rule>
    </pattern>

    <!-- ncx toc items must exist in the navdoc, and must follow the same structure and order as in the navdoc -->
    <pattern>
        <rule context="ncx:navPoint">
            <let name="navRef" value="/*/html:*[1]/html:body/html:nav[tokenize(@epub:type,'\s+')='toc']//html:a[@href=current()/ncx:content/@src]"/>
            <report test="count($navRef) = 0">[nordic_nav_ncx_3_a] toc items in the NCX must also occur in the navigation document.<value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
            <assert test="count($navRef) != 1 or count(ancestor-or-self::ncx:navPoint) = count($navRef/ancestor::html:li)">
                <![CDATA[[nordic_nav_ncx_3_b] The navPoint in the NCX (]]>
                <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count(ancestor::ncx:navPoint)"/>
                <![CDATA[ ancestors ]]>
                <value-of
                    select="if (ancestor::ncx:navPoint) then concat('(&quot;',string-join(ancestor::ncx:navPoint/ncx:navLabel/normalize-space(string-join(.//text(),'')),'&quot;, &quot;'),'&quot;)') else ''"/>
                <![CDATA[, while the corresponding item in the navigation document (]]>
                <value-of select="$navRef/concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count($navRef/ancestor::html:li)-1"/>
                <![CDATA[ ancestors ]]>
                <value-of
                    select="if ($navRef/ancestor::html:li[1]/ancestor::html:li) then $navRef/concat('(&quot;',string-join(ancestor::html:li[1]/ancestor::html:li/normalize-space(string-join((* except (html:ol | html:ul))//text(),'')),'&quot;, &quot;'),'&quot;)') else ''"/>
                <![CDATA[. The item in the navigation document and the navPoint in the NCX must have the same number of ancestors. Maybe the item are placed on the wrong nesting level or under the wrong headline.]]>
            </assert>
            <assert test="count($navRef) != 1 or count(preceding-sibling::ncx:navPoint) = count($navRef/ancestor::html:li[1]/preceding-sibling::html:li)">
                <![CDATA[[nordic_nav_ncx_3_c] The navPoint in the NCX (]]>
                <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count(preceding-sibling::ncx:navPoint)"/>
                <![CDATA[ preceding siblings ]]>
                <value-of
                    select="if (preceding-sibling::ncx:navPoint) then concat('(&quot;',string-join((preceding-sibling::ncx:navPoint)[position() &lt;= 3]/ncx:navLabel/normalize-space(string-join(.//text(),'')),'&quot;, &quot;'),'&quot;') else ''"/>
                <value-of select="if (count(preceding-sibling::ncx:navPoint) &gt; 3) then concat(' and ',count(preceding-sibling::ncx:navPoint)-3,' more') else ''"/>
                <value-of select="if (preceding-sibling::ncx:navPoint) then ')' else ''"/>
                <![CDATA[, while the corresponding item in the navigation document (]]>
                <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count($navRef/ancestor::html:li[1]/preceding-sibling::html:li)"/>
                <![CDATA[ preceding siblings ]]>
                <value-of
                    select="if ($navRef/ancestor::html:li[1]/preceding-sibling::html:li) then $navRef/concat('(&quot;',string-join((ancestor::html:li[1]/preceding-sibling::html:li)[position() &lt;= 3]/normalize-space(string-join((* except (html:ol | html:ul))//text(),'')),'&quot;, &quot;'),'&quot;') else ''"/>
                <value-of
                    select="if (count($navRef/ancestor::html:li[1]/preceding-sibling::html:li) &gt; 3) then $navRef/concat(' and ',count(ancestor::html:li[1]/preceding-sibling::html:li)-3,' more') else ''"/>
                <value-of select="if ($navRef/ancestor::html:li[1]/preceding-sibling::html:li) then ')' else ''"/>
                <![CDATA[. The item in the navigation document and the navPoint in the NCX must have the same number of preceding siblings. Maybe the items are not placed in the correct order or maybe some of the preceding items are missing.]]>
            </assert>
        </rule>
    </pattern>

    <!-- ncx page-list items must exist in the navdoc, and must follow the same order as in the navdoc -->
    <pattern>
        <rule context="ncx:pageTarget">
            <let name="navRef" value="/*/html:*[1]/html:body/html:nav[tokenize(@epub:type,'\s+')='page-list']//html:a[@href=current()/ncx:content/@src]"/>
            <report test="count($navRef) = 0">[nordic_nav_ncx_4_a] pageTarget items in the NCX must also occur as page references in the navigation document. <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
            <assert test="count($navRef) != 1 or count(preceding-sibling::ncx:pageTarget) = count($navRef/ancestor::html:li[1]/preceding-sibling::html:li)">
                <![CDATA[[nordic_nav_ncx_4_b] The pageTarget in the NCX (]]>
                <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count(preceding-sibling::ncx:pageTarget)"/>
                <![CDATA[ preceding siblings ]]>
                <value-of
                    select="if (preceding-sibling::ncx:pageTarget) then concat('(&quot;',string-join((preceding-sibling::ncx:pageTarget)[position() &lt;= 3]/ncx:navLabel/normalize-space(string-join(.//text(),'')),'&quot;, &quot;'),'&quot;') else ''"/>
                <value-of select="if (count(preceding-sibling::ncx:pageTarget) &gt; 3) then concat(' and ',count(preceding-sibling::ncx:pageTarget)-3,' more') else ''"/>
                <value-of select="if (preceding-sibling::ncx:pageTarget) then ')' else ''"/>
                <![CDATA[, while the page reference in the navigation document (]]>
                <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
                <![CDATA[) has ]]>
                <value-of select="count($navRef/ancestor::html:li[1]/preceding-sibling::html:li)"/>
                <![CDATA[ preceding siblings ]]>
                <value-of
                    select="if ($navRef/ancestor::html:li[1]/preceding-sibling::html:li) then concat('(&quot;',$navRef/string-join((ancestor::html:li[1]/preceding-sibling::html:li)[position() &lt;= 3]/normalize-space(string-join((* except (html:ol | html:ul))//text(),'')),'&quot;, &quot;'),'&quot;') else ''"/>
                <value-of
                    select="if (count($navRef/ancestor::html:li[1]/preceding-sibling::html:li) &gt; 3) then $navRef/concat(' and ',count(ancestor::html:li[1]/preceding-sibling::html:li)-3,' more') else ''"/>
                <value-of select="if ($navRef/ancestor::html:li[1]/preceding-sibling::html:li) then ')' else ''"/>
                <![CDATA[. The page reference in the navigation document and the pageTarget in the NCX must have the same number of preceding siblings. Maybe the items are not placed in the correct order or maybe some of the preceding items are missing.]]>
            </assert>
        </rule>
    </pattern>

    <!-- navdoc references must all be unique -->
    <pattern>
        <rule context="html:a">
            <report test="@href = preceding::html:a/@href">[nordic_nav_ncx_5] Two references in the navigation document can not point to the same location in the content. <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
        </rule>
    </pattern>

    <!-- ncx references must all be unique -->
    <pattern>
        <rule context="ncx:navPoint | ncx:pageTarget">
            <report test="@href = (preceding::ncx:navPoint/@href | preceding::ncx:pageTarget/@href)">[nordic_nav_ncx_6] Two references in the NCX (navPoint or pageTarget) can not point to the same
                location in the content. <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></report>
        </rule>
    </pattern>

    <!-- toc headline must be the same in the ncx and navdoc -->
    <pattern>
        <rule context="ncx:navLabel[parent::ncx:navMap]">
            <assert test="normalize-space(string-join(.//text(),'')) = /*/html:*/html:body/html:nav[tokenize(@epub:type,'\s+')='toc']/html:h1/normalize-space(string-join(.//text(),''))"
                >[nordic_nav_ncx_7] The navLabel in the NCX navMap must correspond to the h1 in the toc in the navigation document. The NCX navLabel has the value "<value-of
                    select="normalize-space(string-join(.//text(),''))"/>", while the page-list h1 in the navigation document <value-of
                    select="(/*/html:*/html:body/html:nav[tokenize(@epub:type,'\s+')='toc']/html:h1/concat('has the value &quot;',normalize-space(string-join(.//text(),'')),'&quot;'), 'does not exist')[1]"
                />.</assert>
        </rule>
    </pattern>

    <!-- page-list headline must be the same in the ncx and navdoc -->
    <pattern>
        <rule context="ncx:navLabel[parent::ncx:pageList]">
            <assert test="normalize-space(string-join(.//text(),'')) = /*/html:*/html:body/html:nav[tokenize(@epub:type,'\s+')='page-list']/html:h1/normalize-space(string-join(.//text(),''))"
                >[nordic_nav_ncx_8] The navLabel in the NCX pageList must correspond to the h1 in the page-list in the navigation document. The NCX navLabel has the value "<value-of
                    select="normalize-space(string-join(.//text(),''))"/>", while the page-list h1 in the navigation document <value-of
                    select="(/*/html:*/html:body/html:nav[tokenize(@epub:type,'\s+')='page-list']/html:h1/concat('has the value &quot;',normalize-space(string-join(.//text(),'')),'&quot;'), 'does not exist')[1]"
                />.</assert>
        </rule>
    </pattern>

</schema>
