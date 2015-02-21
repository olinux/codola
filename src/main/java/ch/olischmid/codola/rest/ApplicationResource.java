package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.ApplicationSetup;
import ch.olischmid.codola.config.SSHKey;
import ch.olischmid.codola.git.control.Git;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.rest.models.InstallationState;
import ch.olischmid.codola.rest.models.InstallationStep;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by oli on 18.01.15.
 */
@Path("app")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {

    public static final String CLONETEMPLATEGIT ="cloneTemplateGIT";
    public static final String LATEX ="latex";
    public static final String SSH_KEY ="sshKey";

    @Inject
    ApplicationSetup applicationSetup;
    @Inject
    SSHKey sshKey;
    @Inject
    Git git;
    @Inject
    LaTeX latex;

    @PUT
    public void updateApplication() throws IOException, InterruptedException {
        git.pullAllGitRepos();
        latex.updateCTANPackages();
    }

    @GET
    @Path("publicKey")
    public Response getPublicKey() throws IOException {
        return Response.ok((Object) sshKey.getPublicSSHKeyPath().toFile()).type("text/plain").build();
    }

    @GET
    @Path("installed")
    public InstallationState getInstallationState() throws IOException {
        InstallationStep ssh = new InstallationStep(SSH_KEY, "SSH Key", sshKey.isInstalled());
        InstallationStep cloneGitTemplate = new InstallationStep(CLONETEMPLATEGIT, "Clone Template-GIT", git.isInstalled());
        InstallationStep cloneGitDefault = new InstallationStep(CLONETEMPLATEGIT, "Clone Default-GIT", false);
        InstallationStep l = new InstallationStep(LATEX, "Latex", latex.isInstalled());
        return new InstallationState(applicationSetup.isInstalled(), Arrays.asList(ssh, cloneGitTemplate, cloneGitDefault, l));
    }

    @POST
    @Path(CLONETEMPLATEGIT)
    public InstallationState cloneTemplateGIT() throws IOException, InterruptedException {
        git.install();
        updateApplication();
        return getInstallationState();
    }

    @POST
    @Path(LATEX)
    public InstallationState installLatex() throws IOException, InterruptedException {
        latex.install();
        updateApplication();
        return getInstallationState();
    }

    @POST
    @Path(SSH_KEY)
    public InstallationState createNewSSHKey() throws IOException, InterruptedException {
        sshKey.install();
        updateApplication();
        return getInstallationState();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("sshKeyFiles")
    public InstallationState uploadSSHKeys(@Context HttpServletRequest request) throws IOException, InterruptedException, ServletException {
        for (Part part : request.getParts()) {
            System.out.println(part.getSubmittedFileName());
        };
        return getInstallationState();
    }

    @GET
    @Path("firebaseUrl")
    public Response getFirebaseURL(){
        return Response.ok("https://intense-heat-7510.firebaseio.com/firepads/").type("text/plain").build();
    }
}
