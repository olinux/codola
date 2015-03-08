package ch.olischmid.codola.git.control;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by oli on 06.03.15.
 */
public abstract class AbstractGIT {

    private static final String TEMPORARY_CODOLA_COMMIT = "CoDoLa change";

    private static final String GIT_CONFIG_DIRECTORY=".git";
    protected static final String MASTER_BRANCH_NAME = "master";
    protected final static String ORIGIN = "origin";
    private Git gitRepo;


    /**
     * The locking object which ensures the synchronized access to the git repo if an actual checkout is required.
     */
    protected Object gitLock = new Object();

    public void install(String remoteRepo) throws IOException, GitAPIException {
        if(!isInstalled()) {
            Path path = getPath();
            System.out.println("Cloning from " + remoteRepo + " to " + path.toString());
            Git templateRepo = Git.cloneRepository()
                    .setURI(remoteRepo)
                    .setDirectory(path.toFile())
                    .call();
            templateRepo.pull().call();
        }
    }

    public void pull() throws IOException, GitAPIException{
        synchronized (gitLock) {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(getGitPath().toFile()).readEnvironment().findGitDir().build();
            Git git = new Git(repository);
            git.pull().call();
        }
    }

    public Git getGitRepo() throws IOException, GitAPIException {
        if(gitRepo==null) {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(getGitPath().toFile()).readEnvironment().findGitDir().build();
            gitRepo = new Git(repository);
        }
        return gitRepo;
    }

    public Path getGitPath() throws IOException {
        return getPath().resolve(GIT_CONFIG_DIRECTORY);
    }


    public boolean isInstalled() throws IOException {
        return Files.exists(getPath());
    }

    /**
     * Checks out the given branch. Important: Make sure, that the invoking context obtains the @link{#gitLock} before invocation.
     */
    protected void checkoutBranch(String branchName) throws IOException, GitAPIException {
        getGitRepo().checkout().setName(branchName).call();
    }


    protected void commitAllChanges(String branchName, String user, String message) throws GitAPIException, IOException {
        checkoutBranch(branchName);
        RevCommit lastCommit = getLastCommit(branchName);
        boolean amend = lastCommit.getFullMessage().equals(TEMPORARY_CODOLA_COMMIT);
        CommitCommand commitCommand = getGitRepo().commit().setAll(true).setAmend(amend).setMessage(message == null ? TEMPORARY_CODOLA_COMMIT : message);
        if(user!=null) {
            commitCommand.setAuthor(new PersonIdent(user, user + "@codola"));
        };
        commitCommand.call();
    }

    protected RevCommit getLastCommit(String branchName) throws IOException, GitAPIException {
        Ref branch = getGitRepo().getRepository().getRef(branchName);
        Iterable<RevCommit> commits = getGitRepo().log().add(branch.getObjectId()).call();
        return commits.iterator().next();
    }

    protected void pushToOrigin(String branchName) throws IOException, GitAPIException {
        PullResult call = getGitRepo().pull().setRebase(true).call();
        if(call.isSuccessful()){
            //We were able to rebase our changes without conflicts with the origin - there's no reason why we shouldn't push it now...
            RefSpec refSpec = new RefSpec().setSource(branchName).setDestination(branchName);
            getGitRepo().push().setRemote(ORIGIN).setRefSpecs(refSpec).call();
        }
        else{
            throw new NotMergedException();
        }
    }

    protected void removeBranchOnOrigin(String branchName) throws IOException, GitAPIException {
        RefSpec refSpec = new RefSpec().setSource(null).setDestination("refs/heads/"+branchName);
        getGitRepo().push().setRemote(ORIGIN).setForce(true).setRefSpecs(refSpec).call();
    }

    public abstract Path getPath() throws IOException;
}
