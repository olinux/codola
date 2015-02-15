package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.ApplicationSetup;
import ch.olischmid.codola.config.SSHKey;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by oli on 18.01.15.
 */
@Path("app")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    ApplicationSetup applicationSetup;
    @Inject
    SSHKey sshKey;

    @PUT
    public void updateApplication() throws IOException, InterruptedException {
        applicationSetup.setup();
    }

    @GET
    @Path("publicKey")
    public Response getPublicKey() throws IOException {
        return Response.ok((Object) sshKey.getPublicSSHKeyPath().toFile()).type("text/plain").build();
    }

}
