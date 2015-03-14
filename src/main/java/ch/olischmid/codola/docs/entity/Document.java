package ch.olischmid.codola.docs.entity;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by oli on 27.02.15.
 */
@Getter
public abstract class Document {

    public Document(String name, Path directory, String repository, String branch, Path buildDirectory) throws IOException, GitAPIException {
        this.name = name;
        this.directory = directory;
        this.repository = repository;
        this.branch = branch;
        this.buildDirectory = buildDirectory;
    }

    String mainFile = "main.tex";
    final String name;
    final Path directory;
    final String repository;
    final String branch;
    final Path buildDirectory;

    public Path getPathToMainFile(){
        return getDirectory().resolve(mainFile);
    }

    public void writeContentOfFile(String path, String content) throws IOException {
        Files.write(getDirectory().resolve(path), content.getBytes(StandardCharsets.UTF_8));
    }

    public void createNewFile(String fileName) throws IOException {
        java.nio.file.Path filePath = getDirectory();
        Path fullPath = filePath.resolve(fileName);
        Files.createDirectories(fullPath.getParent());
        Files.createFile(fullPath);
    }

    public void addFile(String fileName, InputStream file) throws IOException, GitAPIException {
        Files.copy(file, getDirectory().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public void copyToBuildDirectory() throws IOException {
        FileUtils.copyDirectory(getDirectory().toFile(), getBuildDirectory().toFile());
    }

    public void remove() throws IOException {
        FileUtils.deleteDirectory(getBuildDirectory().toFile());
    }
}
