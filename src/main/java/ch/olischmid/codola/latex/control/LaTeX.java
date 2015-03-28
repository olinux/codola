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

package ch.olischmid.codola.latex.control;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import ch.olischmid.codola.utils.ShellUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by oli on 29.01.15.
 */
public class LaTeX {

    private static final String INSTALL_SCRIPT = "installLatex.sh";
    private static final String INSTALL_CTAN_SCRIPT = "installCTan.sh";
    public static final String ADDITIONAL_CTAN_PACKAGES = "CTAN_packages.txt";
    private static final String TEXLIVE_PROFILE = "texlive.profile";
    private static final String LATEX_SCRIPT = "latex.sh";
    private static final String LATEX_SUBFOLDER = "latex";
    private static final String LATEX_BUILDFOLDER = "build";

    @Inject
    Configuration configuration;
    @Inject
    ShellUtils shell;
    @Inject
    GIT git;


    public void install() throws IOException, InterruptedException {
        Path installScript = shell.copyShellScriptFromClassPathToBasePath(INSTALL_SCRIPT);
        shell.copyShellScriptFromClassPathToBasePath(INSTALL_CTAN_SCRIPT);
        shell.copyShellScriptFromClassPathToBasePath(TEXLIVE_PROFILE);
        shell.executeShellScript(installScript, LATEX_SUBFOLDER);
        shell.copyShellScriptFromClassPathToBasePath(LATEX_SCRIPT);
        Files.createDirectories(getLatexBuildFolder());
    }

    public boolean isInstalled() throws IOException {
        return Files.exists(getLatexBuildFolder());
    }

    private Path getInstallCTanScriptPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(INSTALL_CTAN_SCRIPT);
    }

    public void updateCTANPackages() throws IOException, InterruptedException {
        if (isInstalled()) {
            Path packageList = git.getPath(GIT.TEMPLATE_REPOSITORY).resolve(ADDITIONAL_CTAN_PACKAGES);
            if (Files.exists(packageList)) {
                shell.executeShellScript(getInstallCTanScriptPath(), LATEX_SUBFOLDER, packageList.toString());
            }
        }
    }

    public LaTeXBuild build(String document, String mainFile, String branch) throws IOException, InterruptedException {
        Path buildPath = getPathForDocument(document, branch);
        String buildLog= null;
        if (Files.exists(buildPath)) {
            Path templateRepoDirectory = buildPath.resolve(GIT.TEMPLATE_REPOSITORY);
            if (!Files.exists(templateRepoDirectory)) {
                Files.createSymbolicLink(templateRepoDirectory, git.getPath(GIT.TEMPLATE_REPOSITORY));
            }
            buildLog = shell.executeShellScript(getLatexShellScript(), LATEX_SUBFOLDER, buildPath.toString(), FileEndings.LATEX.appendFileEnding(mainFile));
        }
        return getLaTeXBuild(document, branch, mainFile, buildLog);
    }

    public LaTeXBuild build(DocumentManager docMgr) throws IOException, InterruptedException, GitAPIException {
        Path buildPath = docMgr.getDocument().getBuildDirectory();
        Files.deleteIfExists(buildPath.resolve(GIT.TEMPLATE_REPOSITORY));
        if (Files.exists(buildPath)) {
            FileUtils.deleteDirectory(buildPath.toFile());
        }
        Files.createDirectories(buildPath);
        docMgr.copyToBuildDirectory();
        return build(docMgr.getDocument().getName(), docMgr.getDocument().getMainFile(), docMgr.getDocument().getBranch());
    }


    public Path getLatexBuildFolder() throws IOException {
        return configuration.getConfigurationRoot().resolve(LATEX_BUILDFOLDER);
    }


    public Path getLatexShellScript() throws IOException {
        return configuration.getConfigurationRoot().resolve(LATEX_SCRIPT);
    }

    public LaTeXBuild getLaTeXBuild(String document, String branch, String mainFile, String buildLog) throws IOException {
        return new LaTeXBuild(document, mainFile, buildLog, getPathForDocument(document, branch));
    }

    public Path getPDF(String document, String branch) throws IOException {
        File[] files = getLatexBuildFolder().resolve(document).resolve(branch).toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(FileEndings.PDF.ending);
            }
        });
        if (files != null && files.length > 0) {
            return Paths.get(files[0].getAbsolutePath());
        }
        return null;
    }

    public Path getPathForDocument(String document, String branch) throws IOException {
        return getLatexBuildFolder().resolve(document).resolve(branch);
    }

}
