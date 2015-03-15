package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.boundary.docMgr.DedicatedDocumentManager;
import ch.olischmid.codola.docs.boundary.docMgr.DefaultDocumentManager;
import ch.olischmid.codola.docs.boundary.docMgr.UploadedDocumentManager;
import ch.olischmid.codola.docs.control.DocumentRepository;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.docs.entity.DocumentInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by oli on 21.02.15.
 */
public class Documents {

    @Inject
    Configuration configuration;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    DefaultDocumentManager defaultDocMgr;

    @Inject
    DedicatedDocumentManager dedicatedDocMgr;

    @Inject
    UploadedDocumentManager uploadedDocMgr;

    @Inject
    LaTeX latex;


    public List<DocumentInfo> getDocuments(DocumentType type) throws IOException, GitAPIException {
        List<DocumentInfo> result;
        switch(type){
            case DEFAULT:
                result = documentRepository.getDefaultDocuments();
                break;
            case UPLOADED:
                result = documentRepository.getUploadedDocuments();
                break;
            case DEDICATED:
                result = documentRepository.getDedicatedDocuments();
                break;
            default:
                result = Collections.emptyList();
                break;
        }
        for(DocumentInfo d : result){
            DocumentManager documentMgr = getDocumentMgr(d.getDisplayName(), d.getGitRepository(), d.getFullBranchName());
            d.setUnpushedChanges(documentMgr.hasUnPushedChanges());
        }
        return result;
    }


    public DocumentManager getDocumentMgr(String name, String repository, String branch) throws GitAPIException, IOException {
        Path p = configuration.getAbsoluteGitDirectory().resolve(repository);
        Path buildDirectory = latex.getPathForDocument(name);
        Document d = Document.createDocument(name, repository, branch, p, buildDirectory);
        DocumentManager mgr = null;
        switch(d.getDocumentType()){
            case DEDICATED:
                mgr = dedicatedDocMgr;
                break;
            case DEFAULT:
                mgr = defaultDocMgr;
                break;
            case UPLOADED:
                mgr = uploadedDocMgr;
                break;
            default:
                break;
        }
        if(mgr!=null){
            mgr.setDocument(d);
        }
        return mgr;
    }


    public LaTeXBuild buildDocument(DocumentManager document) throws IOException, InterruptedException, GitAPIException {
        return latex.build(document);
    }

    public Path getPDF(String name) throws IOException {
        return latex.getPDF(name);
    }

    /**
     * Initializes a new document - in fact, a new branch is created (on base of the current master branch) which is immediately pushed to the remote repo.
     */
    public void createNewDocument(String document, String mainFile) throws IOException, GitAPIException, URISyntaxException {
       documentRepository.createNewDefaultDocument(document);
        DocumentManager documentMgr = getDocumentMgr(document, DocumentType.DEFAULT_REPOSITORY, document);
        documentMgr.setAsMainFile(mainFile);
        InputStream fileOfDocument = documentMgr.getFileOfDocument(mainFile);
        if(fileOfDocument==null){
            documentMgr.createFileForDocument(mainFile);
        }

    }

    public String createNewDocumentFromZIP(InputStream inputStream) throws IOException {
        return documentRepository.createNewDocumentFromZIP(inputStream);
    }

}
