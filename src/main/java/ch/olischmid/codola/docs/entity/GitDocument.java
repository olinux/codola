package ch.olischmid.codola.docs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by oli on 22.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@AllArgsConstructor
public class GitDocument {

    final String displayName;
    final String fullBranchName;
    /*
     * The url of the dedicated GIT-repository or null if default
     */
    final String gitRepository;

}
