package ch.olischmid.codola.config;

import ch.olischmid.codola.utils.ShellUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by oli on 15.02.15.
 */
public class SSHKey {


    private final static String KEY_FILE_NAME="codola_ssh";
    private static final String ssh_key_shell = "ssh_key.sh";

    @Inject
    Configuration configuration;
    @Inject
    ShellUtils shell;

    public void install() throws IOException, InterruptedException {
        shell.copyShellScriptFromClassPathToBasePath(ssh_key_shell);
        if(!Files.exists(getPrivateSSHKeyPath())){
            shell.executeShellScript(configuration.getConfigurationRoot().resolve(ssh_key_shell), getPrivateSSHKeyPath().toString());
        }
    }

    public Path getPrivateSSHKeyPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(KEY_FILE_NAME);
    }

    public Path getPublicSSHKeyPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(KEY_FILE_NAME+".pub");
    }

}
