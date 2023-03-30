package lxo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import pojo.maec.*;

/**
 *
 * @author L. Hurtis, Tibor Galko
 */
public class OWLWriter {

    private final OWLOntologyManager m;
    private OWLOntology ontology;
    OWLDataFactory dataFactory;
    PrefixManager pm;
    private final MAECDocument maecDocument;
    private final Map<String, MAECObject> actions;
    private final List<MAECObject> instances;

    public OWLWriter(MAECDocument maecDocument) {
        m = OWLManager.createOWLOntologyManager();
        dataFactory = m.getOWLDataFactory();

        this.maecDocument = maecDocument;
        actions = new HashMap<>();
        instances = new ArrayList<>();
    }

    public boolean writeFile(File blankOntology) throws OWLOntologyCreationException, OWLOntologyStorageException {
        try {
            ontology = m.loadOntologyFromOntologyDocument(blankOntology);

            String base1 = ontology.getOntologyID().getOntologyIRI().toString();
            String base2 = base1.replaceAll("Optional", "");
            String base3 = base2.replaceAll("\\[", "");
            String base4 = base3.replaceAll("]", "");
            String finalbase = base4;

            pm = new DefaultPrefixManager(finalbase);

            boolean ret = saveObservableObjectsToOntology(maecDocument.getObservable_objects());
            if (!ret) {
                throw new OWLOntologyCreationException("Bad MAEC format in observable objects.");
            }

            ret = saveMAECObjectsToOntology(maecDocument.getMaec_objects());
            if (!ret) {
                throw new OWLOntologyCreationException("Bad MAEC format in maec objects.");
            }
            

            m.saveOntology(ontology);
            
            return true;

        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            System.out.println(e.getLocalizedMessage());
            throw e;
        }
    }

    // Tato metoda by sa dala rozdelit na viac mensich podla typu objektov ale
    // vlastnosti objektov by boli duplicitne
    private boolean saveObservableObjectsToOntology(Map<String, ObservableObject> observableObjects) {

        // Data properties
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
        OWLDataProperty observableEntropy = dataFactory.getOWLDataProperty("entropy", pm);
        OWLDataProperty observableTimestamp = dataFactory.getOWLDataProperty("timestamp", pm);
        OWLDataProperty observableNumberOfPESections = dataFactory.getOWLDataProperty("numberOfPESections", pm);

        // Object properties
        OWLObjectProperty sectionRef = dataFactory.getOWLObjectProperty("has_pe_section", pm);
        OWLObjectProperty parentDirRef = dataFactory.getOWLObjectProperty("is_in_directory", pm);
        OWLObjectProperty parentRef = dataFactory.getOWLObjectProperty("parent_reference", pm);

        // Classes
        OWLClass peSectionClass = dataFactory.getOWLClass("PESection", pm);

        List<OWLDataPropertyAssertionAxiom> toDo = new ArrayList<>();

        for (Entry<String, ObservableObject> pair : observableObjects.entrySet()) {
            ObservableObject o = pair.getValue();

            if (o.getType() != null) {
                String objectIdentifier = o.getHashableString();
                if (objectIdentifier == null) {
                    switch (o.getType()) {
                        case "windows-registry-key": {
                            objectIdentifier = o.getKey();
                            break;
                        }
                        case "process": {
                            objectIdentifier = o.getCommand_line();
                            break;
                        }
                        case "directory": {
                            objectIdentifier = o.getPath();
                            break;
                        }
                        case "file": {
                            objectIdentifier = o.getName();
                        }
                    }
                }
                if (objectIdentifier == null) {
                    System.out.println("Observable object with NULL identifier " + o);
                    return false;
                }

                OWLClass observableClass = dataFactory.getOWLClass("" + o.getType(), pm);
                OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + objectIdentifier, pm);

                if (o.getName() != null) {
                    OWLDataPropertyAssertionAxiom nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableName, observableInd, o.getName());
                    toDo.add(nameAxiom);
                }
                if (o.getPath() != null) {
                    OWLDataPropertyAssertionAxiom pathAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observablePath, observableInd, o.getPath());
                    toDo.add(pathAxiom);
                }
                if (o.getKey() != null) {
                    OWLDataPropertyAssertionAxiom keyAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableKey, observableInd, o.getKey());
                    toDo.add(keyAxiom);
                }
                if (o.getCommand_line() != null) {
                    OWLDataPropertyAssertionAxiom cmdAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableCmd, observableInd, o.getCommand_line());
                    toDo.add(cmdAxiom);
                }
                if (o.getValue() != null) {
                    OWLDataPropertyAssertionAxiom valueAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableValue, observableInd, o.getValue());
                    toDo.add(valueAxiom);
                }
                if (o.getVersion() != null) {
                    OWLDataPropertyAssertionAxiom versionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableVersion, observableInd, o.getVersion());
                    toDo.add(versionAxiom);
                }
                if (o.getSize() != null) {
                    OWLDataPropertyAssertionAxiom sizeAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSize, observableInd, o.getSize());
                    toDo.add(sizeAxiom);
                }
                if (o.getVendor() != null) {
                    OWLDataPropertyAssertionAxiom vendorAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableVendor, observableInd, o.getVendor());
                    toDo.add(vendorAxiom);
                }
                if (o.getMime_type() != null) {
                    OWLDataPropertyAssertionAxiom mimeAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableMimeType, observableInd, o.getMime_type());
                    toDo.add(mimeAxiom);
                }

                // Pridanie hashov
                if (o.getHashes() != null) {
                    if (o.getHashes().getMD5() != null) {
                        OWLDataPropertyAssertionAxiom md5Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableMD5, observableInd, o.getHashes().getMD5());
                        toDo.add(md5Axiom);
                    }
                    if (o.getHashes().getSHA1() != null) {
                        OWLDataPropertyAssertionAxiom sha1Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA1, observableInd, o.getHashes().getSHA1());
                        toDo.add(sha1Axiom);
                    }
                    if (o.getHashes().getSHA256() != null) {
                        OWLDataPropertyAssertionAxiom sha256Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA256, observableInd, o.getHashes().getSHA256());
                        toDo.add(sha256Axiom);
                    }
                    if (o.getHashes().getSHA512() != null) {
                        OWLDataPropertyAssertionAxiom sha512Axiom = dataFactory.getOWLDataPropertyAssertionAxiom(observableSHA512, observableInd, o.getHashes().getSHA512());
                        toDo.add(sha512Axiom);
                    }
                }

                // Pridanie udajov o adresaroch
                if (o.getParent_directory_ref() != null) {
                    ObservableObject parentDir = observableObjects.get(o.getParent_directory_ref());
                    OWLNamedIndividual observableInd2 = dataFactory.getOWLNamedIndividual(":" + parentDir.getHashableString(), pm);
                    OWLObjectPropertyAssertionAxiom parentRefAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(parentDirRef, observableInd, observableInd2);
                    m.addAxiom(ontology, parentRefAxiom);
                }
                if (o.getParent_ref() != null) {
                    ObservableObject parent = observableObjects.get(o.getParent_ref());
                    OWLNamedIndividual observableInd2 = dataFactory.getOWLNamedIndividual(":" + parent.getHashableString(), pm);
                    OWLObjectPropertyAssertionAxiom parentAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(parentRef, observableInd, observableInd2);
                    m.addAxiom(ontology, parentAxiom);
                }

                // Pridanie udajov o PE hlavickach
                if ("file".equals(o.getType()) && o.getExtensions() != null) {
                    if (o.getExtensions().containsKey("windows-pebinary-ext")) {
                        Extension extension = o.getExtensions().get("windows-pebinary-ext");
                        if (extension.getNumber_of_sections() != 0) {
                            OWLDataPropertyAssertionAxiom sectionCount = dataFactory.getOWLDataPropertyAssertionAxiom(observableNumberOfPESections, observableInd, extension.getNumber_of_sections());
                            toDo.add(sectionCount);
                        }
                        if (extension.getTime_date_stamp() != null) {
                            OWLDataPropertyAssertionAxiom timestamp = dataFactory.getOWLDataPropertyAssertionAxiom(observableTimestamp, observableInd, extension.getTime_date_stamp());
                            toDo.add(timestamp);
                        }
                        if (extension.getSections() != null) {
                            List<Section> sections = extension.getSections();
                            for (Section section : sections) {
                                if (section.getName() != null) {
                                    OWLNamedIndividual sectionInd = dataFactory.getOWLNamedIndividual(":" + section.getOntologyId(), pm);

                                    OWLDataPropertyAssertionAxiom name = dataFactory.getOWLDataPropertyAssertionAxiom(observableName, sectionInd, section.getName());
                                    toDo.add(name);

                                    if (section.getEntropy() >= 0) {
                                        OWLDataPropertyAssertionAxiom entropy = dataFactory.getOWLDataPropertyAssertionAxiom(observableEntropy, sectionInd, section.getEntropy());
                                        toDo.add(entropy);
                                    }
                                    if (section.getEntropy() >= 0) {
                                        OWLDataPropertyAssertionAxiom entropy = dataFactory.getOWLDataPropertyAssertionAxiom(observableSize, sectionInd, section.getSize());
                                        toDo.add(entropy);
                                    }

                                    OWLObjectPropertyAssertionAxiom sectionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(sectionRef, observableInd, sectionInd);
                                    m.addAxiom(ontology, sectionAxiom);

                                    OWLClassAssertionAxiom A = dataFactory.getOWLClassAssertionAxiom(peSectionClass, sectionInd);
                                    m.addAxiom(ontology, A);
                                } else {
                                    System.out.println("Bad PE section definition, no name given.");
                                }
                            }
                        }
                    }
                }

                OWLClassAssertionAxiom A = dataFactory.getOWLClassAssertionAxiom(observableClass, observableInd);
                m.addAxiom(ontology, A);
            } else {
                System.out.println("ObservableObject without type " + o);
                return false;
            }
        }
        for (OWLDataPropertyAssertionAxiom AX : toDo) {
            m.addAxiom(ontology, AX);
        }
        return true;
    }

    private boolean saveMAECObjectsToOntology(List<MAECObject> maec_objects) {
        boolean ret = true;
        for (MAECObject maecObject : maec_objects) {
            switch (maecObject.getType()) {
                case "malware-instance": {
                    ret = saveMalwareInstanceToOntology(maecObject);
                    if (maecObject.getDynamic_features() != null && maecObject.getDynamic_features().getProcess_tree() != null) {
                        this.instances.add(maecObject);
                    }
                    break;
                }
                case "malware-action": {
                    ret = saveMalwareActionToOntology(maecObject);
                    break;
                }
            }
        }
        // tu sa pouzivaju vytvorene mapy na ulozenie processov
        // Procesy netreba robit ak nebola robena dynamicka analyza
        if (!this.instances.isEmpty()) {
            saveEveryProcessTree();
        }

        return ret;
    }

    private boolean saveMalwareInstanceToOntology(MAECObject maecObject) {
        OWLClass instanceClass = dataFactory.getOWLClass("Malware_Instance", pm);

        // ulozenie input objektov
        String objectOntologyId;
        List<String> instanceObjectRefs = maecObject.getInstance_object_refs();
        if (instanceObjectRefs == null) {
            System.out.println("No instance refs\n" + maecObject);
            return false; // instance refs je required
        }
        // Kazdy instance musi mat rovnaky hash cize staci zobrat prvy objekt ako ID
        objectOntologyId = maecDocument.getObservable_objects().get(instanceObjectRefs.get(0)).getHashableString();        
        OWLNamedIndividual instanceInd = dataFactory.getOWLNamedIndividual(":" + objectOntologyId, pm);
        OWLClassAssertionAxiom I = dataFactory.getOWLClassAssertionAxiom(instanceClass, instanceInd);
        m.addAxiom(ontology, I);

        return true;
    }

    private String createMalwareActionIdentifier(MAECObject maecObject) {
        // Vytvori hash pre malware-action objekt z input a output objektov
        if (maecObject.getName() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (maecObject.getInput_object_refs() != null) {
            for (String oRef : maecObject.getInput_object_refs()) {
                sb.append(maecDocument.getObservable_objects().get(oRef).getObjectHash());
            }
        }
        if (maecObject.getOutput_object_refs() != null) {
            for (String oRef : maecObject.getOutput_object_refs()) {
                sb.append(maecDocument.getObservable_objects().get(oRef).getObjectHash());
            }
        }
        sb.append(maecObject.getName());

        String hash = HashUtil.getHashableString(sb.toString());
        // Toto sa tu musi nastavit lebo sa to pouziva pri ukladani process_tree
        maecObject.setActionHash(hash);

        return hash;
    }

    private boolean saveMalwareActionToOntology(MAECObject maecObject) {
        String actionOntologyId = createMalwareActionIdentifier(maecObject);
        if (actionOntologyId == null) {
            System.out.println("Action has no name " + maecObject);
            return false;
        }
        
        // Pridanie akcie na dalsie spracovanie, pouziva sa pri dynamic_features
        // inak by trebalo iterovat znova cez list.
        this.actions.put(maecObject.getId(), maecObject);

        // Ziskaj OWL triedu a vytvor individuala s menom objektu
        OWLClass actionClass = dataFactory.getOWLClass("Malware_Action", pm);
        OWLNamedIndividual actionInd = dataFactory.getOWLNamedIndividual(":" + actionOntologyId, pm);
        OWLClassAssertionAxiom I = dataFactory.getOWLClassAssertionAxiom(actionClass, actionInd);
        m.addAxiom(ontology, I);

        // ulozenie mena akcie
        OWLDataProperty actionName = dataFactory.getOWLDataProperty("name", pm);
        OWLDataPropertyAssertionAxiom nameAxiom;
        if (maecObject.getName() != null) {
            nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(actionName, actionInd, maecObject.getName());
        } else {
            nameAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(actionName, actionInd, "ACTION-WITHOUT-NAME");
        }
        m.addAxiom(ontology, nameAxiom);

        // ulozenie input objektov
        OWLObjectProperty actionInput = dataFactory.getOWLObjectProperty("input", pm);
        List<String> inputObjectRefs = maecObject.getInput_object_refs();
        String objectOntologyId;
        if (inputObjectRefs != null) {
            for (String inputObjectRef : inputObjectRefs) {
                objectOntologyId = maecDocument.getObservable_objects().get(inputObjectRef).getHashableString();
                OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + objectOntologyId, pm);
                OWLObjectPropertyAssertionAxiom outputObject = dataFactory.getOWLObjectPropertyAssertionAxiom(actionInput, actionInd, observableInd);
                m.addAxiom(ontology, outputObject);
            }
        }

        // ulozenie output objektov
        OWLObjectProperty actionOutput = dataFactory.getOWLObjectProperty("output", pm);
        List<String> outputObjectRefs = maecObject.getOutput_object_refs();
        if (outputObjectRefs != null) {
            for (String outputObjectRef : outputObjectRefs) {
                objectOntologyId = maecDocument.getObservable_objects().get(outputObjectRef).getHashableString();
                OWLNamedIndividual observableInd = dataFactory.getOWLNamedIndividual(":" + objectOntologyId, pm);
                OWLObjectPropertyAssertionAxiom outputObject = dataFactory.getOWLObjectPropertyAssertionAxiom(actionOutput, actionInd, observableInd);
                m.addAxiom(ontology, outputObject);
            }
        }
        return true;
    }

    // Toto sa musi volat po ulozeni malware-action
    private void saveEveryProcessTree() {
        String processObjectOntologyId, actionHash, instanceObjRef, instanceOntologyId;
        OWLObjectProperty Iprocess = dataFactory.getOWLObjectProperty("instance-process", pm);
        OWLObjectProperty initiatedAction = dataFactory.getOWLObjectProperty("initiated_malware_action", pm);

        // Pouzite na vytvorenie pocitadla
        Map<String, Map<String, Integer>> processActionCallCount = new HashMap<>();

        // Kazdy instance moze vytvorit ine procesy
        for (MAECObject instance : this.instances) {
            instanceObjRef = instance.getInstance_object_refs().get(0);
            instanceOntologyId = maecDocument.getObservable_objects().get(instanceObjRef).getHashes().getMD5();
            OWLNamedIndividual instanceInd = dataFactory.getOWLNamedIndividual(":" + instanceOntologyId, pm);

            // Ulozenie dat z dynamickej analyzy
            DynamicFeatures dynamicFeatures = instance.getDynamic_features();
            if (dynamicFeatures != null) {
                List<ProcessTreeNode> processTree = dynamicFeatures.getProcess_tree();
                if (processTree != null) {
                    int count;
                    
                    Map<String, Integer> actionCount;
                    for (ProcessTreeNode node : processTree) {
                        processObjectOntologyId = maecDocument.getObservable_objects().get(node.getProcess_ref()).getHashableString();
                        OWLNamedIndividual processInd = dataFactory.getOWLNamedIndividual(":" + processObjectOntologyId, pm);
                        OWLObjectPropertyAssertionAxiom A = dataFactory.getOWLObjectPropertyAssertionAxiom(Iprocess, instanceInd, processInd);
                        m.addAxiom(ontology, A);
                        
                        // Ak process uz existuje zvysuju sa jeho hodnoty
                        if (processActionCallCount.containsKey(processObjectOntologyId)) {
                            actionCount = processActionCallCount.get(processObjectOntologyId);
                        } else {
                            actionCount = new HashMap<>();                            
                        }

                        List<String> initActionRefs = node.getInitiated_action_refs();
                        if (initActionRefs != null) {
                            // Prechadza vsetky akcie ktore proces vytvoril
                            for (String actionRef : initActionRefs) {
                                // ActionHash sa vytvori a ulozi pri prvom ukladani akcii
                                actionHash = this.actions.get(actionRef).getActionHash();
                                if (actionHash == null) {
                                    System.out.println("Action has no actionHash set " + actionRef);
                                    continue;
                                }
                                OWLNamedIndividual initActionInd = dataFactory.getOWLNamedIndividual(":" + actionHash, pm);
                                OWLObjectPropertyAssertionAxiom outputObject = dataFactory.getOWLObjectPropertyAssertionAxiom(initiatedAction, processInd, initActionInd);
                                m.addAxiom(ontology, outputObject);

                                count = 1;
                                if (actionCount.containsKey(actionHash)) {
                                    count = actionCount.get(actionHash);
                                    actionCount.put(actionHash, count + 1);
                                } else {
                                    actionCount.put(actionHash, count);
                                }
                            }
                            // Ulozenie na vytvorenie counterov 
                            // TODO moze sa stat ze process hash je rovnaky ako predosly a data sa prepisu
//                            System.out.println(node.getProcess_ref());
//                            System.out.println(processObjectOntologyId);
                            processActionCallCount.put(processObjectOntologyId, actionCount);
                            
                        }
                    }
                    saveActionCounters(instanceOntologyId, processActionCallCount);
                }
            }
        }
    }

    // processActionCallCount obsahuje <Id procesu v ontologii, <Id akcie v ontologii, pocet volani>>
    private void saveActionCounters(String instanceOntologyId, Map<String, Map<String, Integer>> processActionCallCount) {
        OWLClass counterClass = dataFactory.getOWLClass("counter", pm);
        OWLObjectProperty processDo = dataFactory.getOWLObjectProperty("do", pm);
        OWLObjectProperty counterDo = dataFactory.getOWLObjectProperty("count", pm);
        OWLDataProperty timesExecuted = dataFactory.getOWLDataProperty("times_executed", pm);

        
        String processHash, actionHash;
        for (Map.Entry<String, Map<String, Integer>> pair : processActionCallCount.entrySet()) {
            processHash = pair.getKey();
            OWLNamedIndividual processInd = dataFactory.getOWLNamedIndividual(":" + processHash, pm);
            for (Map.Entry<String, Integer> actionPair : pair.getValue().entrySet()) {
                actionHash = actionPair.getKey();
                OWLNamedIndividual actInd = dataFactory.getOWLNamedIndividual(":" + actionHash, pm);

                // Id pocitadla je vytvorene ako hash akcie a instance zahashovane
                String counterIri = HashUtil.getHashableString(actionHash + instanceOntologyId);
                OWLNamedIndividual counterInd = dataFactory.getOWLNamedIndividual(":" + counterIri + "Counter", pm);

                OWLClassAssertionAxiom classAxiom = dataFactory.getOWLClassAssertionAxiom(counterClass, counterInd);
                OWLDataPropertyAssertionAxiom countAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(timesExecuted, counterInd, actionPair.getValue());
                OWLObjectPropertyAssertionAxiom processDoAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(processDo, processInd, counterInd);
                OWLObjectPropertyAssertionAxiom counterDoAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(counterDo, counterInd, actInd);

                m.addAxiom(ontology, classAxiom);
                m.addAxiom(ontology, countAxiom);
                m.addAxiom(ontology, processDoAxiom);
                m.addAxiom(ontology, counterDoAxiom);
            }
        }
    }
}
