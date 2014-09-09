<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	<ns uri="http://www.w3.org/1999/xhtml" prefix="html"/>

	<pattern id="edupub.headings">
		<rule context="html:body[exists(html:* except (html:section|html:aside|html:article|html:nav))]">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value="//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[empty(ancestor::html:section|ancestor::html:aside|ancestor::html:article|ancestor::html:nav)]"/>
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">The body element requires a heading when it is used as an implied section.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of body.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:section">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value=".//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[(ancestor::html:section|ancestor::html:article|ancestor::html:aside|ancestor::html:nav)[1]=current()]"/> 
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">Section does not have a heading.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of section.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:article">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value=".//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[(ancestor::html:section|ancestor::html:article|ancestor::html:aside|ancestor::html:nav)[1]=current()]"/> 
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0">Article does not have a heading.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of article.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space($headings)) = 0">Empty ranked heading detected.</report>
		</rule>
		
		
		<rule context="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
			<let name="current-rank" value="number(substring(name(current()),2))"/>
			<let name="topmost-heading" value="ancestor::html:body//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[1]"/>
			<let name="topmost-rank" value="number(substring(name($topmost-heading[1]),2))"/>
			<let name="topmost-nest" value="number(empty($topmost-heading[1]/(ancestor::html:section|ancestor::html:article|ancestor::html:nav)))"/>
			<let name="current-nesting" value="count(ancestor::html:section|ancestor::html:article|ancestor::html:aside|ancestor::html:nav)"/>
			<let name="expected-rank" value="$topmost-rank - $topmost-nest + $current-nesting"/>
			
			<report test="$expected-rank &lt; 7 and not($current-rank = $expected-rank)">The heading rank h<value-of select="$current-rank"/> does match the current nesting level (<value-of select="$expected-rank"/>).</report>
			
			<report test="$expected-rank &gt; 5 and $current-rank &lt; 6">The current heading rank should be h6.</report>
		</rule>
	</pattern>
</schema>
