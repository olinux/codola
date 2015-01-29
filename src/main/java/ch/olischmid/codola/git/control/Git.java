package ch.olischmid.codola.git.control;

import ch.olischmid.codola.config.Configuration;
import ch.olischmid.codola.utils.ShellUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by oli on 29.01.15.
 */
public class Git {

    public static final String TEMPLATE_REPO_DIRECTORY = "templates";
    private final static String RELATIVE_GIT_DIRECTORY = "git";

    private final static String DEFAULT_GIT_REPO = "https://github.com/olinux/codola_resources.git";

    private final static String GIT_URL_PROPERTY = "codola.gitUrl";
    private static final String gitCloneShell = "gitclone.sh";

    private static final String gitPullShell = "gitpull.sh";


    @Inject
    ShellUtils shell;

    @Inject
    Configuration configuration;

    @Inject
    Logger logger;

    private String getTemplateGitRepositoryUrl() {
        return System.getProperty(GIT_URL_PROPERTY, DEFAULT_GIT_REPO);
    }

    public Path getAbsoluteGitDirectory() throws IOException {
        return configuration.getConfigurationRoot().resolve(RELATIVE_GIT_DIRECTORY);
    }

    public Path getAbsoluteTemplateDirectory() throws IOException {
        return getAbsoluteGitDirectory().resolve(TEMPLATE_REPO_DIRECTORY);
    }

    private Path getPullShellScriptPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(gitPullShell);
    }

    private Path getCloneShellScriptPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(gitCloneShell);
    }

    public void install() throws IOException, InterruptedException {
        shell.copyShellScriptFromClassPathToBasePath(gitCloneShell);
        shell.copyShellScriptFromClassPathToBasePath(gitPullShell);
        Files.createDirectories(getAbsoluteGitDirectory());
        Files.createDirectories(getAbsoluteTemplateDirectory());
        cloneTemplateRepo();
    }


    private void cloneTemplateRepo() throws IOException, InterruptedException {
        logger.fine("Cloning directory " + TEMPLATE_REPO_DIRECTORY);
        shell.executeShellScript(getCloneShellScriptPath(), getAbsoluteGitDirectory().toString(), getTemplateGitRepositoryUrl(), TEMPLATE_REPO_DIRECTORY);
    }

    public void pullAllGitRepos() throws IOException, InterruptedException {
        for (File f : getAbsoluteGitDirectory().toFile().listFiles()) {
            if (f.isDirectory()) {
                logger.fine("Pulling directory " + f.getName());
                shell.executeShellScript(getPullShellScriptPath(), getAbsoluteGitDirectory().toString(), f.getName());
            }
        }
    }

}
