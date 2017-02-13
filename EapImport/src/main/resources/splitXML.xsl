<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:for-each select="*/%toSplitBy">
    			<xsl:apply-templates select="/*">
    				<xsl:with-param name="position">
    					<xsl:number value="count(preceding-sibling::*[name() = name(current())])" format="1"/>
   					</xsl:with-param>
    			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="@*|node()">
        <xsl:param name="position" select="'1'" />
        <xsl:copy>
            <xsl:apply-templates select="@*|node()">
            	<xsl:with-param name="position">
            		<xsl:number value="$position" format="1" />
            	</xsl:with-param>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
	
	<xsl:template match="/*/%toSplitBy">
		<xsl:param name="position" select="'2'"/>
		<xsl:if test="count(preceding-sibling::*[name() = name(current())]) = $position">
			<xsl:copy>
            	<xsl:apply-templates select="@*|node()"/>
        	</xsl:copy>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>