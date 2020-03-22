package lxo;

public class Action {

    private String ID;
    private String Name;
    private String OutputObjectRef;
    private String InputObjectRef;
    private String actionHash;

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getActionHash() {
        return actionHash;
    }

    public void setActionHash(String actionHash) {
        this.actionHash = actionHash;
    }

    public String getOutputObjectRef() {
        return OutputObjectRef;
    }

    public void setOutputObjectRef(String outputObjectRef) {
        OutputObjectRef = outputObjectRef;
    }

    public String getInputObjectRef() {
        return InputObjectRef;
    }

    public void setInputObjectRef(String inputObjectRef) {
        InputObjectRef = inputObjectRef;
    }

    public Action(String ID) {
        this.ID = ID;
        this.Name = null;
        this.InputObjectRef = null;
        this.OutputObjectRef = null;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getID() {
        return ID;
    }


    @Override
    public String toString() {
        return "MALWARE ACTION:\n" + this.getID() + "\n" + this.getName() + "\n" + getInputObjectRef()+ "\n" + getOutputObjectRef();
    }
}
