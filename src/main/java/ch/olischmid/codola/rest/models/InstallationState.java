package ch.olischmid.codola.rest.models;

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
public class InstallationState {

    final List<InstallationStep> steps;
    final boolean installed;

    public InstallationState(List<InstallationStep> steps){
        this.steps = steps;
        this.installed = isInstalled();
    }

    private boolean isInstalled(){
        for(InstallationStep step : steps){
            if(!step.isInstalled()){
                return false;
            }
        }
        return true;
    }

}
