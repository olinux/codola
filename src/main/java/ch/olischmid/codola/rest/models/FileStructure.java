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

package ch.olischmid.codola.rest.models;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jgit.lib.ObjectId;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Comparator;
import java.util.List;

/**
 * Created by oli on 07.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class FileStructure {

    final String name;
    @XmlTransient
    final ObjectId objectId;
    final boolean directory;
    final boolean mainFile;
    final List<FileStructure> subelements;
    @Setter
    boolean symlink;
    @Setter
    boolean readonly;

    public FileStructure(String name, ObjectId objectId, boolean directory, boolean mainFile, List<FileStructure> subelements) {
        this.name = name;
        this.objectId = objectId;
        this.directory = directory;
        this.mainFile = mainFile;
        this.subelements = subelements;
    }


    public FileStructure getSubElement(String name){
        for(FileStructure subelement : subelements){
            if(subelement.getName().equals(name)){
                return subelement;
            }
        }
        return null;
    }

    public static final Comparator<FileStructure> SORTBYTYPEANDNAME  = new Comparator<FileStructure>(){

        @Override
        public int compare(FileStructure f1, FileStructure f2) {
            if(f1==null){
                return f2==null ? 0 : 1;
            }
            //Symlinks on top
            if(f1.symlink && !f2.symlink){
                return -1;
            }
            else if(f2.symlink && !f1.symlink){
                return 1;
            }
            //Directories on top
            else if(f1.isDirectory() && !f2.isDirectory()){
                return -1;
            }
            else if(f2.isDirectory() && !f1.isDirectory()){
                return 1;
            }
            //Otherwise by name
            else {
                return f1.getName().compareTo(f2.getName());
            }
        }
    };
}
