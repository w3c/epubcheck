<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" queryBinding="xslt2">
	
	<ns uri="http://www.w3.org/1999/xhtml" prefix="h"/>
	<ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>
	
	<!--EPUB Indexes constraint checks, can be applied to any XHTML -->
	
	<pattern id="index">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index']">
			<let name="semchilds"
				value="descendant::h:*[ancestor::h:*[@epub:type or self::h:ul][1] is current()]"/>
			<assert test="self::h:body|self::h:section|self::h:article|self::h:aside|self::h:nav">The
				'index' type is only allowed on 'body' or sectioning elements.</assert>
			<assert
				test="count($semchilds[self::h:h1|self::h:h2|self::h:h3|self::h:h4|self::h:h5|self::h:h6]) le 1"
				>At most one heading element child must be present.</assert>
			<assert test="count($semchilds[tokenize(@epub:type,'\s+')='index-headnotes']) le 1">At most
				one 'index-headnotes' child must be present.</assert>
			<assert
				test="if ($semchilds[tokenize(@epub:type,'\s+')='index-group'])
				then empty($semchilds[self::h:ul or tokenize(@epub:type,'\s+')='index-entry-list'])
				else count($semchilds[self::h:ul or tokenize(@epub:type,'\s+')='index-entry-list'])=1"
				>An 'index' must contain one and only one 'index-entry-list' (possibly implied) OR one or
				more 'index-group's.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-headnotes">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-headnotes']">
			<assert test="self::h:header|self::h:section|self::h:article|self::h:aside|self::h:nav">The
				'index-headnotes' type is only allowed on 'header' or sectioning elements.</assert>
			<assert test="ancestor::h:*[@epub:type][1][tokenize(@epub:type,'\s+')='index']">The
				'index-headnotes' type is only allowed as a child of 'index'.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-legend">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-legend']">
			<assert test="self::h:dl|self::h:section|self::h:article|self::h:aside|self::h:nav">The
				'index-legend' type is only allowed on 'dl' or sectioning elements.</assert>
			<assert test="ancestor::h:*[@epub:type][1][tokenize(@epub:type,'\s+')='index-headnotes']">The
				'index-legend' type is only allowed as a child of 'index-headnotes'.</assert>
			<!--Note: index-legend should be or contain a 'dl', however this is not a requirement-->
		</rule>
	</pattern>
	
	<pattern id="index-group">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-group']">
			<assert test="self::h:section|self::h:article|self::h:aside|self::h:nav">The 'index-group'
				type is only allowed on sectioning elements.</assert>
			<assert test="ancestor::h:*[@epub:type][1][tokenize(@epub:type,'\s+')='index']">The
				'index-group' type is only allowed on children of 'index'.</assert>
			<assert
				test="count(descendant::h:*
				[ancestor::h:*[self::h:ul or @epub:type][1] is current()]
				[self::h:h1|self::h:h2|self::h:h3|self::h:h4|self::h:h5|self::h:h6]
				) le 1"
				>'index-group' must not have more than one heading child.</assert>
			<assert
				test="count(descendant::h:ul
				[ancestor::h:*[self::h:ul or @epub:type][1] is current()]
				[not(@epub-type) or tokenize(@epub:type,'\s+')='index-entry-list']
				) = 1"
				>'index-group' must have exactly one 'index-entry-list' child.</assert>
		</rule>
	</pattern>
	
	
	<pattern id="index-entry-list">
		<rule
			context="h:*[tokenize(@epub:type,'\s+')='index-entry-list']
			|h:ul[not(@epub:type)
			and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
			and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes'])]">
			<assert test="self::h:ul">The 'index-entry-list' type is only allowed on 'ul'
				elements.</assert>
			<assert
				test="ancestor::h:*[@epub:type][1][tokenize(@epub:type,'\s+')=('index','index-group','index-entry')]
				|ancestor::h:*[self::h:li][self::h:li]
				[parent::h:ul
				[tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]"
				>The 'index-entry-list' type is only allowed on children of 'index', 'index-group' or
				'index-entry'.</assert>
			<assert test="exists(h:li)">At least one 'index-entry' child.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-entry">
		<rule
			context="h:*[tokenize(@epub:type,'\s+')='index-entry']
			|h:li[parent::h:ul
			[tokenize(@epub:type,'\s+')='index-entry-list' 
			or (not(@epub:type)
			and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
			and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]">
			<let name="semchilds"
				value="descendant::h:*[ancestor::h:*[@epub:type or self::h:li][1] is current()]"/>
			<assert test="self::h:li">The 'index-entry' type is only allowed on 'li' elements.</assert>
			<assert
				test="parent::h:ul[tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]"
				>The 'index-entry' type is only allowed on children of (possibly-implied)
				'index-entry-list'.</assert>
			<assert
				test="count($semchilds
				[tokenize(@epub:type,'\s+')='index-term' 
				and not(tokenize(@epub:type,'\s+')=('index-xref-related','index-xref-preferred'))])
				=1"
				>An 'index-entry' must have one and only one 'index-term' child.</assert>
			<assert
				test="exists($semchilds
				[self::h:ul[not(@epub:type)]
				or tokenize(@epub:type,'\s+')=('index-entry-list','index-editor-note','index-locator-list',
				'index-locator','index-locator-range','index-xref-preferred','index-xref-related')])"
				>An 'index-entry' must have at least one child with the type 'index-entry-list' or
				'index-locator-list' or 'index-locator', 'index-locator-range' or 'index-editor-note' or
				'index-xref-preferred' or 'index-xref-related'.</assert>
			<assert
				test="count($semchilds
				[self::h:ul[not(@epub:type)] or
				tokenize(@epub:type,'\s+')=('index-entry-list')])
				le 1"
				>An 'index-entry' must have at most one child with the (possibly implied) type
				'index-entry-list'.</assert>
			<assert
				test="if ($semchilds[tokenize(@epub:type,'\s+')='index-locator-list']) 
				then empty($semchilds[tokenize(@epub:type,'\s+')=('index-locator','index-locator-range')])
				else true()"
				>An 'index-entry' must not have both 'index-locator-list' and 'index-locator' or
				'index-locator-range' children.</assert>
			<assert
				test="count($semchilds
				[tokenize(@epub:type,'\s+')='index-locator-list']) 
				le 1"
				>An 'index-entry' must have at most one child with the type 'index-locator-list'.</assert>
			<assert
				test="count($semchilds
				[tokenize(@epub:type,'\s+')='index-editor-note']) 
				le 1"
				>An 'index-entry' must have at most one child with the type 'index-editor-note'.</assert>
			<assert
				test="if ($semchilds
				[tokenize(@epub:type,'\s+')='index-xref-preferred']) 
				then empty($semchilds[tokenize(@epub:type,'\s+')='index-xref-related']) 
				else true()"
				>An 'index-entry' must not have both 'index-xref-preferred' and 'index-xref-related'
				children.</assert>
		</rule>
	</pattern>
	
	<pattern abstract="true" id="index-entry-child">
		<rule context="h:*[tokenize(@epub:type,'\s+')='$type']">
			<assert
				test="ancestor::h:*[self::h:li or @epub:type][1]
				[tokenize(@epub:type,'\s+')='index-entry'
				or self::h:li and parent::h:ul[
				tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]"
				>The '<xsl:value-of select="'$type'"/>' type is only allowed on children of elements with
				the (possibly implied) type 'index-entry'.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-term">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-term']">
			<assert
				test="self::h:a|self::h:abbr|self::h:area|self::h:audio|self::h:b|self::h:bdi|self::h:bdo|self::h:br
				|self::h:button|self::h:canvas|self::h:cite|self::h:code|self::h:data|self::h:datalist|self::h:del
				|self::h:dfn|self::h:em|self::h:embed|self::h:i|self::h:iframe|self::h:img|self::h:input|self::h:ins
				|self::h:kbd|self::h:keygen|self::h:label|self::h:map|self::h:mark|self::h:math|self::h:meter
				|self::h:noscript|self::h:object|self::h:output|self::h:progress|self::h:q|self::h:ruby|self::h:s
				|self::h:samp|self::h:script|self::h:select|self::h:small|self::h:span|self::h:strong|self::h:sub
				|self::h:sup|self::h:svg|self::h:textarea|self::h:time|self::h:u|self::h:var|self::h:video"
				>The 'index-term' type is only allowed on phrasing content elements.</assert>
			<assert
				test="parent::h:*[tokenize(@epub:type,'\s+')=('index-entry','index-xref-related','index-xref-preferred')]
				|ancestor::h:*[self::h:li or @epub:type][1][self::h:li]
				[parent::h:ul[tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]"
				>The 'index-term' type is only allowed on children of elements with the type 'index-entry'
				(possibly implied), 'index-xref-preferred' or 'index-xref-related'.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-editor-note" is-a="index-entry-child">
		<param name="type" value="index-editor-note"/>
	</pattern>
	
	
	<pattern id="index-locator-list">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-locator-list']">
			<assert test="self::h:ul">The 'index-locator-list' type is only allowed on 'ul'
				elements.</assert>
			<assert
				test="exists(descendant::h:*[tokenize(@epub:type,'\s+')='index-locator']|descendant::h:a[not(@epub:type)])"
				>An 'index-entry-list' must have at least one descendant with the (possibly implied)
				'index-locator' type.</assert>
		</rule>
	</pattern>
	<pattern id="index-locator-list-2" is-a="index-entry-child">
		<param name="type" value="index-locator-list"/>
	</pattern>
	
	<pattern id="index-locator">
		<rule
			context="h:*[tokenize(@epub:type,'\s+')='index-locator']
			|h:a[ancestor::h:*[tokenize(@epub:type,'\s+')=('index-locator-list','index-locator-range')]]">
			<assert test="self::h:a">The 'index-locator' type is only allowed on 'a' elements.</assert>
			<assert
				test="ancestor::h:*[tokenize(@epub:type,'\s+')=('index-locator-list','index-locator-range')]
				| ancestor::h:*[self::h:li or @epub:type][1][self::h:li]
				[parent::h:ul[tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]"
				>The 'index-locator' type is only allowed on children of elements with the (possibly
				implied) type 'index-entry' or descendants of 'index-locator-list' or 'index-range'.
			</assert>
		</rule>
	</pattern>
	
	<pattern id="index-locator-range">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-locator-range']">
			<assert
				test="ancestor::h:*[tokenize(@epub:type,'\s+')=('index-locator-list')]
				| ancestor::h:*[self::h:li or @epub:type][1][self::h:li]
				[parent::h:ul[tokenize(@epub:type,'\s+')='index-entry-list' 
				or (not(@epub:type)
				and ancestor::h:*[tokenize(@epub:type,'\s+')='index'] 
				and empty(ancestor::h:*[tokenize(@epub:type,'\s+')='index-headnotes']))]]"
				>The 'index-locator-range' type is only allowed on children of elements with the (possibly
				implied) type 'index-entry' or descendants of 'index-locator-list'.</assert>
			<assert test="exists(descendant::h:a) and count(descendant::h:a) le 2">Must contain one or two
				'index-locator' child.</assert>
		</rule>
	</pattern>
	
	<pattern id="index-xref">
		<rule context="h:*[tokenize(@epub:type,'\s+')=('index-xref-preferred','index-xref-related')]">
			<assert
				test="exists(descendant-or-self::h:*[tokenize(@epub:type,'\s+')=('index-term','index-term-category')])"
				>An 'index-xref-preferred' or 'index-xref-related' must have at least one child with the
				type 'index-term' or 'index-term-category'.</assert>
		</rule>
	</pattern>
	<pattern id="index-xref-related" is-a="index-entry-child">
		<param name="type" value="index-xref-related"/>
	</pattern>
	<pattern id="index-xref-preferred" is-a="index-entry-child">
		<param name="type" value="index-xref-preferred"/>
	</pattern>
	
	<pattern id="index-term-category">
		<rule context="h:*[tokenize(@epub:type,'\s+')='index-term-category']">
			<assert test="self::h:a">The 'index-term-category' type is only allowed on 'a'
				elements.</assert>
			<assert
				test="ancestor-or-self::h:*[tokenize(@epub:type,'\s+')=('index-xref-related','index-xref-preferred')]"
				>The 'index-term-category' type is only allowed on elements (or descendants of elements)
				with the type 'index-xref-preferred' or 'index-xref-related'.</assert>
		</rule>
	</pattern>
	
</schema>
