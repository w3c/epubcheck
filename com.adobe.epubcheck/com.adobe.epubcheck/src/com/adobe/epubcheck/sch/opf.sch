<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
  
  <sch:ns prefix="dc" uri="http://purl.org/dc/elements/1.1/" />
  <sch:ns prefix="opf" uri="http://www.idpf.org/2007/opf" />
   
  <sch:pattern name="opf_idAttrUnique" id="opf_idAttrUnique">
      <!-- id attribute value must be unique for any id attribute in opf file-->
      <sch:rule context="//*[@id]">
         <sch:assert test="count(//@id[. = current()/@id]) = 1"> 
         The "id" attribute does not have a unique value! 
         </sch:assert> 
      </sch:rule>
  </sch:pattern>
    
</sch:schema>
