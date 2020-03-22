package lxo;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Map;
import java.util.TreeMap;

public class Executer {

    private Map<OWLNamedIndividual,Integer> actions = new TreeMap<>();
    private String ID;

    public Map<OWLNamedIndividual, Integer> getActions() {
        return actions;
    }

    public void setActions(Map<OWLNamedIndividual, Integer> actions) {
        this.actions = actions;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
