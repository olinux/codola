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

package ch.olischmid.codola.git.control;

import ch.olischmid.codola.app.control.Configuration;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GIT {
    public final static String TEMPLATE_REPOSITORY = "templates";
    private static final String TEMPORARY_CODOLA_COMMIT = "CoDoLa change";
    private static final String GIT_CONFIG_DIRECTORY = ".git";
    public static final String MASTER_BRANCH_NAME = "master";
    public final static String ORIGIN = "origin";
    private Git gitRepo;

    @Inject
    Configuration configuration;

    @Inject
    GitManager gitManager;

    public Object getGitLock(String repository) {
        return gitManager.getLockingObject(repository);
    }

    public boolean install(String remoteRepo, String repository) throws IOException, GitAPIException {
        if (!isInstalled(repository)) {
            Path path = getPath(repository);
            System.out.println("Cloning from " + remoteRepo + " to " + path.toString());
            Git repo = Git.cloneRepository()
                    .setURI(remoteRepo)
                    .setDirectory(path.toFile()).call();
            repo.pull().call();
            return true;
        }
        return false;
    }

    public Git getGitRepo(String repo) throws IOException, GitAPIException {
        if (gitRepo == null) {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(getGitPath(repo).toFile()).readEnvironment().findGitDir().build();
            gitRepo = new Git(repository);
        }
        return gitRepo;
    }

    public Path getGitPath(String repository) throws IOException {
        return getPath(repository).resolve(GIT_CONFIG_DIRECTORY);
    }


    public boolean isInstalled(String repository) throws IOException {
        return Files.exists(getPath(repository));
    }

    /**
     * Checks out the given branch. Important: Make sure, that the invoking context obtains the @link{#gitLock} before invocation.
     */
    public void checkoutBranch(String repository, String branchName) throws IOException, GitAPIException {
        Git gitRepo = getGitRepo(repository);
        List<Ref> localBranches = gitRepo.branchList().call();
        for (Ref localBranch : localBranches) {
            if(localBranch.getName().equals(getFullBranchName(branchName))){
                gitRepo.checkout().setName(branchName).call();
                return;
            }
        }
        gitRepo.checkout().setCreateBranch(true).setName(branchName).setStartPoint(ORIGIN+"/"+branchName).call();

    }

    public boolean hasUnPushedChanges(String repository, String branchName) throws IOException, GitAPIException {
        BranchTrackingStatus of = BranchTrackingStatus.of(getGitRepo(repository).getRepository(), getFullBranchName(branchName));
        //There is no remote branch - so our local branch is not pushed and therefore has unpushed changes
        if(of==null){
            return true;
        }
        int aheadCount = of.getAheadCount();
        return aheadCount > 0;
    }


    public void commitAllChanges(String repository, String branchName, String user, String message) throws GitAPIException, IOException {
        checkoutBranch(repository, branchName);
        RevCommit lastCommit = getLastCommit(repository, branchName);
        boolean amend = lastCommit.getFullMessage().equals(TEMPORARY_CODOLA_COMMIT);
        CommitCommand commitCommand = getGitRepo(repository).commit().setAll(true).setAmend(amend).setMessage(message == null ? TEMPORARY_CODOLA_COMMIT : message);
        if (user != null) {
            commitCommand.setAuthor(new PersonIdent(user, user + "@codola"));
        }
        ;
        commitCommand.call();
    }

    public RevCommit getLastCommit(String repository, String branchName) throws IOException, GitAPIException {
        Ref branch = getGitRepo(repository).getRepository().getRef(branchName);
        Iterable<RevCommit> commits = getGitRepo(repository).log().add(branch.getObjectId()).call();
        return commits.iterator().next();
    }

    public List<Ref> getBranches(String repository) throws IOException, GitAPIException {
        Collection<Ref> call = getGitRepo(repository).lsRemote().setHeads(true).call();
        List<Ref> result = new ArrayList<>();
        for (Ref ref : call) {
            result.add(ref);
        }
        return result;
    }

    public void pushToOrigin(String repository, String branchName) throws IOException, GitAPIException {
        PullResult call = getGitRepo(repository).pull().setRebase(true).call();
        if(call.isSuccessful()){
            //We were able to rebase our changes without conflicts with the origin - there's no reason why we shouldn't push it now...
            RefSpec refSpec = new RefSpec().setSource(branchName).setDestination(branchName);
            getGitRepo(repository).push().setRemote(ORIGIN).setRefSpecs(refSpec).call();
        }
        else{
            throw new NotMergedException();
        }
    }

    public void removeBranchOnOrigin(String repository, String branchName) throws IOException, GitAPIException {
        RefSpec refSpec = new RefSpec().setSource(null).setDestination(getFullBranchName(branchName));
        getGitRepo(repository).push().setRemote(ORIGIN).setForce(true).setRefSpecs(refSpec).call();
    }

    public Path getPath(String repository) throws IOException{
        return configuration.getAbsoluteGitDirectory().resolve(repository);
    }

    public String getFullBranchName(String simpleBranchName){
        return "refs/heads/"+simpleBranchName;
    }

}
