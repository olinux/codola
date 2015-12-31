
package ch.olischmid.codola.process.handler;

import ch.olischmid.codola.process.Task;
import ch.olischmid.codola.process.data.DocumentOrder;

public class ApplycssHandler implements TaskHandler {
            @Override
            public void handleTask(DocumentOrder documentOrder) {
                System.out.println("Handle task applycss");
            }

}
        