package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.boundary.docMgr.DedicatedDocumentManager;
import ch.olischmid.codola.docs.boundary.docMgr.DefaultDocumentManager;
import ch.olischmid.codola.docs.boundary.docMgr.UploadedDocumentManager;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.docs.entity.GitDocument;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
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
 * Created by oli on 21.02.15.
 */
public class Documents {

    @Inject
    Configuration configuration;

    @Inject
    DefaultDocumentManager defaultDocMgr;

    @Inject
    DedicatedDocumentManager dedicatedDocMgr;

    @Inject
    UploadedDocumentManager uploadedDocMgr;

    @Inject
    LaTeX latex;


    @Inject
    GIT git;


    public DocumentManager getDocumentMgr(String name, String repository, String branch) throws GitAPIException, IOException {
        Path p = configuration.getAbsoluteGitDirectory().resolve(repository);
        Path buildDirectory = latex.getPathForDocument(name);
        Document d = Document.createDocument(name, repository, branch, p, buildDirectory);
        DocumentManager mgr = null;
        switch(d.getDocumentType()){
            case DEDICATED:
                mgr = dedicatedDocMgr;
                break;
            case DEFAULT:
                mgr = defaultDocMgr;
                break;
            case UPLOADED:
                mgr = uploadedDocMgr;
                break;
            default:
                break;
        }
        if(mgr!=null){
            mgr.setDocument(d);
        }
        return mgr;
    }


    public LaTeXBuild buildDocument(DocumentManager document) throws IOException, InterruptedException, GitAPIException {
        return latex.build(document);
    }


    public Path getPDF(String name) throws IOException {
        return latex.getPDF(name);
    }

    /**
     * Initializes a new document - in fact, a new branch is created (on base of the current master branch) which is immediately pushed to the remote repo.
     */
    public void createNewDocument(String document) throws IOException, GitAPIException, URISyntaxException {
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
     * @return a list of documents available on the default repository.
     * This method is not synchronized, since this information can be extracted from the git metadata and therefore doesn't have to actually switch branches.
     */
    private List<GitDocument> getDefaultDocuments() throws GitAPIException, IOException {
        Git defaultGitRepo = git.getGitRepo(DocumentType.DEFAULT_REPOSITORY);
        List<Ref> branches = defaultGitRepo.branchList().call();
        List<GitDocument> documents = new ArrayList<>();
        for (Ref branch : branches) {
            String simpleBranchName = getSimpleBranchName(branch);
            if (!simpleBranchName.equals(GIT.MASTER_BRANCH_NAME)) {
                Iterable<RevCommit> commits = defaultGitRepo.log().add(branch.getObjectId()).call();
                Iterator<RevCommit> commitIterator = commits.iterator();
                while (commitIterator.hasNext()) {
                    RevCommit next = commitIterator.next();
                    System.out.println(next.getName() + " " + " " + next.getFullMessage() + " " + next.getAuthorIdent().getName());
                }
                documents.add(new GitDocument(simpleBranchName, branch.getName(), null, false));
            }
        }
        return documents;
    }


    private List<GitDocument> getDedicatedGitDocuments() throws IOException {
        File[] files = configuration.getAbsoluteGitDirectory().toFile().listFiles();
        List<GitDocument> documents = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                switch (file.getName()) {
                    case DocumentType.DEFAULT_REPOSITORY:
                    case DocumentType.UPLOADS_REPOSITORY:
                    case GIT.TEMPLATE_REPOSITORY:
                        break;
                    default:
                        documents.add(new GitDocument(file.getName(), file.getName(), file.getName(), true));
                        break;
                }
            }
        }
        return documents;
    }

    public List<GitDocument> getDocuments(DocumentType type) throws IOException, GitAPIException {
        switch(type){
            case DEFAULT:
               return getDefaultDocuments();
            case UPLOADED:
                return getUploadedDocuments();
            case DEDICATED:
                return getDedicatedGitDocuments();
        }
        return Collections.emptyList();
    }


    private List<GitDocument> getUploadedDocuments() throws IOException {
        Path path =getPathForUploadedDocuments();
        List<GitDocument> documents = new ArrayList<>();
        if (Files.exists(path)) {
            File[] files = path.toFile().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    documents.add(new GitDocument(file.getName(), null, DocumentType.UPLOADS_REPOSITORY, true));
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




}
