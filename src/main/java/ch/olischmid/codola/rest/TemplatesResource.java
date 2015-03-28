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

import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.templates.boundary.Templates;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("templates")
@Produces(MediaType.APPLICATION_JSON)
public class TemplatesResource {

    @Inject
    Templates templates;

    @Inject
    GIT git;


    @GET
    @Path("ctans")
    public List<String> packages() throws IOException {
        return templates.getAdditionalCTANPackages();
    }

    @GET
    @Path("files")
    public Response getFileList() throws IOException, GitAPIException {
        return Response.ok(templates.getFiles()).build();
    }


    @GET
    @Path("files/{file}")
    public Response getFile(@PathParam("file") String file) throws IOException, GitAPIException {
        String fileDecoded = URLDecoder.decode(file, StandardCharsets.UTF_8.name());
        InputStream fileIS =templates.getFile(fileDecoded);
        if(fileIS!=null){
            return Response.ok(fileIS).type("text/plain").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
