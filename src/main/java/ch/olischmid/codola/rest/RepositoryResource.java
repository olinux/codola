package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.DocumentInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("documents/{repository}")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryResource {

    @PathParam("repository")
    String repository;

    @Inject
    Documents documents;

    @Inject
    GIT git;

    /**
     * Register the given GIT repository in this CoDoLa instance
     */
    @POST
    public void registerRepository(String remoteRepo) throws IOException, GitAPIException {
        git.install(remoteRepo, repository);
    }

    /**
     * @return the documents registered in this repository.
     */
    @GET
    public List<DocumentInfo> getDocuments() throws IOException, GitAPIException {
        return documents.getDocuments(DocumentType.getTypeByRepository(repository));
    }
}
