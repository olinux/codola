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
        if(fileWithoutEnding.endsWith(ending)) {
            return fileWithoutEnding;
        }
        else {
            String newfileWithoutEnding = null;
            for (FileEndings fileEndings : FileEndings.values()) {
                if(fileWithoutEnding.endsWith(fileEndings.ending)){
                    newfileWithoutEnding = fileWithoutEnding.substring(0, fileWithoutEnding.length()-fileEndings.ending.length());
                    break;
                }
            }
            return (newfileWithoutEnding!=null ? newfileWithoutEnding : fileWithoutEnding) + ending;
        }
    }
}
