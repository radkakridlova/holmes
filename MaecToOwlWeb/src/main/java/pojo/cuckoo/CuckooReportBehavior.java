package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
type TasksReportBehavior struct {
	Processes []*TasksReportBhvPcs   `json:"processes"`
	Summary   *TasksReportBhvSummary `json:"summary"`
}
*/
public class CuckooReportBehavior {
    
    private CuckooReportBhvPcs[] processes;
    private CuckooReportBhvSummary summary;
    
    public CuckooReportBehavior() {
        
    }

    public CuckooReportBhvPcs[] getProcesses() {
        return processes;
    }

    public void setProcesses(CuckooReportBhvPcs[] processes) {
        this.processes = processes;
    }

    public CuckooReportBhvSummary getSummary() {
        return summary;
    }

    public void setSummary(CuckooReportBhvSummary summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "CuckooReportBehavior{" + "processes=" + processes + ", summary=" + summary + '}';
    }
}
