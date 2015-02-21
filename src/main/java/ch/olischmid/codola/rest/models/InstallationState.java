package ch.olischmid.codola.rest.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by oli on 15.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@AllArgsConstructor
public class InstallationState {

    final boolean installationStarted;
    List<InstallationStep> steps;

    public final static String installTemplateGITRepoCommand="installTemplateGIT";

}
