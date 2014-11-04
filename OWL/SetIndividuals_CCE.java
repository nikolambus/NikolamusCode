package cz.makub;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class SetIndividuals_CCE {
	
	//Use here the same IRI as for the main ontology Curac1 in order to have consistent concepts
		public static IRI ontologyIRI = IRI.create("http://surgipedia.sfb125.de/images/a/a4/Curac1");	
		public static File file0 = new File ("D:/DiplArbeit/OurWork/ForPapers/PhaseRec/ONTOLOGY/Mergedont_Curac1_CCE_new.owl");
		public static File file1 = new File ("D:/DiplArbeit/OurWork/ForPapers/PhaseRec/ONTOLOGY/Mergedont_Curac1_CCE_with_phase_individs.owl");
		public static OWLObjectProperty previousPhase;
		public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    public static OWLDataFactory factory = manager.getOWLDataFactory();
		
		public static void main (String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		    
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file0);
		    previousPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#previousPhase"));

			//create classes, individuals and previousPhase-relationships for phases 
		    Set<OWLAxiom> phases = setPhaseClassesAndIndividualsAndPreviousPhaseProperties();
		    manager.addAxioms(ontology, phases);
		    
		    //save in RDF/XML format
	        file1.createNewFile();
	        manager.saveOntology(ontology, IRI.create(file1.toURI()));
		}
	
		//set OP type specific phase properties and create appropriate individuals
		public static Set<OWLAxiom> setPhaseClassesAndIndividualsAndPreviousPhaseProperties () throws OWLOntologyCreationException {
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
}
