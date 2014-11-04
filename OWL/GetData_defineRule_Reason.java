package cz.makub;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class OurWork4 {

	private static final String BASE_IRI = "http://surgipedia.sfb125.de/images/a/a4/Curac1";
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(new File("D:/DiplArbeit/OurWork/SurgiPedia/Curac1.owl"));
		OWLOntology curac1 = m.loadOntology(ontologyIRI);
		OWLDataFactory df = m.getOWLDataFactory();
		
		//initialize reasoner
		//OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
        //OWLReasoner reasoner = reasonerFactory.createReasoner(curac1, new SimpleConfiguration());
				
		//Get a reference to the needed classes so that we can process it with the reasoner.
		OWLClass phaseClass = df.getOWLClass(IRI.create(BASE_IRI + "#Phase"));
		OWLClass activePhaseClass = df.getOWLClass(IRI.create(BASE_IRI + "#ActivePhase"));
		OWLClass gallbladderClass = df.getOWLClass(IRI.create(BASE_IRI + "#gallbladder"));
		OWLClass atraumatic_grasperClass = df.getOWLClass(IRI.create(BASE_IRI + "#atraumatic_grasper"));

	    
		//Now we initialize the needed properties which will connect our individual in a SWRL rule.
	    OWLObjectProperty previousPhaseProperty = df.getOWLObjectProperty(IRI.create(BASE_IRI + "#previousPhase"));
	    OWLObjectProperty graspProperty = df.getOWLObjectProperty(IRI.create(BASE_IRI + "#grasp"));

	    
		//Now let's define a simple SWRL rule.
		SWRLVariable p = df.getSWRLVariable(IRI.create(BASE_IRI + "#P"));
		SWRLVariable pre = df.getSWRLVariable(IRI.create(BASE_IRI + "#Pre"));
		SWRLVariable struct = df.getSWRLVariable(IRI.create(BASE_IRI + "#Struct"));
		SWRLVariable l = df.getSWRLVariable(IRI.create(BASE_IRI + "#l"));
        SWRLClassAtom classAtom1 = df.getSWRLClassAtom(phaseClass, pre);
        SWRLClassAtom classAtom2 = df.getSWRLClassAtom(phaseClass, p);
        SWRLClassAtom classAtom3 = df.getSWRLClassAtom(gallbladderClass, struct);
        SWRLClassAtom classAtom4 = df.getSWRLClassAtom(atraumatic_grasperClass, l);
        SWRLObjectPropertyAtom propAtom1 = df.getSWRLObjectPropertyAtom(graspProperty, l, struct);
        SWRLObjectPropertyAtom propAtom2 = df.getSWRLObjectPropertyAtom(previousPhaseProperty, p, pre);
        Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
        antecedent.add(classAtom1);
        antecedent.add(classAtom2);
        antecedent.add(classAtom3);
        antecedent.add(classAtom4);
        antecedent.add(propAtom1);
        antecedent.add(propAtom2);
        SWRLRule rule = df.getSWRLRule(antecedent, Collections.singleton(df.getSWRLClassAtom(activePhaseClass, p)));
        m.applyChange(new AddAxiom(curac1, rule));
        
		//Now we initialize a couple of individuals of the class (type) Phase
		OWLIndividual startPhase = df.getOWLNamedIndividual(IRI.create(BASE_IRI + "#start"));
	    OWLIndividual dissectionPhase = df.getOWLNamedIndividual(IRI.create(BASE_IRI + "#dissection"));
		OWLIndividual gallbladder_infundibulum = df.getOWLNamedIndividual(IRI.create(BASE_IRI + "#gallbladder_infundibulum"));
	    OWLIndividual atraumatic_grasper = df.getOWLNamedIndividual(IRI.create(BASE_IRI + "#atraumatic_grasper"));
	    
	    //create a set of our axioms that specify that a certain individual belongs to a certain class 
	    Set<OWLAxiom> IndividualsCreationAxioms = new HashSet<OWLAxiom>();
	    IndividualsCreationAxioms.add(df.getOWLClassAssertionAxiom(phaseClass, startPhase));
	    IndividualsCreationAxioms.add(df.getOWLClassAssertionAxiom(phaseClass, dissectionPhase));
	    IndividualsCreationAxioms.add(df.getOWLClassAssertionAxiom(gallbladderClass, gallbladder_infundibulum));
	    IndividualsCreationAxioms.add(df.getOWLClassAssertionAxiom(atraumatic_grasperClass, atraumatic_grasper));

        // Add all axioms listet above
        m.addAxioms(curac1, IndividualsCreationAxioms);
        
        //describe real relationships between our individuals
	    OWLObjectPropertyAssertionAxiom assertion_start_dissection = df.getOWLObjectPropertyAssertionAxiom(previousPhaseProperty, dissectionPhase, startPhase);
        m.applyChange(new AddAxiom(curac1, assertion_start_dissection));
        OWLObjectPropertyAssertionAxiom assertion_grasp = df.getOWLObjectPropertyAssertionAxiom(graspProperty, atraumatic_grasper, gallbladder_infundibulum);
        m.applyChange(new AddAxiom(curac1, assertion_grasp));
        
        //Before we start with reasoning save the changed ontology in an external file
        File output = new File("D:/DiplArbeit/OurWork/SurgiPedia/Curac1_before_reasoning_2.owl");
        output.createNewFile();
        m.saveOntology(curac1, IRI.create(output.toURI()));
		
        //initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(curac1);
		
		KnowledgeBase kb = reasoner.getKB();
		// Create a Pellet graph using the KB from OWLAPI
		PelletInfGraph graph = new org.mindswap.pellet.jena.PelletReasoner().bind( kb );
		// Wrap the graph in a model
		InfModel model = ModelFactory.createInfModel( graph );
		
		// Use the model to answer SPARQL queries
		//...
		
		//Now it should perform reasoning
		reasoner.getKB().realize();
		
		//And now print the new individual-class assignment on the screen
		//reasoner.getKB().printClassTree();
		
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m, curac1);
		
		//Now we can save our ontology after reasoning in an another external file 
		File file = new File("D:/DiplArbeit/OurWork/SurgiPedia/Curac1_after_reasoning_2.owl");
		file.createNewFile();
		m.saveOntology(curac1, IRI.create(file.toURI()));
	}
}
