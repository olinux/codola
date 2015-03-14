package ch.olischmid.codola.git.control;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by oli on 13.03.15.
 */
@Singleton
public class GitManager {

    public final Map<String, Object> LOCKERREPOSITORY = Collections.synchronizedMap(new HashMap<String, Object>());


    public Object getLockingObject(String key){
        if(key==null){
            throw new NullPointerException();
        }
        Object object = LOCKERREPOSITORY.get(key);
        if(object==null){
            //TODO validate if the repository exists
            object = new Object();
            LOCKERREPOSITORY.put(key, object);
        }
        return object;
    }

}
