package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.docs.entity.BranchInfo;
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

    void setDocument(Document document);

    Document getDocument();

    void removeFileFromDocument(String fileName) throws IOException, GitAPIException;

    void pushDocument(String user, String message) throws GitAPIException, IOException;

    void updateContentOfFile(String path, String content) throws GitAPIException, IOException;

    void createFileForDocument(String fileName) throws IOException, GitAPIException;

    void addFileForDocument(String fileName, InputStream file) throws GitAPIException, IOException;

    void copyToBuildDirectory() throws IOException, GitAPIException;

    InputStream getFileOfDocument(String path) throws IOException, GitAPIException;

    List<FileStructure> getFileStructure() throws IOException, GitAPIException;

    void removeDocument() throws IOException, GitAPIException;

    void checkForUnpushedChanges(BranchInfo branch) throws IOException, GitAPIException;

    void setAsMainFile(String fileName) throws IOException, GitAPIException;

}
