package ch.olischmid.codola.git.control;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DedicatedDocumentManager implements DocumentManager {

    @Inject
    FileUtils fileUtils;

    @Inject
    GIT git;

    @Override
    public void removeFileFromDocument(Document document, String fileName) throws IOException, GitAPIException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            git.getGitRepo(document.getRepository()).rm().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), document.getName(), null, null);
        }
    }

    @Override
    public void pushDocument(Document document, String user, String message) throws GitAPIException, IOException {
        String branch = document.getBranch();
        if (branch != null) {
            synchronized (git.getGitLock(document.getRepository())) {
                git.checkoutBranch(document.getRepository(), branch);
                git.commitAllChanges(document.getRepository(), branch, user, message);
                git.pushToOrigin(document.getRepository(), branch);
            }
        }
    }


    @Override
    public void updateContentOfFile(Document document, String path, String content) throws GitAPIException, IOException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.writeContentOfFile(path, content);
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }

    @Override
    public void createFileForDocument(Document document, String fileName) throws IOException, GitAPIException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.createNewFile(fileName);
            git.getGitRepo(document.getRepository()).add().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }

    @Override
    public void addFileForDocument(Document document, String fileName, InputStream file) throws GitAPIException, IOException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.addFile(fileName, file);
            git.getGitRepo(document.getRepository()).add().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }


    @Override
    public void copyToBuildDirectory(Document document) throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), document.getBranch());
            document.copyToBuildDirectory();
        }
    }


    @Override
    public InputStream getFileOfDocument(Document document, String path) throws IOException, GitAPIException {
        String branch = document.getBranch();
        RevCommit lastCommit = git.getLastCommit(document.getRepository(), branch);
        RevTree tree = lastCommit.getTree();
        TreeWalk walk = new TreeWalk(git.getGitRepo(document.getRepository()).getRepository());
        walk.addTree(tree);
        walk.setRecursive(true);
        walk.setFilter(PathFilter.create(path));
        ObjectId objectId = walk.next() ? walk.getObjectId(0) : null;
        return objectId != null ? git.getGitRepo(document.getRepository()).getRepository().open(objectId).openStream() : null;

    }

    //this could potentially be done even without checking out the branch and therefore without locking - but it's much more comfortable with asking the file system. Anyways: If this represents a bottleneck, read the file structure from the git meta data.
    @Override
    public List<FileStructure> getFileStructure(Document document) throws IOException, GitAPIException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), document.getBranch());
            return fileUtils.getFileStructure(document);
        }
    }

    @Override
    public void removeDocument(Document document) throws IOException, GitAPIException {
        git.remove(document.getRepository());
        document.remove();
    }

}
