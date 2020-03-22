package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
type TasksReportBhvSummary struct {
	Files   []string `json:"files"`
	Keys    []string `json:"keys"`
	Mutexes []string `json:"mutexes"`
}
*/
public class CuckooReportBhvSummary {
    
    private String[] files;
    private String[] keys;
    private String[] mutexes;
    
    public CuckooReportBhvSummary() {
        
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public String[] getMutexes() {
        return mutexes;
    }

    public void setMutexes(String[] mutexes) {
        this.mutexes = mutexes;
    }

    @Override
    public String toString() {
        return "CuckooReportBhvSummary{" + "files=" + files + ", keys=" + keys + ", mutexes=" + mutexes + '}';
    }
}
