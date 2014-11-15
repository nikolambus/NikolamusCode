package SemFormToPatient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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


public class RDFExportParser {

    // Use here the same IRI as your SMW or Surgipedia
  	public IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
  	
    // Use special IRI for individuals
   	public IRI indIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/Individual#");	
   	
  	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public OWLDataFactory factory = manager.getOWLDataFactory();

    public String name = null;
    public String phase;
    public String symptom1;
    public String symptom2;
    public String untersuchung1;
    public String untersuchung2;
    public String untersuchung3;
    public String uErg1;
    public String uErg2;
    public String uErg3;
    public String diagnose1;
    public String diagnose2;

    
    public void buildPatient(String rdfExportText, String outputPath) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {

    	OWLOntology o = manager.createOntology(basicIRI);

    	
    	/* before we go through RDF export text line by line we should assert that all subject individuals are initialized.
    	   E.g.: the property "Hat_Name" occurs after the property "Hat_Diagnose" but the individual extracted from the line with "Hat_Name" 
    	   is needed already by at the "Hat_Diagnose" line. So we should initialize it at the very beginning. */
    	/* this lookahead can be done with the multiline modus of pattern.matches - (?sm) */
    	
    	if (Pattern.matches("(?sm).*^		<property:Hat_Name rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>$.*", rdfExportText)) {
			name = extractPatientValueMultiLine(rdfExportText, "Hat_Name");
    		bindIndividualToTheClass(o, getClassFromName("Patient"), name);
		}
    	
		//going through the RDF export text line by line 
	    BufferedReader br = new BufferedReader(new StringReader(rdfExportText));
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	System.out.println(line);
	    	
	    	// match the line with a concept. Internal shortcuts are used.
	    	if (Pattern.matches("		<property:Hat_Phase rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		// we would like to have the "1"-endings for all our individuals (excepting name) to better differ them from the classes .
	    		phase = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), phase);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("BefindetSichInDerPhase"), name, phase);
	    	}
	    	
	    	if (Pattern.matches("		<property:Hat_Symptom1 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		symptom1 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), symptom1);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Hat_Symptom"), name, symptom1);
	    	}
	    	
	    	if (Pattern.matches("		<property:Hat_Symptom2 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		symptom2 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), symptom2);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Hat_Symptom"), name, symptom2);
	    	}
	    	
	       	if (Pattern.matches("		<property:Hat_Untersuchung1 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		untersuchung1 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), untersuchung1);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("WirdUntersuchtDurch"), name, untersuchung1);
	    	}
	       	
	    	if (Pattern.matches("		<property:Hat_Untersuchungsergebnis1 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		uErg1 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), uErg1);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Zeigt"), untersuchung1, uErg1);
	    	}
	    	
	    	if (Pattern.matches("		<property:Hat_Untersuchung2 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		untersuchung2 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), untersuchung2);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("WirdUntersuchtDurch"), name, untersuchung2);
	    	}
	       	
	    	if (Pattern.matches("		<property:Hat_Untersuchungsergebnis2 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		uErg2 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), uErg2);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Zeigt"), untersuchung2, uErg2);
	    	}
	    	
	    	if (Pattern.matches("		<property:Hat_Untersuchung3 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		untersuchung3 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), untersuchung3);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("WirdUntersuchtDurch"), name, untersuchung3);
	    	}
	    	
	    	if (Pattern.matches("		<property:Hat_Untersuchungsergebnis3 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		uErg3 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), uErg3);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Zeigt"), untersuchung3, uErg3);
	    	}    		
	    	
	    	if (Pattern.matches("		<property:Hat_Diagnose1 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		diagnose1 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), diagnose1);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Hat_Diagnose"), name, diagnose1);
	    	}    	
	    	
	    	if (Pattern.matches("		<property:Hat_Diagnose2 rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
	    		diagnose2 = extractPatientValue(line) + "1";
	    		bindIndividualToTheClass(o, getClassFromName(extractPatientValue(line)), diagnose2);
	    		bindIndividualsToTheProperty(o, getObjectPropertyFromName("Hat_Diagnose"), name, diagnose2);
	    	} 
	    }
	    
	    br.close();	

	    //Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        manager.saveOntology(o, IRI.create(output.toURI()));
		
  	}
  	
  	public OWLClass getClassFromName (String clsLabel) {
		OWLClass cls = factory.getOWLClass(IRI.create(basicIRI + clsLabel));
		return cls; 
	}

	public OWLObjectProperty getObjectPropertyFromName (String propLabel) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(basicIRI + propLabel));
		return prop; 	
	}
	
	public void bindIndividualToTheClass (OWLOntology o, OWLClass cls, String indLabel) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(indIRI + indLabel));
		manager.addAxiom(o, factory.getOWLClassAssertionAxiom(cls, ind));
		
	}
	
	public void bindIndividualsToTheProperty (OWLOntology o, OWLObjectProperty prop, String ind1Label, String ind2Label) {
		OWLNamedIndividual ind1 = factory.getOWLNamedIndividual(IRI.create(indIRI + ind1Label));
		OWLNamedIndividual ind2 = factory.getOWLNamedIndividual(IRI.create(indIRI + ind2Label));
	    manager.addAxiom(o, factory.getOWLObjectPropertyAssertionAxiom(prop, ind1, ind2));
	}      
	
	//e.g.: parse "Fred" from "		<property:Hat_Name rdf:resource=\"&wiki;Fred\"/>"
	public String extractPatientValue (String line) {
		
		//find out what string hides away behind the pattern ([a-zA-Z_0-9:,?\"./]+)
		Pattern pattern = Pattern.compile("\\s*<property:[a-zA-Z_0-9:,?\"./]+ rdf:resource=\"&wiki;([a-zA-Z_0-9:,?\"./]+)\"/>");
		Matcher mtch = pattern.matcher(line);
		if (mtch.find()) {
			return mtch.group(1);
		}
		else 
			return ""; 
	}
	
	/* e.g.: parse "Fred" from "		<property:PROP rdf:resource=\"&wiki;Fred\"/>" 
	found from the whole RDF export text via multilines modus of pattern.matches */ 
	public String extractPatientValueMultiLine (String rdfExportText, String prop) {
		
		//find out what string hides away behind the pattern ([a-zA-Z_0-9:,?\"./]+)
		Pattern pattern = Pattern.compile("(?sm).*^		<property:"+prop+" rdf:resource=\"&wiki;([a-zA-Z_0-9:,?\"./]+)\"/>$.*");
		Matcher mtch = pattern.matcher(rdfExportText);
		if (mtch.find()) {
			return mtch.group(1);
		}
		else 
			return ""; 
	}

}
