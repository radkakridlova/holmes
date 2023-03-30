package pojo.maec;

import java.util.List;

/**
 *
 * @author Tibor Galko
 */
public class Extension {
    
    public Extension() {
        
    }
    
    
    private String pe_type;
    private String machine_hex;
    private int number_of_sections;
    private String time_date_stamp;
    private String pointer_to_symbol_table_hex;
    private int number_of_symbols;
    private int size_of_optional_header;
    private String characteristics_hex;
    private OptionalHeader optional_header;
    private List<Section> sections;

    public String getPe_type() {
        return pe_type;
    }

    public void setPe_type(String pe_type) {
        this.pe_type = pe_type;
    }

    public String getMachine_hex() {
        return machine_hex;
    }

    public void setMachine_hex(String machine_hex) {
        this.machine_hex = machine_hex;
    }

    public int getNumber_of_sections() {
        return number_of_sections;
    }

    public void setNumber_of_sections(int number_of_sections) {
        this.number_of_sections = number_of_sections;
    }

    public String getTime_date_stamp() {
        return time_date_stamp;
    }

    public void setTime_date_stamp(String time_date_stamp) {
        this.time_date_stamp = time_date_stamp;
    }

    public String getPointer_to_symbol_table_hex() {
        return pointer_to_symbol_table_hex;
    }

    public void setPointer_to_symbol_table_hex(String pointer_to_symbol_table_hex) {
        this.pointer_to_symbol_table_hex = pointer_to_symbol_table_hex;
    }

    public int getNumber_of_symbols() {
        return number_of_symbols;
    }

    public void setNumber_of_symbols(int number_of_symbols) {
        this.number_of_symbols = number_of_symbols;
    }

    public int getSize_of_optional_header() {
        return size_of_optional_header;
    }

    public void setSize_of_optional_header(int size_of_optional_header) {
        this.size_of_optional_header = size_of_optional_header;
    }

    public String getCharacteristics_hex() {
        return characteristics_hex;
    }

    public void setCharacteristics_hex(String characteristics_hex) {
        this.characteristics_hex = characteristics_hex;
    }

    public OptionalHeader getOptional_header() {
        return optional_header;
    }

    public void setOptional_header(OptionalHeader optional_header) {
        this.optional_header = optional_header;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        return "\nExtension{" + "pe_type=" + pe_type + ", machine_hex=" + machine_hex + ", number_of_sections=" + number_of_sections + ", time_date_stamp=" + time_date_stamp + ", pointer_to_symbol_table_hex=" + pointer_to_symbol_table_hex + ", number_of_symbols=" + number_of_symbols + ", size_of_optional_header=" + size_of_optional_header + ", characteristics_hex=" + characteristics_hex + ", optional_header=" + optional_header + ", sections=" + sections + '}';
    }
}
