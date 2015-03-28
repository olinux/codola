package ch.olischmid.codola.docs.entity;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by oli on 28.03.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class BranchInfo {
    final String name;

    public BranchInfo(String name) {
        this.name = name;
    }

    @Setter
    boolean unpushedChanges;
}
