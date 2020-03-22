package lxo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.security.*;

/**
 *
 * @author Tibor Galko podla bakalarskej práce Lukáša Hurtiša - BP Ontologický
 * model pre bezpečnostnú doménu
 *
 * Prerobené z oknovej aplikacie na webovy servis
 */
public class MaecToOwl {

    private final File InputFile;
    private final File OutputFile;
    
    
    public MaecToOwl(File inputFile, File outputFile) {
        this.InputFile = inputFile;
        this.OutputFile = outputFile;
    }

    public void convert() throws IllegalArgumentException, OWLOntologyStorageException, OWLOntologyCreationException {
        lxo.Scan scan = readFile();
        lxo.Scan scan2 = setHashes(scan);
        System.out.println("ok");

        boolean completed = writeFile(scan2, OutputFile);
        if (completed) {
            System.out.println("DONE");
        } else {
            System.out.println("ERROR");
        }
    }

    private lxo.Scan readFile() throws UnsupportedOperationException {
        System.out.println("Reading file");
        JSONParser parser = new JSONParser();
        try (FileReader fr = new FileReader(InputFile)) {
            Object obj = parser.parse(fr);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray maecobjects = (JSONArray) jsonObject.get("maec_objects");
            if (maecobjects == null) {
                return null;
            }

            System.out.println("Getting malware instance");
            lxo.Instance instance = getMalwareInstance(maecobjects);
            if (instance == null) {
                throw new UnsupportedOperationException("Error while getting instance.");
            }

            System.out.println("Getting malware action list");
            Map<String, List<lxo.Action>> malware_actions = getMalwareActionList(maecobjects);
            if (malware_actions.isEmpty()) {
                throw new UnsupportedOperationException("Error while getting actions");
            }

            System.out.println("Getting observable objects list");
            TreeMap<String, lxo.Observable> observable_objects = getObservableObjectsList(jsonObject);
            if (observable_objects.isEmpty()) {
                throw new UnsupportedOperationException("Error while getting observable objects");
            }

            int ActionsCounter = 0;
            Iterator<Map.Entry<String, List<lxo.Action>>> I = malware_actions.entrySet().iterator();
            while (I.hasNext()) {
                ActionsCounter += I.next().getValue().size();
            }

            System.out.println("Getting malware action details");
            getMalwareActionDetails(malware_actions, maecobjects);

            System.out.println(ActionsCounter + " Malware Actions and " + observable_objects.size() + " Observable Objects were found");

            lxo.Scan scan = new lxo.Scan();

            System.out.println("Setting values to new Scan.");
            scan.setInstance(instance);
            scan.setMapOfActions(malware_actions);
            scan.setMapOfObjects(observable_objects);
            
            return scan;
            
        } catch (IOException | ParseException e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }
    

    private lxo.Instance getMalwareInstance(JSONArray maecobjects) {
        for (int i = 0; i <= maecobjects.size() - 1; i++) {
            JSONObject O = (JSONObject) maecobjects.get(i);

            String S = (String) O.get("type");
            if (S.compareTo("malware-instance") == 0) {
                String ID = (String) O.get("id");

                lxo.Instance instance = new lxo.Instance(ID);

                if (O.get("instance_object_refs") != null) {
                    JSONArray instanceObjects = (JSONArray) O.get("instance_object_refs");
                    String insttref = (String) instanceObjects.get(0);
                    instance.setInstanceObjectRef(insttref);
                }

                return instance;
            }
        }
        return null;
    }

    private void getMalwareActionDetails(Map<String, List<lxo.Action>> list, JSONArray maecobjects) {
        int hack = 0;

        Iterator<Map.Entry<String, List<lxo.Action>>> I = list.entrySet().iterator();

        while (I.hasNext()) {
            Map.Entry E = I.next();
            List<lxo.Action> smallList = (List<lxo.Action>) E.getValue();

            for (lxo.Action A : smallList) {
                for (int i = hack; i <= maecobjects.size() - 1; i++) {
                    JSONObject O = (JSONObject) maecobjects.get(i);
                    String S = (String) O.get("type");

                    if (S.compareTo("malware-action") == 0) {
                        String ID = (String) O.get("id");
                        String NAME = (String) O.get("name");

                        if (A.getID().compareTo(ID) == 0) {
                            A.setName(NAME);

                            if (O.get("output_object_refs") != null) {
                                JSONArray outputObjects = (JSONArray) O.get("output_object_refs");
                                String outref = (String) outputObjects.get(0);
                                A.setOutputObjectRef(outref);
                            }
                            if (O.get("input_object_refs") != null) {
                                JSONArray inputObjects = (JSONArray) O.get("input_object_refs");
                                String inref = (String) inputObjects.get(0);
                                A.setInputObjectRef(inref);
                            }
                            break;
                        }
                    }
                }
            }
            hack += smallList.size();
        }

    }

    private Map<String, List<lxo.Action>> getMalwareActionList(JSONArray maecobjects) {
        Map<String, List<lxo.Action>> sorted_mallware_actions = new HashMap<>();

        for (int i = 0; i <= maecobjects.size() - 1; i++) {

            JSONObject O = (JSONObject) maecobjects.get(i);

            String S = (String) O.get("type");
            if (S.compareTo("malware-instance") == 0) {

                JSONObject dynamicFeatures = (JSONObject) O.get("dynamic_features");
                if (dynamicFeatures == null) {
                    System.out.println("No dynamic_features found.");
                    return null;
                }
                JSONArray processTree = (JSONArray) dynamicFeatures.get("process_tree");
                if (processTree == null) {
                    System.out.println("No process_tree found");
                    return null;
                }

                for (int q = 0; q <= processTree.size() - 1; q++) {
                    JSONObject processTreeChild = (JSONObject) processTree.get(q);
                    JSONArray actions = (JSONArray) processTreeChild.get("initiated_action_refs");

                    String processes = processTreeChild.get("process_ref").toString();
                    List<lxo.Action> mallware_actions = new ArrayList<>();

                    if (actions != null) {
                        String action = actions.toString();
                        String action2 = action.substring(1, action.length() - 1);
                        String[] action3 = action2.split(",");

                        for (String a3 : action3) {
                            String action4 = a3.substring(1, a3.length() - 1);
                            lxo.Action A = new lxo.Action(action4);
                            mallware_actions.add(A);
                        }
                    }
                    sorted_mallware_actions.put(processes, mallware_actions);
                }
            }
        }
        return sorted_mallware_actions;
    }

    private TreeMap<String, lxo.Observable> getObservableObjectsList(JSONObject jsonObject) {
        TreeMap<String, lxo.Observable> map = new TreeMap<>();
        
        JSONObject observableObjects = (JSONObject) jsonObject.get("observable_objects");

        Collection C = observableObjects.keySet();
        Iterator I = C.iterator();

        while (I.hasNext()) {

            String index = I.next().toString();
            JSONObject obs = (JSONObject) observableObjects.get(index);

            lxo.Observable o = new lxo.Observable(index);
            o.setType((String) obs.get("type"));
            if (obs.get("name") != null) {
                o.setName((String) obs.get("name"));
            }
            if (obs.get("size") != null) {
                o.setSize((String) obs.get("size"));
            }
            if (obs.get("hashes") != null) {
                JSONObject hashes = (JSONObject) obs.get("hashes");
                o.setSha512((String) hashes.get("SHA-512"));
                o.setMd5((String) hashes.get("MD5"));
                o.setSha256((String) hashes.get("SHA-256"));
                o.setSha1((String) hashes.get("SHA-1"));
            }
            if (obs.get("values") != null) {
                Map<String, String> M = new HashMap<>();
                JSONArray values = (JSONArray) obs.get("values");
                for (int i = 0; i <= values.size() - 1; i++) {
                    JSONObject value = (JSONObject) values.get(i);
                    if (value.get("data") != null) {
                        M.put("data", (String) value.get("data"));
                    }
                    if (value.get("data_type") != null) {
                        M.put("data_type", (String) value.get("data_type"));
                    }
                    if (value.get("name") != null) {
                        M.put("name", (String) value.get("name"));
                    }
                }

                o.setValues(M);

            }
            if (obs.get("key") != null) {
                o.setKey((String) obs.get("key"));
            }
            if (obs.get("value") != null) {
                o.setValue((String) obs.get("value"));
            }
            if (obs.get("vendor") != null) {
                o.setVendor((String) obs.get("vendor"));
            }
            if (obs.get("path") != null) {
                o.setPath((String) obs.get("path"));
            }
            if (obs.get("command_line") != null) {
                o.setCommandLine((String) obs.get("command_line"));
            }
            if (obs.get("mime_type") != null) {
                o.setMimeType((String) obs.get("mime_type"));
            }
            if (obs.get("binary_ref") != null) {
                o.setBinaryRef((String) obs.get("binary_ref"));
            }
            if (obs.get("version") != null) {
                o.setVersion((String) obs.get("version"));
            }
            if (obs.get("parent_directory_ref") != null) {
                o.setParentDirectoryRef((String) obs.get("parent_directory_ref"));
            }
            if (obs.get("parent_ref") != null) {
                o.setParrentRef((String) obs.get("parent_ref"));
            }
            if (obs.get("child_refs") != null) {
                List<String> childList = new ArrayList<>();
                JSONArray childs = (JSONArray) obs.get("child_refs");

                for (int i = 0; i <= childs.size() - 1; i++) {
                    String child = (String) childs.get(i);
                    childList.add(child);
                }
                o.setChildRef(childList);
            }
            map.put(index, o);

        }
        return map;
    }

    private String getHashableString(lxo.Observable O) {
        String S = "";

        if (O.getMd5() != null) {
            S = S + O.getMd5();
            return S;
        }
        if (O.getSha1() != null) {
            S = S + O.getSha1();
            return S;
        }
        if (O.getSha256() != null) {
            S = S + O.getSha256();
            return S;
        }
        if (O.getSha512() != null) {
            S = S + O.getSha512();
            return S;
        }

        S = S + O.getType() + O.getName() + O.getPath() + O.getCommandLine() + O.getKey() + O.getMimeType();
        S = S.replaceAll("null", "");

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(S));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return "NULL";
    }

    private String getHashableString(String S) {

        S = S.replaceAll("null", "");

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(S));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return "NULL";
    }

    private boolean writeFile(lxo.Scan scan2, File W) throws OWLOntologyCreationException, OWLOntologyStorageException {

        Map<String, List<lxo.Action>> malware_actions = scan2.getMapOfActions();
        TreeMap<String, lxo.Observable> observable_objects = scan2.getMapOfObjects();

        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o;

        try {
            o = m.loadOntologyFromOntologyDocument(W);

            String base1 = o.getOntologyID().getOntologyIRI().toString();
            String base2 = base1.replaceAll("Optional", "");
            String base3 = base2.replaceAll("\\[", "");
            String base4 = base3.replaceAll("]", "");
            String finalbase = base4;

            PrefixManager pm = new DefaultPrefixManager(finalbase);
            OWLDataFactory dataFactory = m.getOWLDataFactory();

            lxo.Instance instance = scan2.getInstance();
            OWLClass instanceClass = dataFactory.getOWLClass("Malware_Instance", pm);
            OWLNamedIndividual instanceInd = dataFactory.getOWLNamedIndividual(":" + instance.getID(), pm);
            OWLClassAssertionAxiom I = dataFactory.getOWLClassAssertionAxiom(instanceClass, instanceInd);
            m.addAxiom(o, I);

            saveObjectsToOntology(observable_objects, o, m, pm, dataFactory);
            OWLObjectProperty Iprocess = dataFactory.getOWLObjectProperty("instance-process", pm);

            Iterator<Map.Entry<String, List<lxo.Action>>> X = malware_actions.entrySet().iterator();
            while (X.hasNext()) {
                Map.Entry E = X.next();
                String PID = (String) E.getKey();
                lxo.Observable O = observable_objects.get(PID);
                OWLNamedIndividual object = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);
                OWLObjectPropertyAssertionAxiom XX = dataFactory.getOWLObjectPropertyAssertionAxiom(Iprocess, instanceInd, object);
                m.addAxiom(o, XX);
            }

            List<lxo.Executer> executedActionsByProcessess = saveActionsToOntology(malware_actions, observable_objects, o, m, pm, dataFactory);
            setActionsToProcessess(executedActionsByProcessess, observable_objects, o, m, pm, finalbase, dataFactory);

            m.saveOntology(o);
            return true;

        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            System.out.println(e.getLocalizedMessage());
            throw e;
        }
    }

    private void setActionsToProcessess(List<lxo.Executer> executedActionsByProcessess, TreeMap<String, lxo.Observable> observable_objects, OWLOntology o, OWLOntologyManager m, PrefixManager pm, String finalbase, OWLDataFactory dataFactory) throws OWLOntologyStorageException {

        OWLClass counterClass = dataFactory.getOWLClass("counter", pm);
        OWLObjectProperty processDo = dataFactory.getOWLObjectProperty("do", pm);
        OWLObjectProperty counterDo = dataFactory.getOWLObjectProperty("count", pm);
        OWLDataProperty timesExecuted = dataFactory.getOWLDataProperty("times_executed", pm);

        for (lxo.Executer E : executedActionsByProcessess) {
            Map<OWLNamedIndividual, Integer> actions = E.getActions();
            lxo.Observable executer = observable_objects.get(E.getID());

            Iterator<Map.Entry<OWLNamedIndividual, Integer>> actionIterator = actions.entrySet().iterator();

            OWLNamedIndividual processInd = dataFactory.getOWLNamedIndividual(":" + executer.getObjectHash(), pm);

            while (actionIterator.hasNext()) {
                Map.Entry entry = actionIterator.next();
                OWLNamedIndividual executed = (OWLNamedIndividual) entry.getKey();
                Integer Ncount = (Integer) entry.getValue();
                IRI iri = executed.getIRI();
                String actionIri = iri.toString().replaceAll(finalbase, "");
                String executerIri = executer.getObjectHash();
                String counterIriRaw = actionIri + executerIri;
                String counterIri = getHashableString(counterIriRaw);
                OWLNamedIndividual counterInd = dataFactory.getOWLNamedIndividual(":" + counterIri + "Counter", pm);
                OWLClassAssertionAxiom C = dataFactory.getOWLClassAssertionAxiom(counterClass, counterInd);
                OWLDataPropertyAssertionAxiom CC = dataFactory.getOWLDataPropertyAssertionAxiom(timesExecuted, counterInd, Ncount);
                OWLObjectPropertyAssertionAxiom CCC = dataFactory.getOWLObjectPropertyAssertionAxiom(processDo, processInd, counterInd);
                OWLObjectPropertyAssertionAxiom CCCC = dataFactory.getOWLObjectPropertyAssertionAxiom(counterDo, counterInd, executed);

                m.addAxiom(o, C);
                m.addAxiom(o, CC);
                m.addAxiom(o, CCC);
                m.addAxiom(o, CCCC);
            }
        }

    }

    private List<lxo.Executer> saveActionsToOntology(Map<String, List<lxo.Action>> malware_actions, TreeMap<String, lxo.Observable> observable_objects, OWLOntology o, OWLOntologyManager m, PrefixManager pm, OWLDataFactory dataFactory) throws OWLOntologyStorageException {

        List<lxo.Executer> Executers = new ArrayList<>();

        OWLClass actionClass = dataFactory.getOWLClass("Malware_Action", pm);
        OWLDataProperty actionName = dataFactory.getOWLDataProperty("name", pm);
        OWLObjectProperty actionInput = dataFactory.getOWLObjectProperty("input", pm);
        OWLObjectProperty actionOutput = dataFactory.getOWLObjectProperty("output", pm);

        Iterator<Map.Entry<String, List<lxo.Action>>> I = malware_actions.entrySet().iterator();

        while (I.hasNext()) {
            Map.Entry E = I.next();
            List<lxo.Action> smallList = (List<lxo.Action>) E.getValue();
            lxo.Executer executer = new lxo.Executer();
            Map<OWLNamedIndividual, Integer> list = new TreeMap<>();

            if (!smallList.isEmpty()) {
                for (lxo.Action A : smallList) {

                    OWLNamedIndividual actionInd = dataFactory.getOWLNamedIndividual("" + A.getActionHash(), pm);
                    OWLClassAssertionAxiom actionIndAxiom = dataFactory.getOWLClassAssertionAxiom(actionClass, actionInd);
                    OWLDataPropertyAssertionAxiom nameAxiom;
                    if (A.getName() != null) {
                        nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(actionName, actionInd, A.getName());
                    } else {
                        nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(actionName, actionInd, "ACTION-WITHOUT-NAME");
                    }

                    m.addAxiom(o, actionIndAxiom);
                    m.addAxiom(o, nameAxiom);

                    if (A.getOutputObjectRef() != null) {
                        lxo.Observable O = observable_objects.get(A.getOutputObjectRef());
                        OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);
                        OWLObjectPropertyAssertionAxiom outputObject = dataFactory.getOWLObjectPropertyAssertionAxiom(actionOutput, actionInd, observableInd);
                        m.addAxiom(o, outputObject);
                    }

                    if (A.getInputObjectRef() != null) {
                        lxo.Observable O = observable_objects.get(A.getInputObjectRef());
                        OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);
                        OWLObjectPropertyAssertionAxiom inputObject = dataFactory.getOWLObjectPropertyAssertionAxiom(actionInput, actionInd, observableInd);
                        m.addAxiom(o, inputObject);
                    }

                    if (list.get(actionInd) == null) {
                        list.put(actionInd, 1);
                    } else {
                        int X = list.get(actionInd);
                        list.put(actionInd, X + 1);
                    }
                }
                executer.setActions(list);
            }
            executer.setID((String) E.getKey());
            Executers.add(executer);

        }
        return Executers;
    }

    private void saveObjectsToOntology(TreeMap<String, lxo.Observable> observable_objects, OWLOntology o, OWLOntologyManager m, PrefixManager pm, OWLDataFactory dataFactory) {

        Iterator<Map.Entry<String, lxo.Observable>> I = observable_objects.entrySet().iterator();
        Iterator<Map.Entry<String, lxo.Observable>> I2 = observable_objects.entrySet().iterator();

        OWLDataProperty observableName = dataFactory.getOWLDataProperty("name", pm);
        OWLDataProperty observablePath = dataFactory.getOWLDataProperty("path", pm);
        OWLDataProperty observableKey = dataFactory.getOWLDataProperty("key", pm);
        OWLDataProperty observableCmd = dataFactory.getOWLDataProperty("commandLine", pm);
        OWLDataProperty observableValue = dataFactory.getOWLDataProperty("value", pm);
        OWLDataProperty observableVersion = dataFactory.getOWLDataProperty("version", pm);
        OWLDataProperty observableSize = dataFactory.getOWLDataProperty("size", pm);
        OWLDataProperty observableVendor = dataFactory.getOWLDataProperty("vendor", pm);
        OWLDataProperty observableMimeType = dataFactory.getOWLDataProperty("mimeType", pm);
        OWLDataProperty observableMD5 = dataFactory.getOWLDataProperty("md5", pm);
        OWLDataProperty observableSHA1 = dataFactory.getOWLDataProperty("sha1", pm);
        OWLDataProperty observableSHA256 = dataFactory.getOWLDataProperty("sha256", pm);
        OWLDataProperty observableSHA512 = dataFactory.getOWLDataProperty("sha512", pm);

        List<OWLDataPropertyAssertionAxiom> toDo = new ArrayList<>();

        while (I.hasNext()) {
            Map.Entry entry = I.next();
            lxo.Observable O = (lxo.Observable) entry.getValue();
            if (O.getType() != null) {
                OWLClass observableClass = dataFactory.getOWLClass("" + O.getType(), pm);
                OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);

                if (O.getName() != null) {
                    OWLDataPropertyAssertionAxiom nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableName, observableInd, O.getName());
                    toDo.add(nameAxiom);
                }
                if (O.getPath() != null) {
                    OWLDataPropertyAssertionAxiom pathAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observablePath, observableInd, O.getPath());
                    toDo.add(pathAxiom);
                }
                if (O.getKey() != null) {
                    OWLDataPropertyAssertionAxiom keyAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableKey, observableInd, O.getKey());
                    toDo.add(keyAxiom);
                }
                if (O.getCommandLine() != null) {
                    OWLDataPropertyAssertionAxiom cmdAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableCmd, observableInd, O.getCommandLine());
                    toDo.add(cmdAxiom);
                }
                if (O.getValue() != null) {
                    OWLDataPropertyAssertionAxiom valueAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableValue, observableInd, O.getValue());
                    toDo.add(valueAxiom);
                }
                if (O.getVersion() != null) {
                    OWLDataPropertyAssertionAxiom versionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableVersion, observableInd, O.getVersion());
                    toDo.add(versionAxiom);
                }
                if (O.getSize() != null) {
                    OWLDataPropertyAssertionAxiom sizeAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSize, observableInd, O.getSize());
                    toDo.add(sizeAxiom);
                }
                if (O.getVendor() != null) {
                    OWLDataPropertyAssertionAxiom vendorAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableVendor, observableInd, O.getVendor());
                    toDo.add(vendorAxiom);
                }
                if (O.getMimeType() != null) {
                    OWLDataPropertyAssertionAxiom mimeAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableMimeType, observableInd, O.getMimeType());
                    toDo.add(mimeAxiom);
                }
                if (O.getMd5() != null) {
                    OWLDataPropertyAssertionAxiom md5Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableMD5, observableInd, O.getMd5());
                    toDo.add(md5Axiom);
                }
                if (O.getSha1() != null) {
                    OWLDataPropertyAssertionAxiom sha1Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA1, observableInd, O.getSha1());
                    toDo.add(sha1Axiom);
                }
                if (O.getSha256() != null) {
                    OWLDataPropertyAssertionAxiom sha256Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA256, observableInd, O.getSha256());
                    toDo.add(sha256Axiom);
                }
                if (O.getSha512() != null) {
                    OWLDataPropertyAssertionAxiom sha512Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA512, observableInd, O.getSha512());
                    toDo.add(sha512Axiom);
                }

                OWLClassAssertionAxiom A = dataFactory.getOWLClassAssertionAxiom(observableClass, observableInd);

                m.addAxiom(o, A);
                for (OWLDataPropertyAssertionAxiom AX : toDo) {
                    m.addAxiom(o, AX);
                }
            }
        }

        OWLObjectProperty parentDirRef = dataFactory.getOWLObjectProperty("is_in_directory", pm);
        OWLObjectProperty parentRef = dataFactory.getOWLObjectProperty("parrent_reference", pm);

        while (I2.hasNext()) {
            Map.Entry entry = I2.next();
            lxo.Observable O = (lxo.Observable) entry.getValue();
            if (O.getType() != null) {
                if (O.getParentDirectoryRef() != null) {
                    lxo.Observable parentDir = observable_objects.get(O.getParentDirectoryRef());
                    OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);
                    OWLNamedIndividual observableInd2 = dataFactory.getOWLNamedIndividual(":" + parentDir.getObjectHash(), pm);
                    OWLObjectPropertyAssertionAxiom parentRefAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(parentDirRef, observableInd, observableInd2);
                    m.addAxiom(o, parentRefAxiom);
                }
                if (O.getParrentRef() != null) {
                    lxo.Observable parent = observable_objects.get(O.getParrentRef());
                    OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + O.getObjectHash(), pm);
                    OWLNamedIndividual observableInd2 = dataFactory.getOWLNamedIndividual(":" + parent.getObjectHash(), pm);
                    OWLObjectPropertyAssertionAxiom parentAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(parentRef, observableInd, observableInd2);
                    m.addAxiom(o, parentAxiom);
                }
            }
        }
    }

    private lxo.Scan setHashes(lxo.Scan scan) {

        lxo.Instance instance = scan.getInstance();

        Map<String, List<lxo.Action>> mallware_actions = scan.getMapOfActions();

        TreeMap<String, lxo.Observable> observable_objects = scan.getMapOfObjects();

        lxo.Observable instanceObject = observable_objects.get(instance.getInstanceObjectRef());

        if (instanceObject.getMd5() != null) {
            instance.setID(instanceObject.getMd5());
        }
        if ((instance.getID() == null) && (instanceObject.getSha1() != null)) {
            instance.setID(instanceObject.getSha1());
        }
        if ((instance.getID() == null) && (instanceObject.getSha256() != null)) {
            instance.setID(instanceObject.getSha256());
        }
        if ((instance.getID() == null) && (instanceObject.getSha512() != null)) {
            instance.setID(instanceObject.getSha512());
        }

        Iterator<Map.Entry<String, lxo.Observable>> I2 = observable_objects.entrySet().iterator();

        while (I2.hasNext()) {
            Map.Entry entry = I2.next();
            lxo.Observable O = (lxo.Observable) entry.getValue();
            String hash = getHashableString(O);
            O.setObjectHash(hash);
        }

        Iterator<Map.Entry<String, List<lxo.Action>>> I3 = mallware_actions.entrySet().iterator();

        while (I3.hasNext()) {
            Map.Entry<String, List<lxo.Action>> E = I3.next();
            List<lxo.Action> smallList = E.getValue();

            for (lxo.Action A : smallList) {

                String S = "";

                if (A.getInputObjectRef() != null) {
                    S = S + observable_objects.get(A.getInputObjectRef()).getObjectHash();
                }
                if (A.getOutputObjectRef() != null) {
                    S = S + observable_objects.get(A.getOutputObjectRef()).getObjectHash();
                }

                S = S + A.getName();
                S = getHashableString(S);
                A.setActionHash(S);
            }
        }

        scan.setInstance(instance);
        scan.setMapOfActions(mallware_actions);
        scan.setMapOfObjects(observable_objects);

        return scan;
    }
}
