package ch.olischmid.codola.docs.entity;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by oli on 13.03.15.
 */
public class UploadedDocument extends Document {

    public static final String REPOSITORY = "uploads";

    public UploadedDocument(String name, Path directory, Path buildDirectory) throws IOException, GitAPIException {
        super(name, directory.resolve(name), REPOSITORY, null, buildDirectory);
    }
}
