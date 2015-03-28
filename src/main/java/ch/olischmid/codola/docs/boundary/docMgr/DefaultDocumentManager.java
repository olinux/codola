package ch.olischmid.codola.docs.boundary.docMgr;

import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;

public class DefaultDocumentManager extends GitDocumentManager {

    @Override
    public void removeDocument() throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())){
            //Ensure to leave the branch we want to delete - for simplicity just go to MASTER
            git.checkoutBranch(DocumentType.DEFAULT_REPOSITORY, GIT.MASTER_BRANCH_NAME);
            git.getGitRepo(DocumentType.DEFAULT_REPOSITORY).branchDelete().setBranchNames(document.getName()).setForce(true).call();
            //Remove the branch on the origin
            git.removeBranchOnOrigin(DocumentType.DEFAULT_REPOSITORY, document.getName());
        }
        //First detach the template repository - otherwise it will be deleted as well
        Files.deleteIfExists(document.getBuildDirectory().resolve(GIT.TEMPLATE_REPOSITORY));
        FileUtils.deleteDirectory(document.getBuildDirectory().toFile());
    }



}
