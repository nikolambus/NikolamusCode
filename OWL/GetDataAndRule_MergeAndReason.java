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
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class ChildPughA {

	private static final String BASE_IRI = "http://www.semanticweb.org/niko/";
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

		//load at first the patient file as an ontology
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(new File("C:/Users/Administrator/Desktop/Baustellen/Benedikt/Rules_benchmark/n3_swrl_rdf/Why/patient_swrl.owl"));
		OWLOntology cpa = m.loadOntology(ontologyIRI);
		
		//load then the rule as an another ontology
		IRI ontologyIRI2 = IRI.create(new File("C:/Users/Administrator/Desktop/Baustellen/Benedikt/Rules_benchmark/n3_swrl_rdf/Why/simple_rule_swrl.owl"));
		OWLOntology rulesCCE = m.loadOntology(ontologyIRI2);
				
		OWLOntologyMerger merger = new OWLOntologyMerger(m);
		//here we can not use the same IRI as for our main ontology curac1,
		//because the compiler instead of compiling performs complaining 
		IRI ontologyIRI3 = IRI.create("http://Niko/mergedont");
		OWLOntology mergedOnt = merger.createMergedOntology(m, ontologyIRI3);     
			
		//save merged ontology in RDF/XML format
		File mergedOntOutput = new File("C:/Users/Administrator/Desktop/MergeResult.owl");
		mergedOntOutput.createNewFile();
		m.saveOntology(mergedOnt, IRI.create(mergedOntOutput.toURI()));  	
	
		//initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(mergedOnt);		
	
		//Now it should perform reasoning
		reasoner.getKB().realize();
				
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m, mergedOnt);
		
		//and save again the result - merged and reasoned ontology
		File file = new File("C:/Users/Administrator/Desktop/MergeAndReasonResult.owl");
		file.createNewFile();
		m.saveOntology(mergedOnt, IRI.create(file.toURI()));
	}
}
