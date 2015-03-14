package ch.olischmid.codola.git.control;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
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

    @Override
    public void removeFileFromDocument(Document document, String fileName) throws IOException, GitAPIException {
        Files.deleteIfExists(document.getDirectory().resolve(fileName));
    }

    @Override
    public void pushDocument(Document document, String user, String message) throws GitAPIException, IOException {
        //Push is not supported - don't do anything
    }

    @Override
    public void updateContentOfFile(Document document, String path, String content) throws GitAPIException, IOException {
        document.writeContentOfFile(path, content);
    }

    @Override
    public void createFileForDocument(Document document, String fileName) throws IOException, GitAPIException {
        document.createNewFile(fileName);
    }

    @Override
    public void addFileForDocument(Document document, String fileName, InputStream file) throws GitAPIException, IOException {
        document.addFile(fileName, file);
    }

    @Override
    public void copyToBuildDirectory(Document document) throws IOException, GitAPIException {
        document.copyToBuildDirectory();
    }

    @Override
    public InputStream getFileOfDocument(Document document, String path) throws IOException, GitAPIException {
        return Files.newInputStream(document.getDirectory().resolve(path), StandardOpenOption.READ);

    }

    @Override
    public List<FileStructure> getFileStructure(Document document) throws IOException, GitAPIException {
        return fileUtils.getFileStructure(document);
    }

    @Override
    public void removeDocument(Document document) throws IOException, GitAPIException {
        document.remove();
    }
}
