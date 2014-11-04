package cz.makub;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class RulesForCCE {

	//Use here the same IRI as for the main ontology Curac1 in order to have consistent concepts
	public static IRI ontologyIRI = IRI.create("http://surgipedia.sfb125.de/images/a/a4/Curac1");	
	public static OWLClass currentPhase; 
    public static OWLClass detectedPhase;
    public static OWLObjectProperty previousPhase;
    public static Set<String> visitedPhaseString = new HashSet<String>();
    
	public static void main (String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLOntology ontology = manager.createOntology(ontologyIRI);
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    
	    //create basic concepts with their IRIs
	    currentPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#CurrentPhase"));
	    previousPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#previousPhase"));
	    detectedPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#DetectedPhase"));
	  
	    //init the currentPhase-class with the "start"-individual
	    OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "start"));
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(currentPhase, start1));
	    
	    //create rule1
	    SWRLRule rulePP = buildRule("port_placement", "port", "unknown", "place");
        manager.applyChange(new AddAxiom(ontology, rulePP));

	    //create rule2
        SWRLRule ruleMob1 = buildRule("mobilisation", "atraumatic_grasper", "gallbladder_fundus", "grasp");
        manager.applyChange(new AddAxiom(ontology, ruleMob1));
        
	    SWRLRule ruleMob2 = buildRule("mobilisation", "atraumatic_grasper", "gastrocolic_ligament", "grasp");
        manager.applyChange(new AddAxiom(ontology, ruleMob2));

	    SWRLRule ruleDis1 = buildRule("dissection", "atraumatic_grasper", "hepatoduodenal_ligament", "grasp");
	    manager.applyChange(new AddAxiom(ontology, ruleDis1));

	    SWRLRule ruleDis2 = buildRule("dissection", "atraumatic_grasper", "hepatoduodenal_ligament", "lift");
	    manager.applyChange(new AddAxiom(ontology, ruleDis2));

	    SWRLRule ruleDis3 = buildRule("dissection", "instrument", "calot_triangle", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleDis3));
        
	    SWRLRule ruleDis4 = buildRule("dissection", "sharp_instrument", "hepatoduodenal_ligament", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleDis4));

	    SWRLRule ruleResCA1 = buildRule("resection_cystic_artery", "sharp_instrument", "cystic_artery", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleResCA1));

	    SWRLRule ruleResCA2 = buildRule("resection_cystic_artery", "clip", "cystic_artery", "clipping");
        manager.applyChange(new AddAxiom(ontology, ruleResCA2));

	    SWRLRule ruleResCD1 = buildRule("resection_cystic_duct", "sharp_instrument", "cystic_duct", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleResCD1));

	    SWRLRule ruleResCD2 = buildRule("resection_cystic_duct", "clip", "cystic_duct", "clipping");
        manager.applyChange(new AddAxiom(ontology, ruleResCD2));

	    SWRLRule ruleResG1 = buildRule("resection_gallbladder", "sharp_instrument", "gallbladder_serosa", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleResG1));

	    SWRLRule ruleResG2 = buildRule("resection_gallbladder", "sharp_instrument", "gallbladder_serosa", "dissect");
        manager.applyChange(new AddAxiom(ontology, ruleResG2));

	    SWRLRule ruleResG3 = buildRule("resection_gallbladder", "sharp_instrument", "gallbladder", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleResG3));

	    SWRLRule ruleResG4 = buildRule("resection_gallbladder", "sharp_instrument", "gallbladder_liverbed", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleResG4));

	    SWRLRule ruleClose1 = buildRule("closure", "specimen_bag", "unknown", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleClose1));
        
        SWRLRule ruleClose2 = buildRule("closure", "specimen_bag", "organ", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleClose2));
        
        //Strange rules with the instrument "drain". It should be called "drainage" as by the next two rules.
        
        //SWRLRule ruleDrain1 = buildRule("drain", "drain", "unknown", "putting_action");
        //manager.applyChange(new AddAxiom(ontology, ruleDrain1));
        
        //SWRLRule ruleDrain2 = buildRule("drain", "drain", "unknown", "instrumental_property");
        //manager.applyChange(new AddAxiom(ontology, ruleDrain2));
        
        SWRLRule ruleDrainage1 = buildRule("drain", "drainage", "unknown", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleDrainage1));
        
        SWRLRule ruleDrainage2 = buildRule("drain", "drainage", "organ", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleDrainage2));
     
        //save in RDF/XML format
        File output = new File("D:/DiplArbeit/OurWork/USECASE/RULES/CCE_rules.owl");
        output.createNewFile();
        manager.saveOntology(ontology, IRI.create(output.toURI()));
        
	}

	public static SWRLRule buildRule (String cl_Phase, String cl_Instrument, String cl_Structure, String pr_Activity) throws OWLOntologyCreationException{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    //OWLOntology ontology = manager.createOntology(ontologyIRI);
	    OWLDataFactory factory = manager.getOWLDataFactory();
        
        System.out.println("creating variables...");

        SWRLVariable varP = factory.getSWRLVariable(IRI.create(ontologyIRI + "#p"));
        SWRLVariable varPre = factory.getSWRLVariable(IRI.create(ontologyIRI + "#pre"));
        SWRLVariable varI = factory.getSWRLVariable(IRI.create(ontologyIRI + "#i"));
        SWRLVariable varStruct = factory.getSWRLVariable(IRI.create(ontologyIRI + "#struct"));
                
        SWRLClassAtom p = factory.getSWRLClassAtom(getClassFromName(cl_Phase), varP);
        SWRLClassAtom pre = factory.getSWRLClassAtom(currentPhase, varPre);
        SWRLClassAtom instr = factory.getSWRLClassAtom(getClassFromName(cl_Instrument), varI);
        SWRLClassAtom struct = factory.getSWRLClassAtom(getClassFromName(cl_Structure), varStruct);
        
        SWRLObjectPropertyAtom skillAtom =  factory.getSWRLObjectPropertyAtom(getObjectPropertyFromName(pr_Activity), varI, varStruct);
        SWRLObjectPropertyAtom previousPhaseAtom =  factory.getSWRLObjectPropertyAtom(previousPhase, varP, varPre);

        Set<SWRLAtom> body = new HashSet<SWRLAtom>();
        body.add(p);
        body.add(pre);
        body.add(instr);
        body.add(struct);
        body.add(skillAtom);
        body.add(previousPhaseAtom);
        SWRLClassAtom head = factory.getSWRLClassAtom(detectedPhase, varP);
        
        return factory.getSWRLRule(body,Collections.singleton(head));
        
	}
	
	public static OWLClass getClassFromName (String text) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass textOwlClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + text));
		return textOwlClass; 
		
	}
	
	public static OWLObjectProperty getObjectPropertyFromName (String text) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty textOwlObjectProperty = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + text));
		return textOwlObjectProperty; 
		
	}
	
	public static Set<OWLAxiom> getPhaseClassesAndIndividualsAndPreviousPhaseProperties () throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    
	    System.out.println("creating phase classes...");
	    OWLClass phaseStart = factory.getOWLClass(IRI.create(ontologyIRI + "#start"));
	    OWLClass phaseMobilisation = factory.getOWLClass(IRI.create(ontologyIRI + "#mobilisation"));
	    OWLClass phasePort_placement = factory.getOWLClass(IRI.create(ontologyIRI + "#port_placement"));
	    OWLClass phaseDissection = factory.getOWLClass(IRI.create(ontologyIRI + "#dissection"));
	    OWLClass phaseResection_cystic_Artery = factory.getOWLClass(IRI.create(ontologyIRI + "#resection_cystic_artery"));
	    OWLClass phaseResection_cystic_duct = factory.getOWLClass(IRI.create(ontologyIRI + "#resection_cystic_duct"));
	    OWLClass phaseResection_gallbladder = factory.getOWLClass(IRI.create(ontologyIRI + "#resection_gallbladder"));
	    OWLClass phaseClosure = factory.getOWLClass(IRI.create(ontologyIRI + "#closure"));
	    OWLClass phaseDrain = factory.getOWLClass(IRI.create(ontologyIRI + "#drain"));
	    OWLClass phaseEnd = factory.getOWLClass(IRI.create(ontologyIRI + "#end"));

	    System.out.println("creating phase individuals...");
		OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#start1"));
		OWLNamedIndividual port_placement1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#port_placement1"));
		OWLNamedIndividual mobilisation1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#mobilistation1"));
		OWLNamedIndividual dissection1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#dissection1"));
		OWLNamedIndividual resection_cystic_artery1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#resection_cystic_artery1"));
		OWLNamedIndividual resection_cystic_duct1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#resection_cystic_duct1"));
		OWLNamedIndividual resection_gallbladder1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#resection_gallbladder1"));
		OWLNamedIndividual closure1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#closure1"));
		OWLNamedIndividual drain1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#drain1"));
		OWLNamedIndividual end1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#end1"));
		
	    System.out.println("creating class-individual relationships for phases...");
	    Set<OWLAxiom> OurAxioms = new HashSet<OWLAxiom>();
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseStart, start1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phasePort_placement, port_placement1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseMobilisation, mobilisation1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDissection, dissection1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_cystic_Artery, resection_cystic_artery1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_cystic_duct, resection_cystic_duct1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_gallbladder, resection_gallbladder1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseClosure, closure1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDrain, drain1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseEnd, end1));
        
        System.out.println("creating previous phase relations....");
        
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, start1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, start1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, start1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, start1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, port_placement1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, port_placement1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, mobilisation1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_artery1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_duct1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, dissection1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_duct1, resection_cystic_artery1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, resection_cystic_artery1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_artery1, resection_cystic_duct1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, resection_cystic_duct1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, resection_gallbladder1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, closure1, resection_gallbladder1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, closure1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, closure1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, drain1));
	    
	    return OurAxioms;
	}
	
	private static OWLAxiom setCurrentPhase(String somePhase) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    
		OWLNamedIndividual somePhase1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + somePhase));
		return factory.getOWLClassAssertionAxiom(currentPhase, somePhase1);
	}
	
	private static OWLAxiom getCurrentPhase() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    
		OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#start1"));
		return factory.getOWLClassAssertionAxiom(currentPhase, start1);
	}
}