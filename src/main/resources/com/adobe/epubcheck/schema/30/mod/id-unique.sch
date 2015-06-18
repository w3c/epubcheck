<?xml version="1.0" encoding="UTF-8"?>
<pattern id="id-unique" xmlns="http://purl.oclc.org/dsdl/schematron">
    <!-- note: assumes that NCName lexical constraints are tested elsewhere -->
    <let name="id-set" value="//*[@id]"/>
    <rule context="*[@id]">
        <assert test="count($id-set[normalize-space(@id) = normalize-space(current()/@id)]) = 1"
            >Duplicate '<value-of select="normalize-space(current()/@id)"/>'</assert>
    </rule>
</pattern>
