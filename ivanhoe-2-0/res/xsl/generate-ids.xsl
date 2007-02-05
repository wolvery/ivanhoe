<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE xsl:stylesheet>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/TR/REC-html40" exclude-result-prefixes="html" version="1.1">

<xsl:output method="xml" indent="yes" encoding="iso-8859-1"/>
<!-- Add @doctype-system and optionally doctype-public with appropriate values
      to create a <!DOCTYPE declaration on the output file, e.g., 
doctype-system='ead.dtd'
		 Note that the declaration subset will not be copied. -->

<!-- xsl:strip-space elements='*'/ -->

<!-- This copies one xml file to a copy of the same xml file. Note that a 
processing
instruction in a declaration subset will not be copied.  -->
<xsl:template match="/">
	<xsl:apply-templates select="*|comment()|processing-instruction()"/>
</xsl:template>

<xsl:template match="@*|comment()|processing-instruction()|text()">
   <xsl:copy>
     <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
   </xsl:copy>
</xsl:template>


<xsl:template match="*">
	<xsl:copy>
		<xsl:if test="not(@id)">
			<xsl:attribute name="id">
				<xsl:value-of select="generate-id()"/>
			</xsl:attribute>
		</xsl:if>
		  <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
	</xsl:copy>	
</xsl:template>

<xsl:template match="@TEIform |         @anchored |         @status |         @place |         @default |         @org |         @sample |         @part |         @to |         @targOrder |         @direct |         @rows |         @cols"/>

</xsl:stylesheet>