package ch.olischmid.codola.git.control;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.docs.entity.GitDocument;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * There is only one GIT repository - we therefore need to ensure a correct locking.
 */

@Singleton
public class DefaultGIT extends AbstractGIT {

    @Inject
    Configuration configuration;

    @Inject
    FileUtils fileUtils;

    @Override
    public Path getPath() throws IOException {
        return configuration.getAbsoluteGitDirectory().resolve("default");
    }


    /**
     * @return a list of documents available on the default repository.
     * This method is not synchronized, since this information can be extracted from the git metadata and therefore doesn't have to actually switch branches.
     */
    public List<GitDocument> getDocuments() throws GitAPIException, IOException {
        Git defaultGitRepo = getGitRepo();
        List<Ref> branches = defaultGitRepo.branchList().call();
        List<GitDocument> documents = new ArrayList<>();
        for (Ref branch : branches) {
            String simpleBranchName = getSimpleBranchName(branch);
            if (!simpleBranchName.equals(MASTER_BRANCH_NAME)) {
                Iterable<RevCommit> commits = defaultGitRepo.log().add(branch.getObjectId()).call();
                Iterator<RevCommit> commitIterator = commits.iterator();
                while (commitIterator.hasNext()) {
                    RevCommit next = commitIterator.next();
                    System.out.println(next.getName() + " " + " " + next.getFullMessage() + " " + next.getAuthorIdent().getName());
                }
                documents.add(new GitDocument(simpleBranchName, branch.getName(), null));
            }
        }
        return documents;
    }


    /**
     * Initializes a new document - in fact, a new branch is created (on base of the current master branch) which is immediately pushed to the remote repo.
     */
    public void createNewDocument(String document) throws IOException, GitAPIException, URISyntaxException {
        synchronized (gitLock) {
            Git git = getGitRepo();
            if (git != null) {
                git.checkout().setName(MASTER_BRANCH_NAME).call();
                git.pull().call();
                git.branchCreate().setName(document).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
                StoredConfig config = git.getRepository().getConfig();
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "remote", ORIGIN);
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "merge", "refs/heads/" + document);
                config.save();
                pushToOrigin(document);
            }
        }
    }

    public void removeDocument(Document document) throws IOException, GitAPIException {
        synchronized (gitLock){
            //Ensure to leave the branch we want to delete - for simplicity just go to MASTER
            checkoutBranch(MASTER_BRANCH_NAME);
            getGitRepo().branchDelete().setBranchNames(document.getName()).setForce(true).call();
            //Remove the branch on the origin
            removeBranchOnOrigin(document.getName());
        }
    }

    public void removeFileFromDocument(Document document, String fileName) throws IOException, GitAPIException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            getGitRepo().rm().addFilepattern(fileName).call();
            commitAllChanges(document.getName(), null, null);
        }
    }



    public void pushDocument(Document document, String user, String message) throws GitAPIException, IOException {
        synchronized (gitLock) {
            checkoutBranch(document.getName());
            commitAllChanges(document.getName(), user, message);
            pushToOrigin(document.getName());
        }
    }


    public void updateContentOfFile(Document document, String path, String content) throws GitAPIException, IOException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            document.writeContentOfFile(path, content);
            commitAllChanges(document.getName(), null, null);
        }
    }

    public void createFileForDocument(Document document, String fileName) throws IOException, GitAPIException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            document.createNewFile(fileName);
            getGitRepo().add().addFilepattern(fileName).call();
            commitAllChanges(document.getName(), null, null);
        }
    }

    public void addFileForDocument(Document document, String fileName, InputStream file) throws GitAPIException, IOException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            document.addFile(fileName, file);
            getGitRepo().add().addFilepattern(fileName).call();
            commitAllChanges(document.getName(), null, null);
        }
    }


    public void externalizeDocument(Document document, Path targetPath) throws IOException, GitAPIException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            document.copyToPath(targetPath);
        }
    }

    /**
     * @return the actual branch name (excluding prefixes such as "ref", "head", etc)
     */
    private String getSimpleBranchName(Ref branch) {
        return branch.getName().substring(branch.getName().lastIndexOf('/') + 1);
    }

    public InputStream getFileOfDocument(Document document, String path) throws IOException, GitAPIException {
        RevCommit lastCommit = this.getLastCommit(document.getName());
        RevTree tree = lastCommit.getTree();
        TreeWalk walk = new TreeWalk(getGitRepo().getRepository());
        walk.addTree(tree);
        walk.setRecursive(true);
        walk.setFilter(PathFilter.create(path));
        ObjectId objectId = walk.next() ? walk.getObjectId(0) : null;
        return objectId!=null ? getGitRepo().getRepository().open(objectId).openStream() : null;
    }

    //this could potentially be done even without checking out the branch and therefore without locking - but it's much more comfortable with asking the file system. Anyways: If this represents a bottleneck, read the file structure from the git meta data.
    public List<FileStructure> getFileStructure(Document document) throws IOException, GitAPIException {
        synchronized (gitLock){
            checkoutBranch(document.getName());
            return fileUtils.getFileStructure(document);
        }
    }

}
