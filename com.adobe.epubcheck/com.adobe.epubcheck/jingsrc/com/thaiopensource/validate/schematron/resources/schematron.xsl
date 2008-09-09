<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
	xmlns:sch="http://www.ascc.net/xml/schematron"
        xmlns:loc="http://www.thaiopensource.com/ns/location"
        xmlns:err="http://www.thaiopensource.com/ns/error"
	xmlns:saxon="http://icl.com/saxon"
        xmlns:xj="http://xml.apache.org/xalan/java">

<xsl:param name="phase" select="'#DEFAULT'"/>
<xsl:param name="diagnose" select="false()"/>

<xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

<xsl:output indent="yes"/>

<xsl:key name="phase"
         match="/sch:schema/sch:phase[@id]"
         use="normalize-space(@id)"/>

<xsl:key name="active"
         match="/sch:schema/sch:phase[@id]/sch:active"
         use="normalize-space(@pattern)"/>
    
<xsl:key name="rule"
         match="/sch:schema/sch:pattern/sch:rule[@id]"
         use="normalize-space(@id)"/>

<xsl:key name="pattern"
         match="/sch:schema/sch:pattern[@id]"
         use="normalize-space(@id)"/>

<xsl:key name="diagnostic"
         match="/sch:schema/sch:diagnostics/sch:diagnostic[@id]"
         use="normalize-space(@id)"/>

<xsl:template match="sch:schema">
  <axsl:stylesheet version="1.0">
    <xsl:for-each select="sch:ns">
      <xsl:attribute name="{concat(@prefix,':dummy-for-xmlns')}" namespace="{@uri}"/>
    </xsl:for-each>
    <xsl:apply-templates select="." mode="check"/>
    <axsl:template match="/">
      <result>
        <axsl:apply-templates select="/" mode="all"/>
      </result>
    </axsl:template>
    <xsl:apply-templates select="sch:key|sch:diagnostics/sch:diagnostic"/>
    <xsl:choose>
      <xsl:when test="normalize-space($phase)='#DEFAULT' and @defaultPhase">
        <xsl:call-template name="process-phase">
          <xsl:with-param name="p" select="normalize-space(@defaultPhase)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="normalize-space($phase)='#DEFAULT'">
        <xsl:call-template name="process-phase">
          <xsl:with-param name="p" select="'#ALL'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="process-phase">
          <xsl:with-param name="p" select="normalize-space($phase)"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    <axsl:template match="*|/" mode="all">
      <axsl:apply-templates select="*" mode="all"/>
    </axsl:template>
    <xsl:call-template name="define-location"/>
  </axsl:stylesheet>
</xsl:template>

<xsl:template name="process-phase">
  <xsl:param name="p"/>
  <xsl:choose>
    <xsl:when test="$p='#ALL'">
      <xsl:call-template name="process-patterns">
	<xsl:with-param name="patterns" select="sch:pattern"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="active-patterns" select="key('phase',$p)/sch:active/@pattern"/>
      <xsl:call-template name="process-patterns">
        <xsl:with-param name="patterns" select="
              sch:pattern[key('active',normalize-space(@id))[normalize-space(../@id)=$p]]"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="process-patterns">
  <xsl:param name="patterns"/>
  <xsl:variable name="npatterns" select="count($patterns)"/>
  <xsl:for-each select="$patterns">
    <xsl:variable name="pattern-index" select="position()"/>
    <xsl:variable name="not-last" select="not(position()=$npatterns)"/>
    <xsl:for-each select="sch:rule">
      <xsl:choose>
	<xsl:when test="@context">
	  <axsl:template match="{@context}" mode="M{$pattern-index}" priority="{1 + (1 div position())}"
			 name="R{$pattern-index}.{position()}">
	    <xsl:call-template name="location"/>
	    <xsl:apply-templates select="*" mode="assertion"/>
	    <xsl:if test="$not-last">
	       <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
	    </xsl:if>
	  </axsl:template>
	  <axsl:template match="{@context}" mode="all"
			 priority="{($npatterns + 1 - $pattern-index) + (1 div position())}">
	    <xsl:call-template name="location"/>
	    <axsl:call-template name="R{$pattern-index}.{position()}"/>
	    <axsl:apply-templates select="*" mode="all"/>
	  </axsl:template>
          <xsl:apply-templates select="sch:key"/>
	</xsl:when>
	<xsl:otherwise>
	  <axsl:template name="A{normalize-space(@id)}">
	    <xsl:apply-templates select="*" mode="assertion"/>
	  </axsl:template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <axsl:template match="*" mode="M{$pattern-index}">
      <xsl:if test="$not-last">
	 <axsl:apply-templates select="." mode="M{$pattern-index + 1}"/>
      </xsl:if>
    </axsl:template>
  </xsl:for-each>
</xsl:template>

<xsl:template match="sch:extends" mode="assertion">
  <axsl:call-template name="A{normalize-space(@rule)}"/>
</xsl:template>

<xsl:template match="sch:report" mode="assertion">
  <axsl:if test="{@test}">
    <xsl:call-template name="location"/>
    <report>
      <xsl:call-template name="assertion"/>
    </report>
  </axsl:if>
</xsl:template>

<xsl:template match="sch:assert" mode="assertion">
  <axsl:if test="not({@test})">
    <xsl:call-template name="location"/>
    <failed-assertion>
      <xsl:call-template name="assertion"/>
    </failed-assertion>
  </axsl:if>
</xsl:template>

<xsl:template match="*" mode="assertion"/>

<xsl:template match="sch:rule/sch:key">
  <axsl:key match="{../@context}" name="{@name}" use="{@path}">
    <xsl:call-template name="location"/>
  </axsl:key>
</xsl:template>

<xsl:template match="sch:schema/sch:key">
  <axsl:key match="{@match}" name="{@name}" use="{@path}">
    <xsl:call-template name="location"/>
  </axsl:key>
</xsl:template>

<xsl:template name="assertion">
  <xsl:copy-of select="@role|@test|@icon|@id|@xml:lang"/>
  <xsl:choose>
    <xsl:when test="@subject">
      <axsl:for-each select="{@subject}">
	<xsl:call-template name="location"/>
        <xsl:call-template name="assertion-body"/>
      </axsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="assertion-body"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="assertion-body">
  <axsl:call-template name="location"/>
  <xsl:if test="* or normalize-space(text())">
    <statement>
      <xsl:apply-templates/>
    </statement>
  </xsl:if>
  <xsl:if test="$diagnose and @diagnostics and normalize-space(@diagnostics)">
    <xsl:call-template name="call-diagnostics">
      <xsl:with-param name="list" select="concat(normalize-space(@diagnostics),' ')"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="call-diagnostics">
  <xsl:param name="list"/>
  <xsl:variable name="head" select="substring-before($list,' ')"/>
  <xsl:variable name="tail" select="substring-after($list,' ')"/>
  <axsl:call-template name="D{$head}"/>
  <xsl:if test="$tail">
    <xsl:call-template name="call-diagnostics">
      <xsl:with-param name="list" select="substring-after($tail,' ')"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="sch:diagnostic">
  <xsl:if test="$diagnose">
    <axsl:template name="D{normalize-space(@id)}">
      <diagnostic>
	<xsl:copy-of select="@icon"/>
	<xsl:apply-templates/>
      </diagnostic>
    </axsl:template>
  </xsl:if>
</xsl:template>

<xsl:template match="sch:name">
  <xsl:choose>
    <xsl:when test="@path">
      <axsl:value-of select="name({@path})">
        <xsl:call-template name="location"/>
      </axsl:value-of>
    </xsl:when>
    <xsl:otherwise>
      <axsl:value-of select="name()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="sch:value-of">
  <axsl:value-of select="{@select}">
    <xsl:call-template name="location"/>
  </axsl:value-of>
</xsl:template>

<xsl:template match="sch:dir">
  <dir>
    <xsl:copy-of select="@value"/>
    <xsl:apply-templates/>
  </dir>
</xsl:template>

<xsl:template match="sch:emph">
  <emph>
    <xsl:apply-templates/>
  </emph>
</xsl:template>

<xsl:template match="sch:span">
  <span>
    <xsl:copy-of select="@class"/>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="*"/>

<xsl:variable name="saxon"
              select="function-available('saxon:lineNumber')
                      and function-available('saxon:systemId')"/>

<!-- The JDK 1.4 version of Xalan is buggy and gets an exception if we try
     to use these extension functions, so detect this version and don't use it. -->
<xsl:variable name="xalan"
              xmlns:xalan="http://xml.apache.org/xalan"
              select="function-available('xj:org.apache.xalan.lib.NodeInfo.lineNumber')
                      and function-available('xj:org.apache.xalan.lib.NodeInfo.systemId')
                      and function-available('xalan:checkEnvironment')
                      and not(contains(xalan:checkEnvironment()//item[@key='version.xalan2'],
                                       'Xalan Java 2.2'))"/>

<xsl:template name="define-location">
  <axsl:template name="location">
    <xsl:choose>
      <xsl:when test="$saxon">
	<axsl:attribute name="line-number">
	  <axsl:value-of select="saxon:lineNumber()"/>
	</axsl:attribute>
	<axsl:attribute name="system-id">
	  <axsl:value-of select="saxon:systemId()"/>
	</axsl:attribute>
      </xsl:when>
      <xsl:when test="$xalan">
	<axsl:attribute name="line-number">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
	</axsl:attribute>
	<axsl:attribute name="system-id">
	  <axsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
	</axsl:attribute>
      </xsl:when>
    </xsl:choose>
  </axsl:template>
</xsl:template>

<xsl:template name="location">
  <xsl:choose>
    <xsl:when test="$saxon">
      <xsl:attribute name="loc:line-number">
	<xsl:value-of select="saxon:lineNumber()"/>
      </xsl:attribute>
      <xsl:attribute name="loc:system-id">
	<xsl:value-of select="saxon:systemId()"/>
      </xsl:attribute>
    </xsl:when>
    <xsl:when test="$xalan">
      <xsl:attribute name="loc:line-number">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.lineNumber()"/>
      </xsl:attribute>
      <xsl:attribute name="loc:system-id">
	<xsl:value-of select="xj:org.apache.xalan.lib.NodeInfo.systemId()"/>
      </xsl:attribute>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="sch:schema" mode="check">
  <xsl:if test="@defaultPhase and not(key('phase',normalize-space(@defaultPhase)))">
    <err:error message="default_phase_missing" arg="{normalize-space(@defaultPhase)}">
      <xsl:call-template name="location"/>
    </err:error>
  </xsl:if>
  <xsl:if test="normalize-space($phase) != '#DEFAULT'
                and normalize-space($phase) != '#ALL'
                and not(key('phase',normalize-space($phase)))">
    <err:error message="phase_missing" arg="{normalize-space($phase)}"/>
  </xsl:if>
  <xsl:apply-templates select="sch:phase/sch:active|sch:pattern/sch:rule/sch:*" mode="check"/>
</xsl:template>

<xsl:template match="sch:active" mode="check">
  <xsl:if test="not(key('pattern', normalize-space(@pattern)))">
    <err:error message="active_missing" arg="{normalize-space(@pattern)}">
      <xsl:call-template name="location"/>
    </err:error>
  </xsl:if>
</xsl:template>

<xsl:template match="sch:extends" mode="check">
  <xsl:variable name="r" select="key('rule', normalize-space(@rule))"/>
  <xsl:if test="not($r)">
    <err:error message="extends_missing" arg="{normalize-space(@rule)}">
      <xsl:call-template name="location"/>
    </err:error>
  </xsl:if>
  <xsl:if test="$r/@context">
    <err:error message="extends_concrete" arg="{normalize-space(@rule)}">
      <xsl:call-template name="location"/>
    </err:error>
  </xsl:if>
  <xsl:apply-templates mode="check-cycles" select="$r">
    <xsl:with-param name="nodes" select=".."/>
    <xsl:with-param name="node-to-check" select="."/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="sch:assert|sch:report" mode="check">
  <xsl:if test="@diagnostics and normalize-space(@diagnostics)">
    <xsl:call-template name="check-diagnostics">
      <xsl:with-param name="list" select="concat(normalize-space(@diagnostics),' ')"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="check-diagnostics">
  <xsl:param name="list"/>
  <xsl:variable name="head" select="substring-before($list,' ')"/>
  <xsl:variable name="tail" select="substring-after($list,' ')"/>
  <xsl:if test="not(key('diagnostic',$head))">
    <err:error message="diagnostic_missing" arg="{$head}"/>
  </xsl:if>
  <xsl:if test="$tail">
    <xsl:call-template name="check-diagnostics">
      <xsl:with-param name="list" select="$tail"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="check"/>

<xsl:template mode="check-cycles" match="sch:rule">
  <xsl:param name="nodes" select="/.."/>
  <xsl:param name="node-to-check"/>
  <xsl:variable name="nodes-or-self" select="$nodes|."/>
  <xsl:choose>
    <xsl:when test="count($nodes) = count($nodes-or-self)">
      <xsl:for-each select="$node-to-check">
        <err:error message="extends_cycle" arg="{normalize-space(@rule)}">
          <xsl:call-template name="location"/>
        </err:error>
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <xsl:for-each select="sch:extends">
        <xsl:apply-templates select="key('rule',normalize-space(@rule))" mode="check-cycles">
          <xsl:with-param name="nodes" select="$nodes-or-self"/>
          <xsl:with-param name="node-to-check" select="$node-to-check"/>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
