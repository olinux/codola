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

package ch.olischmid.codola.rest;

import ch.olischmid.codola.docs.boundary.DocumentManager;
import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.BranchInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;

@Path("documents/{repository}/{document}")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

    @PathParam("repository")
    String repository;

    @PathParam("document")
    String document;

    @Inject
    Documents documents;

    @Inject
    GIT git;


    @POST
    public void createDocument(String mainFile) throws IOException, GitAPIException, URISyntaxException {
        DocumentType typeByRepository = DocumentType.getTypeByRepository(repository);
        if (DocumentType.DEFAULT == typeByRepository) {
            documents.createNewDocument(document, mainFile);
        } else {
            throw new UnsupportedOperationException("New document branches can only be created on the default repository");
        }
    }


    /**
     * Removes the current document
     */
    @DELETE
    public Response deleteDocument() throws IOException, InterruptedException, GitAPIException {
        for (BranchInfo branchInfo : documents.getBranches(repository)) {
            DocumentManager documentMgr = documents.getDocumentMgr(document, repository, branchInfo.getName());
            documentMgr.removeDocument();
        }
        return Response.ok().build();
    }

}
