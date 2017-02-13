<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" indent="no" />

	<xsl:template match="/">
		<xsl:for-each select="/*%level
				[name() = following-sibling::*/name()
  				and
    			not(name() = preceding-sibling::*/name())
   				]">%parents<xsl:value-of select="name()" /><xsl:text>,</xsl:text>
   		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>