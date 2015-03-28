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

package ch.olischmid.codola.templates.boundary;

import ch.olischmid.codola.git.control.GIT;
import ch.olischmid.codola.latex.control.LaTeX;
import ch.olischmid.codola.rest.models.FileStructure;
import ch.olischmid.codola.utils.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by oli on 21.02.15.
 */
public class Templates {
    @Inject
    GIT git;

    @Inject
    FileUtils fileUtils;

    public List<String> getAdditionalCTANPackages() throws IOException {
        Path packageList = git.getPath(GIT.TEMPLATE_REPOSITORY).resolve(LaTeX.ADDITIONAL_CTAN_PACKAGES);
        if (Files.exists(packageList)) {
            return Files.readAllLines(packageList, StandardCharsets.UTF_8);
        }
        return Collections.emptyList();
    }

    public List<FileStructure> getFiles() throws IOException {
        List<FileStructure> subelements = fileUtils.getFileStructure(git.getPath(GIT.TEMPLATE_REPOSITORY).toFile(), null).getSubelements();
        Iterator<FileStructure> iterator = subelements.iterator();
        //REMOVE ADDITIONAL_CTAN_PACKAGES from list
        while(iterator.hasNext()){
            if(LaTeX.ADDITIONAL_CTAN_PACKAGES.equals(iterator.next().getName())){
                iterator.remove();
            }
        }
        return subelements;
    }

    public InputStream getFile(String path) throws IOException, GitAPIException {
        return Files.newInputStream(git.getPath(GIT.TEMPLATE_REPOSITORY).resolve(path), StandardOpenOption.READ);
    }

}
