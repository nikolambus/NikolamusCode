

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
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class RulesExecution {

		//Use here the same IRI as for the main ontology Curac1 in order to have consistent concepts
		public static IRI ontologyIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
		public static File file0 = new File ("C:/Users/Administrator/Desktop/Baustellen/Martin/von Protege/RudisOntology_with_3_initial_rules_and_individuals.owl");
		public static File file1 = new File ("C:/Users/Administrator/Desktop/Baustellen/Martin/von Protege/RudisOntology_with_3_initial_rules_and_individuals_test_remove.owl");
		public static OWLClass currentPhase; 
	    public static OWLClass detectedPhase;
	    public static OWLObjectProperty previousPhase;
		public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    public static OWLDataFactory factory = manager.getOWLDataFactory();

	    
		public static void main (String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file0);
		    
			OWLNamedIndividual patient1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "Fred"));
			OWLObjectProperty phaseProperty = getObjectPropertyFromName("befindet_sich_in_der_Phase");

		    
		    removePropertyValuesForIndividual(ontology, patient1, phaseProperty);
			//save in RDF/XML format
	        file1.createNewFile();
	        manager.saveOntology(ontology, IRI.create(file1.toURI()));
	    
	        /*
	        Important steps from RulesManagement8.java (owltutorial). Here unused
	        
	        //create basic concepts with their IRIs
		    currentPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#CurrentPhase"));
		    previousPhase = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#previousPhase"));
		    detectedPhase = factory.getOWLClass(IRI.create(ontologyIRI + "#DetectedPhase"));
		    
		    //We load an ontology that already contains rules
		    //Set<SWRLRule> rules = getRules(ontology);
		    
			setCurrentPhase(ontology, "port_placement1");
		    getDetectedPhase(ontology, "atraumatic_grasper", "gallbladder_fundus", "grasp");
		     
		    */
	        
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
	  	}
		
		//Here we do not give to this method a Set<SWRLRule> parameter because the rules are already in the ontology 
		private static void getDetectedPhase(OWLOntology onto, String instrument, String structure, String activity) throws OWLOntologyCreationException {	    
			OWLClass instrumentClass = getClassFromName(instrument);
			OWLClass structureClass = getClassFromName(structure);
			OWLObjectProperty activityProperty = getObjectPropertyFromName(activity);
			
			/*SWRLRule neededRule = null;
			
			for (SWRLRule i : rules) {
				//System.out.println ("Actual rule is: \n" + i);
				if (i.getClassesInSignature().contains(instrumentClass) && i.getClassesInSignature().contains(structureClass)) {
					if (i.getObjectPropertiesInSignature().contains(activityProperty))
						neededRule = i;
				}				
			}
			System.out.println ("The used rule is: \n" + neededRule);*/

			//create individuals for instrument and structure classes and connect them via activity
			OWLNamedIndividual instrument1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + instrument + "1"));
			OWLNamedIndividual structure1 = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + structure + "1"));
			Set<OWLAxiom> ourAxioms = new HashSet<OWLAxiom>();
			ourAxioms.add(factory.getOWLClassAssertionAxiom(instrumentClass, instrument1));
			ourAxioms.add(factory.getOWLClassAssertionAxiom(structureClass, structure1));
		    ourAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(activityProperty, instrument1, structure1));
		    manager.addAxioms(onto, ourAxioms);
	        
	        //initialize the reasoner with help of the Pellet-package
	        PelletReasoner reasoner2 = PelletReasonerFactory.getInstance().createReasoner(onto);
	     		
	     	//Now it should perform reasoning
	     	reasoner2.getKB().realize();
	     		
	     	//And now print the new individual-class assignment on the screen
	     	//reasoner.getKB().printClassTree();
	     		
	     	//Now let us fill the results of the reasoning in our ontology variable
	     	InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner2);
	     	generator.fillOntology(manager, onto);
	     	
	     	//Now we just finding out which individual was asserted to the detectedPhase-class 
		    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		    OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
	        NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getInstances(detectedPhase, true);
	        Set<OWLNamedIndividual> detectedIndis = individualsNodeSet.getFlattened();
	        //Get the first instance from the class currentPhase with help of "Iterator" and "hasNext-command"
	        Iterator<OWLNamedIndividual> i = detectedIndis.iterator();
	        OWLNamedIndividual detected = i.next();
	        System.out.println ("Detected phase is: \n" + detected);
		}
		
	public static OWLClass getClassFromName (String text) {
		OWLClass textOwlClass = factory.getOWLClass(IRI.create(ontologyIRI + text));
		return textOwlClass; 
		
	}

	public static OWLObjectProperty getObjectPropertyFromName (String text) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty textOwlObjectProperty = factory.getOWLObjectProperty(IRI.create(ontologyIRI + text));
		return textOwlObjectProperty; 
		
	}
	
	public static void removePropertyValuesForIndividual (OWLOntology onto, OWLNamedIndividual individ, OWLObjectProperty property) {

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
	    
        //Ask the reasoner for the instances of currentPhase
        NodeSet<OWLNamedIndividual> individualsNodeSet = reasoner.getObjectPropertyValues(individ, property);        
        Set<OWLNamedIndividual> individuals = individualsNodeSet.getFlattened();

        //init a remover-visitor
        OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(onto));

        System.out.println("Values of " + property + ":");
        for (OWLNamedIndividual ind : individuals) {
        	System.out.println("    " + ind);
		
        	//accept that this ind should be removed
        	ind.accept(remover);
        }
        System.out.println("\n");
        manager.applyChanges(remover.getChanges());
        
	}

	
}
