package ch.olischmid.codola.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by oli on 29.01.15.
 */

public class Configuration {

    public final static String CODOLA_RESOURCEPATH = "codola.resourcePath";
    public final static String DEFAULT_CODOLA_RESOURCEPATH = "/tmp/codola/resources";
    public static final String TEMPLATE_REPO_DIRECTORY = "templates";

    public Path configurationRoot;

    public Path getConfigurationRoot() throws IOException {
        if(configurationRoot==null){
            configurationRoot = initializeConfigurationRoot();
        }
        return configurationRoot;
    }


    Path initializeConfigurationRoot() throws IOException {
        Path root = Paths.get(System.getProperty(CODOLA_RESOURCEPATH, DEFAULT_CODOLA_RESOURCEPATH));
        if(!Files.exists(root)){
            Files.createDirectories(root);
        }
        return root;
    }

}
