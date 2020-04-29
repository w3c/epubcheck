<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <ns uri="http://www.w3.org/1999/xhtml" prefix="html"/>
    <ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
    <pattern id="mapping.doc.version">
        <rule context="html:head">
            <assert
                test="html:meta[@name = 'epub.multiple.renditions.version' and @content = '1.0']">A
                meta tag with the name "epub.multiple.renditions.version" and value "1.0" is
                required.</assert>
        </rule>
    </pattern>

    <pattern id="resourcemap.nav">
        <rule context="html:body">
            <assert test="count(html:nav[tokenize(@epub:type, '\s+') = 'resource-map']) = 1">A
                Rendition Mapping Document must contain exactly one "resource-map" nav
                element.</assert>
            <report test="html:nav[normalize-space(@epub:type) = '']">A nav element of a Rendition
                Mapping Document must identify its nature in an epub:type attribute.</report>
        </rule>
    </pattern>
</schema>
