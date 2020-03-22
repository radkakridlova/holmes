package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
type TasksReportBhvPcs struct {
	Name      string                   `json:"process_name"`
	Id        int                      `json:"process_id"`
	ParentId  int                      `json:"parent_id"`
	FirstSeen float64                  `json:"first_seen"`
	Calls     []*TasksReportBhvPcsCall `json:"calls"`
}
*/
public class CuckooReportBhvPcs {
    
    private String process_name;
    private int process_id;
    private int parent_id;
    private float first_seen;
    private CuckooReportBhvPcsCall calls;
    
    public CuckooReportBhvPcs() {
        
    }

    public String getProcess_name() {
        return process_name;
    }

    public void setProcess_name(String process_name) {
        this.process_name = process_name;
    }

    public int getProcess_id() {
        return process_id;
    }

    public void setProcess_id(int process_id) {
        this.process_id = process_id;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public float getFirst_seen() {
        return first_seen;
    }

    public void setFirst_seen(float first_seen) {
        this.first_seen = first_seen;
    }

    public CuckooReportBhvPcsCall getCalls() {
        return calls;
    }

    public void setCalls(CuckooReportBhvPcsCall calls) {
        this.calls = calls;
    }

    @Override
    public String toString() {
        return "CuckooReportBhvPcs{" + "process_name=" + process_name + ", process_id=" + process_id + ", parent_id=" + parent_id + ", first_seen=" + first_seen + ", calls=" + calls + '}';
    }
}
