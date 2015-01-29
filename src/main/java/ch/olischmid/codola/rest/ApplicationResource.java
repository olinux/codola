package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.ApplicationSetup;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by oli on 18.01.15.
 */
@Path("app")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    @Inject
    ApplicationSetup applicationSetup;

    @PUT
    public void updateApplication() throws IOException, InterruptedException {
        applicationSetup.setup();
    }


}
