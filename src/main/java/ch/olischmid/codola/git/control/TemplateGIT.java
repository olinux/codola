package ch.olischmid.codola.git.control;

import ch.olischmid.codola.app.control.Configuration;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by oli on 06.03.15.
 */
public class TemplateGIT extends AbstractGIT{

    public final String TEMPLATE_SUBFOLDER="templates";

    @Inject
    Configuration configuration;

    public Path getPath() throws IOException {
        return configuration.getAbsoluteGitDirectory().resolve(TEMPLATE_SUBFOLDER);
    }


}
