<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <ns uri="http://www.idpf.org/2007/opf" prefix="opf"/>
    <ns uri="http://purl.org/dc/elements/1.1/" prefix="dc"/>
    
    <pattern id="preview.dctype">
        <rule context="opf:metadata">
            <assert test="dc:type[normalize-space(.)='preview']">An EPUB Preview publication must have a 'preview' dc:type.</assert>
        </rule>
    </pattern>
    <pattern id="preview.source">
        <rule context="opf:metadata">
            <assert test="dc:source[empty(@refines)]">WARNING: An EPUB Preview publication should link back to its source Publication using a dc:source element.</assert>
        </rule>
        <rule context="dc:source[empty(@refines)]">
            <assert test="normalize-space() ne normalize-space(//dc:identifier[@id=/opf:package/@unique-identifier])">A Preview Publication must not use the same package identifier as its source Publication.</assert>
        </rule>
    </pattern>
    
</schema>
