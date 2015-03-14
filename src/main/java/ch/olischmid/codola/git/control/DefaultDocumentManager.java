package ch.olischmid.codola.git.control;

import ch.olischmid.codola.docs.entity.DefaultDocument;
import ch.olischmid.codola.docs.entity.Document;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;
import java.net.URISyntaxException;

public class DefaultDocumentManager extends DedicatedDocumentManager {

    /**
     * Initializes a new document - in fact, a new branch is created (on base of the current master branch) which is immediately pushed to the remote repo.
     */
    public void createNewDocument(String document) throws IOException, GitAPIException, URISyntaxException {
        synchronized (git.getGitLock(document)) {
            Git repo = git.getGitRepo(DefaultDocument.REPOSITORY);
            if (repo != null) {
                repo.checkout().setName(GIT.MASTER_BRANCH_NAME).call();
                repo.pull().call();
                repo.branchCreate().setName(document).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
                StoredConfig config = repo.getRepository().getConfig();
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "remote", GIT.ORIGIN);
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "merge", "refs/heads/" + document);
                config.save();
                git.pushToOrigin(DefaultDocument.REPOSITORY, document);
            }
        }
    }

    @Override
    public void removeDocument(Document document) throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())){
            //Ensure to leave the branch we want to delete - for simplicity just go to MASTER
            git.checkoutBranch(DefaultDocument.REPOSITORY, GIT.MASTER_BRANCH_NAME);
            git.getGitRepo(DefaultDocument.REPOSITORY).branchDelete().setBranchNames(document.getName()).setForce(true).call();
            //Remove the branch on the origin
            git.removeBranchOnOrigin(DefaultDocument.REPOSITORY, document.getName());
        }
        document.remove();
    }

}
