package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.app.control.Configuration;
import ch.olischmid.codola.docs.entity.*;
import ch.olischmid.codola.git.control.DedicatedDocumentManager;
import ch.olischmid.codola.git.control.DefaultDocumentManager;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.control.LaTeX;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.enterprise.inject.New;
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
 * Created by oli on 21.02.15.
 */
public class Documents {

    @Inject
    Configuration configuration;

    @Inject
    @New
    DefaultDocumentManager defaultGIT;

    @Inject
    @New
    DedicatedDocumentManager documentGIT;

    @Inject
    LaTeX latex;

    @Inject
    GIT git;


    public Document getDocument(String name, String repository, String branch) throws GitAPIException, IOException {
        Path p = configuration.getAbsoluteGitDirectory().resolve(repository);
        Path buildDirectory = latex.getPathForDocument(name);
        switch(repository){
            case DefaultDocument.REPOSITORY:
                return new DefaultDocument(name, p, buildDirectory);
            case UploadedDocument.REPOSITORY:
                return new UploadedDocument(name, p, buildDirectory);
            default:
                return new DedicatedDocument(name, p, repository, branch, buildDirectory);
        }
    }

    public synchronized void createNewDocument(String document) throws IOException, GitAPIException, URISyntaxException {
        defaultGIT.createNewDocument(document);
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
    public List<GitDocument> getDocuments() throws GitAPIException, IOException {
        Git defaultGitRepo = git.getGitRepo(DefaultDocument.REPOSITORY);
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


    public List<GitDocument> getDedicatedGitDocuments() throws IOException {
        File[] files = configuration.getAbsoluteGitDirectory().toFile().listFiles();
        List<GitDocument> documents = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                switch (file.getName()) {
                    case DefaultDocument.REPOSITORY:
                    case UploadedDocument.REPOSITORY:
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


    public List<GitDocument> getUploadedDocuments() throws IOException {
        Path path =getPathForUploadedDocuments();
        List<GitDocument> documents = new ArrayList<>();
        if (Files.exists(path)) {
            File[] files = path.toFile().listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    documents.add(new GitDocument(file.getName(), null, UploadedDocument.REPOSITORY, true));
                }
            }
        }
        return documents;
    }


    private Path getPathForUploadedDocuments() throws IOException {
        return git.getPath(UploadedDocument.REPOSITORY);
    }


    /**
     * @return the actual branch name (excluding prefixes such as "ref", "head", etc)
     */
    private String getSimpleBranchName(Ref branch) {
        return branch.getName().substring(branch.getName().lastIndexOf('/') + 1);
    }




}
