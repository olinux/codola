package ch.olischmid.codola.docs.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by oli on 22.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class DocumentInfo {

    public DocumentInfo(String displayName, String fullBranchName, String gitRepository, boolean dedicated){
        this.displayName = displayName;
        this.fullBranchName = fullBranchName;
        this.gitRepository = gitRepository;
        this.dedicated = dedicated;
    }

    final String displayName;
    final String fullBranchName;
    /*
     * The url of the dedicated GIT-repository or null if default
     */
    final String gitRepository;
    final boolean dedicated;

    @Setter
    boolean unpushedChanges;
}
