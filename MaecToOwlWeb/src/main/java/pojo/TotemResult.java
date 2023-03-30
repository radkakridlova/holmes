package pojo;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Reprezentacia vysledkov ziskanych z Holmes systemu
 * @author Tibor Galko
 */

/*
                Filename:         req.OriginalRequest.Filename,
		Data:             string(resultsJ),
		MD5:              md5String,
		SHA1:             sha1String,
		SHA256:           sha256String,
		ServiceName:      req.Service,
		Tags:             req.OriginalRequest.Tags,
		Comment:          req.OriginalRequest.Comment,
		StartedDateTime:  req.Started,
		FinishedDateTime: time.Now(),

type Result struct {
	Filename         string    `json:"filename"`
	Data             string    `json:"data"`
	MD5              string    `json:"md5"`
	SHA1             string    `json:"sha1"`
	SHA256           string    `json:"sha256"`
	ServiceName      string    `json:"service_name"`
	Tags             []string  `json:"tags"`
	Comment          string    `json:"comment"`
	StartedDateTime  time.Time `json:"started_date_time"`
	FinishedDateTime time.Time `json:"finished_date_time"`
}
*/
@XmlRootElement(name="Result")
public class TotemResult {
    
    private String filename;
    private String data; // data musia byt citatelny string, nie marshallnuty
    private String md5;
    private String sha1;
    private String sha256;
    private String service_name;
    private String[] tags;
    private String comment;
    private String started_date_time;
    private String finished_date_time;
    
    
    public TotemResult() {
        
    }

    public String getFilename() {
        return filename;
    }

    @XmlElement
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getData() {
        return data;
    }

    @XmlElement
    public void setData(String data) {
        this.data = data;
    }

    public String getMd5() {
        return md5;
    }

    @XmlElement
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    @XmlElement
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    @XmlElement
    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getService_name() {
        return service_name;
    }

    @XmlElement
    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String[] getTags() {
        return tags;
    }

    @XmlElement
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getComment() {
        return comment;
    }

    @XmlElement
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStarted_date_time() {
        return started_date_time;
    }

    @XmlElement
    public void setStarted_date_time(String started_date_time) {
        this.started_date_time = started_date_time;
    }

    public String getFinished_date_time() {
        return finished_date_time;
    }

    @XmlElement
    public void setFinished_date_time(String finished_date_time) {
        this.finished_date_time = finished_date_time;
    }

    @Override
    public String toString() {
        return "TotemRequest{" + "filename=" + filename + ", data=" + data + ", md5=" + md5 + ", sha1=" + sha1 + ", sha256=" + sha256 + ", service_name=" + service_name + ", comment=" + comment + ", started_date_time=" + started_date_time + ", finished_date_time=" + finished_date_time + '}';
    }
}
