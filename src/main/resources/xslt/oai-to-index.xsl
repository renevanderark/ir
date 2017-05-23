<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:didl="urn:mpeg:mpeg21:2002:02-DIDL-NS"
                xmlns:dii="urn:mpeg:mpeg21:2002:01-DII-NS"
                xmlns:wmp="http://www.surfgroepen.nl/werkgroepmetadataplus"
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="didl mods dc dcterms dii wmp oai">

    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="source_set" />
    <xsl:param name="source" />

    <xsl:template match="/">
        <add>
            <xsl:apply-templates select="//oai:record" />
        </add>
    </xsl:template>

    <xsl:template match="//oai:record">
        <xsl:if test="not(./oai:header[@status='deleted'])">
            <doc>
                <field name="id">
                    <xsl:value-of select="normalize-space(./oai:header/oai:identifier)" />
                </field>
                <field name="oai_datestamp_s">
                    <xsl:value-of select="normalize-space(./oai:header/oai:datestamp)" />
                </field>
                <field name="source_s">
                    <xsl:value-of select="$source" />
                </field>
                <field name="sourceSet_s">
                    <xsl:value-of select="$source_set" />
                </field>
                <xsl:call-template name="metadata">
                    <xsl:with-param name="md" select="./oai:metadata/didl:DIDL" />
                </xsl:call-template>
            </doc>
        </xsl:if>

    </xsl:template>

    <xsl:template name="metadata">
        <xsl:param name="md" />


        <xsl:for-each select="$md/didl:Item/didl:Descriptor/didl:Statement[@mimeType='application/xml']/dii:Identifier">
            <field name="diiIdentifier_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>


        <xsl:for-each select="$md//mods:mods/mods:genre">
            <field name="genre_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <xsl:for-each select="$md//mods:mods/mods:originInfo/mods:dateOther[@type='embargo']">
            <field name="embargoDate_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <xsl:for-each select="$md//mods:mods/mods:originInfo/mods:dateIssued[@encoding='w3cdtf']">
            <field name="dateIssued_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <xsl:for-each select="$md//mods:mods/mods:originInfo/mods:publisher">
            <field name="publisher_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>


        <xsl:for-each select="$md//dcterms:accessRights">
            <field name="accessRights_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <xsl:for-each select="$md//wmp:rights/dc:description" >
            <field name="wmpRightsDescription_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <xsl:for-each select="$md//wmp:rights/dc:rights" >
            <field name="wmpRights_ss"><xsl:value-of select="normalize-space(.)" /></field>
        </xsl:for-each>

        <field name="objectCount_i">
            <xsl:value-of select="normalize-space(count($md//rdf:type[@rdf:resource='info:eu-repo/semantics/objectFile']))" />
        </field>

    </xsl:template>






</xsl:stylesheet>