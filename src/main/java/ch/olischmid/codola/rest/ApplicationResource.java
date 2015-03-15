package ch.olischmid.codola.rest;

import ch.olischmid.codola.config.SSHKey;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.rest.models.InstallationState;
import ch.olischmid.codola.rest.models.InstallationStep;
import org.eclipse.jgit.api.errors.GitAPIException;

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
    public static final String CLONEDEFAULTGIT ="cloneDefaultGIT";
    public static final String LATEX ="latex";
    public static final String SSH_KEY ="sshKey";

    @Inject
    SSHKey sshKey;

    @Inject
    GIT git;

    @Inject
    LaTeX latex;

    @PUT
    public void updateApplication() throws IOException, InterruptedException, GitAPIException {
        git.getGitRepo(GIT.TEMPLATE_REPOSITORY);
        git.getGitRepo(DocumentType.DEFAULT_REPOSITORY);
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
        InstallationStep cloneGitTemplate = new InstallationStep(CLONETEMPLATEGIT, "Clone Template-GIT", git.isInstalled(GIT.TEMPLATE_REPOSITORY));
        InstallationStep cloneGitDefault = new InstallationStep(CLONEDEFAULTGIT, "Clone Default-GIT", git.isInstalled(DocumentType.DEFAULT_REPOSITORY));
        InstallationStep l = new InstallationStep(LATEX, "Latex", latex.isInstalled());
        return new InstallationState(Arrays.asList(ssh, cloneGitTemplate, cloneGitDefault, l));
    }

    @POST
    @Path(CLONETEMPLATEGIT)
    public InstallationState cloneTemplateGIT() throws IOException, InterruptedException, GitAPIException {
        //TODO make configurable
        git.install("https://github.com/olinux/codola_resources.git", GIT.TEMPLATE_REPOSITORY);
        return getInstallationState();
    }

    @POST
    @Path(CLONEDEFAULTGIT)
    public InstallationState cloneDefaultGIT() throws IOException, InterruptedException, GitAPIException {
        //TODO make configurable
        git.install("file:///home/oli/projects/codola/tmp/defaultrepo.git", DocumentType.DEFAULT_REPOSITORY);
        return getInstallationState();
    }

    @POST
    @Path(LATEX)
    public InstallationState installLatex() throws IOException, InterruptedException, GitAPIException {
        latex.install();
        updateApplication();
        return getInstallationState();
    }

    @POST
    @Path(SSH_KEY)
    public InstallationState createNewSSHKey() throws IOException, InterruptedException, GitAPIException {
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
        //TODO make configurable
        return Response.ok("https://intense-heat-7510.firebaseio.com/firepads/").type("text/plain").build();
    }
}
