package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.Configuration;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import ch.olischmid.codola.rest.models.FileStructure;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Inject
    Logger logger;

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

    @GET
    @Path("{uuid}/files")
    public Response getFileList(@PathParam("uuid") String uuid) throws IOException {
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(UUID.fromString(uuid));
        Properties documentProperties = getDocumentProperties(p);
        java.nio.file.Path mainFile = documentProperties!=null ? getPathToMainFile(p, documentProperties) : null;
        FileStructure f = buildFileStructRec(p.toFile(), mainFile);
        return Response.ok(f.getSubelements()).build();
    }

    private Properties getDocumentProperties(java.nio.file.Path documentPath){
        java.nio.file.Path config = documentPath.resolve(".codola");
        if(Files.exists(config)){
            Properties prop = new Properties();
            try {
                prop.load(Files.newInputStream(config));
                return prop;
            } catch (IOException e) {
                logger.log(Level.WARNING, "Document properties were not readable from "+config.toString(), e);
                return null;
            }
        }
        return null;
    }

    private java.nio.file.Path getPathToMainFile(java.nio.file.Path documentPath, Properties documentProperties){
        String mainFile = documentProperties.getProperty("mainFile");
        if(mainFile!=null){
            return documentPath.resolve(mainFile);
        }
        return null;
    }

    private FileStructure buildFileStructRec(File f, java.nio.file.Path  mainFile){
        if(f.isDirectory()){
            List<FileStructure> fileStructures = new ArrayList<>();
            for(File subfile : f.listFiles()){
                fileStructures.add(buildFileStructRec(subfile, mainFile));
            }
            Collections.sort(fileStructures, FileStructure.SORTBYTYPEANDNAME);
            return new FileStructure(f.getName(), true, Files.isSymbolicLink(f.toPath()), false, fileStructures);
        }
        else{
            boolean isMainFile = mainFile==null ? false : f.toPath().toAbsolutePath().equals(mainFile.toAbsolutePath());
            return new FileStructure(f.getName(), false, false, isMainFile, null);
        }
    }


    @GET
    @Path("{uuid}/files/{file}")
    public Response getFile(@PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(UUID.fromString(uuid)).resolve(fileDecoded);
        return Response.ok((Object)p.toFile()).type("text/plain").build();
    }

    @PUT
    @Path("{uuid}/files/{file}")
    public Response updateFile(String content, @PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException, InterruptedException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(UUID.fromString(uuid)).resolve(fileDecoded);
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        laTeXBuilder.buildDocument(UUID.fromString(uuid), fileDecoded);
        return Response.ok().build();
    }

    @POST
    @Path("{uuid}/files/{file}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFile(@Context HttpServletRequest request, @PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException, InterruptedException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(UUID.fromString(uuid)).resolve(fileDecoded);

        return Response.ok().build();
    }
}
