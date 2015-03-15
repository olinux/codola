package ch.olischmid.codola.docs.control;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.docs.entity.DocumentInfo;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
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

    /**
     * @return a list of documents available on the default repository.
     * This method is not synchronized, since this information can be extracted from the git metadata and therefore doesn't have to actually switch branches.
     */
    public List<DocumentInfo> getDefaultDocuments() throws GitAPIException, IOException {
        Git defaultGitRepo = git.getGitRepo(DocumentType.DEFAULT_REPOSITORY);
        List<Ref> branches = defaultGitRepo.branchList().call();
        List<DocumentInfo> documents = new ArrayList<>();
        for (Ref branch : branches) {
            String simpleBranchName = getSimpleBranchName(branch);
            if (!simpleBranchName.equals(GIT.MASTER_BRANCH_NAME)) {
                Iterable<RevCommit> commits = defaultGitRepo.log().add(branch.getObjectId()).call();
                Iterator<RevCommit> commitIterator = commits.iterator();
                while (commitIterator.hasNext()) {
                    RevCommit next = commitIterator.next();
                    System.out.println(next.getName() + " " + " " + next.getFullMessage() + " " + next.getAuthorIdent().getName());
                }
                documents.add(new DocumentInfo(simpleBranchName, branch.getName(), DocumentType.DEFAULT_REPOSITORY, false));
            }
        }
        return documents;
    }


    public List<DocumentInfo> getDedicatedDocuments() throws IOException {
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
                        //FIXME - It doesn't need to be the master branch
                        documents.add(new DocumentInfo(file.getName(), GIT.MASTER_BRANCH_NAME, file.getName(), true));
                        break;
                }
            }
        }
        return documents;
    }

    public List<DocumentInfo> getUploadedDocuments() throws IOException {
        Path path = getPathForUploadedDocuments();
        List<DocumentInfo> documents = new ArrayList<>();
        if (Files.exists(path)) {
            File[] files = path.toFile().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    documents.add(new DocumentInfo(file.getName(), null, DocumentType.UPLOADS_REPOSITORY, true));
                }
            }
        }
        return documents;
    }


    private Path getPathForUploadedDocuments() throws IOException {
        return git.getPath(DocumentType.UPLOADS_REPOSITORY);
    }


    /**
     * @return the actual branch name (excluding prefixes such as "ref", "head", etc)
     */
    private String getSimpleBranchName(Ref branch) {
        return branch.getName().substring(branch.getName().lastIndexOf('/') + 1);
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
