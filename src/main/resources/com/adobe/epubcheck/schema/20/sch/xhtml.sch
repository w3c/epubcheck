<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">

  <sch:ns prefix="h" uri="http://www.w3.org/1999/xhtml"/>

  <sch:pattern name="nested_hyperlinks">
    <sch:rule context="h:a">
      <sch:report test="descendant::h:a">The "a" element cannot contain any nested "a"
        elements.</sch:report>
    </sch:rule>
  </sch:pattern>

</sch:schema>
