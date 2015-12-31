
package ch.olischmid.codola.process;

import java.util.ArrayList;
import java.util.List;

public enum Task {
            
       /**
       * 
       **/
       INTIATELATEX("intiateLatex"),

       /**
       * 
       **/
       MD2TEX("md2tex"),

       /**
       * 
       **/
       PLANTUML2EPS("plantuml2eps"),

       /**
       * 
       **/
       APPLYLATEXSTYLE("applylatexstyle"),

       /**
       * 
       **/
       LATEXBUILD("latexbuild"),

       /**
       * 
       **/
       MD2HTML("md2html"),

       /**
       * 
       **/
       PLANTUML2PNG("plantuml2png"),

       /**
       * 
       **/
       LATEX2HTML("latex2html"),

       /**
       * 
       **/
       APPLYCSS("applycss"),

       /**
       * 
       **/
       HTMLBUILD("htmlbuild"),

       /**
       * 
       **/
       INITIATEHTML("initiateHTML"),

       /**
       * Collect all files and combine them to a single folder structure
       **/
       ASSEMBLE("assemble"),

       /**
       * 
       **/
       CLEANUP("cleanup"),

       /**
       * 
       **/
       DOWNLOAD("download"),

       /**
       * 
       **/
       EMAIL("email"),

       /**
       * 
       **/
       DEPLOYMENT("deployment"),

       /**
       * 
       **/
       ARCHIVE("archive"),

            ;
            private final String id;

            private Task(String id){
            this.id = id;
            }

            public String getId(){
            return id;
            }
            }
        