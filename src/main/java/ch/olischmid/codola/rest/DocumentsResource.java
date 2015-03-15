package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.DocumentInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
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

@Path("documents/{repository}")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

    @PathParam("repository")
    String repository;

    @QueryParam("branch")
    String branch;

    @Inject
    Documents documents;

    @Inject
    GIT git;


    @POST
    public void checkoutBranch(String remoteRepo) throws IOException, GitAPIException {
        git.install(remoteRepo, repository);
    }

    @GET
    public List<DocumentInfo> getDefaultDocuments() throws IOException, GitAPIException {
        return documents.getDocuments(DocumentType.getTypeByRepository(repository));
    }

    @GET
    @Path("{name}")
    public Response getPDF(@PathParam("name") String document) throws IOException, GitAPIException {
        java.nio.file.Path pdf = documents.getPDF(document);
        if(pdf==null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pdf.toFile()).type("application/pdf").build();
    }


    @GET
    @Path("{name}/files")
    public Response getFileList(@PathParam("name") String document) throws IOException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        return Response.ok(documentMgr.getFileStructure()).build();
    }


    @GET
    @Path("{name}/files/{file}")
    public Response getFile(@PathParam("name") String document, @PathParam("file") String file) throws IOException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        InputStream fileIS = documentMgr.getFileOfDocument(fileDecoded);
        if(fileIS!=null){
            return Response.ok(fileIS).type("text/plain").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    @PUT
    @Path("{name}/mainfile")
    @Produces("text/plain")
    public void updateMainFile(String file, @PathParam("name") String document) throws IOException, InterruptedException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.setAsMainFile(file);
    }


    @PUT
    @Path("{name}/files/{file}")
    @Produces("text/plain")
    public String updateFile(String content, @PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.updateContentOfFile(fileDecoded, content);
        //Copy the file to its external folder (we don't want to block the git repo for the whole build process)
        documentMgr.copyToBuildDirectory();
        //build the document
        LaTeXBuild laTeXBuild = documents.buildDocument(documentMgr);
        return laTeXBuild.getBuildLog();
    }


    @POST
    @Path("{name}")
    public void createDocument(String mainFile, @PathParam("name") String name) throws IOException, GitAPIException, URISyntaxException {
        documents.createNewDocument(name, mainFile);
    }

    @PUT
    @Path("{name}")
    public void pushDocument(@Context HttpServletRequest request, @PathParam("name") String document, String message) throws IOException, GitAPIException, URISyntaxException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.pushDocument(request.getRemoteUser(), message);
    }


    @POST
    @Path("{name}/files/{file}")
    public void createFile(@Context HttpServletRequest request, @PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.createFileForDocument(fileDecoded);
    }


    @POST
    @Path("{name}/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Context HttpServletRequest request, @PathParam("name") String document) throws IOException, URISyntaxException, ServletException, FileUploadException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        List<ch.olischmid.codola.rest.models.File.FileMeta> fileInfo = new ArrayList<>();
        if(ServletFileUpload.isMultipartContent(request)){
            ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> fileItems = fileUpload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                String name = fileItem.getName();
                long size = fileItem.getSize();
                documentMgr.addFileForDocument(name, fileItem.getInputStream());
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
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.removeDocument();
        return Response.ok().build();
    }

    @DELETE
    @Path("{name}/files/{file}")
    public Response deleteFile(@PathParam("name") String document, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.removeFileFromDocument(fileDecoded);
        return Response.ok().build();
    }

}
