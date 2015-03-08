package ch.olischmid.codola.latex.control;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.git.control.TemplateGIT;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import ch.olischmid.codola.utils.ShellUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by oli on 29.01.15.
 */
public class LaTeX {

    private static final String INSTALL_SCRIPT = "installLatex.sh";
    private static final String INSTALL_CTAN_SCRIPT = "installCTan.sh";
    private static final String ADDITIONAL_CTAN_PACKAGES = "CTAN_packages.txt";
    private static final String TEXLIVE_PROFILE = "texlive.profile";
    private static final String LATEX_SCRIPT = "latex.sh";
    private static final String LATEX_SUBFOLDER = "latex";
    private static final String LATEX_BUILDFOLDER = "build";

    @Inject
    Configuration configuration;
    @Inject
    ShellUtils shell;
    @Inject
    TemplateGIT templateGIT;


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
            Path packageList = templateGIT.getPath().resolve(ADDITIONAL_CTAN_PACKAGES);
            if (Files.exists(packageList)) {
                shell.executeShellScript(getInstallCTanScriptPath(), LATEX_SUBFOLDER, packageList.toString());
            }
        }
    }

    public List<String> getAdditionalCTANPackages() throws IOException {
        Path packageList = templateGIT.getPath().resolve(ADDITIONAL_CTAN_PACKAGES);
        if (Files.exists(packageList)) {
            return Files.readAllLines(packageList, StandardCharsets.UTF_8);
        }
        return Collections.emptyList();
    }

    public LaTeXBuild build(String name, String document) throws IOException, InterruptedException {
        Path buildPath = getLatexBuildFolder().resolve(name);
        if (Files.exists(buildPath)) {
            Path templateRepoDirectory = buildPath.resolve(templateGIT.TEMPLATE_SUBFOLDER);
            if (!Files.exists(templateRepoDirectory)) {
                Files.createSymbolicLink(templateRepoDirectory, templateGIT.getPath());
            }
            shell.executeShellScript(getLatexShellScript(), LATEX_SUBFOLDER, buildPath.toString(), FileEndings.LATEX.appendFileEnding(document));
        }
        return getLaTeXBuild(name, document);
    }

    public LaTeXBuild build(Document document) throws IOException, InterruptedException, GitAPIException {
        Path buildPath = getPathForDocument(document.getName());
        Files.deleteIfExists(buildPath.resolve(templateGIT.TEMPLATE_SUBFOLDER));
        if (Files.exists(buildPath)) {
            FileUtils.deleteDirectory(buildPath.toFile());
        }
        Files.createDirectories(buildPath);
        document.copyToPath(buildPath);
        return build(document.getName(), document.getMainFile());
    }


    public Path getLatexBuildFolder() throws IOException {
        return configuration.getConfigurationRoot().resolve(LATEX_BUILDFOLDER);
    }


    public Path getLatexShellScript() throws IOException {
        return configuration.getConfigurationRoot().resolve(LATEX_SCRIPT);
    }

    public LaTeXBuild getLaTeXBuild(String id, String document) throws IOException {
        Path path = getLatexBuildFolder().resolve(id);
        return new LaTeXBuild(id, document, path);
    }


    public List<LaTeXBuild> getLaTeXBuilds() throws IOException {
        List<LaTeXBuild> builds = new ArrayList<>();
        File[] files = getLatexBuildFolder().toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File f : files) {
            //TODO How to find the correct document?
            builds.add(getLaTeXBuild(f.getName(), null));
        }
        return builds;
    }

    public Path getPDF(String name) throws IOException {
        File[] files = getLatexBuildFolder().resolve(name).toFile().listFiles(new FilenameFilter() {
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

    public Path getPathForDocument(String name) throws IOException {
        return getLatexBuildFolder().resolve(name);
    }

    public UUID extractZipFile(InputStream inputStream) throws IOException {
        UUID uuid = UUID.randomUUID();
        Path targetPath = getLatexBuildFolder().resolve(uuid.toString());
        Files.createDirectories(targetPath);
        try (ZipInputStream zip = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path file = targetPath.resolve(entry.getName());
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                    try (FileOutputStream fout = new FileOutputStream(file.toFile())) {
                        for (int c = zip.read(); c != -1; c = zip.read()) {
                            fout.write(c);
                        }
                        zip.closeEntry();
                        fout.close();
                    }
                }
            }
            zip.close();
        }
        return uuid;
    }

}
