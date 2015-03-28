package ch.olischmid.codola.docs.boundary.docMgr;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Created by oli on 14.03.15.
 */
public class UploadedDocumentManager implements DocumentManager {

    @Inject
    FileUtils fileUtils;

    @Setter
    @Getter
    Document document;


    @Override
    public void removeFileFromDocument(String fileName) throws IOException, GitAPIException {
        Files.deleteIfExists(document.getDirectory().resolve(fileName));
    }

    @Override
    public void pushDocument(String user, String message) throws GitAPIException, IOException {
        //Push is not supported - don't do anything
    }

    @Override
    public void updateContentOfFile(String path, String content) throws GitAPIException, IOException {
        document.writeContentOfFile(path, content);
    }

    @Override
    public void createFileForDocument(String fileName) throws IOException, GitAPIException {
        document.createNewFile(fileName);
    }

    @Override
    public void addFileForDocument(String fileName, InputStream file) throws GitAPIException, IOException {
        document.addFile(fileName, file);
    }

    @Override
    public void copyToBuildDirectory() throws IOException, GitAPIException {
        document.copyToBuildDirectory();
    }

    @Override
    public InputStream getFileOfDocument(String path) throws IOException, GitAPIException {
        return Files.newInputStream(document.getDirectory().resolve(path), StandardOpenOption.READ);

    }

    @Override
    public List<FileStructure> getFileStructure() throws IOException, GitAPIException {
        return fileUtils.getFileStructure(document);
    }

    @Override
    public void removeDocument() throws IOException, GitAPIException {
        document.remove();
    }

    @Override
    public void checkForUnpushedChanges(BranchInfo branchInfo) {
        //There is no branch for uploaded documents - so they can't be unpushed... we do nothing.
    }

    @Override
    public void setAsMainFile(String fileName) throws IOException, GitAPIException {
        document.setMainFile(fileName);
    }

}
