package pojo.maec;

import java.util.List;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 * @author Tibor Galko
 */
public class Capability {
    public Capability() {
        
    }
    
    private String name;
    private String description;
    private String behavior_refs;
    private List<Capability> refined_capabilities;
    private CapabilityAttribute attributes;
    private List<ExternalReference> references;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBehavior_refs() {
        return behavior_refs;
    }

    public void setBehavior_refs(String behavior_refs) {
        this.behavior_refs = behavior_refs;
    }

    public List<Capability> getRefined_capabilities() {
        return refined_capabilities;
    }

    public void setRefined_capabilities(List<Capability> refined_capabilities) {
        this.refined_capabilities = refined_capabilities;
    }

    public CapabilityAttribute getAttributes() {
        return attributes;
    }

    public void setAttributes(CapabilityAttribute attributes) {
        this.attributes = attributes;
    }

    public List<ExternalReference> getReferences() {
        return references;
    }

    public void setReferences(List<ExternalReference> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return "Capability{" + "name=" + name + ", description=" + description + ", behavior_refs=" + behavior_refs + ", refined_capabilities=" + refined_capabilities + ", attributes=" + attributes + ", references=" + references + '}';
    }
}
