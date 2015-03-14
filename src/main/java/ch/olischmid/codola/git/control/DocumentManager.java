package ch.olischmid.codola.git.control;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by oli on 14.03.15.
 */
public interface DocumentManager {

    void removeFileFromDocument(Document document, String fileName) throws IOException, GitAPIException;

    void pushDocument(Document document, String user, String message) throws GitAPIException, IOException;

    void updateContentOfFile(Document document, String path, String content) throws GitAPIException, IOException;

    void createFileForDocument(Document document, String fileName) throws IOException, GitAPIException;

    void addFileForDocument(Document document, String fileName, InputStream file) throws GitAPIException, IOException;

    void copyToBuildDirectory(Document document) throws IOException, GitAPIException;

    InputStream getFileOfDocument(Document document, String path) throws IOException, GitAPIException;

    //this could potentially be done even without checking out the branch and therefore without locking - but it's much more comfortable with asking the file system. Anyways: If this represents a bottleneck, read the file structure from the git meta data.
    List<FileStructure> getFileStructure(Document document) throws IOException, GitAPIException;

    void removeDocument(Document document) throws IOException, GitAPIException;
}
