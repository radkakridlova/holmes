package pojo.maec;

/**
 *
 * @author Tibor Galko
 */
public class ObfuscationMethod {
    public ObfuscationMethod() {
        
    }
    
    private String method;
    private int layer_order;
    private String packer_name;
    private String encryption_algorithm;
    private String packer_version;
    private String packer_signature;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getLayer_order() {
        return layer_order;
    }

    public void setLayer_order(int layer_order) {
        this.layer_order = layer_order;
    }

    public String getPacker_name() {
        return packer_name;
    }

    public void setPacker_name(String packer_name) {
        this.packer_name = packer_name;
    }

    public String getEncryption_algorithm() {
        return encryption_algorithm;
    }

    public void setEncryption_algorithm(String encryption_algorithm) {
        this.encryption_algorithm = encryption_algorithm;
    }

    public String getPacker_version() {
        return packer_version;
    }

    public void setPacker_version(String packer_version) {
        this.packer_version = packer_version;
    }

    public String getPacker_signature() {
        return packer_signature;
    }

    public void setPacker_signature(String packer_signature) {
        this.packer_signature = packer_signature;
    }

    @Override
    public String toString() {
        return "ObfuscationMethod{" + "method=" + method + ", layer_order=" + layer_order + ", packer_name=" + packer_name + ", encryption_algorithm=" + encryption_algorithm + ", packer_version=" + packer_version + ", packer_signature=" + packer_signature + '}';
    }
}
