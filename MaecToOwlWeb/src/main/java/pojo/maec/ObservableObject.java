package pojo.maec;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lxo.HashUtil;

/**
 * Podla
 * https://docs.oasis-open.org/cti/stix/v2.0/stix-v2.0-part4-cyber-observable-objects.pdf
 *
 * @author Tibor Galko
 */
public class ObservableObject {

    public ObservableObject() {

    }
    private boolean INPUT;
    private String id;
    private String objectHash;

    private String type;
    private String name;
    private Map<String, Extension> extensions;
    private String size;
    private String key;
    private String path;
    private String command_line;
    private String mime_type;
    private String binary_ref;
    private String version;
    private String value;
    private String vendor;
    private List<WindowsRegistryValueType> values;
    private String parent_directory_ref;
    private String parent_ref;
    private List<String> child_refs;
    private String pid;
    private String created;
    private Hashes hashes;

    public boolean isINPUT() {
        return INPUT;
    }

    public void setINPUT(boolean INPUT) {
        this.INPUT = INPUT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectHash() {
        return objectHash;
    }

    public void setObjectHash(String objectHash) {
        this.objectHash = objectHash;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Extension> extensions) {
        this.extensions = extensions;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCommand_line() {
        return command_line;
    }

    public void setCommand_line(String command_line) {
        this.command_line = command_line;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public String getBinary_ref() {
        return binary_ref;
    }

    public void setBinary_ref(String binary_ref) {
        this.binary_ref = binary_ref;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public List<WindowsRegistryValueType> getValues() {
        return values;
    }

    public void setValues(List<WindowsRegistryValueType> values) {
        this.values = values;
    }

    public String getParent_directory_ref() {
        return parent_directory_ref;
    }

    public void setParent_directory_ref(String parent_directory_ref) {
        this.parent_directory_ref = parent_directory_ref;
    }

    public String getParent_ref() {
        return parent_ref;
    }

    public void setParent_ref(String parent_ref) {
        this.parent_ref = parent_ref;
    }

    public List<String> getChild_refs() {
        return child_refs;
    }

    public void setChild_refs(List<String> child_refs) {
        this.child_refs = child_refs;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Hashes getHashes() {
        return hashes;
    }

    public void setHashes(Hashes hashes) {
        this.hashes = hashes;
    }

    // Toto je identifikator pouzity v ontologii
    public String getHashableString() {
        String S = "";

        if (hashes != null) {
            if (hashes.getMD5() != null) {
                S = S + hashes.getMD5();
                return S;
            }
            if (hashes.getSHA1() != null) {
                S = S + hashes.getSHA1();
                return S;
            }
            if (hashes.getSHA256() != null) {
                S = S + hashes.getSHA256();
                return S;
            }
            if (hashes.getSHA512() != null) {
                S = S + hashes.getSHA512();
                return S;
            }
        }

        // TODO je toto dostatocne unikatne ?
        S = S + type + name + path + command_line + key + mime_type;
        S = S.replaceAll("null", "").toLowerCase(Locale.ENGLISH);

        String hash = HashUtil.getHashableString(S);
        if (hash != null) {
            return hash;
        }

        return "NULL";
    }

    @Override
    public String toString() {
        return "ObservableObject{" + "INPUT=" + INPUT + ", id=" + id + ", objectHash=" + objectHash + ", type=" + type + ", name=" + name + ", extensions=" + extensions + ", size=" + size + ", key=" + key + ", path=" + path + ", command_line=" + command_line + ", mime_type=" + mime_type + ", binary_ref=" + binary_ref + ", version=" + version + ", value=" + value + ", vendor=" + vendor + ", values=" + values + ", parent_directory_ref=" + parent_directory_ref + ", parent_ref=" + parent_ref + ", child_refs=" + child_refs + ", pid=" + pid + ", created=" + created + ", hashes=" + hashes + '}';
    }
}
