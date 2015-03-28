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

package ch.olischmid.codola.docs.boundary.docMgr;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GitDocumentManager implements DocumentManager {

    @Inject
    FileUtils fileUtils;

    @Inject
    GIT git;

    @Setter
    @Getter
    Document document;


    @Override
    public void removeFileFromDocument(String fileName) throws IOException, GitAPIException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            git.getGitRepo(document.getRepository()).rm().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), document.getBranch(), null, null);
        }
    }

    @Override
    public void pushDocument(String user, String message) throws GitAPIException, IOException {
        String branch = document.getBranch();
        if (branch != null) {
            synchronized (git.getGitLock(document.getRepository())) {
                git.checkoutBranch(document.getRepository(), branch);
                git.commitAllChanges(document.getRepository(), branch, user, message);
                git.pushToOrigin(document.getRepository(), branch);
            }
        }
    }


    @Override
    public void updateContentOfFile(String path, String content) throws GitAPIException, IOException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.writeContentOfFile(path, content);
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }

    @Override
    public void createFileForDocument(String fileName) throws IOException, GitAPIException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.createNewFile(fileName);
            git.getGitRepo(document.getRepository()).add().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }

    @Override
    public void addFileForDocument(String fileName, InputStream file) throws GitAPIException, IOException {
        String branch = document.getBranch();
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), branch);
            document.addFile(fileName, file);
            git.getGitRepo(document.getRepository()).add().addFilepattern(fileName).call();
            git.commitAllChanges(document.getRepository(), branch, null, null);
        }
    }


    @Override
    public void copyToBuildDirectory() throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), document.getBranch());
            document.copyToBuildDirectory();
        }
    }


    @Override
    public InputStream getFileOfDocument(String path) throws IOException, GitAPIException {
        String branch = document.getBranch();
        RevCommit lastCommit = git.getLastCommit(document.getRepository(), branch);
        RevTree tree = lastCommit.getTree();
        TreeWalk walk = new TreeWalk(git.getGitRepo(document.getRepository()).getRepository());
        walk.addTree(tree);
        walk.setRecursive(true);
        walk.setFilter(PathFilter.create(path));
        ObjectId objectId = walk.next() ? walk.getObjectId(0) : null;
        return objectId != null ? git.getGitRepo(document.getRepository()).getRepository().open(objectId).openStream() : null;

    }

    //this could potentially be done even without checking out the branch and therefore without locking - but it's much more comfortable with asking the file system. Anyways: If this represents a bottleneck, read the file structure from the git meta data.
    @Override
    public List<FileStructure> getFileStructure() throws IOException, GitAPIException {
        String branch = document.getBranch();
        List<FileStructure> fileStructure;
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), document.getBranch());
            return fileUtils.getFileStructure(document);
        }
    }

    @Override
    public void removeDocument() throws IOException, GitAPIException {
        document.remove();
    }

    @Override
    public void checkForUnpushedChanges(BranchInfo branch) throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())) {
            branch.setUnpushedChanges(git.hasUnPushedChanges(document.getRepository(), branch.getName()));
        }
    }

    @Override
    public void setAsMainFile(String fileName) throws IOException, GitAPIException {
        synchronized (git.getGitLock(document.getRepository())) {
            git.checkoutBranch(document.getRepository(), document.getBranch());
            document.setMainFile(fileName);
            git.getGitRepo(document.getRepository()).add().addFilepattern(Document.CODOLA_PROPERTIES).call();
            git.commitAllChanges(document.getRepository(), document.getBranch(), null, null);
        }
    }

}
