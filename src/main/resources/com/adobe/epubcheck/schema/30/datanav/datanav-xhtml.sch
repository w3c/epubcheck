<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

    <ns uri="http://www.w3.org/1999/xhtml" prefix="h"/>
    <ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>

    <pattern>
        <rule context="h:nav[empty(ancestor::h:nav)]">
            <assert test="normalize-space(@epub:type)">A "nav" element in a Data Navigation Document
                must have an "epub:type" attribute (to identify the nature of the navigation
                component it contains).</assert>
        </rule>
    </pattern>

    <pattern>
        <rule context="h:nav[tokenize(@epub:type, '\s+') = 'region-based']">
            <assert test="count(*) = 1 and h:ol">A region-based nav element must contain exactly one
                child ol element. </assert>
        </rule>
    </pattern>
    <pattern>
        <rule context="h:nav[tokenize(@epub:type, '\s+') = 'region-based']//h:li">
            <assert test="h:*[1]/(self::h:a | self::h:span)">The first child of a region-based nav
                list item must be either an "a" or "span" element.</assert>
            <assert test="if (count(h:*) > 1) then  count(h:*) = 2 and h:*[2]/self::h:ol else true()">The first child of
                a region-based nav list item can only be followed by a single "ol" element.</assert>
        </rule>
    </pattern>
    <pattern>
        <rule context="h:nav[tokenize(@epub:type, '\s+') = 'region-based']//h:a">
            <assert test="normalize-space(.)=''">WARNING: "a" elements in region-based navs should not contain text labels.</assert>
        </rule>
    </pattern>
    <pattern>
        <rule context="h:nav[tokenize(@epub:type, '\s+') = 'region-based']//h:span">
            <assert test="count(h:*)=2 and count(h:a) = 2">"span" elements in region-base navs must contain exactly two "a" elements.</assert>
        </rule>
    </pattern>

</schema>
