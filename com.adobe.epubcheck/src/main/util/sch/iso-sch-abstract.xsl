<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0" xmlns:iso="http://purl.oclc.org/dsdl/schematron">
  
  <xsl:key name="ap" match="iso:pattern[@abstract='true']" use="@id"/>
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="iso:pattern[@abstract='true']"/>

  <xsl:template match="iso:pattern[@is-a]">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='is-a')]"/>
      <xsl:variable name="ap" select="key('ap', @is-a)"/>
      <xsl:if test="not($ap)">
        <xsl:message terminate="no">
          Cannot find abstract pattern <xsl:value-of select="@is-a"/> referred from pattern <xsl:value-of select="@id"/>.
        </xsl:message>
      </xsl:if>
      <xsl:if test="$ap[2]">
        <xsl:message terminate="no">
          More than one definitions for abstract pattern <xsl:value-of select="@is-a"/> referred from pattern <xsl:value-of select="@id"/>.
        </xsl:message>
      </xsl:if>
      <xsl:apply-templates
        select="$ap[1]/node()"
        mode="instantiate">
        <xsl:with-param name="params" select="iso:param"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="instantiate">
    <xsl:param name="params"/>
    <xsl:copy>
      <xsl:apply-templates select="node() | @*" mode="instantiate">
        <xsl:with-param name="params" select="$params"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*" mode="instantiate">
    <xsl:param name="params"/>
    <xsl:choose>
      <xsl:when test="contains(., '$')">
        <xsl:attribute name="{name()}">
          <xsl:call-template name="replaceParameters">
            <xsl:with-param name="params" select="$params"/>
            <xsl:with-param name="value" select="."/>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="replaceParameters">
    <xsl:param name="params"/>
    <xsl:param name="value"/>
    <xsl:choose>
      <xsl:when test="count($params)=0">
        <xsl:value-of select="$value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replaceParameters">
          <xsl:with-param name="params" select="$params[position()>1]"/>
          <xsl:with-param name="value">
            <xsl:call-template name="replaceParameter">
              <xsl:with-param name="param" select="$params[1]"/>
              <xsl:with-param name="value" select="$value"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="replaceParameter">
    <xsl:param name="value"/>
    <xsl:param name="param"/>
    <xsl:variable name="pname" select="concat('$',  $param/@name)"/>
    <xsl:choose>
      <xsl:when test="not(contains($value, $pname))">
        <xsl:value-of select="$value"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="before" select="substring-before($value, $pname)"/>
        <xsl:variable name="after" select="substring-after($value, $pname)"/>
        <xsl:value-of select="$before"/>
        <xsl:value-of select="$param/@value"/>
        <xsl:call-template name="replaceParameter">
          <xsl:with-param name="param" select="$param"/>
          <xsl:with-param name="value" select="$after"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
