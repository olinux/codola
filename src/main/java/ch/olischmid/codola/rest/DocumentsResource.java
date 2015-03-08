package ch.olischmid.codola.rest;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import ch.olischmid.codola.rest.models.FileStructure;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

   /* @GET
    @Path("{uuid}")
    public Response getDefaultDocument(@PathParam("uuid") String uuid) throws IOException {
      return Response.ok((Object) laTeXBuilder.getPDFByUUID(UUID.fromString(uuid)).toFile()).type("application/pdf").build();
    }
*/
    @GET
    @Path("{uuid}/files")
    public Response getFileList(@PathParam("uuid") String uuid) throws IOException {
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(uuid);
        Properties documentProperties = getDocumentProperties(p);
        java.nio.file.Path mainFile = documentProperties!=null ? getPathToMainFile(p, documentProperties) : null;
        FileStructure f = buildFileStructRec(p.toFile(), mainFile);
        return Response.ok(f.getSubelements()).build();
    }

    @GET
    @Path("{uuid}/files/{file}")
    public Response getFile(@PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(uuid).resolve(fileDecoded);
        return Response.ok((Object)p.toFile()).type("text/plain").build();
    }

    @PUT
    @Path("{uuid}/files/{file}")
    public Response updateFile(String content, @PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException, InterruptedException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(uuid).resolve(fileDecoded);
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        laTeXBuilder.buildDocument(UUID.fromString(uuid), fileDecoded);
        return Response.ok().build();
    }

    @DELETE
    @Path("{uuid}/files/{file}")
    public Response deleteFile(String content, @PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException, InterruptedException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(uuid).resolve(fileDecoded);
        Files.deleteIfExists(p);
        return Response.ok().build();
    }

    @POST
    @Path("{uuid}/files/{file}")
    public Response createFile(String content, @PathParam("uuid") String uuid, @PathParam("file") String file) throws IOException, InterruptedException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        java.nio.file.Path p = laTeXBuilder.getPathForDocument(uuid).resolve(fileDecoded);
        Files.createDirectories(p.getParent());
        Files.createFile(p);
        return Response.ok().build();
    }


    @POST
    @Path("{uuid}/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(@Context HttpServletRequest request, @PathParam("uuid")String uuid) throws IOException, URISyntaxException, ServletException, FileUploadException {
        java.nio.file.Path pathForDocument = laTeXBuilder.getPathForDocument(uuid);
        List<ch.olischmid.codola.rest.models.File.FileMeta> fileInfo = new ArrayList<>();
        if(ServletFileUpload.isMultipartContent(request)){
            ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> fileItems = fileUpload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                String name = fileItem.getName();
                long size = fileItem.getSize();
                java.nio.file.Path filePath = pathForDocument.resolve(fileItem.getName());
                Files.copy(fileItem.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                String url = "/url";
                String urlPreview = "/previewUrl";
                fileInfo.add(new ch.olischmid.codola.rest.models.File.FileMeta(name, size, url, urlPreview));
            }
        }
        ch.olischmid.codola.rest.models.File.Entity entity = new ch.olischmid.codola.rest.models.File.Entity(fileInfo);
        return Response.ok(entity, MediaType.APPLICATION_JSON).build();
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
            return new FileStructure(f.getName(), null, true, Files.isSymbolicLink(f.toPath()), false, fileStructures);
        }
        else{
            boolean isMainFile = mainFile==null ? false : f.toPath().toAbsolutePath().equals(mainFile.toAbsolutePath());
            return new FileStructure(f.getName(), null, false, false, isMainFile, null);
        }
    }
}
