package ch.olischmid.codola.latex.boundary;

import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.latex.entity.LaTeXBuild;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
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

    public LaTeXBuild buildFromZip(InputStream zipFile, String document) throws IOException, InterruptedException {
        UUID uuid = latex.extractZipFile(zipFile);
        return latex.build(uuid, document);
    }

    public Path getPDFByUUID(UUID uuid) throws IOException {
        return latex.getPDF(uuid);
    }
}
