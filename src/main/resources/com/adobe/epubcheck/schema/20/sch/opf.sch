<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">

  <sch:ns prefix="dc" uri="http://purl.org/dc/elements/1.1/"/>
  <sch:ns prefix="opf" uri="http://www.idpf.org/2007/opf"/>

  <sch:pattern id="opf_idAttrUnique">
    <!-- id attribute value must be unique for any id attribute in opf file-->
    <sch:rule context="//*[@id]">
      <sch:assert test="count(//@id[normalize-space(.) = normalize-space(current()/@id)]) = 1">The
        "id" attribute does not have a unique value</sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="opf_guideReferenceUnique">
    <!-- guide/reference element should be unique (#493) -->
    <sch:rule context="opf:reference">
      <sch:let name="current_type_normalized" value="normalize-space(lower-case(@type))"/>
      <sch:let name="current_href_normalized" value="normalize-space(lower-case(@href))"/>
      <sch:assert test="
        count(//opf:reference[
          normalize-space(lower-case(@type)) = $current_type_normalized and
          normalize-space(lower-case(@href)) = $current_href_normalized
        ]) le 1">WARNING: Duplicate "reference" elements with the same "type" and "href" attributes</sch:assert>
    </sch:rule>
  </sch:pattern>

</sch:schema>
