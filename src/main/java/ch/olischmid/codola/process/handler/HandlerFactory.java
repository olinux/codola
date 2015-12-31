package ch.olischmid.codola.process.handler;
/**
 * GENERATED WITH TRANSFORM.XSL
 **/
 import ch.olischmid.codola.process.Task;
 import javax.inject.Inject;

 public class HandlerFactory {

        
        @Inject
        IntiateLatexHandler intiateLatex;
        
        @Inject
        Md2texHandler md2tex;
        
        @Inject
        Plantuml2epsHandler plantuml2eps;
        
        @Inject
        ApplylatexstyleHandler applylatexstyle;
        
        @Inject
        LatexbuildHandler latexbuild;
        
        @Inject
        Md2htmlHandler md2html;
        
        @Inject
        Plantuml2pngHandler plantuml2png;
        
        @Inject
        Latex2htmlHandler latex2html;
        
        @Inject
        ApplycssHandler applycss;
        
        @Inject
        HtmlbuildHandler htmlbuild;
        
        @Inject
        InitiateHTMLHandler initiateHTML;
        
        @Inject
        AssembleHandler assemble;
        
        @Inject
        CleanupHandler cleanup;
        
        @Inject
        DownloadHandler download;
        
        @Inject
        EmailHandler email;
        
        @Inject
        DeploymentHandler deployment;
        
        @Inject
        ArchiveHandler archive;
        

        public TaskHandler getHandler(Task task){
            switch(task){
            
            case  INTIATELATEX:
                return intiateLatex;
            case  MD2TEX:
                return md2tex;
            case  PLANTUML2EPS:
                return plantuml2eps;
            case  APPLYLATEXSTYLE:
                return applylatexstyle;
            case  LATEXBUILD:
                return latexbuild;
            case  MD2HTML:
                return md2html;
            case  PLANTUML2PNG:
                return plantuml2png;
            case  LATEX2HTML:
                return latex2html;
            case  APPLYCSS:
                return applycss;
            case  HTMLBUILD:
                return htmlbuild;
            case  INITIATEHTML:
                return initiateHTML;
            case  ASSEMBLE:
                return assemble;
            case  CLEANUP:
                return cleanup;
            case  DOWNLOAD:
                return download;
            case  EMAIL:
                return email;
            case  DEPLOYMENT:
                return deployment;
            case  ARCHIVE:
                return archive;
            default:
                throw new UnsupportedOperationException("No handler defined for task "+task);
            }
        }
 }




