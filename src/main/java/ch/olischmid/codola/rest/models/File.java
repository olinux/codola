package ch.olischmid.codola.rest.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by oli on 15.02.15.
 */
public class File {
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FileUrl {
        String url;

        public FileUrl(String url) {
            this.url = url;
        }

        public FileUrl() {
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FileMeta {
        String name;
        long size;
        String url;
        String delete_url;
        String delete_type;
        String thumbnail_url;

        public FileMeta(String filename, long size, String url, String urlPreview) {
            this.name = filename;
            this.size = size;
            this.url = url;
            this.delete_url = url;
            this.delete_type = "DELETE";
            this.thumbnail_url = urlPreview;
        }

        public FileMeta() {
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Entity {
        private List<FileMeta> files;

        public Entity(List<FileMeta> files) {
            this.files = files;
        }

        public Entity() {
        }
    }
}
