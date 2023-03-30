package pojo.maec;

/**
 *
 * @author Tibor Galko
 */
public class SignatureMetadata {
    public SignatureMetadata() {
        
    }
    
    private String signature_type;
    private String name;
    private String description;
    private String author;
    private ExternalReference reference;
    private String severity;
    private String external_id;

    public String getSignature_type() {
        return signature_type;
    }

    public void setSignature_type(String signature_type) {
        this.signature_type = signature_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ExternalReference getReference() {
        return reference;
    }

    public void setReference(ExternalReference reference) {
        this.reference = reference;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    @Override
    public String toString() {
        return "SignatureMetadata{" + "signature_type=" + signature_type + ", name=" + name + ", description=" + description + ", author=" + author + ", reference=" + reference + ", severity=" + severity + ", external_id=" + external_id + '}';
    }
}
