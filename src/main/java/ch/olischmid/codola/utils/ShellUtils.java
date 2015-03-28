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

package ch.olischmid.codola.utils;

import ch.olischmid.codola.app.control.Configuration;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by oli on 29.01.15.
 */
public class ShellUtils {


    @Inject
    Configuration configuration;


    public String executeShellScript(Path shellScript, String... args) throws IOException, InterruptedException {
        String[] arg;
        if(args!=null){
            arg=new String[args.length+1];
            arg[0] = shellScript.toFile().getAbsolutePath();
            for(int i=0; i<args.length; i++){
                arg[i+1]=args[i];
            }
        }
        else{
            arg = new String[1];
            arg[0]=shellScript.toFile().getAbsolutePath();
        }
        ProcessBuilder processBuilder = new ProcessBuilder(arg);
        Process process = processBuilder.start();
        StringWriter outputStream = new StringWriter();
        IOUtils.copy(process.getInputStream(), outputStream);
        processBuilder.start().waitFor();
        System.out.print(outputStream.toString());
        return outputStream.toString();
    }


    /**
     * Copies the shell script lying in classpath under "shell/${resource}" to the configured root directory and adds the executable flag.
     *
     * @return the path the file is copied to.
     */
    public Path copyShellScriptFromClassPathToBasePath(String resource) throws IOException {
        Path targetPath = configuration.getConfigurationRoot().resolve(resource);
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("shell"+ File.separator+resource)) {
            Files.copy(is, targetPath);
            targetPath.toFile().setExecutable(true);
        }
        return targetPath;
    }

    public boolean touch(String fileName) throws IOException {
        Path path = configuration.getConfigurationRoot().resolve(fileName);
        if(Files.notExists(path)){
            Files.createFile(path);
            return true;
        }
        return false;
    }
}
