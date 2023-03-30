package pojo.maec;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Tibor Galko
 */
public class Hashes {
    public Hashes() {
        
    }
    
    private String MD5;
    
    @SerializedName("SHA-1")
    private String SHA1;
    
    @SerializedName("SHA-256")
    private String SHA256;
    
    @SerializedName("SHA-512")
    private String SHA512;

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public String getSHA1() {
        return SHA1;
    }

    public void setSHA1(String SHA1) {
        this.SHA1 = SHA1;
    }

    public String getSHA256() {
        return SHA256;
    }

    public void setSHA256(String SHA256) {
        this.SHA256 = SHA256;
    }

    public String getSHA512() {
        return SHA512;
    }

    public void setSHA512(String SHA512) {
        this.SHA512 = SHA512;
    }

    @Override
    public String toString() {
        return "\nHashes{" + "MD5=" + MD5 + ", SHA1=" + SHA1 + ", SHA256=" + SHA256 + ", SHA512=" + SHA512 + '}';
    }
}
