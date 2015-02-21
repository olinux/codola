package ch.olischmid.codola.utils;

import ch.olischmid.codola.config.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by oli on 29.01.15.
 */
public class ShellUtils {


    @Inject
    Configuration configuration;


    public void executeShellScript(Path shellScript, String... args) throws IOException, InterruptedException {
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
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.start().waitFor();
        //TODO Exception handling
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
