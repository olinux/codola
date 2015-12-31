package ch.olischmid.codola.process.handler;

import ch.olischmid.codola.process.ProcessShare;
import ch.olischmid.codola.process.Task;
import ch.olischmid.codola.process.data.DocumentOrder;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.inject.Inject;

public class TaskDispatchingHandler implements WorkItemHandler {

    @Inject
    @ProcessShare
    DocumentOrder documentOrder;

    @Inject
    HandlerFactory handlerFactory;


    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        Task task = Task.valueOf(workItem.getName().toUpperCase());
        TaskHandler taskHandler = handlerFactory.getHandler(task);
        taskHandler.handleTask(documentOrder);
        workItemManager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {

    }
}




