package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.Configuration;
import ch.olischmid.codola.latex.boundary.LaTeXBuilder;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * Created by oli on 18.01.15.
 */
@Path("latex")
@Produces(MediaType.APPLICATION_JSON)
public class LatexResource {

    @Inject
    Configuration configuration;

    @Inject
    LaTeXBuilder latex;


    @GET
    @Path("ctans")
    public List<String> packages() throws IOException {
        return latex.getAdditionalCTANPackages();
    }
}
