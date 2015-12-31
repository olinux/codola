package ch.olischmid.codola.process;

import ch.olischmid.codola.process.data.DocumentOrder;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

/**
 * Created by oli on 28.05.15.
 */
public class DocumentOrderProducer {


    @Produces
    @ProcessShare
    @RequestScoped
    public DocumentOrder produceDocumentOrder(){
        return new DocumentOrder();
    }

}
