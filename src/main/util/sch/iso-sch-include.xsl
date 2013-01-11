<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
    xmlns:iso="http://purl.oclc.org/dsdl/schematron" exclude-result-prefixes="iso">

    <xsl:template match="/">  
        <xsl:choose>
            <xsl:when test="namespace-uri(/*[1])='http://relaxng.org/ns/structure/1.0'">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:when test="namespace-uri(/*[1])='http://www.w3.org/2001/XMLSchema'">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:when test="namespace-uri(/*[1])='http://purl.oclc.org/dsdl/schematron'">
                <xsl:apply-templates mode="resolveIncludes" select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="/"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="node() | @*" mode="resolveIncludes">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="resolveIncludes"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="iso:include" mode="resolveIncludes">
        <xsl:choose>
            <xsl:when test="contains(@href, '#')">
                <xsl:variable name="document-uri" select="substring-before(@href, '#')"/>
                <xsl:variable name="fragment-id" select="substring-after(@href, '#')"/>
                <xsl:choose>
                    <xsl:when test="$fragment-id!=''">
                        <xsl:variable name="doc" select="document($document-uri,.)"/>
                        <xsl:if test="not($doc)">
                            <xsl:message terminate="no">
                                <xsl:text>Unable to open referenced included file: </xsl:text>
                                <xsl:value-of select="$document-uri"/>
                            </xsl:message>
                        </xsl:if>
                        <xsl:apply-templates select="$doc/iso:*[@id=$fragment-id]" mode="resolveIncludes"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message terminate="no">
                            <xsl:text>Invalid href attribute value for include: </xsl:text>
                            <xsl:value-of select="@href"/>
                        </xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="doc" select="document(@href,.)"/>
                <xsl:if test="not($doc)">
                    <xsl:message terminate="no">
                        <xsl:text>Unable to open referenced included file: </xsl:text>
                        <xsl:value-of select="@href"/>
                    </xsl:message>
                </xsl:if>
                <xsl:if test="$doc/iso:schema">
                    <xsl:message terminate="no">
                        <xsl:text>The Schematron include should not point to a schema element,</xsl:text>
                        <xsl:text> it should point to an element that is valid when it replaces the include: </xsl:text>
                        <xsl:value-of select="@href"/>
                    </xsl:message>
                </xsl:if>
                <xsl:apply-templates select="$doc/iso:*" mode="resolveIncludes"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="iso:include[normalize-space(@href)='']" mode="resolveIncludes">
        <xsl:message terminate="no">
            <xsl:text>Invalid empty href attribute value for include.</xsl:text>
        </xsl:message>
    </xsl:template>
</xsl:stylesheet>
