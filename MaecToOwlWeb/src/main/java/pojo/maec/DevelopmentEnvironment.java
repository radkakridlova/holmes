package pojo.maec;

import java.util.List;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 * @author Tibor Galko
 */
public class DevelopmentEnvironment {
    
    public DevelopmentEnvironment() {
        
    }
    
    private List<String> tool_refs;
    private List<String> debugging_file_refs;

    public List<String> getTool_refs() {
        return tool_refs;
    }

    public void setTool_refs(List<String> tool_refs) {
        this.tool_refs = tool_refs;
    }

    public List<String> getDebugging_file_refs() {
        return debugging_file_refs;
    }

    public void setDebugging_file_refs(List<String> debugging_file_refs) {
        this.debugging_file_refs = debugging_file_refs;
    }

    @Override
    public String toString() {
        return "DevelopmentEnvironment{" + "tool_refs=" + tool_refs + ", debugging_file_refs=" + debugging_file_refs + '}';
    }
}
