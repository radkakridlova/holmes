package pojo.maec;

/**
 *
 * @author Tibor Galko
 */
public class WindowsRegistryValueType {
    
    public WindowsRegistryValueType() {
        
    }
    
    private String name;
    private String data;
    private String data_type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    @Override
    public String toString() {
        return "WindowsRegistryValueType{" + "name=" + name + ", data=" + data + ", data_type=" + data_type + '}';
    }
}
