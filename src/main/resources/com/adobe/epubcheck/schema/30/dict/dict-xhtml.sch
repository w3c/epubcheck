<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" queryBinding="xslt2">

  <ns uri="http://www.w3.org/1999/xhtml" prefix="h"/>
  <ns uri="http://www.idpf.org/2007/ops" prefix="epub"/>

  <!--
    Note: Currently untested
      - terms ancestor context (for future extensibility)
	-->

  <pattern id="dictionary">
    <rule context="h:*[tokenize(@epub:type,'\s+')='dictionary']">
      <assert test="self::h:body|self::h:section">The 'dictionary' type is only allowed on 'body' or
        'section' elements.</assert>
      <assert test="exists(h:article)">A 'dictionary' must have at least one article child.</assert>
    </rule>
  </pattern>

  <pattern id="dictionary-entry">
    <rule
      context="h:*[tokenize(@epub:type,'\s+')='dictionary']/h:article | h:*[tokenize(@epub:type,'\s+')='dictentry']">
      <assert test="self::h:article">The 'dictentry' type is only allowed on 'article'
        elements.</assert>
      <assert
        test="exists(.//h:dfn except .//h:dfn[ancestor::h:*[tokenize(@epub:type,'\s+')='condensed-entry']])"
        >A dictionary entry must have at least one 'dfn' descendant (outside of the optional
        condensed entry 'aside').</assert>
    </rule>
  </pattern>

  <pattern id="condensed-entry">
    <rule context="h:*[tokenize(@epub:type,'\s+')='condensed-entry']">
      <assert test="self::h:aside">The 'condensed-entry' type is only allowed on 'aside'
        elements.</assert>
      <assert test="parent::h:article[parent::h:*[tokenize(@epub:type,'\s+')='dictionary']]">The
        'condensed-entry' type is only allowed on a child element of an 'article' dictionary
        entry.</assert>
      <assert test="exists(@hidden[lower-case(.)=('','hidden')])">A 'condensed-entry' must have a
        'hidden' attribute with its value set to 'hidden' or the empty string.</assert>
    </rule>
  </pattern>

  <pattern id="part-of-speech-list">
    <rule context="h:*[tokenize(@epub:type,'\s+')='part-of-speech-list']">
      <assert test="self::h:ol">The 'part-of-speech-list' type is only allowed on 'ol'
        elements.</assert>
    </rule>
  </pattern>

  <pattern id="part-of-speech-group">
    <rule
      context="h:*[tokenize(@epub:type,'\s+')='part-of-speech-group'] | h:ol[tokenize(@epub:type,'\s+')='part-of-speech-list']/h:li">
      <let name="semchilds"
        value="descendant::h:*[ancestor::h:*[@epub:type or self::h:li][1] is current()]"/>
      <assert test="exists($semchilds[tokenize(@epub:type,'\s+')='part-of-speech'])">A
        'part-of-speech-group' must contain an element of type 'part-of-speech'.</assert>
    </rule>
  </pattern>

  <pattern id="sense-list">
    <rule context="h:*[tokenize(@epub:type,'\s+')='sense-list']">
      <assert test="self::h:ol">The 'sense-list' type is only allowed on 'ol' elements.</assert>
    </rule>
  </pattern>

  <pattern id="tran-info">
    <rule context="h:*[tokenize(@epub:type,'\s+')='tran-info']">
      <assert test="exists(../h:*[tokenize(@epub:type,'\s+')='tran'])">An element of type
        'tran-info' must have a sibling element of type 'tran'.</assert>
    </rule>
  </pattern>

  <pattern id="idiom">
    <rule context="h:*[tokenize(@epub:type,'\s+')='idiom']">
      <assert test="self::h:dfn">The 'idiom' type is only allowed on 'dfn' elements.</assert>
    </rule>
  </pattern>

  <pattern id="phrase-list">
    <rule context="h:*[tokenize(@epub:type,'\s+')='phrase-list']">
      <assert test="self::h:ol | self::h:ul">The 'phrase-list' type is only allowed on 'ol' or 'ul'
        elements.</assert>
    </rule>
  </pattern>

  <pattern id="phrase-group">
    <rule
      context="h:*[tokenize(@epub:type,'\s+')='phrase-group'] | h:*[tokenize(@epub:type,'\s+')='phrase-list']/h:li">
      <assert test="exists(.//h:*[tokenize(@epub:type,'\s+')=('example','idiom')])">A 'phrase-group'
        must contain an element of type 'idiom' or 'example'.</assert>
    </rule>
  </pattern>

</schema>
