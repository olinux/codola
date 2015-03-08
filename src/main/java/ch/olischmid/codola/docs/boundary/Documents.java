package ch.olischmid.codola.docs.boundary;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.git.control.DefaultGIT;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by oli on 21.02.15.
 */
public class Documents {


    @Inject
    DefaultGIT defaultGIT;

    public Document getDefaultDocument(String name) throws GitAPIException, IOException {
        return new Document(name, defaultGIT.getPath());
    }

    public synchronized void createNewDocument(String document) throws IOException, GitAPIException, URISyntaxException {
        defaultGIT.createNewDocument(document);
    }

}
