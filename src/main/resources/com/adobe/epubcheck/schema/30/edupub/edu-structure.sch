<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	<ns uri="http://www.w3.org/1999/xhtml" prefix="html"/>
	
	<!-- following variable declarations are used to test h# nesting depth -->
	<!-- checks if the body contains anything other than a single section or article - i.e., is it an implied section 
		- previously tested if >1 article/section children with : or count(//html:body/child::html:*[self::html:article or self::html:section]) &gt; 1
		  but ambiguous whether multiple section elements is an implied body or just a weird breakup of the file
	-->
	<let name="body-is-section" value="exists(//html:body/(html:* except (html:article|html:section)))"/>
	
	<!-- check if implied heading -->
	<let name="body-label-len" value="string-length(normalize-space(//html:body/@aria-label))"/>
	
	<!-- finds the topmost heading in the file that is the descendant of the body (not sectioning element ancestors) or the first descendant of a section or article -->
	<let name="topmost-heading" value="//html:body//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[not(ancestor::html:aside|ancestor::html:nav) and count(ancestor::html:section|ancestor::html:article) le 1]"/>
	
	<!-- extract the starting rank from the topmost-heading -->
	<let name="topmost-heading-rank" value="if ($body-label-len &gt; 0) then 1 else if (exists($topmost-heading)) then number(substring(name($topmost-heading[1]),2)) else 1"/>
	
	<!-- find the nesting depth of the topmost heading (0 if body, 1 if a section or article around it) -->
	<let name="topmost-heading-nest" value="if ($body-label-len &gt; 0) then 0 else if (empty($topmost-heading[1]/(ancestor::html:section|ancestor::html:article|ancestor::html:nav))) then 0 else 1"/>
	
	
	<pattern id="edupub.headings">
		<rule context="html:body[html:* except (html:article|html:section|html:aside|html:nav)]">
			<let name="headings" value=".//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[empty(ancestor::html:section|ancestor::html:aside|ancestor::html:article|ancestor::html:nav)]"/>
			
			<report test="@aria-label and $body-label-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$body-label-len &gt; 0 or count($headings) &gt; 0">The body element requires a heading when it is used as an implied section.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of body.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space(concat($headings,$headings/html:img/@alt,$headings//@aria-label))) = 0">Empty ranked heading detected.</report>
			
			<report test="@aria-label and (normalize-space($headings) = normalize-space(@aria-label))">The value of the "aria-label" attribute must not be the same as the content of the heading.</report>
		</rule>
		
		
		<rule context="html:section|html:article">
			<let name="arialabel-len" value="string-length(normalize-space(@aria-label))"/>
			<let name="headings" value=".//(html:h1|html:h2|html:h3|html:h4|html:h5|html:h6)[(ancestor::html:section|ancestor::html:article|ancestor::html:aside|ancestor::html:nav)[last()] = current()]"/>
			
			<report test="@aria-label and $arialabel-len = 0">Empty aria-label attribute found.</report>
			
			<assert test="$arialabel-len &gt; 0 or count($headings) &gt; 0"><value-of select="name()"/> does not have a heading.</assert>
			
			<!-- <report test="$arialabel-len &gt; 0 and count($headings) &gt; 0">The aria-label attribute must not be mixed with ranked headings.</report> -->
			
			<report test="count($headings) &gt; 1">More than one ranked heading found as direct descendant of <value-of select="name()"/>.</report>
			
			<report test="count($headings) = 1 and string-length(normalize-space(concat($headings,$headings/html:img/@alt,$headings//@aria-label))) = 0">Empty ranked heading detected.</report>
			
			<report test="@aria-label and (normalize-space($headings) = normalize-space(@aria-label))">The value of the "aria-label" attribute must not be the same as the content of the heading.</report>
		</rule>
		
		<rule context="html:h1|html:h2|html:h3|html:h4|html:h5|html:h6">
			<!-- get the # from the h# tag found -->
			<let name="current-rank" value="number(substring(name(current()),2))"/>
			
			<!-- find nesting depth -->
			<let name="current-nesting" value="count(ancestor::html:section|ancestor::html:article|ancestor::html:aside|ancestor::html:nav)"/>
			
			<!-- derive the expected rank of this heading from the implied body or sectioning -->
			<let name="expected-rank" value="if ($body-is-section) then $topmost-heading-rank - $topmost-heading-nest + $current-nesting else $topmost-heading-rank + $current-nesting - 1"/>
			
			<!-- report ranked headings in sectioning roots -->
			<report test="ancestor::html:figure or ancestor::html:blockquote">Ranked headings are not valid in figure or blockquote</report>
			
			<!-- if the expected rank is below 6, check that it matches what is expected -->
			<report test="$expected-rank &lt; 6 and not($current-rank = $expected-rank)">The heading rank h<value-of select="$current-rank"/> does not match the current nesting level (<value-of select="$expected-rank"/>).</report>
			
			<!-- otherwise, just stop testing after 5 and report any headings that aren't six, since no higher exist -->
			<report test="$expected-rank &gt; 5 and $current-rank &lt; 6">The current heading rank should be h6.</report>
		</rule>
	</pattern>
	
	<pattern id="edupub.sectioning">
		<rule context="*[parent::html:body or parent::html:section][not(self::html:section)]">
			<report test="preceding-sibling::html:section">Non-section elements not allowed between or after section elements.</report>
		</rule>
	</pattern>
	
	<pattern id="edupub.subtitles">
		<rule context="html:p[@epub:type='subtitle'][preceding-sibling::*[self::html:h1|self::html:h2|self::html:h3|self::html:h4|self::html:h5|self::html:h6]]">
			<assert test="ancestor::html:header">Section subtitles must be wrapped in a header element.</assert>
		</rule>
	</pattern>
</schema>
