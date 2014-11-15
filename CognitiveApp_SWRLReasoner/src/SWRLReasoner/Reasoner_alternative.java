package SWRLReasoner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class Reasoner_alternative {

    // Use here the same IRI as your SMW or Surgipedia
  	public static IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
  	public static OWLOntologyManager m = OWLManager.createOWLOntologyManager();
  	public static OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
  	public static OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
  	public static OWLOntologyManager m3 = OWLManager.createOWLOntologyManager();

    public static void action(String rule, String patient, String outputPath) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

    	/* obsolete solution 1 which works only if we are working with one manager. Thus ontologies should have 
    	 * IRIs different from "http://localhost/mediawiki/index.php/Special:URIResolver/".
    	*/
    	OWLOntology ruleOnto = m.loadOntology(IRI.create(rule));
    	OWLOntology patOnto = m.loadOntology(IRI.create(patient));
    	
    	OWLOntologyMerger merger = new OWLOntologyMerger(m);
		OWLOntology mergedOnt = merger.createMergedOntology(m, basicIRI); 
    	
    	/* obsolete solution 2 does not work because ruleOnto and patOnto have the same IRI and 
  	  	ontologies substitutes the former onto with the latter one and does not contain them both  
  		
  		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
  		ontologies.add(patOnto);
		OWLOntology mergedOnt = m3.createOntology(basicIRI, ontologies); */
    	/* load at first the rule and the patient files as ontologies. 
    	Actually they have the same base IRI and there is a rule: one ontology per IRI within a manager. 
    	So we need different manager. */

    	
		/*
		//initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(mergedOnt);		
		
		//manager.addAxioms(newOntology, oldOntology.getAxioms());
		//Now it should perform reasoning
		reasoner.getKB().realize();
				
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m, mergedOnt);
		*/
		
		//Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        m3.saveOntology(mergedOnt, IRI.create(output.toURI()));
    }	
    
}
