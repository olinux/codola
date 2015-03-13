package ch.olischmid.codola.rest;

/**
 * Created by oli on 08.03.15.
 */

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.docs.entity.GitDocument;
import ch.olischmid.codola.git.control.DefaultGIT;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Path("documents/default")
@Produces(MediaType.APPLICATION_JSON)
public class DefaultDocumentsResource {

    @Inject
    LaTeXBuilder laTeXBuilder;

    @Inject
    Configuration configuration;

    @Inject
    Documents documents;

    @Inject
    DefaultGIT defaultGIT;

    @GET
    public List<GitDocument> getDefaultDocuments() throws IOException, GitAPIException {
        return defaultGIT.getDocuments();
    }

    @GET
    @Path("{name}")
    public Response getDocument(@PathParam("name") String document) throws IOException, GitAPIException {
        return Response.ok((Object) laTeXBuilder.getPDF(document).toFile()).type("application/pdf").build();
    }


    @GET
    @Path("{name}/files")
    public Response getFileListForDefaultDocument(@PathParam("name") String document) throws IOException, GitAPIException {
        Document doc = documents.getDefaultDocument(document);        ;
        return Response.ok(defaultGIT.getFileStructure(doc)).build();
    }


    @GET
    @Path("{name}/files/{file}")
    public Response getDefaultFile(@PathParam("name") String document, @PathParam("file") String file) throws IOException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        Document doc = documents.getDefaultDocument(document);
        InputStream fileIS = defaultGIT.getFileOfDocument(doc, fileDecoded);
        if(fileIS!=null){
            return Response.ok(fileIS).type("text/plain").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    @PUT
    @Path("{name}/files/{file}")
    @Produces("text/plain")
    public String updateDefaultFile(String content, @PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        Document doc = documents.getDefaultDocument(document);
        defaultGIT.updateContentOfFile(doc, file, content);
        //Copy the file to its external folder (we don't want to block the git repo for the whole build process)
        defaultGIT.externalizeDocument(doc, laTeXBuilder.getPathForDocument(doc.getName()));
        //build the document
        LaTeXBuild laTeXBuild = laTeXBuilder.buildDocument(doc);
        return laTeXBuild.getBuildLog();
    }


    @POST
    @Path("{name}")
    public void createNewDocument(@PathParam("name") String name) throws IOException, GitAPIException, URISyntaxException {
        defaultGIT.createNewDocument(name);
    }

    @PUT
    @Path("{name}")
    public void pushDocument(@Context HttpServletRequest request, @PathParam("name") String document, String message) throws IOException, GitAPIException, URISyntaxException {
        Document doc = documents.getDefaultDocument(document);
        defaultGIT.pushDocument(doc, request.getRemoteUser(), message);
    }


    @POST
    @Path("{name}/files/{file}")
    public void createFileForDocument(@Context HttpServletRequest request, @PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        Document doc = documents.getDefaultDocument(document);
        defaultGIT.createFileForDocument(doc, fileDecoded);
    }


    @POST
    @Path("{name}/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFileToDefault(@Context HttpServletRequest request, @PathParam("name")String document) throws IOException, URISyntaxException, ServletException, FileUploadException, GitAPIException {
        Document doc = documents.getDefaultDocument(document);
        List<ch.olischmid.codola.rest.models.File.FileMeta> fileInfo = new ArrayList<>();
        if(ServletFileUpload.isMultipartContent(request)){
            ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> fileItems = fileUpload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                String name = fileItem.getName();
                long size = fileItem.getSize();
                defaultGIT.addFileForDocument(doc, name, fileItem.getInputStream());
                String url = "/url";
                String urlPreview = "/previewUrl";
                fileInfo.add(new ch.olischmid.codola.rest.models.File.FileMeta(name, size, url, urlPreview));
            }
        }
        ch.olischmid.codola.rest.models.File.Entity entity = new ch.olischmid.codola.rest.models.File.Entity(fileInfo);
        return Response.ok(entity, MediaType.APPLICATION_JSON).build();
    }

    @DELETE
    @Path("{name}")
    public Response deleteDocument(@PathParam("name") String document) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(document, StandardCharsets.UTF_8.name());
        Document doc = documents.getDefaultDocument(document);
        defaultGIT.removeDocument(doc);
        return Response.ok().build();
    }

    @DELETE
    @Path("{name}/files/{file}")
    public Response deleteFile(@PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        Document doc = documents.getDefaultDocument(document);
        defaultGIT.removeFileFromDocument(doc, fileDecoded);
        return Response.ok().build();
    }

}
