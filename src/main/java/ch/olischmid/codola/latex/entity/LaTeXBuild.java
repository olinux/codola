package ch.olischmid.codola.latex.entity;

import ch.olischmid.codola.latex.commons.FileEndings;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.nio.file.Path;

/**
 * Created by oli on 29.01.15.
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
public class LaTeXBuild {

    String id;

    String document;

    String buildLog;

    @XmlTransient
    Path filePath;

    public Path getDocument(FileEndings ending){
        return filePath.resolve(ending.appendFileEnding(document));
    }

}
