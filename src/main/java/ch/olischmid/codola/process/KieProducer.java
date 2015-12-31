package ch.olischmid.codola.process;

import ch.olischmid.codola.process.data.DeliveryType;
import ch.olischmid.codola.process.handler.TaskDispatchingHandler;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.utils.KieHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * Created by oli on 28.05.15.
 */
public class KieProducer {

        @Produces
        @Singleton
        public KieBase produceKieBase(){
                return new KieHelper().addResource(ResourceFactory.newClassPathResource("process/documentbuilder.bpmn2")).build();
        }

        @Produces
        public KieSession produceKieSession(KieBase kieBase, TaskDispatchingHandler taskDispatchingHandler){
                KieSession kieSession = kieBase.newKieSession();
                for (Task task : Task.values()) {
                        kieSession.getWorkItemManager().registerWorkItemHandler(task.getId(), taskDispatchingHandler);
                }
                return kieSession;
        }

        @Produces
        public DeliveryType produceDeliveryType(){
                return DeliveryType.DEPLOY;
        }

}