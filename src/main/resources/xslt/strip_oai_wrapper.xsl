<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:oai="http://www.openarchives.org/OAI/2.0/">

    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="/">
        <xsl:apply-templates select="//oai:metadata" />
    </xsl:template>

    <xsl:template match="//oai:metadata">
        <xsl:copy-of select="node()"/>
    </xsl:template>

</xsl:stylesheet>