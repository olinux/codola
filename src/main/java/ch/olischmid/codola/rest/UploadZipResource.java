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
import ch.olischmid.codola.docs.entity.DocumentType;
import ch.olischmid.codola.latex.commons.FileEndings;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("upload/zip")
@Produces(MediaType.APPLICATION_JSON)
public class UploadZipResource {

    @Inject
    Documents documents;


    /**
     * curl -X POST --header "Content-Type: application/zip" --data-binary @test.zip "http://localhost:8080/codola/rest/documents/zip/main" > test.pdf
     *
     */
    @POST
    @Consumes("application/zip")
    @Produces("application/pdf")
    public Response buildPDF(InputStream zipFile) throws IOException, InterruptedException, GitAPIException {
        String newDocumentFromZIP = documents.createNewDocumentFromZIP(zipFile);
        DocumentManager documentMgr = documents.getDocumentMgr(newDocumentFromZIP, DocumentType.UPLOADS_REPOSITORY, null);
        documentMgr.copyToBuildDirectory();
        LaTeXBuild laTeXBuild = documents.buildDocument(documentMgr);
        return Response.ok(laTeXBuild.getDocument(FileEndings.PDF).toFile()).type("application/pdf").build();
    }
}
