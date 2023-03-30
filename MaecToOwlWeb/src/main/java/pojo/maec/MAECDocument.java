package pojo.maec;

import java.util.List;
import java.util.Map;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 * @author Tibor Galko
 */
public class MAECDocument {
    public MAECDocument() {
        
    }
    
    private String id;
    private String type;
    private String schema_version;
    private List<MAECObject> maec_objects;
    private Map<String, ObservableObject> observable_objects;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSchema_version() {
        return schema_version;
    }

    public void setSchema_version(String schema_version) {
        this.schema_version = schema_version;
    }

    public List<MAECObject> getMaec_objects() {
        return maec_objects;
    }

    public void setMaec_objects(List<MAECObject> maec_objects) {
        this.maec_objects = maec_objects;
    }

    public Map<String, ObservableObject> getObservable_objects() {
        return observable_objects;
    }

    public void setObservable_objects(Map<String, ObservableObject> observable_objects) {
        this.observable_objects = observable_objects;
    }
    
    // This is only for basic validation. If some object is bad it should be caught later
    public boolean isValid() {
        if (this.maec_objects == null || this.maec_objects.isEmpty()) {
            return false;
        }
        if (this.observable_objects == null || this.observable_objects.isEmpty()) {
            return false;
        }
        return !(this.schema_version == null || this.type == null || this.id == null);
    }

    @Override
    public String toString() {
        return "MAECDocument{" + "id=" + id + ", type=" + type + ", schema_version=" + schema_version + ", maec_objects=" + maec_objects + ", observable_objects=" + observable_objects + '}';
    }
}
