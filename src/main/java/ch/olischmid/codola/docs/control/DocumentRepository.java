package ch.olischmid.codola.docs.control;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.DocumentInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by oli on 15.03.15.
 */
public class DocumentRepository {

    @Inject
    GIT git;

    @Inject
    Configuration configuration;

    @Inject
    DocumentUtils documentUtils;

    /**
     * @return a list of documents available on the default repository.
     * This method is not synchronized, since this information can be extracted from the git metadata and therefore doesn't have to actually switch branches.
     */
    public List<DocumentInfo> getDefaultDocuments() throws GitAPIException, IOException {
        Git defaultGitRepo = git.getGitRepo(DocumentType.DEFAULT_REPOSITORY);
        List<Ref> branches = defaultGitRepo.branchList().call();
        List<DocumentInfo> documents = new ArrayList<>();
        for (Ref branch : branches) {
            String simpleBranchName = documentUtils.getSimpleBranchName(branch);
            if (!simpleBranchName.equals(GIT.MASTER_BRANCH_NAME)) {
                Iterable<RevCommit> commits = defaultGitRepo.log().add(branch.getObjectId()).call();
                Iterator<RevCommit> commitIterator = commits.iterator();
                while (commitIterator.hasNext()) {
                    RevCommit next = commitIterator.next();
                    System.out.println(next.getName() + " " + " " + next.getFullMessage() + " " + next.getAuthorIdent().getName());
                }
                BranchInfo branchInfo = new BranchInfo(simpleBranchName);
                documents.add(new DocumentInfo(simpleBranchName, DocumentType.DEFAULT_REPOSITORY, branchInfo, Arrays.asList(branchInfo), false));
            }
        }
        return documents;
    }


    public List<DocumentInfo> getDedicatedDocuments() throws IOException, GitAPIException {
        File[] files = configuration.getAbsoluteGitDirectory().toFile().listFiles();
        List<DocumentInfo> documents = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                switch (file.getName()) {
                    case DocumentType.DEFAULT_REPOSITORY:
                    case DocumentType.UPLOADS_REPOSITORY:
                    case GIT.TEMPLATE_REPOSITORY:
                        break;
                    default:
                        String simpleBranchName = documentUtils.getSimpleBranchName(git.getGitRepo(file.getName()).getRepository().getFullBranch());
                        documents.add(new DocumentInfo(file.getName(), file.getName(), new BranchInfo(simpleBranchName), getAllBranches(file.getName()), true));
                        break;
                }
            }
        }
        return documents;
    }

    public List<BranchInfo> getAllBranches(String repository) throws IOException, GitAPIException {
        List<BranchInfo> branches = new ArrayList<>();
        for (Ref ref : git.getBranches(repository)) {
            branches.add(new BranchInfo(documentUtils.getSimpleBranchName(ref)));
        }
        return branches;
    }

    public List<DocumentInfo> getUploadedDocuments() throws IOException {
        Path path = getPathForUploadedDocuments();
        List<DocumentInfo> documents = new ArrayList<>();
        if (Files.exists(path)) {
            File[] files = path.toFile().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    documents.add(new DocumentInfo(file.getName(), DocumentType.UPLOADS_REPOSITORY, null, Collections.<BranchInfo>emptyList(), true));
                }
            }
        }
        return documents;
    }

    private Path getPathForUploadedDocuments() throws IOException {
        return git.getPath(DocumentType.UPLOADS_REPOSITORY);
    }

    public String createNewDocumentFromZIP(InputStream inputStream) throws IOException {
        UUID uuid = UUID.randomUUID();
        Path targetPath = getPathForUploadedDocuments().resolve(uuid.toString());
        Files.createDirectories(targetPath);
        try (ZipInputStream zip = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path file = targetPath.resolve(entry.getName());
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                    try (FileOutputStream fout = new FileOutputStream(file.toFile())) {
                        for (int c = zip.read(); c != -1; c = zip.read()) {
                            fout.write(c);
                        }
                        zip.closeEntry();
                        fout.close();
                    }
                }
            }
            zip.close();
        }
        return uuid.toString();
    }


    /**
     * Initializes a new document - in fact, a new branch is created (on base of the current master branch) which is immediately pushed to the remote repo.
     */
    public void createNewDefaultDocument(String document) throws IOException, GitAPIException, URISyntaxException {
        synchronized (git.getGitLock(document)) {
            Git repo = git.getGitRepo(DocumentType.DEFAULT_REPOSITORY);
            if (repo != null) {
                repo.checkout().setName(GIT.MASTER_BRANCH_NAME).call();
                repo.pull().call();
                repo.branchCreate().setName(document).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
                StoredConfig config = repo.getRepository().getConfig();
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "remote", GIT.ORIGIN);
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, document, "merge", "refs/heads/" + document);
                config.save();
                git.pushToOrigin(DocumentType.DEFAULT_REPOSITORY, document);
            }
        }
    }
}
