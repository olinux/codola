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

import ch.olischmid.codola.process.Task;
import ch.olischmid.codola.process.ProcessShare;
import ch.olischmid.codola.process.data.DeliveryType;
import ch.olischmid.codola.process.data.DocumentOrder;
import ch.olischmid.codola.process.data.FinalFormat;
import org.kie.api.runtime.KieSession;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("process")
@Produces(MediaType.APPLICATION_JSON)
public class ProcessResource {

    @Inject
    private KieSession ksession;

//    public void startProcess() {
//        RuntimeEngine runtimeEngine = ksession.getRuntimeEngine(EmptyContext.get());
//        KieSession ksession = runtimeEngine.getKieSession();
//        ProcessInstance userTask = ksession.startProcess("UserTask");
//
//        ksession.disposeRuntimeEngine(runtimeEngine);
//    }

    @Inject
    DeliveryType deliveryType;

    @Inject
    @ProcessShare
    DocumentOrder documentOrder;

    @GET
    public String getPublicKey(@QueryParam("format") String format, @QueryParam("delivery") String delivery) throws IOException {
//        startProcess();
        documentOrder.setDeliveryType(DeliveryType.valueOf(delivery));
        documentOrder.setFinalFormat(FinalFormat.valueOf(format));
		ksession.setGlobal("document", documentOrder);
        ksession.startProcess("ch.olischmid.codola.process.builder.DocumentBuilder");
        return "Hello world "+ ksession;
    }

}