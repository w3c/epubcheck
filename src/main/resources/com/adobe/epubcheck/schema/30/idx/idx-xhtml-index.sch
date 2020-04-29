<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" queryBinding="xslt2">

	<ns uri="http://www.w3.org/1999/xhtml" prefix="h"/>
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>

	<!--EPUB Indexes constraint checks, must be applied only to Indexes XHTML -->

	<pattern id="index-ocurrence">
		<rule context="h:html">
			<assert test="exists(.//*[tokenize(@epub:type,'\s+')='index'])">At least one "index"
				element must be present in a document declared as an index in the OPF.</assert>
		</rule>
	</pattern>

	<pattern id="index-only">
		<rule
			context="h:html/h:body[empty(.//h:*[normalize-space(text()) 
			    and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index'])])]">
			<assert test="tokenize(@epub:type,'/s+')='index'">The document containins only index
				content, its "body" element must have the epub:type "index"</assert>
		</rule>
	</pattern>

</schema>
