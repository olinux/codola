package ch.olischmid.codola.config;

import ch.olischmid.codola.git.control.Git;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.utils.ShellUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Initializes the setup
 */
@Singleton
@Startup
public class ApplicationSetup {

    private final static String INSTALLMARKER = "installed.txt";

    @Inject
    LaTeX latex;

    @Inject
    Git git;

    @Inject
    SSHKey sshKey;

    @Inject
    ShellUtils shell;

    @Inject
    Configuration configuration;

    @PostConstruct
    public void setup() throws IOException, InterruptedException {
        if(shell.touch(INSTALLMARKER)) {
            git.install();
            latex.install();
            sshKey.install();
        }
        git.pullAllGitRepos();
        latex.updateCTANPackages();
    }



}
