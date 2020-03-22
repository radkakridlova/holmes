package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
type TasksReportInfo struct {
	Started float64         `json:"started"`
	Ended   float64         `json:"ended"`
	Id      int             `json:"id"`
	Machine json.RawMessage `json:"machine"` //can be TasksReportInfoMachine OR string
}
*/
public class CuckooReportInfo {
    
    private float started;
    private float ended;
    private int id;
    private CuckooReportMachine machine;
    
    public CuckooReportInfo() {
        
    }

    public float getStarted() {
        return started;
    }

    public void setStarted(float started) {
        this.started = started;
    }

    public float getEnded() {
        return ended;
    }

    public void setEnded(float ended) {
        this.ended = ended;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CuckooReportMachine getMachine() {
        return machine;
    }

    public void setMachine(CuckooReportMachine machine) {
        this.machine = machine;
    }

    @Override
    public String toString() {
        return "CuckooReportInfo{" + "started=" + started + ", ended=" + ended + ", id=" + id + ", machine=" + machine + '}';
    }
}
