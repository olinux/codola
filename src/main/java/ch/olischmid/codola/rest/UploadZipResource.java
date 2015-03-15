package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by oli on 08.03.15.
 */
@Path("upload/zip")
@Produces(MediaType.APPLICATION_JSON)
public class UploadZipResource {

    @Inject
    LaTeXBuilder laTeXBuilder;

    @Inject
    Documents documents;


    /**
     * curl -X POST --header "Content-Type: application/zip" --data-binary @test.zip "http://localhost:8080/codola/rest/documents/zip/main" > test.pdf
     *
     */
    @POST
    @Consumes("application/zip")
    @Produces("application/pdf")
    public Response buildPDF(InputStream zipFile) throws IOException, InterruptedException, GitAPIException {
        String newDocumentFromZIP = documents.createNewDocumentFromZIP(zipFile);
        DocumentManager documentMgr = documents.getDocumentMgr(newDocumentFromZIP, DocumentType.UPLOADS_REPOSITORY, null);
        documentMgr.copyToBuildDirectory();
        LaTeXBuild laTeXBuild = documents.buildDocument(documentMgr);
        return Response.ok((Object)laTeXBuild.getDocument(FileEndings.PDF).toFile()).type("application/pdf").build();
    }
}
