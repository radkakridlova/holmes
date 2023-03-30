package pojo.maec;

import java.util.List;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 *
 * Aktualne su implementovane iba typy malware-instance a malware-action
 *
 * @author Tibor Galko
 */
public class MAECObject {

    public MAECObject() {

    }

    // Vseobecne vlastnosti
    private String id;
    private String type;
    private String name;
    private String description;

    // Vlastnosti malware-instance
    // STIX ObservableObject id instancie
    private List<String> instance_object_refs;
    private List<Capability> capabilities;
    private DynamicFeatures dynamic_features;
    private StaticFeatures static_features;
    private List<Analysis> analysis_metadata;
    private List<SignatureMetadata> triggered_signatures;

    // Action properties
    private boolean is_successful;
    private List<String> input_object_refs;
    private List<String> output_object_refs;
    private String api_call;
    private String actionHash;

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

    public List<String> getInstance_object_refs() {
        return instance_object_refs;
    }

    public void setInstance_object_refs(List<String> instance_object_refs) {
        this.instance_object_refs = instance_object_refs;
    }

    public StaticFeatures getStatic_features() {
        return static_features;
    }

    public void setStatic_features(StaticFeatures static_features) {
        this.static_features = static_features;
    }

    public List<Analysis> getAnalysis_metadata() {
        return analysis_metadata;
    }

    public void setAnalysis_metadata(List<Analysis> analysis_metadata) {
        this.analysis_metadata = analysis_metadata;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public DynamicFeatures getDynamic_features() {
        return dynamic_features;
    }

    public void setDynamic_features(DynamicFeatures dynamic_features) {
        this.dynamic_features = dynamic_features;
    }

    public List<SignatureMetadata> getTriggered_signatures() {
        return triggered_signatures;
    }

    public void setTriggered_signatures(List<SignatureMetadata> triggered_signatures) {
        this.triggered_signatures = triggered_signatures;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOutput_object_refs() {
        return output_object_refs;
    }

    public void setOutput_object_refs(List<String> output_object_refs) {
        this.output_object_refs = output_object_refs;
    }

    public List<String> getInput_object_refs() {
        return input_object_refs;
    }

    public void setInput_object_refs(List<String> input_object_refs) {
        this.input_object_refs = input_object_refs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApi_call() {
        return api_call;
    }

    public void setApi_call(String api_call) {
        this.api_call = api_call;
    }

    public String getActionHash() {
        return actionHash;
    }

    public void setActionHash(String actionHash) {
        this.actionHash = actionHash;
    }

    public boolean isIs_successful() {
        return is_successful;
    }

    public void setIs_successful(boolean is_successful) {
        this.is_successful = is_successful;
    }

    @Override
    public String toString() {
        return "MAECObject{" + "id=" + id + ", type=" + type + ", name=" + name + ", description=" + description + ", instance_object_refs=" + instance_object_refs + ", capabilities=" + capabilities + ", dynamic_features=" + dynamic_features + ", static_features=" + static_features + ", analysis_metadata=" + analysis_metadata + ", triggered_signatures=" + triggered_signatures + ", is_successful=" + is_successful + ", input_object_refs=" + input_object_refs + ", output_object_refs=" + output_object_refs + ", api_call=" + api_call + ", actionHash=" + actionHash + '}';
    }
}
