package pojo.maec;

import java.util.List;

/**
 *
 * @author Tibor Galko
 */
public class CapabilityAttribute {
    public CapabilityAttribute() {
        
    }
    
    private List<String> persistence_scope;
    private String technique;

    public List<String> getPersistence_scope() {
        return persistence_scope;
    }

    public void setPersistence_scope(List<String> persistence_scope) {
        this.persistence_scope = persistence_scope;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    @Override
    public String toString() {
        return "CapabilityAttribute{" + "persistence_scope=" + persistence_scope + ", technique=" + technique + '}';
    }
}
