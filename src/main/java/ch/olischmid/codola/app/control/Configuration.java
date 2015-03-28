/*
 * Copyright © 2015 The CoDoLa developer team
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
        if (configurationRoot == null) {
            configurationRoot = initializeConfigurationRoot();
        }
        return configurationRoot;
    }

    public Path getAbsoluteGitDirectory() throws IOException {
        return getConfigurationRoot().resolve(RELATIVE_GIT_DIRECTORY);
    }


    Path initializeConfigurationRoot() throws IOException {
        Path root = Paths.get(System.getProperty(CODOLA_RESOURCEPATH, DEFAULT_CODOLA_RESOURCEPATH));
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        return root;
    }

}
