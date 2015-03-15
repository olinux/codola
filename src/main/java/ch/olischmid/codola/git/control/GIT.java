package ch.olischmid.codola.git.control;

import ch.olischmid.codola.app.control.Configuration;
import org.apache.commons.io.FileUtils;
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

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GIT {
    public final static String TEMPLATE_REPOSITORY="templates";
    private static final String TEMPORARY_CODOLA_COMMIT = "CoDoLa change";
    private static final String GIT_CONFIG_DIRECTORY=".git";
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
        if(!isInstalled(repository)) {
            Path path = getPath(repository);
            System.out.println("Cloning from " + remoteRepo + " to " + path.toString());
            Git repo = Git.cloneRepository()
                    .setURI(remoteRepo)
                    .setDirectory(path.toFile())
                    .call();
            repo.pull().call();
            return true;
        }
        return false;
    }

    public void remove(String repository) throws IOException {
        Path path = getPath(repository);
        FileUtils.deleteDirectory(path.toFile());
    }


    public Git getGitRepo(String repo) throws IOException, GitAPIException {
        if(gitRepo==null) {
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
        getGitRepo(repository).checkout().setName(branchName).call();
    }


    public void commitAllChanges(String repository, String branchName, String user, String message) throws GitAPIException, IOException {
        checkoutBranch(repository, branchName);
        RevCommit lastCommit = getLastCommit(repository, branchName);
        boolean amend = lastCommit.getFullMessage().equals(TEMPORARY_CODOLA_COMMIT);
        CommitCommand commitCommand = getGitRepo(repository).commit().setAll(true).setAmend(amend).setMessage(message == null ? TEMPORARY_CODOLA_COMMIT : message);
        if(user!=null) {
            commitCommand.setAuthor(new PersonIdent(user, user + "@codola"));
        };
        commitCommand.call();
    }

    public RevCommit getLastCommit(String repository, String branchName) throws IOException, GitAPIException {
        Ref branch = getGitRepo(repository).getRepository().getRef(branchName);
        Iterable<RevCommit> commits = getGitRepo(repository).log().add(branch.getObjectId()).call();
        return commits.iterator().next();
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
        RefSpec refSpec = new RefSpec().setSource(null).setDestination("refs/heads/"+branchName);
        getGitRepo(repository).push().setRemote(ORIGIN).setForce(true).setRefSpecs(refSpec).call();
    }

    public Path getPath(String repository) throws IOException{
        return configuration.getAbsoluteGitDirectory().resolve(repository);
    }


}
