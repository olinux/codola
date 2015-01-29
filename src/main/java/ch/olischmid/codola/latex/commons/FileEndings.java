package ch.olischmid.codola.latex.commons;

/**
 * Created by oli on 29.01.15.
 */
public enum FileEndings {

    LATEX(".tex"), PDF(".pdf");

    public final String ending;

    private FileEndings(String ending){
        this.ending = ending;
    }

    public String appendFileEnding(String fileWithoutEnding){
        return fileWithoutEnding+ending;
    }
}
