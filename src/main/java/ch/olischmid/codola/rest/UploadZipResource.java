package ch.olischmid.codola.rest;

import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by oli on 08.03.15.
 */
@Path("documents/zip")
@Produces(MediaType.APPLICATION_JSON)
public class UploadZipResource {

    @Inject
    LaTeXBuilder laTeXBuilder;

    /**
     * curl -X POST --header "Content-Type: application/zip" --data-binary @test.zip "http://localhost:8080/codola/rest/documents/zip/main" > test.pdf
     *
     */
    @POST
    @Consumes("application/zip")
    @Produces("application/pdf")
    @Path("{doc}")
    public Response buildPDF(InputStream zipFile, @PathParam("doc") String document) throws IOException, InterruptedException {
        LaTeXBuild laTeXBuild = laTeXBuilder.buildFromZip(zipFile, document);
        Response r = Response.ok((Object) laTeXBuild.getDocument(FileEndings.PDF).toFile()).type("application/pdf").build();
        //TODO remove build directory
        return r;
    }
}
