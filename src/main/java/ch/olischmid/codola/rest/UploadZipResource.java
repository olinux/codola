package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.docs.entity.UploadedDocument;
import ch.olischmid.codola.git.control.DedicatedDocumentManager;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
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

    @Inject
    DedicatedDocumentManager documentGIT;


    /**
     * curl -X POST --header "Content-Type: application/zip" --data-binary @test.zip "http://localhost:8080/codola/rest/documents/zip/main" > test.pdf
     *
     */
    @POST
    @Consumes("application/zip")
    @Produces("application/pdf")
    public Response buildPDF(InputStream zipFile) throws IOException, InterruptedException, GitAPIException {
        String newDocumentFromZIP = documents.createNewDocumentFromZIP(zipFile);
        Document document = documents.getDocument(newDocumentFromZIP, UploadedDocument.REPOSITORY, null);
        documentGIT.copyToBuildDirectory(document, laTeXBuilder.getPathForDocument(document.getName()));
        LaTeXBuild laTeXBuild = laTeXBuilder.buildDocument(document);
        return Response.ok((Object)laTeXBuild.getDocument(FileEndings.PDF).toFile()).type("application/pdf").build();
    }
}
