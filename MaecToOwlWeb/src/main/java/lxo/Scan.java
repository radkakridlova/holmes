package lxo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Scan {

    private Instance instance;
    private Map<String,List<Action>> mapOfActions;
    private TreeMap<String,Observable> mapOfObjects;

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Map<String, List<Action>> getMapOfActions() {
        return mapOfActions;
    }

    public void setMapOfActions(Map<String, List<Action>> mapOfActions) {
        this.mapOfActions = mapOfActions;
    }

    public TreeMap<String, Observable> getMapOfObjects() {
        return mapOfObjects;
    }

    public void setMapOfObjects(TreeMap<String, Observable> mapOfObjects) {
        this.mapOfObjects = mapOfObjects;
    }
}
