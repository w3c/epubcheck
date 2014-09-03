<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	<ns uri="http://www.w3.org/1999/xhtml" prefix="html"/>

	<pattern id="edupub.headings">
		<rule context="html:body[child::html:*[not(self::html:section) and not(self::html:aside) and not(self::html:article) and not(self::html:nav)]]">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value="descendant::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][not(ancestor::html:section) and not(ancestor::html:aside) and not(ancestor::html:article) and not(ancestor::html:nav)]"/>
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">The body element requires a heading when it is used as an implied section.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of body.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:section">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value="descendant::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][ancestor::html:section[1]=current() and not(current()/descendant::html:*[self::html:article or self::html:aside or self::html:nav]/descendant::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6] = .)]"/>
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">Section does not have a heading.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of section.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:article">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value="descendant::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][ancestor::html:article[1]=current() and not(current()/descendant::html:*[self::html:article or self::html:aside or self::html:nav]/descendant::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6] = .)]"/>
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">Article does not have a heading.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of article.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
			<let name="current-rank" value="number(substring(name(current()),2))"/>
			<let name="topmost-heading" value="ancestor::html:body//html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][1]"/>
			<let name="topmost-rank" value="number(substring(name($topmost-heading[1]),2))"/>
			<let name="topmost-nest" value="if (count($topmost-heading[1]/ancestor::html:section) + count($topmost-heading[1]/ancestor::html:article) +  count($topmost-heading[1]/ancestor::html:nav) = 0) then 0 else 1"/>
			<let name="current-nesting" value="count(ancestor::html:section)+count(ancestor::html:article) + count(ancestor::html:aside) + count(ancestor::html:nav)"/>
			<let name="expected-rank" value="$topmost-rank - $topmost-nest + $current-nesting"/>
			
			<report test="$expected-rank &lt; 7 and not($current-rank = $expected-rank)">The heading rank h<value-of select="$current-rank"/> does match the current nesting level (<value-of select="$expected-rank"/>).</report>
			
			<report test="$expected-rank &gt; 5 and $current-rank &lt; 6">The current heading rank should be h6.</report>
		</rule>
	</pattern>
</schema>
