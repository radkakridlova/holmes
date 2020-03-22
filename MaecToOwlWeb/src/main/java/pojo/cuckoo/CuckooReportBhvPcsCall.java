package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
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
*/
public class CuckooReportBhvPcsCall {
    
    private String category;
    private int status;
    private String return_pcs;
    private String timestamp;
    private String thread_id;
    private int repeated;
    private String api;
    private String arguments;
    private int id;
    
    public CuckooReportBhvPcsCall() {
        
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReturn_pcs() {
        return return_pcs;
    }

    public void setReturn_pcs(String return_pcs) {
        this.return_pcs = return_pcs;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getThread_id() {
        return thread_id;
    }

    public void setThread_id(String thread_id) {
        this.thread_id = thread_id;
    }

    public int getRepeated() {
        return repeated;
    }

    public void setRepeated(int repeated) {
        this.repeated = repeated;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CuckooReportBhvPcsCall{" + "category=" + category + ", status=" + status + ", return_pcs=" + return_pcs + ", timestamp=" + timestamp + ", thread_id=" + thread_id + ", repeated=" + repeated + ", api=" + api + ", arguments=" + arguments + ", id=" + id + '}';
    }
}
