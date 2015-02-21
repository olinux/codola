package ch.olischmid.codola.rest.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by oli on 15.02.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@AllArgsConstructor
public class InstallationStep {

    private final String postMethod;
    private final String name;
    private final boolean installed;
    private final Map<String, String> parameters = new LinkedHashMap<>();


}
