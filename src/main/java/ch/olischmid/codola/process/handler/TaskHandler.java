package ch.olischmid.codola.process.handler;

import ch.olischmid.codola.process.Task;
import ch.olischmid.codola.process.data.DocumentOrder;

/**
 * Created by oli on 28.05.15.
 */
public interface TaskHandler {

    public void handleTask(DocumentOrder documentOrder);
}
