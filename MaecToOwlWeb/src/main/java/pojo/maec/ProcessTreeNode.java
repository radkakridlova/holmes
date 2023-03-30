package pojo.maec;

import java.util.List;

/**
 * Jeden uzol v dynamickom process_tree
 * @author Tibor Galko
 */
public class ProcessTreeNode {
    public ProcessTreeNode() {
        
    }
    
    private String ordinal_position;
    private String process_ref;
    private List<String> initiated_action_refs;
    private String parent_action_ref;

    public String getOrdinal_position() {
        return ordinal_position;
    }

    public void setOrdinal_position(String ordinal_position) {
        this.ordinal_position = ordinal_position;
    }

    public String getProcess_ref() {
        return process_ref;
    }

    public void setProcess_ref(String process_ref) {
        this.process_ref = process_ref;
    }

    public List<String> getInitiated_action_refs() {
        return initiated_action_refs;
    }

    public void setInitiated_action_refs(List<String> initiated_action_refs) {
        this.initiated_action_refs = initiated_action_refs;
    }

    public String getParent_action_ref() {
        return parent_action_ref;
    }

    public void setParent_action_ref(String parent_action_ref) {
        this.parent_action_ref = parent_action_ref;
    }

    @Override
    public String toString() {
        return "Process{" + "ordinal_position=" + ordinal_position + ", process_ref=" + process_ref + ", initiated_action_refs=" + initiated_action_refs + ", parent_action_ref=" + parent_action_ref + '}';
    }
}
