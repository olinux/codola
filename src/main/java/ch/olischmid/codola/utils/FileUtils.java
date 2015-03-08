package ch.olischmid.codola.utils;

import ch.olischmid.codola.docs.entity.Document;
import ch.olischmid.codola.rest.models.FileStructure;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oli on 07.03.15.
 */
public class FileUtils {

    public List<FileStructure> getFileStructure(Document document){
        FileStructure structure = buildFileStructRec(document.getDirectory().toFile(), document.getPathToMainFile());
        return structure.getSubelements();
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
            return new FileStructure(f.getName(), null, true, Files.isSymbolicLink(f.toPath()), false, fileStructures);
        }
        else{
            boolean isMainFile = mainFile==null ? false : f.toPath().toAbsolutePath().equals(mainFile.toAbsolutePath());
            return new FileStructure(f.getName(), null, false, false, isMainFile, null);
        }
    }
}
