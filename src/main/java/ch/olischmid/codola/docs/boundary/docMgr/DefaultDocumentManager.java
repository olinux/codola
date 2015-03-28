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
