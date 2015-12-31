<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text"/>
    <xsl:template match="/">
package ch.olischmid.codola.process;



import java.util.ArrayList;
import java.util.List;

public enum Task {
       <xsl:for-each select="//*[local-name() = 'task']">
       /**
       * <xsl:value-of select="*[local-name()='documentation']"></xsl:value-of>
       **/<xsl:for-each select="@*[local-name()='taskName']">
           <xsl:text>
       </xsl:text>
       <xsl:value-of select="translate(., 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>("<xsl:value-of select="."/>"),<xsl:text>
</xsl:text>
        </xsl:for-each>

        </xsl:for-each>
        ;
        private final String id;

        private Task(String id){
            this.id = id;
        }

        public String getId(){
            return id;
        }
}


    </xsl:template>

</xsl:stylesheet>