package pojo.maec;

import java.util.List;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 * @author Tibor Galko
 */
public class DynamicFeatures {
    public DynamicFeatures() {
        
    }
    
    private List<ProcessTreeNode> process_tree;
    private List<String> behavior_refs;
    private List<String> action_refs;
    private List<String> network_traffic_refs;

    public List<ProcessTreeNode> getProcess_tree() {
        return process_tree;
    }

    public void setProcess_tree(List<ProcessTreeNode> process_tree) {
        this.process_tree = process_tree;
    }

    public List<String> getBehavior_refs() {
        return behavior_refs;
    }

    public void setBehavior_refs(List<String> behavior_refs) {
        this.behavior_refs = behavior_refs;
    }

    public List<String> getAction_refs() {
        return action_refs;
    }

    public void setAction_refs(List<String> action_refs) {
        this.action_refs = action_refs;
    }

    public List<String> getNetwork_traffic_refs() {
        return network_traffic_refs;
    }

    public void setNetwork_traffic_refs(List<String> network_traffic_refs) {
        this.network_traffic_refs = network_traffic_refs;
    }

    @Override
    public String toString() {
        return "DynamicFeatures{" + "process_tree=" + process_tree + ", behavior_refs=" + behavior_refs + ", action_refs=" + action_refs + ", network_traffic_refs=" + network_traffic_refs + '}';
    }
}
