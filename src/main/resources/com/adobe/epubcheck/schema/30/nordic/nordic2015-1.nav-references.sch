<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2" xmlns:c="http://www.w3.org/ns/xproc-step">

    <title>Nordic EPUB3 Navigation Document content reference rules</title>

    <!--
        Example input to this schematron (c:result elements are assumed to be in reading order):
        
        <html>
            <c:result>
                <c:result xml:base="..." data-sectioning-element="body" data-sectioning-id="..." data-heading-element="h1" data-heading-id="...">Chapter 1</c:result>
                <c:result xml:base="..." data-sectioning-element="body" data-sectioning-id="..." data-heading-element="h1" data-heading-id="...">Chapter 2</c:result>
                <c:result xml:base="..." data-pagebreak-element="div" data-pagebreak-id="...">1</c:result>
                <c:result xml:base="..." data-pagebreak-element="span" data-pagebreak-id="...">2</c:result>
                ...
            </c:result>
            <head>...</head>
            <body>
                ...
                <nav epub:type="nav">
                    ...
                </nav>
                ...
                <nav epub:type="page-list">
                    ...
                </nav>
                ...
            </body>
        </html>
    -->

    <ns prefix="html" uri="http://www.w3.org/1999/xhtml"/>
    <ns prefix="opf" uri="http://www.idpf.org/2007/opf"/>
    <ns prefix="dc" uri="http://purl.org/dc/elements/1.1/"/>
    <ns prefix="epub" uri="http://www.idpf.org/2007/ops"/>
    <ns prefix="nordic" uri="http://www.mtm.se/epub/"/>
    <ns prefix="c" uri="http://www.w3.org/ns/xproc-step"/>
    <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>
    

    <!-- Rule 1: All headings in the book must be referenced from the navigation document -->
    <pattern id="nav_references_1">
        <rule context="c:result/c:result[@data-sectioning-element]">
            <let name="sectioning-ref" value="if (@data-sectioning-id) then concat(replace(@xml:base,'.*/',''),'#',@data-sectioning-id) else ()"/>
            <let name="heading-ref" value="if (@data-heading-id) then concat(replace(@xml:base,'.*/',''),'#',@data-heading-id) else ()"/>
            <let name="nav-ref" value="//html:nav[tokenize(@epub:type,'\s+')='toc']//html:a[$sectioning-ref and ends-with(@href, $sectioning-ref) or $heading-ref and ends-with(@href, $heading-ref)]"/>

            <assert test="count($nav-ref) = 1">[nordic_nav_references_1] All headings in the content documents must be referenced exactly once in the navigation document. In the content document
                    "<value-of select="replace(@xml:base,'.*/','')"/>", the <value-of
                    select="if (@data-heading-element) then concat('heading &quot;', text(), '&quot;', if (@data-heading-id) then concat(' with id=&quot;', @data-heading-id,'&quot;') else '', ' inside the ') else ''"
                /> "<value-of select="@data-sectioning-element"/>" element<value-of select="if (@data-sectioning-id) then concat(' with id=&quot;', @data-sectioning-id,'&quot;') else ''"/> is
                    <value-of select="if (count($nav-ref)=0) then 'not referenced' else 'referenced multiple times'"/> from the navigation document.</assert>

            <assert test="count($nav-ref) = 0 or not(@data-heading-id) or normalize-space(string-join(.//text(),'')) = normalize-space(string-join($nav-ref//text(),''))">[nordic_nav_references_1] The
                text for the heading in the navigation document ("<value-of select="normalize-space(string-join($nav-ref//text(),''))"/>") should match the headline in the content document ("<value-of
                    select="normalize-space(string-join(.//text(),''))"/>" at <value-of select="($heading-ref, $sectioning-ref)[1]"/>)</assert>
        </rule>
    </pattern>

    <!-- Rule 2: The toc must be in reading order and nested correctly -->
    <pattern id="nav_references_2">
        <rule context="html:a[ancestor::html:nav[tokenize(@epub:type,'\s+')='toc']]">
            <let name="href" value="substring-before(@href,'#')"/>
            <let name="fragment" value="substring-after(@href,'#')"/>
            <let name="result-ref" value="/*/c:result/c:result[(@data-sectioning-id, @data-heading-id) = $fragment]"/>
            <let name="document-in-nav" value="((preceding::html:a | self::*) intersect ancestor::html:nav//html:a)[substring-before(@href,'#') = $href][1]"/>
            <let name="document-in-nav-depth" value="count($document-in-nav/ancestor::html:li)"/>
            <let name="depth-in-nav" value="count(ancestor::html:li)"/>
            <let name="depth-in-content" value="$result-ref/xs:integer((@data-heading-depth, @data-sectioning-depth)[1])"/>

            <assert test="$result-ref">[nordic_nav_references_2a] All references from the navigation document must reference either a sectioning element or a headline in one of the content documents:
                    <value-of select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/></assert>
            <report test="count($result-ref) &gt; 1">[nordic_nav_references_2a] All references from the navigation document must reference exactly one sectioning element or headline in one of the
                content documents, there are multiple sections or headlines matching the href="<value-of select="@href"/>" in <value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>; <value-of
                    select="string-join($result-ref/concat(replace(@xml:base,'.*/',''),'#',$fragment), ',')"/></report>

            <!-- TODO: commented out due to performance issues! this assertion needs to be rewritten in a much more efficient way before re-enabling it -->
            <!--<let name="preceding-refs-which-is-following-in-content"
                value="for $a in (preceding::html:a intersect ancestor::html:nav//html:a) return $result-ref/following-sibling::c:result[ends-with(concat(@xml:base,'#',(@data-sectioning-id, @data-heading-id)[1]), $a/@href)]"/>
            <report test="$result-ref and count($preceding-refs-which-is-following-in-content)">[nordic_nav_references_2a] The table of contents in the navigation document must reference the headlines
                in the correct order. The headline with id="<value-of select="$fragment"/>" in the document "<value-of select="$href"/>" is referenced from the navigation document after the headline
                with id="<value-of select="$preceding-refs-which-is-following-in-content[1]/substring-after(@href,'#')"/>" in the document "<value-of
                    select="$preceding-refs-which-is-following-in-content[1]/substring-before(@href,'#')"/>", but in the content document it occurs before it.</report>-->

            <assert test="not($result-ref) or $depth-in-nav = $depth-in-content + $document-in-nav-depth - 1">[nordic_nav_references_2b] The nesting of headlines in the content does not match the
                nesting of headlines in the navigation document. The toc item `<value-of
                    select="concat('&lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>` in the navigation document is not nested at the correct
                level. The referenced document (<value-of select="$href"/>) occurs in the navigation document at nesting depth <value-of select="$document-in-nav-depth"/> (<value-of
                    select="if ($document-in-nav-depth = 1) then 'it is not contained inside other sections such as a part or a chapter'
                    else concat('it is contained inside ',string-join($document-in-nav/ancestor::html:li[1]/ancestor::html:li/concat('&quot;',(text(),*[not(local-name()=('ol','ul'))]/string-join(.//text(),''))[normalize-space()][1],'&quot;'),', which is contained inside'))"
                />). The referenced headline (<value-of select="@href"/>) occurs in the navigation document at nesting depth <value-of select="$depth-in-nav"/> (<value-of
                    select="if ($depth-in-nav = 1) then 'it is not contained inside other sections such as a part or a chapter'
                    else concat('it is contained inside ',string-join(ancestor::html:li[1]/ancestor::html:li/concat('&quot;',(text(),*/string-join(.//text(),''))[normalize-space()][1],'&quot;'),', which is contained inside'))"
                />). The referenced headline (`&lt;<value-of select="$result-ref/@data-heading-element"/><value-of select="$result-ref/@data-heading-id/concat(' id=&quot;',.,'&quot;')"/>&gt;<value-of
                    select="$result-ref/text()"/>&lt;/<value-of select="$result-ref/@data-heading-element"/>&gt;) occurs in the content document <value-of select="$href"/> as a `<value-of
                    select="$result-ref/@data-heading-element"/>` which implies that it should be referenced at nesting depth <value-of select="$depth-in-content + $document-in-nav-depth - 1"/> in the
                navigation document.</assert>
        </rule>
    </pattern>

    <!-- Rule 3: All pagebreaks in the book must be referenced from the navigation document -->
    <pattern id="nav_references_3">
        <rule context="c:result/c:result[@data-pagebreak-element]">
            <let name="pagebreak-ref" value="if (@data-pagebreak-id) then concat(replace(@xml:base,'.*/',''),'#',@data-pagebreak-id) else ()"/>
            <let name="nav-ref" value="//html:nav[tokenize(@epub:type,'\s+')='page-list']//html:a[$pagebreak-ref and ends-with(@href, $pagebreak-ref)]"/>

            <assert test="count($nav-ref) = 1">[nordic_nav_references_3] All pagebreaks in the content documents must be referenced exactly once in the navigation document. In the content document
                    "<value-of select="replace(@xml:base,'.*/','')"/>", the pagebreak "<value-of select="text()"/>"<value-of
                    select="if (@data-pagebreak-id) then concat(' with id=&quot;', @data-pagebreak-id,'&quot;') else ''"/> is <value-of
                    select="if (count($nav-ref)=0) then 'not referenced' else 'referenced multiple times'"/> from the navigation document.</assert>

            <report test="count($nav-ref) and not(normalize-space(string-join(.//text(),'')) = ('', normalize-space(string-join($nav-ref//text(),''))))">[nordic_nav_references_3] The page number for
                the pagebreak in the navigation document ("<value-of select="normalize-space(string-join($nav-ref//text(),''))"/>") should match the page number of the referenced pagebreak in the
                content document ("<value-of select="normalize-space(string-join(.//text(),''))"/>" at <value-of select="$pagebreak-ref"/>)</report>

            <report test="count($nav-ref) and normalize-space(string-join(.//text(),'')) = '' and not(normalize-space(string-join($nav-ref//text(),'')) = '-')">[nordic_nav_references_3] The page
                number for the pagebreak in the navigation document ("<value-of select="normalize-space(string-join($nav-ref//text(),''))"/>") should be a dash ("-") when the referenced pagebreak in
                the content document is unnumbered ("<value-of select="normalize-space(string-join(.//text(),''))"/>" at <value-of select="$pagebreak-ref"/>)</report>
        </rule>
    </pattern>

    <!-- Rule 4: The page-list must be in reading order -->
    <pattern id="nav_references_4">
        <rule context="html:a[ancestor::html:nav[tokenize(@epub:type,'\s+')='page-list']]">
            <let name="result-ref" value="/*/c:result/c:result[@data-pagebreak-id = substring-after(@href,'#')]"/>
            <let name="preceding-refs-which-is-following-in-content"
                value="(preceding::html:a intersect ancestor::html:nav//html:a)[@href = $result-ref/following-sibling::c:result/concat(replace(@xml:base,'.*/',''),@data-pagebreak-id)]"/>
            <report test="count($preceding-refs-which-is-following-in-content)">[nordic_nav_references_4] The page list in the navigation document must reference the pagebreaks in the correct order.
                The pagebreak with id="<value-of select="substring-after(@href,'#')"/>" in the document "<value-of select="substring-before(@href,'#')"/>" is referenced from the navigation document
                after the pagebreak with id="<value-of select="$preceding-refs-which-is-following-in-content[1]/substring-after(@href,'#')"/>" in the document "<value-of
                    select="$preceding-refs-which-is-following-in-content[1]/substring-before(@href,'#')"/>", but in the content document it occurs before it.</report>
        </rule>
    </pattern>

</schema>
