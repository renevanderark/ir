<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:strip-space elements="*"/>
    <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

    <xsl:param name="param1" />
    <xsl:param name="param2" />

    <xsl:template match="/">
        <output>
            <kept><xsl:value-of select="normalize-space(output/kept)" /></kept>
            <param1><xsl:value-of select="$param1" /></param1>
            <param2><xsl:value-of select="$param2" /></param2>

        </output>
    </xsl:template>
</xsl:stylesheet>