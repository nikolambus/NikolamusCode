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

public class RulesForPancreas {
	
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
	    
	    //init the currentPhase with "start" and other basic concepts with their IRIs
	    currentPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#CurrentPhase"));
	    previousPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#previousPhase"));
	    detectedPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#DetectedPhase"));
	    
	    //create rule1
	    SWRLRule rulePP = buildRule("port_placement", "port", "unknown", "place");
        manager.applyChange(new AddAxiom(ontology, rulePP));

        
        
	    //create rule2   
	    SWRLRule ruleMob1 = buildRule("mobilisation", "atraumatic_grasper", "gastrocolic_ligament", "grasp");
        manager.applyChange(new AddAxiom(ontology, ruleMob1));
        
        SWRLRule ruleMob2 = buildRule("mobilisation", "atraumatic_grasper", "greater_omentum", "grasp");
        manager.applyChange(new AddAxiom(ontology, ruleMob2));
        
        SWRLRule ruleMob3 = buildRule("mobilisation", "sharp_instrument", "gastrocolic_ligament", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleMob3));
        
        SWRLRule ruleMob4 = buildRule("mobilisation", "sharp_instrument", "adhesion", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleMob4));

        
        
	    SWRLRule ruleDis1 = buildRule("dissection", "sharp_instrument", "dorsal_parietal_peritoneum", "cutting_action");
	    manager.applyChange(new AddAxiom(ontology, ruleDis1));

	    SWRLRule ruleDis2 = buildRule("dissection", "instrument", "dorsal_parietal_peritoneum", "blunt_dissect");
	    manager.applyChange(new AddAxiom(ontology, ruleDis2));

	    SWRLRule ruleDis3 = buildRule("dissection", "atraumatic_grasper", "dorsal_parietal_peritoneum", "grasp");
        manager.applyChange(new AddAxiom(ontology, ruleDis3));

	    SWRLRule ruleDis4 = buildRule("dissection", "blunt_instrument", "splenic_artery", "blunt_dissect");
        manager.applyChange(new AddAxiom(ontology, ruleDis4));
        
	    SWRLRule ruleDis5 = buildRule("dissection", "sharp_instrument", "splenic_artery", "dissect");
        manager.applyChange(new AddAxiom(ontology, ruleDis5));

	    SWRLRule ruleDis6 = buildRule("dissection", "clip", "splenic_artery", "clipping");
        manager.applyChange(new AddAxiom(ontology, ruleDis6));

	    SWRLRule ruleDis7 = buildRule("dissection", "instrument", "splenic_artery", "knot");
        manager.applyChange(new AddAxiom(ontology, ruleDis7));        
        
        SWRLRule ruleDis8 = buildRule("dissection", "blunt_instrument", "splenic_vein", "blunt_dissect");
        manager.applyChange(new AddAxiom(ontology, ruleDis8));  
        
        SWRLRule ruleDis9 = buildRule("dissection", "sharp_instrument", "splenic_vein", "dissect");
        manager.applyChange(new AddAxiom(ontology, ruleDis9));  
        
        SWRLRule ruleDis10 = buildRule("dissection", "clip", "splenic_vein", "clipping");
        manager.applyChange(new AddAxiom(ontology, ruleDis10));  
        
        SWRLRule ruleDis11 = buildRule("dissection", "instrument", "splenic_vein", "knot");
        manager.applyChange(new AddAxiom(ontology, ruleDis11));  
        
        
        
        
        
	    SWRLRule ruleRes1 = buildRule("resection", "sharp_instrument", "pancreas", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes1));

	    SWRLRule ruleRes2 = buildRule("resection", "sharp_instrument", "retropancreatic_tissue", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes2));

	    SWRLRule ruleRes3 = buildRule("resection", "sharp_instrument", "pancreas", "dissect");
        manager.applyChange(new AddAxiom(ontology, ruleRes3));

	    SWRLRule ruleRes4 = buildRule("resection", "sharp_instrument", "tumor", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes4));

	    SWRLRule ruleRes5 = buildRule("resection", "sharp_instrument", "cyst", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes5));

	    SWRLRule ruleRes6 = buildRule("resection", "stapler", "pancreas", "resect");
        manager.applyChange(new AddAxiom(ontology, ruleRes6));

	    SWRLRule ruleRes7 = buildRule("resection", "needle_holder", "pancreas", "suture");
        manager.applyChange(new AddAxiom(ontology, ruleRes7));

	    SWRLRule ruleRes8 = buildRule("resection", "needle_holder", "stomach", "suture");
        manager.applyChange(new AddAxiom(ontology, ruleRes8));
        
        SWRLRule ruleRes9 = buildRule("resection", "sharp_instrument", "splenic_artery", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes9));
        
        SWRLRule ruleRes10 = buildRule("resection", "sharp_instrument", "splenic_vein", "cutting_action");
        manager.applyChange(new AddAxiom(ontology, ruleRes10));
        
        
        
        SWRLRule ruleClose1 = buildRule("closure", "specimen_bag", "organ", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleClose1));
        
        
        
        SWRLRule ruleDrainage1 = buildRule("drain", "drainage", "unknown", "instrumental_property");
        manager.applyChange(new AddAxiom(ontology, ruleDrainage1));
        
        //Strange rule with the instrument "drain". It should be called "drainage" as by the next two rules.
        //SWRLRule ruleDrain2 = buildRule("drain", "drain", "unknown", "instrumental_property");
        //manager.applyChange(new AddAxiom(ontology, ruleDrain2));
     
        
        //save in RDF/XML format
        File output = new File("D:/DiplArbeit/OurWork/USECASE/RULES/Pancreas_rules.owl");
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
	    OWLClass phasePort_placement = factory.getOWLClass(IRI.create(ontologyIRI + "#port_placement"));
	    OWLClass phaseMobilisation = factory.getOWLClass(IRI.create(ontologyIRI + "#mobilisation"));
	    OWLClass phaseDissection = factory.getOWLClass(IRI.create(ontologyIRI + "#dissection"));
	    OWLClass phaseResection = factory.getOWLClass(IRI.create(ontologyIRI + "#resection"));
	    OWLClass phaseClosure = factory.getOWLClass(IRI.create(ontologyIRI + "#closure"));
	    OWLClass phaseDrain = factory.getOWLClass(IRI.create(ontologyIRI + "#drain"));
	    OWLClass phaseEnd = factory.getOWLClass(IRI.create(ontologyIRI + "#end"));

	    System.out.println("creating phase individuals...");
		OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#start1"));
		OWLNamedIndividual port_placement1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#port_placement1"));
		OWLNamedIndividual mobilisation1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#mobilistation1"));
		OWLNamedIndividual dissection1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#dissection1"));
		OWLNamedIndividual resection1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#resection1"));
		OWLNamedIndividual closure1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#closure1"));
		OWLNamedIndividual drain1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#drain1"));
		OWLNamedIndividual end1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#end1"));
		
	    System.out.println("creating class-individual relationships for phases...");
	    Set<OWLAxiom> OurAxioms = new HashSet<OWLAxiom>();
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseStart, start1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phasePort_placement, port_placement1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseMobilisation, mobilisation1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDissection, dissection1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection, resection1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseClosure, closure1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDrain, drain1));
	    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseEnd, end1));
        
        System.out.println("creating previous phase relations....");
        
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, start1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, start1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, port_placement1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, port_placement1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, port_placement1));

	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, mobilisation1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, mobilisation1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, mobilisation1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, mobilisation1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, mobilisation1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, mobilisation1));

	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, dissection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, dissection1));

	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, closure1, resection1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, resection1));
	  
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, closure1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, closure1));
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, closure1));
	    
	    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, drain1));
	    
	    return OurAxioms;
	}
	
	private static OWLAxiom setCurrentPhase() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    
		OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#start1"));
		return factory.getOWLClassAssertionAxiom(currentPhase, start1);
	}
}