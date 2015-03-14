package ch.olischmid.codola.latex.boundary;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.latex.entity.LaTeXBuild;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Created by oli on 18.01.15.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LaTeXBuilder {

    @Inject
    LaTeX latex;

    public List<LaTeXBuild> getLaTeXBuilds() throws IOException {
       return latex.getLaTeXBuilds();
    }

    public List<String> getAdditionalCTANPackages() throws IOException {
        return latex.getAdditionalCTANPackages();
    }

    public LaTeXBuild buildDocument(UUID uuid, String document) throws IOException, InterruptedException {
        return latex.build(uuid.toString(), document);
    }

    public LaTeXBuild buildDocument(Document document) throws IOException, InterruptedException, GitAPIException {
        return latex.build(document);
    }

    public void removeBuild(Document document){

    }

    public Path getPDF(String name) throws IOException {
        return latex.getPDF(name);
    }

    public Path getPathForDocument(String name) throws IOException {
        return latex.getPathForDocument(name);
    }
}
