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

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oli on 07.03.15.
 */
public class FileUtils {

    public List<FileStructure> getFileStructure(Document document){
        FileStructure structure = getFileStructure(document.getDirectory().toFile(), document.getPathToMainFile());
        return structure.getSubelements();
    }

    public FileStructure getFileStructure(File file, java.nio.file.Path  mainFile){
        return buildFileStructRec(file, mainFile);
    }

    private FileStructure buildFileStructRec(File f, java.nio.file.Path  mainFile){
        if(f.isDirectory()){
            List<FileStructure> fileStructures = new ArrayList<>();
            for(File subfile : f.listFiles()){
                //exclude hidden files
                if(!subfile.getName().startsWith(".")){
                    fileStructures.add(buildFileStructRec(subfile, mainFile));
                }
            }
            Collections.sort(fileStructures, FileStructure.SORTBYTYPEANDNAME);
            return new FileStructure(f.getName(), null, true, false, fileStructures);
        }
        else{
            boolean isMainFile = mainFile==null ? false : f.toPath().toAbsolutePath().equals(mainFile.toAbsolutePath());
            return new FileStructure(f.getName(), null, false, isMainFile, null);
        }
    }
}
