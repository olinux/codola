package ch.olischmid.codola.docs.entity;

import ch.olischmid.codola.docs.boundary.Dedicated;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by oli on 13.03.15.
 */
@Dedicated
public class DedicatedDocument extends Document {
    public DedicatedDocument(String name, Path directory, String repository, String branch, Path buildDirectory) throws IOException, GitAPIException {
        super(name, directory, repository, branch==null ? GIT.MASTER_BRANCH_NAME : branch, buildDirectory);
    }
}
