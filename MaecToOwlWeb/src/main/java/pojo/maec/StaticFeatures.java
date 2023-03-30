package pojo.maec;

import java.util.List;

/**
 *
 * @author Tibor Galko
 */
public class StaticFeatures {
    
    public StaticFeatures() {
        
    }
    
    private List<String> strings;
    private List<DevelopmentEnvironment> development_environment;
    private List<String> file_headers;
    private List<ObfuscationMethod> obfuscation_methods;
    private List<ConfigurationParameter> configuration_parameters;
    private List<String> certificates;

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public List<DevelopmentEnvironment> getDevelopment_environment() {
        return development_environment;
    }

    public void setDevelopment_environment(List<DevelopmentEnvironment> development_environment) {
        this.development_environment = development_environment;
    }

    public List<String> getFile_headers() {
        return file_headers;
    }

    public void setFile_headers(List<String> file_headers) {
        this.file_headers = file_headers;
    }

    public List<ObfuscationMethod> getObfuscation_methods() {
        return obfuscation_methods;
    }

    public void setObfuscation_methods(List<ObfuscationMethod> obfuscation_methods) {
        this.obfuscation_methods = obfuscation_methods;
    }

    public List<ConfigurationParameter> getConfiguration_parameters() {
        return configuration_parameters;
    }

    public void setConfiguration_parameters(List<ConfigurationParameter> configuration_parameters) {
        this.configuration_parameters = configuration_parameters;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = certificates;
    }

    @Override
    public String toString() {
        return "StaticFeatures{" + "strings=" + strings + ", development_environment=" + development_environment + ", file_headers=" + file_headers + ", obfuscation_methods=" + obfuscation_methods + ", configuration_parameters=" + configuration_parameters + ", certificates=" + certificates + '}';
    }
}
