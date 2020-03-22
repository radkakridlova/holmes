package lxo;

import java.util.List;
import java.util.Map;

public class Observable {

    private String ID;
    private String Type;
    private boolean INPUT;
    private String Name;
    private String Size;
    private String sha512;
    private String md5;
    private String sha256;
    private String sha1;
    private String Key;
    private String Path;
    private String CommandLine;
    private String MimeType;
    private String BinaryRef;
    private String Version;
    private String Value;
    private String Vendor;
    private Map<String,String> Values;
    private String parentDirectoryRef;
    private String parrentRef;
    private List<String> childRef;
    private String objectHash;

    public String getObjectHash() {
        return objectHash;
    }

    public void setObjectHash(String objectHash) {
        this.objectHash = objectHash;
    }

    public List<String> getChildRef() {
        return childRef;
    }

    public void setChildRef(List<String> childRef) {
        this.childRef = childRef;
    }

    public String getParrentRef() {
        return parrentRef;
    }

    public void setParrentRef(String parrentRef) {
        this.parrentRef = parrentRef;
    }

    public String getParentDirectoryRef() {
        return parentDirectoryRef;
    }

    public void setParentDirectoryRef(String parentDirectoryRef) {
        this.parentDirectoryRef = parentDirectoryRef;
    }

    public Map<String, String> getValues() {
        return Values;
    }

    public void setValues(Map<String, String> values) {
        Values = values;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getVendor() {
        return Vendor;
    }

    public void setVendor(String vendor) {
        Vendor = vendor;
    }

    public String getMimeType() {
        return MimeType;
    }

    public void setMimeType(String mimeType) {
        MimeType = mimeType;
    }

    public String getBinaryRef() {
        return BinaryRef;
    }

    public void setBinaryRef(String binaryRef) {
        BinaryRef = binaryRef;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getCommandLine() {
        return CommandLine;
    }

    public void setCommandLine(String commandLine) {
        CommandLine = commandLine;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getSha512() {
        return sha512;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSize() {
        return Size;
    }

    public void setSize(String size) {
        Size = size;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Observable(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public boolean isINPUT() {
        return INPUT;
    }

    public void setINPUT(boolean INPUT) {
        this.INPUT = INPUT;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    @Override
    public String toString() {
       return "OBSERVABLE OBJECT\nID:" + getID() + "\nType:" + getType() +"\nParrent ID:" + getParrentRef();
    }
}
