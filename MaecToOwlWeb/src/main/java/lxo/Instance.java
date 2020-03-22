package lxo;

public class Instance {

    private String ID;
    private String instanceHash;
    private String InstanceObjectRef;

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getInstanceHash() {
        return instanceHash;
    }

    public void setInstanceHash(String instanceHash) {
        this.instanceHash = instanceHash;
    }

    public String getInstanceObjectRef() {
        return InstanceObjectRef;
    }

    public void setInstanceObjectRef(String instanceObjectRef) {
        InstanceObjectRef = instanceObjectRef;
    }

    public String getID() {
        return ID;
    }

    public Instance(String ID) {
        this.ID = ID;
    }


    @Override
    public String toString() {
        return "MALLWARE INSTANCE:\n" + getID() + "\n";
    }
}

