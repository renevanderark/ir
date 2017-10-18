<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:ghdans="http://gh.kb-dans.nl/combined/v0.9/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:didl="urn:mpeg:mpeg21:2002:02-DIDL-NS"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dcterms="http://purl.org/dc/terms/"
                exclude-result-prefixes="rdf didl ghdans dcterms"
>
    <xsl:output omit-xml-declaration="yes" indent="yes"/>
    <xsl:param name="harvester-name"/>
    <xsl:param name="harvester-version"/>
    <xsl:param name="oai-url" />
    <xsl:param name="download-date" />
    <xsl:param name="sha512-tool-name"/>
    <xsl:param name="sha512-tool-version"/>
    <xsl:param name="tika-version"/>
    <xsl:param name="tika-name"/>

    <xsl:template match="/">

        <procesdata module="1">
            <harvester>
                <naam><xsl:value-of select="$harvester-name" /></naam>
                <versie><xsl:value-of select="$harvester-version" /></versie>
                <downloadDate><xsl:value-of select="$download-date" /></downloadDate>
                <baseURL><xsl:value-of select="$oai-url" /></baseURL>
                <sha512tool>
                    <naam><xsl:value-of select="$sha512-tool-name" /></naam>
                    <versie><xsl:value-of select="$sha512-tool-version" /></versie>
                </sha512tool>
                <SIPcreationDate>
                    <!-- fill in with finalize manifest -->
                </SIPcreationDate>
                <tika>
                    <naam><xsl:value-of select="$tika-name" /></naam> <!--Preproces-->
                    <versie><xsl:value-of select="$tika-version" /></versie> <!--Preproces-->
                </tika>
            </harvester>

            <fileinfo type="metadata">
                <file name="metadata.xml" ID="metadata">
                    <sha512><!-- fill in with finalize manifest --></sha512>
                    <sha512Date><!-- fill in with finalize manifest --></sha512Date>
                    <fileSize><!-- fill in with finalize manifest --></fileSize>
                </file>
            </fileinfo>
            <fileinfo type="object">
                <xsl:apply-templates select="/ghdans:nl_didl_combined/ghdans:nl_didl_norm/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/objectFile']" />
            </fileinfo>
        </procesdata>
    </xsl:template>

    <xsl:template match="/ghdans:nl_didl_combined/ghdans:nl_didl_norm/didl:DIDL/didl:Item/didl:Item/didl:Descriptor/didl:Statement/rdf:type[@rdf:resource='info:eu-repo/semantics/objectFile']">
        <xsl:variable name="count" select="position()" />
            <file> <!--name="uuid"-->
                <xsl:attribute name="ID">
                    <xsl:value-of select="concat('FILE_', format-number($count, '0000'))" />
                </xsl:attribute>
                <extensie><!-- fill in with finalize manifest --></extensie>
                <contentDisposition><!-- fill in with finalize manifest --></contentDisposition>
                <contentType><!-- fill in with finalize manifest --></contentType>
                <fileNaamAfgeleid><!-- fill in with finalize manifest --></fileNaamAfgeleid>
                <accessRights><xsl:value-of select="../../../didl:Descriptor/didl:Statement/dcterms:accessRights" /></accessRights>
                <downloadURL><xsl:value-of select="../../../didl:Component/didl:Resource/@ref" /></downloadURL>
                <sha512><!-- fill in with finalize manifest --></sha512>
                <sha512Date><!-- fill in with finalize manifest --></sha512Date>
                <fileSize><!-- fill in with finalize manifest --></fileSize>
                <mimeType bron="didl"><xsl:value-of select="../../../didl:Component/didl:Resource/@mimeType" /></mimeType>
                <mimeType bron="tika"><!-- fill in with finalize manifest --></mimeType>
                <tikaFileDate><!-- fill in with finalize manifest --></tikaFileDate>
            </file>
    </xsl:template>

</xsl:stylesheet>