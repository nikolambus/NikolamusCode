package cz.makub;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class RulesForCCE_test2 {

	//Use here the same IRI as for the main ontology Curac1 in order to have consistent concepts
	public static IRI ontologyIRI = IRI.create("http://surgipedia.sfb125.de/images/a/a4/Curac1");	
	public static File file1 = new File ("D:/DiplArbeit/OurWork/USECASE/RULES/CCE_rules_test2.owl");
	public static OWLClass currentPhase; 
    public static OWLClass detectedPhase;
    public static OWLObjectProperty previousPhase;
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLDataFactory factory = manager.getOWLDataFactory();

    
	public static void main (String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file1);
	    
	    //create basic concepts with their IRIs
	    currentPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#CurrentPhase"));
	    previousPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#previousPhase"));
	    detectedPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#DetectedPhase"));
	    
	    //init the currentPhase-class with the "start"-individual
	    OWLNamedIndividual dtecs1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#D.Tecs"));
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(detectedPhase, dtecs1));
		
		OWLNamedIndividual pumba1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#Pumba"));
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(currentPhase, pumba1));
			
		OWLNamedIndividual timon1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "Timon"));
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(currentPhase, timon1));
	    
		//test, can I connect to the same ontology via reading it as a parameter in the setCurrentPhase-method.
		//It would work if the currentPhase-class would contain just the "Simba"-individual and nothing more.
		setCurrentPhase(ontology, "Simba");
        System.out.println ("Existing individuals from the mainClass's point of view :" + ontology.getIndividualsInSignature());

		//save in RDF/XML format
        File output = file1;
        output.createNewFile();
        manager.saveOntology(ontology, IRI.create(output.toURI()));
               
	}

	public static OWLClass getClassFromName (String text) {
		OWLClass textOwlClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + text));
		return textOwlClass; 
		
	}

	/* removes all instances of CurrentClass and adds to the CurrentClass a new instance whose name is specified as a parameter "somePhase". */
	private static void setCurrentPhase(OWLOntology onto, String somePhase) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(onto);

        //Ask the reasoner for the instances of currentPhase
        NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(currentPhase, true);
        
        //The reasoner returns a NodeSet. The NodeSet contains individuals.
        //We don't particularly care about the equivalences, so we will flatten this set of sets and print the result.
        //We just want the individuals, so get a flattened set.
        Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();

        //init a remover-visitor
        OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(onto));

        System.out.println("Instances of currentPhase: ");
        for (OWLNamedIndividual ind : individuals) {
        	System.out.println("    " + ind);
		
        	//accept that this ind should be removed
        	ind.accept(remover);
        }
        System.out.println("\n");
        manager.applyChanges(remover.getChanges());

        OWLNamedIndividual somePhase1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + somePhase));
        manager.addAxiom(onto, factory.getOWLClassAssertionAxiom(currentPhase, somePhase1));
        
        System.out.println ("Existing individuals from the method's point of view: " + onto.getIndividualsInSignature());
	}
	
	private static OWLAxiom getCurrentPhase() throws OWLOntologyCreationException {	    
		return null;
	}
}