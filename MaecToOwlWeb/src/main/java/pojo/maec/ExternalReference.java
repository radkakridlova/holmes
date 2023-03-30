package pojo.maec;

/**
 *
 * @author Tibor Galko
 */
public class ExternalReference {
    public ExternalReference() {
        
    }
    
    private String source_name;
    private String description;
    private String url;
    private String external_id;

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    @Override
    public String toString() {
        return "ExternalReference{" + "source_name=" + source_name + ", description=" + description + ", url=" + url + ", external_id=" + external_id + '}';
    }
}
