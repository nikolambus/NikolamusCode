package cz.makub;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

public class MergeOntologies2 {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

		//load at first the ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(new File("C:/Dokumente und Einstellungen/VNG/Desktop/HBPageExport.owl"));
		OWLOntology curac1 = manager.loadOntology(ontologyIRI);
	
		//load then the rules as ontology
		IRI ontologyIRI2 = IRI.create(new File("C:/Dokumente und Einstellungen/VNG/Desktop/HBRuleWithExportIRI.owl"));
		OWLOntology rulesCCE = manager.loadOntology(ontologyIRI2);
		
		OWLOntologyMerger merger = new OWLOntologyMerger(manager);
		//here we can not use the same IRI as for our main ontology curac1,
		//because the compiler instead of compiling performs complaining 
	    IRI ontologyIRI3 = IRI.create("http://Niko/mergedont");
	    OWLOntology mergedOnt = merger.createMergedOntology(manager, ontologyIRI3);     
	
	    //save in RDF/XML format
        File mergedOntOutput = new File("C:/Dokumente und Einstellungen/VNG/Desktop/HBPageExportPlusRule.owl");
        mergedOntOutput.createNewFile();
        manager.saveOntology(mergedOnt, IRI.create(mergedOntOutput.toURI()));  	
        
	}
}