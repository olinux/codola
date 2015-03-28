package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

@Path("documents/{repository}/{document}")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

    @PathParam("repository")
    String repository;

    @PathParam("document")
    String document;

    @Inject
    Documents documents;

    @Inject
    GIT git;


    @POST
    public void createDocument(String mainFile) throws IOException, GitAPIException, URISyntaxException {
        DocumentType typeByRepository = DocumentType.getTypeByRepository(repository);
        if (DocumentType.DEFAULT == typeByRepository) {
            documents.createNewDocument(document, mainFile);
        } else {
            throw new UnsupportedOperationException("New document branches can only be created on the default repository");
        }
    }


    /**
     * Removes the current document
     */
    @DELETE
    public Response deleteDocument() throws IOException, InterruptedException, GitAPIException {
        for (BranchInfo branchInfo : documents.getBranches(repository)) {
            DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branchInfo.getName());
            documentMgr.removeDocument();
        }
        return Response.ok().build();
    }

}
