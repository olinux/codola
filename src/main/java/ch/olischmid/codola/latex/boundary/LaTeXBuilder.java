package ch.olischmid.codola.latex.boundary;

import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.control.LaTeX;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by oli on 18.01.15.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class LaTeXBuilder {

    @Inject
    LaTeX latex;
    @Inject
    GIT git;

    public List<String> getAdditionalCTANPackages() throws IOException {
        Path packageList = git.getPath(GIT.TEMPLATE_REPOSITORY).resolve(LaTeX.ADDITIONAL_CTAN_PACKAGES);
        if (Files.exists(packageList)) {
            return Files.readAllLines(packageList, StandardCharsets.UTF_8);
        }
        return Collections.emptyList();
    }
}
