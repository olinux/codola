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

import ch.olischmid.codola.docs.boundary.Documents;
import ch.olischmid.codola.docs.entity.DocumentInfo;
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.git.control.GIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("documents/{repository}")
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryResource {

    @PathParam("repository")
    String repository;

    @Inject
    Documents documents;

    @Inject
    GIT git;

    /**
     * Register the given GIT repository in this CoDoLa instance
     */
    @POST
    public void registerRepository(String remoteRepo) throws IOException, GitAPIException {
        git.install(remoteRepo, repository);
    }

    /**
     * @return the documents registered in this repository.
     */
    @GET
    public List<DocumentInfo> getDocuments() throws IOException, GitAPIException {
        return documents.getDocuments(DocumentType.getTypeByRepository(repository));
    }
}
