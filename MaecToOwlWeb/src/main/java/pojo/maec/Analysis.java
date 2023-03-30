package pojo.maec;

import java.util.List;

/**
 * Podla https://maecproject.github.io/releases/5.0/MAEC_Core_Specification.pdf
 * @author Tibor Galko
 */
public class Analysis {
    
    public Analysis() {
        
    }
    
    private boolean is_automated;
    private String analysis_type;
    private String description;
    private List<String> tool_refs;

    public boolean isIs_automated() {
        return is_automated;
    }

    public void setIs_automated(boolean is_automated) {
        this.is_automated = is_automated;
    }

    public String getAnalysis_type() {
        return analysis_type;
    }

    public void setAnalysis_type(String analysis_type) {
        this.analysis_type = analysis_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTool_refs() {
        return tool_refs;
    }

    public void setTool_refs(List<String> tool_refs) {
        this.tool_refs = tool_refs;
    }

    @Override
    public String toString() {
        return "\nAnalysis{" + "is_automated=" + is_automated + ", analysis_type=" + analysis_type + ", description=" + description + ", tool_refs=" + tool_refs + '}';
    }
}
