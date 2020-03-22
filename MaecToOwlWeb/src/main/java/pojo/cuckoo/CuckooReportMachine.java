package pojo.cuckoo;

/**
 *
 * @author Tibor Galko
 */

/*
"machine": {
            "status": "stopped", 
            "name": "192.168.56.1011", 
            "label": "192.168.56.1011", 
            "manager": "VirtualBox", 
            "started_on": "2020-03-10 18:21:50", 
            "shutdown_on": "2020-03-10 18:23:59"
        },
 */
public class CuckooReportMachine {
    
    private String status;
    private String name;
    private String label;
    private String manager;
    private String started_on;
    private String shutdown_on;

    public CuckooReportMachine() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStarted_on() {
        return started_on;
    }

    public void setStarted_on(String started_on) {
        this.started_on = started_on;
    }

    public String getShutdown_on() {
        return shutdown_on;
    }

    public void setShutdown_on(String shutdown_on) {
        this.shutdown_on = shutdown_on;
    }

    @Override
    public String toString() {
        return "CuckooReportMachine{" + "status=" + status + ", name=" + name + ", label=" + label + ", manager=" + manager + ", started_on=" + started_on + ", shutdown_on=" + shutdown_on + '}';
    }
}
