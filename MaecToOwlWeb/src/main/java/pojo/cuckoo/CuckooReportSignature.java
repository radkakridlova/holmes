package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
type TasksReportSignature struct {
	Severity    int    `json:"severity"`
	Description string `json:"description"`
	Name        string `json:"name"`
}
*/
public class CuckooReportSignature {
    
    private int severity;
    private String description;
    private String name;
    
    public CuckooReportSignature() {
        
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CuckooReportSignature{" + "severity=" + severity + ", description=" + description + ", name=" + name + '}';
    }
}
