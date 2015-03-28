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

package ch.olischmid.codola.config;

import ch.olischmid.codola.app.control.Configuration;
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

    public boolean isInstalled() throws IOException {
        return Files.exists(getPrivateSSHKeyPath());
    }

    public Path getPrivateSSHKeyPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(KEY_FILE_NAME);
    }

    public Path getPublicSSHKeyPath() throws IOException {
        return configuration.getConfigurationRoot().resolve(KEY_FILE_NAME+".pub");
    }

}
