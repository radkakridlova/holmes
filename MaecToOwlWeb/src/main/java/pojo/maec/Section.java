/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo.maec;

import java.util.List;
import lxo.HashUtil;

/**
 *
 * @author h
 */
public class Section {

    public Section() {

    }

    private String name;
    private long size;
    private double entropy;
    private List<String> hashes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }

    public List<String> getHashes() {
        return hashes;
    }

    public void setHashes(List<String> hashes) {
        this.hashes = hashes;
    }

    // Toto je identifikator pouzity v ontologii
    public String getOntologyId() {
        String S = "";

        if (hashes != null && hashes.size() > 0) {
            S = S + hashes.get(hashes.size() - 1);
            return S;
        }

        // TODO je toto dostatocne unikatne ?
        S = S + size + name + entropy;
        S = S.replaceAll("null", "");

        String hash = HashUtil.getHashableString(S);
        if (hash != null) {
            return hash;
        }

        return "NULL";
    }

    @Override
    public String toString() {
        return "\nSection{" + "name=" + name + ", size=" + size + ", entropy=" + entropy + ", hashes=" + hashes + '}';
    }
}
