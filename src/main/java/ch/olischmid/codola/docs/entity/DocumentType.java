package ch.olischmid.codola.docs.entity;

/**
 * Created by oli on 15.03.15.
 */
public enum DocumentType {

    DEFAULT, DEDICATED, UPLOADED;

    public static final String DEFAULT_REPOSITORY = "default";
    public static final String UPLOADS_REPOSITORY = "uploads";

    public static DocumentType getTypeByRepository(String repository){
        switch(repository){
            case DEFAULT_REPOSITORY:
                return DEFAULT;
            case UPLOADS_REPOSITORY:
                return UPLOADED;
            default:
                return DEDICATED;
        }
    }
}
