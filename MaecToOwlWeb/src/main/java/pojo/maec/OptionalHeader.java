/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo.maec;

/**
 *
 * @author h
 */
public class OptionalHeader {
    public OptionalHeader() {
        
    }
    
    private String magic_hex;
    private int major_linker_version;
    private int minor_linker_version;
    private long size_of_code;
    private long size_of_initialized_data;
    private long size_of_unintialized_data;
    private long address_of_entry_point;
    private long base_of_code;
    private long base_of_data;
    private long image_base;
    private long section_alignment;
    private long file_alignment;
    private int major_os_system_version;
    private int minor_os_system_version;
    private int major_image_version;
    private int minor_image_version;
    private int major_subsystem_version;
    private int minor_subsystem_version;
    private String win32_version_value_hex;
    private long size_of_image;
    private long size_of_headers;
    private String checksum_hex;
    private String subsystem_hex;
    private String dll_characteristics_hex;
    private long size_of_stack_reserve;
    private long size_of_stack_commit;
    private long size_of_heap_reserve;
    private long size_of_heap_commit;
    private String loader_flags_hex;
    private int number_of_rva_and_sizes;

    public String getMagic_hex() {
        return magic_hex;
    }

    public void setMagic_hex(String magic_hex) {
        this.magic_hex = magic_hex;
    }

    public int getMajor_linker_version() {
        return major_linker_version;
    }

    public void setMajor_linker_version(int major_linker_version) {
        this.major_linker_version = major_linker_version;
    }

    public int getMinor_linker_version() {
        return minor_linker_version;
    }

    public void setMinor_linker_version(int minor_linker_version) {
        this.minor_linker_version = minor_linker_version;
    }

    public long getSize_of_code() {
        return size_of_code;
    }

    public void setSize_of_code(long size_of_code) {
        this.size_of_code = size_of_code;
    }

    public long getSize_of_initialized_data() {
        return size_of_initialized_data;
    }

    public void setSize_of_initialized_data(long size_of_initialized_data) {
        this.size_of_initialized_data = size_of_initialized_data;
    }

    public long getSize_of_unintialized_data() {
        return size_of_unintialized_data;
    }

    public void setSize_of_unintialized_data(long size_of_unintialized_data) {
        this.size_of_unintialized_data = size_of_unintialized_data;
    }

    public long getAddress_of_entry_point() {
        return address_of_entry_point;
    }

    public void setAddress_of_entry_point(long address_of_entry_point) {
        this.address_of_entry_point = address_of_entry_point;
    }

    public long getBase_of_code() {
        return base_of_code;
    }

    public void setBase_of_code(long base_of_code) {
        this.base_of_code = base_of_code;
    }

    public long getBase_of_data() {
        return base_of_data;
    }

    public void setBase_of_data(long base_of_data) {
        this.base_of_data = base_of_data;
    }

    public long getImage_base() {
        return image_base;
    }

    public void setImage_base(long image_base) {
        this.image_base = image_base;
    }

    public long getSection_alignment() {
        return section_alignment;
    }

    public void setSection_alignment(long section_alignment) {
        this.section_alignment = section_alignment;
    }

    public long getFile_alignment() {
        return file_alignment;
    }

    public void setFile_alignment(long file_alignment) {
        this.file_alignment = file_alignment;
    }

    public int getMajor_os_system_version() {
        return major_os_system_version;
    }

    public void setMajor_os_system_version(int major_os_system_version) {
        this.major_os_system_version = major_os_system_version;
    }

    public int getMinor_os_system_version() {
        return minor_os_system_version;
    }

    public void setMinor_os_system_version(int minor_os_system_version) {
        this.minor_os_system_version = minor_os_system_version;
    }

    public int getMajor_image_version() {
        return major_image_version;
    }

    public void setMajor_image_version(int major_image_version) {
        this.major_image_version = major_image_version;
    }

    public int getMinor_image_version() {
        return minor_image_version;
    }

    public void setMinor_image_version(int minor_image_version) {
        this.minor_image_version = minor_image_version;
    }

    public int getMajor_subsystem_version() {
        return major_subsystem_version;
    }

    public void setMajor_subsystem_version(int major_subsystem_version) {
        this.major_subsystem_version = major_subsystem_version;
    }

    public int getMinor_subsystem_version() {
        return minor_subsystem_version;
    }

    public void setMinor_subsystem_version(int minor_subsystem_version) {
        this.minor_subsystem_version = minor_subsystem_version;
    }

    public String getWin32_version_value_hex() {
        return win32_version_value_hex;
    }

    public void setWin32_version_value_hex(String win32_version_value_hex) {
        this.win32_version_value_hex = win32_version_value_hex;
    }

    public long getSize_of_image() {
        return size_of_image;
    }

    public void setSize_of_image(long size_of_image) {
        this.size_of_image = size_of_image;
    }

    public long getSize_of_headers() {
        return size_of_headers;
    }

    public void setSize_of_headers(long size_of_headers) {
        this.size_of_headers = size_of_headers;
    }

    public String getChecksum_hex() {
        return checksum_hex;
    }

    public void setChecksum_hex(String checksum_hex) {
        this.checksum_hex = checksum_hex;
    }

    public String getSubsystem_hex() {
        return subsystem_hex;
    }

    public void setSubsystem_hex(String subsystem_hex) {
        this.subsystem_hex = subsystem_hex;
    }

    public String getDll_characteristics_hex() {
        return dll_characteristics_hex;
    }

    public void setDll_characteristics_hex(String dll_characteristics_hex) {
        this.dll_characteristics_hex = dll_characteristics_hex;
    }

    public long getSize_of_stack_reserve() {
        return size_of_stack_reserve;
    }

    public void setSize_of_stack_reserve(long size_of_stack_reserve) {
        this.size_of_stack_reserve = size_of_stack_reserve;
    }

    public long getSize_of_stack_commit() {
        return size_of_stack_commit;
    }

    public void setSize_of_stack_commit(long size_of_stack_commit) {
        this.size_of_stack_commit = size_of_stack_commit;
    }

    public long getSize_of_heap_reserve() {
        return size_of_heap_reserve;
    }

    public void setSize_of_heap_reserve(long size_of_heap_reserve) {
        this.size_of_heap_reserve = size_of_heap_reserve;
    }

    public long getSize_of_heap_commit() {
        return size_of_heap_commit;
    }

    public void setSize_of_heap_commit(long size_of_heap_commit) {
        this.size_of_heap_commit = size_of_heap_commit;
    }

    public String getLoader_flags_hex() {
        return loader_flags_hex;
    }

    public void setLoader_flags_hex(String loader_flags_hex) {
        this.loader_flags_hex = loader_flags_hex;
    }

    public int getNumber_of_rva_and_sizes() {
        return number_of_rva_and_sizes;
    }

    public void setNumber_of_rva_and_sizes(int number_of_rva_and_sizes) {
        this.number_of_rva_and_sizes = number_of_rva_and_sizes;
    }

    @Override
    public String toString() {
        return "\nOptionalHeader{" + "magic_hex=" + magic_hex + ", major_linker_version=" + major_linker_version + ", minor_linker_version=" + minor_linker_version + ", size_of_code=" + size_of_code + ", size_of_initialized_data=" + size_of_initialized_data + ", size_of_unintialized_data=" + size_of_unintialized_data + ", address_of_entry_point=" + address_of_entry_point + ", base_of_code=" + base_of_code + ", base_of_data=" + base_of_data + ", image_base=" + image_base + ", section_alignment=" + section_alignment + ", file_alignment=" + file_alignment + ", major_os_system_version=" + major_os_system_version + ", minor_os_system_version=" + minor_os_system_version + ", major_image_version=" + major_image_version + ", minor_image_version=" + minor_image_version + ", major_subsystem_version=" + major_subsystem_version + ", minor_subsystem_version=" + minor_subsystem_version + ", win32_version_value_hex=" + win32_version_value_hex + ", size_of_image=" + size_of_image + ", size_of_headers=" + size_of_headers + ", checksum_hex=" + checksum_hex + ", subsystem_hex=" + subsystem_hex + ", dll_characteristics_hex=" + dll_characteristics_hex + ", size_of_stack_reserve=" + size_of_stack_reserve + ", size_of_stack_commit=" + size_of_stack_commit + ", size_of_heap_reserve=" + size_of_heap_reserve + ", size_of_heap_commit=" + size_of_heap_commit + ", loader_flags_hex=" + loader_flags_hex + ", number_of_rva_and_sizes=" + number_of_rva_and_sizes + '}';
    }
}
