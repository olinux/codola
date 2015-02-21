package ch.olischmid.codola.rest;

import ch.olischmid.codola.latex.boundary.LaTeXBuilder;
import ch.olischmid.codola.rest.models.File;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by oli on 15.02.15.
 */
@Path("file")
public class FileUploadResource {

    @Inject
    LaTeXBuilder laTeXBuilder;

  /* step 1. get a unique url */



  /* step 3. redirected to the meta info */

    @GET
    @Path("/{key}/meta")
    public Response redirect(@PathParam("key") String key) throws IOException {
      /*  BlobKey blobKey = new BlobKey(key);
        BlobInfo info = blobInfoFactory.loadBlobInfo(blobKey);

        String name = info.getFilename();
        long size = info.getSize();
        String url = "/rest/file/" + key;

        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions.Builder.withBlobKey(blobKey).crop(true).imageSize(80);
        int sizePreview = 80;
        String urlPreview = imagesService
                .getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey)
                        .crop(true).imageSize(sizePreview));
*/

        String name = "name";
        long size = 10l;
        String url = "/url";
        String urlPreview = "/previewUrl";

        File.FileMeta meta = new File.FileMeta(name, size, url, urlPreview);
        List<File.FileMeta> metas = Arrays.asList(meta);
        File.Entity entity = new File.Entity(metas);
        return Response.ok(entity, MediaType.APPLICATION_JSON).build();
    }

  /* step 4. download the file */

    @GET
    @Path("/{key}")
    public Response serve(@PathParam("key") String key, @Context HttpServletResponse response) throws IOException {
        /*BlobKey blobKey = new BlobKey(key);
        final BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
        response.setHeader("Content-Disposition", "attachment; filename=" + blobInfo.getFilename());
        BlobstoreServiceFactory.getBlobstoreService().serve(blobKey, response);*/
        return Response.ok().build();
    }

  /* step 5. delete the file */

    @DELETE
    @Path("/{key}")
    public Response delete(@PathParam("key") String key) {
       /* Status status;
        try {
            blobstoreService.delete(new BlobKey(key));
            status = Status.OK;
        } catch (BlobstoreFailureException bfe) {
            status = Status.NOT_FOUND;
        }*/
        Response.Status status = Response.Status.OK;
        return Response.status(status).build();
    }
}

