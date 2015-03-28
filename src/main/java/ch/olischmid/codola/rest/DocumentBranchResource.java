/*
 * Copyright © 2015 The CoDoLa developer team
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import ch.olischmid.codola.rest.models.File;
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

@Path("documents/{repository}/{document}/{branch}")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentBranchResource {

    @PathParam("repository")
    String repository;

    @PathParam("document")
    String document;

    @PathParam("branch")
    String branch;

    @Inject
    Documents documents;

    @Inject
    GIT git;


    /**
     * @return the generated pdf for the given document
     */
    @GET
    public Response getPDF() throws IOException, GitAPIException {
        java.nio.file.Path pdf = documents.getPDF(document, branch);
        if(pdf==null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(pdf.toFile()).type("application/pdf").build();
    }


    /**
     * "Saves" the document -> in the GIT context, this means, it pushes it to the origin repository. Takes a commit message in the payload.
     */
    @PUT
    public void pushDocument(@Context HttpServletRequest request, String message) throws IOException, GitAPIException, URISyntaxException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.pushDocument(request.getRemoteUser(), message);
    }


    /**
     * Defines the given file to be the "main file" of the document (generation starts from this file)
     *
     * @param file
     */
    @PUT
    @Path("mainfile")
    @Produces("text/plain")
    public void updateMainFile(String file) throws IOException, InterruptedException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.setAsMainFile(file);
    }


    /**
     * @return the files the document consists of (tex-files, images, etc.)
     */
    @GET
    @Path("files")
    public Response getFileList() throws IOException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        return Response.ok(documentMgr.getFileStructure()).build();
    }


    /**
     * Takes all the submitted / uploaded files and adds them to the document
     */
    @POST
    @Path("files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFiles(@Context HttpServletRequest request) throws IOException, URISyntaxException, ServletException, FileUploadException, GitAPIException {
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        List<File.FileMeta> fileInfo = new ArrayList<>();
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


    /**
     * @param file - the file name of a specific file in the document
     * @return the content of the file as plain text
     */
    @GET
    @Path("files/{file}")
    public Response getFile(@PathParam("file") String file) throws IOException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        InputStream fileIS = documentMgr.getFileOfDocument(fileDecoded);
        if(fileIS!=null){
            return Response.ok(fileIS).type("text/plain").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    /**
     * Updates the file with the content within the payload
     */
    @PUT
    @Path("files/{file}")
    @Produces("text/plain")
    public String updateFile(String content, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.updateContentOfFile(fileDecoded, content);
        //Copy the file to its external folder (we don't want to block the git repo for the whole build process)
        documentMgr.copyToBuildDirectory();
        //build the document
        LaTeXBuild laTeXBuild = documents.buildDocument(documentMgr);
        return laTeXBuild.getBuildLog();
    }



    /**
     * Creates a new, empty file
     */
    @POST
    @Path("files/{file}")
    public void createFile(@Context HttpServletRequest request, @PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.createFileForDocument(fileDecoded);
    }

    /**
     * removes the file from the document
     */
    @DELETE
    @Path("files/{file}")
    public Response deleteFile(@PathParam("file") String file) throws IOException, InterruptedException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branch);
        documentMgr.removeFileFromDocument(fileDecoded);
        return Response.ok().build();
    }


}
