<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">

        <xsl:result-document method="text" href="../../java/ch/olischmid/codola/process/Task.java">
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
        </xsl:result-document>
        <!--<xsl:for-each select="//*[local-name() = 'task']">-->
                 <!--<xsl:result-document method="text" href="../../java/ch/olischmid/codola/process/handler/{upper-case(substring(@*[local-name()='taskName'], 1, 1))}{substring(@*[local-name()='taskName'], 2)}Handler.java">-->
<!--package ch.olischmid.codola.process.handler;-->

<!--import ch.olischmid.codola.process.Task;-->
<!--import ch.olischmid.codola.process.data.DocumentOrder;-->

<!--public class <xsl:value-of select="upper-case(substring(@*[local-name()='taskName'], 1, 1))"/><xsl:value-of select="substring(@*[local-name()='taskName'], 2)"/>Handler implements TaskHandler {-->
            <!--@Override-->
            <!--public void handleTask(DocumentOrder documentOrder) {-->
                <!--System.out.println("Handle task <xsl:value-of select="@*[local-name()='taskName']"/>");-->
            <!--}-->

            <!--@Override-->
            <!--public Task getTask() {-->
            <!--return Task.<xsl:value-of select="upper-case(@*[local-name()='taskName'])"/>;-->
            <!--}-->


<!--}-->
        <!--</xsl:result-document>-->
        <!--</xsl:for-each>-->


        <xsl:result-document method="text" href="../../java/ch/olischmid/codola/process/handler/HandlerFactory.java">package ch.olischmid.codola.process.handler;
/**
 * GENERATED WITH TRANSFORM.XSL
 **/
 import ch.olischmid.codola.process.Task;
 import javax.inject.Inject;

 public class HandlerFactory {

        <xsl:for-each select="//*[local-name() = 'task']">
        @Inject
        <xsl:value-of select="concat(upper-case(substring(@*[local-name()='taskName'], 1, 1)), substring(@*[local-name()='taskName'], 2), 'Handler')"/><xsl:text> </xsl:text><xsl:value-of select="@*[local-name()='taskName']"/>;
        </xsl:for-each>

        public TaskHandler getHandler(Task task){
            switch(task){
            <xsl:for-each select="//*[local-name() = 'task']">
            case  <xsl:value-of select="upper-case(@*[local-name()='taskName'])"/>:
                return <xsl:value-of select="@*[local-name()='taskName']"/>;</xsl:for-each>
            default:
                throw new UnsupportedOperationException("No handler defined for task "+task);
            }
        }
 }




</xsl:result-document>

    </xsl:template>

</xsl:stylesheet>