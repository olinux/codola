package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.Configuration;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by oli on 18.01.15.
 */
@Path("documents")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

    @Inject
    LaTeXBuilder laTeXBuilder;

    @Inject
    Configuration configuration;

    @GET
    public List<LaTeXBuild> documents() throws IOException {
       return laTeXBuilder.getLaTeXBuilds();
    }


    /**
     * curl -X POST --header "Content-Type: application/zip" --data-binary @test.zip "http://localhost:8080/codola/rest/documents/main" > test.pdf
     *
     * @param zipFile
     * @param document
     * @return
     * @throws IOException
     * @throws InterruptedException
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

    @GET
    @Path("{uuid}")
    public Response getDocument(@PathParam("uuid") String uuid) throws IOException {
      return Response.ok((Object) laTeXBuilder.getPDFByUUID(UUID.fromString(uuid)).toFile()).type("application/pdf").build();

    }


}
