package ch.olischmid.codola.docs.entity;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by oli on 22.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class DocumentInfo {

    public DocumentInfo(String displayName, String gitRepository, BranchInfo currentBranch, List<BranchInfo> allBranches, boolean dedicated){
        this.displayName = displayName;
        this.gitRepository = gitRepository;
        this.dedicated = dedicated;
        this.allBranches = allBranches;
        this.currentBranch = currentBranch;
    }

    final String displayName;
    final BranchInfo currentBranch;
    final List<BranchInfo> allBranches;
    /*
     * The url of the dedicated GIT-repository or null if default
     */
    final String gitRepository;
    final boolean dedicated;
}
