package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
Report definovany v holmes totem dynamic

type TasksReport struct {
	Info       *TasksReportInfo        `json:"info"`
	Signatures []*TasksReportSignature `json;"signatures"`
	Behavior   *TasksReportBehavior    `json:"behavior"`
}

type TasksReportInfo struct {
	Started float64         `json:"started"`
	Ended   float64         `json:"ended"`
	Id      int             `json:"id"`
	Machine json.RawMessage `json:"machine"` //can be TasksReportInfoMachine OR string
}

type TasksReportInfoMachine struct {
	Name string `json:"name"`
}

type TasksReportSignature struct {
	Severity    int    `json:"severity"`
	Description string `json:"description"`
	Name        string `json:"name"`
}

type TasksReportBehavior struct {
	Processes []*TasksReportBhvPcs   `json:"processes"`
	Summary   *TasksReportBhvSummary `json:"summary"`
}

type TasksReportBhvPcs struct {
	Name      string                   `json:"process_name"`
	Id        int                      `json:"process_id"`
	ParentId  int                      `json:"parent_id"`
	FirstSeen float64                  `json:"first_seen"`
	Calls     []*TasksReportBhvPcsCall `json:"calls"`
}

type TasksReportBhvPcsCall struct {
	Category  string          `json:"category"`
	Status    int             `json:"status"`
	Return    string          `json:"return"`
	Timestamp string          `json:"timestamp"`
	ThreadId  string          `json:"thread_id"`
	Repeated  int             `json:"repeated"`
	Api       string          `json:"api"`
	Arguments json.RawMessage `json:"arguments"`
	Id        int             `json:"id"`
}

type TasksReportBhvPcsCallArg struct {
	Name  string `json:"name"`
	Value string `json:"value"`
}

type TasksReportBhvSummary struct {
	Files   []string `json:"files"`
	Keys    []string `json:"keys"`
	Mutexes []string `json:"mutexes"`
}
*/
public class CuckooReport {
    
    private CuckooReportInfo info;
    private CuckooReportSignature[] signatures;
    private CuckooReportBehavior behavior;
    
    public CuckooReport() {
        
    }

    public CuckooReportInfo getInfo() {
        return info;
    }

    public void setInfo(CuckooReportInfo info) {
        this.info = info;
    }

    public CuckooReportSignature[] getSignatures() {
        return signatures;
    }

    public void setSignatures(CuckooReportSignature[] signatures) {
        this.signatures = signatures;
    }

    public CuckooReportBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CuckooReportBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public String toString() {
        return "CuckooReport{" + "info=" + info + ", signatures=" + signatures + ", behavior=" + behavior + '}';
    }
}
