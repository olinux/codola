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

package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by oli on 14.03.15.
 */
public interface DocumentManager {

    void setDocument(Document document);

    Document getDocument();

    void removeFileFromDocument(String fileName) throws IOException, GitAPIException;

    void pushDocument(String user, String message) throws GitAPIException, IOException;

    void updateContentOfFile(String path, String content) throws GitAPIException, IOException;

    void createFileForDocument(String fileName) throws IOException, GitAPIException;

    void addFileForDocument(String fileName, InputStream file) throws GitAPIException, IOException;

    void copyToBuildDirectory() throws IOException, GitAPIException;

    InputStream getFileOfDocument(String path) throws IOException, GitAPIException;

    List<FileStructure> getFileStructure() throws IOException, GitAPIException;

    void removeDocument() throws IOException, GitAPIException;

    void checkForUnpushedChanges(BranchInfo branch) throws IOException, GitAPIException;

    void setAsMainFile(String fileName) throws IOException, GitAPIException;

}
