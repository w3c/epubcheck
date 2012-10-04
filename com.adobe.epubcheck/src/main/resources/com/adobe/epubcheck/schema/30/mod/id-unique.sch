<?xml version="1.0" encoding="UTF-8"?>
<pattern id="id-unique" xmlns="http://purl.oclc.org/dsdl/schematron">
    <!-- note: assumes that NCName lexical constraints are tested elsewhere -->
    <let name="id-set" value="//*[@id]"/>
    <rule context="*[@id]">
        <assert test="count($id-set[@id = current()/@id]) = 1"
            >Duplicate ID '<value-of select="current()/@id"/>'</assert>
    </rule>
</pattern>
    
