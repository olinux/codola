package ch.olischmid.codola.app.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The overall application configuration - defines, where the most fundamental paths are located, etc.
 */

public class Configuration {

    public final static String CODOLA_RESOURCEPATH = "codola.resourcePath";
    public final static String DEFAULT_CODOLA_RESOURCEPATH = "/tmp/codola/resources";
    private final static String PROPERTYFILE = "codola.properties";
    private final static String RELATIVE_GIT_DIRECTORY = "git";

    public Path configurationRoot;

//    private Properties property;

  /*  private Properties getProperty() throws IOException {
        if(property==null){
            property = new Properties();
            if(Files.exists(getPropertyPath())) {
                try(InputStream is = new FileInputStream(getPropertyPath().toFile())) {
                    property.load(is);
                }
            }
        }
        return property;
    }*/

//    private Path getPropertyPath() throws IOException {
//        return getConfigurationRoot().resolve(PROPERTYFILE);
//    }

  /*  public void addToConfigurationFile(String key, String value) throws IOException {
        Properties property = getProperty();
        property.setProperty(key, value);
        try(Writer w = new FileWriter(getPropertyPath().toFile())) {
            property.store(w, null);
        }
    }

    public String getFromConfigurationFile(String key) throws IOException {
        return getProperty().getProperty(key);
    }*/

    public Path getConfigurationRoot() throws IOException {
        if(configurationRoot==null){
            configurationRoot = initializeConfigurationRoot();
        }
        return configurationRoot;
    }

    public Path getAbsoluteGitDirectory() throws IOException {
        return getConfigurationRoot().resolve(RELATIVE_GIT_DIRECTORY);
    }


    Path initializeConfigurationRoot() throws IOException {
        Path root = Paths.get(System.getProperty(CODOLA_RESOURCEPATH, DEFAULT_CODOLA_RESOURCEPATH));
        if(!Files.exists(root)){
            Files.createDirectories(root);
        }
        return root;
    }

}
