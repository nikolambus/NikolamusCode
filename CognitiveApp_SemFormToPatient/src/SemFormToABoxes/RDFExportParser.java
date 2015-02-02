package SemFormToABoxes;
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
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
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

   	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
   	
  	public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public OWLDataFactory factory = manager.getOWLDataFactory();
    
    /* we use these lists to store the subject, predicate and object of a triple. 
     * 1st subject corresponds to the 1st predicate and 1st object 
     */
	public List<String> SubjectsList = new ArrayList<String>(); 
	public List<String> PredicatesList = new ArrayList<String>(); 
	public List<String> ObjectsList = new ArrayList<String>(); 

    public void buildListsWithSubjectPredicateObject(String rdfexport) throws IOException {
  		
    	//going through the RDF export line by line 
	    BufferedReader br = new BufferedReader(new StringReader(rdfexport));
	    
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	//System.out.println(line);
	    	
	    	/* Predicates without key word "Property:" as well as subjects and objects are rdfexported as shortcuts:
	    	 * "<property:HasSubject2 rdf:resource="&wiki;Patient"/>" 
	    	 * perform appropriate parsing 
	    	 */
	    	if (Pattern.matches("\\s*<property:(HasObject|HasSubject|HasPredicate)\\d rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
    			
	    		//check
	    		//System.out.println(line);
    			
    			//Is it an object/predicate/subject ?
    			String conceptType = line.substring(line.indexOf(":")+1, line.indexOf(" ")-1);
    			
    			//Which triple it belongs to?
    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
    			
    			String conceptName = line.substring(line.indexOf(";")+1, line.indexOf("\"", line.indexOf(";")));
    			
    			//check
    			//System.out.println(conceptType + "  " + conceptNumber);
    			
    			switch (conceptType)
    			{
    			  case "HasSubject":
    				SubjectsList.add(conceptNumber-1, conceptName);
        			break;
    			  case "HasObject":
    				ObjectsList.add(conceptNumber-1, conceptName);
    			    break;
    			  case "HasPredicate":
      				PredicatesList.add(conceptNumber-1, conceptName);
      			    break;
    			  default:
    			    System.err.println( "Unknown concept type: " + conceptType );
    			}
	    	}
	    	else {
	    		/* Predicates with key word "Property" are rdfexported as full resources:
		    	 * 		<property:HasPredicate1 rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Property-3AHas_Age"/>
		    	 * perform appropriate parsing 
		    	 */
	    		if (Pattern.matches("\\s*<property:HasPredicate\\d+ rdf:resource=\"" + base + "Property-3A[a-zA-Z_0-9:,?\"./]+\"/>", line)) {

	    			//Which triple it belongs to?
	    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
	    			
	    			//parse the immediate name of the concept
		    		Pattern patternTab = Pattern.compile("\\s*<property:HasPredicate\\d+ rdf:resource=\"" + base + "Property-3A([a-zA-Z_0-9:,?\"./]+)\"/>");
					Matcher mtchTab = patternTab.matcher(line);
					String conceptName = null;
					while (mtchTab.find()) {
						//store it in the variable conceptName
						conceptName = mtchTab.group(1);
					}
					
					PredicatesList.add(conceptNumber-1, conceptName);
	    		}
	    	}
	    }
	    
	    br.close();	
		System.out.println("Subjects list: " + SubjectsList);
		System.out.println("Predicates list: " + PredicatesList);
		System.out.println("Objects list: " + ObjectsList);
  	}
    
    /* 
     * rdfExportText - RDF export of the ABoxes wiki page
     * indURI - URI of this wiki page
     * outputPath - where we gonna save the corresponding OWL file   
     */
    public void buildABoxes(String rdfExportText, String ABoxesURI, String outputPath) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
      	
    	OWLOntology o = manager.createOntology(basicIRI);
    	
    	//Firstly divide triples obtained from the RDF export into subjects, predicates and objects. Therefore build appropriate lists. 
    	buildListsWithSubjectPredicateObject(rdfExportText);
    	
    	/* Now we'll go the predicates list through (predicate is the most important part) 
    	 * and handle each triple by adding corresponding SWRL axiom to the new ontology
    	 */
	    for (int i=0; i < PredicatesList.size(); i++) {
			
	    	String currentSubject = SubjectsList.get(i);
			String currentPredicate = PredicatesList.get(i);
			String currentObject = ObjectsList.get(i);
			
			/* If the current predicate is "is"-relation, then we handle it in the following way: 
			 * "Subject is Object" --> "Subject(subject1) ^ Object(subjet1)"  
			 */
			if (currentPredicate.equalsIgnoreCase("Is")) {
				
				// Firstly add Subject(subject1)
	    		bindIndividualToTheClass(o, getClassFromName(currentSubject), currentSubject + "1");
	    		
	    		// Secondly add Object(subject1) 
	    		bindIndividualToTheClass(o, getClassFromName(currentObject), currentSubject + "1");
			}
			else {
				
				//get the RDF export of the current predicate
				Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/" + currentPredicate).openStream(), "UTF-8").useDelimiter("\\A");
				String currentPredicatePage = scanner.next();
				
				// Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
				// For this do activate RegEx multiline modus with (?sm)
				if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
					
					// If found, then this property is an OBJECT property.
					
					// Firstly add Subject(subject1) and Object(object1) axioms
		    		bindIndividualToTheClass(o, getClassFromName(currentSubject), currentSubject + "1");
		    		bindIndividualToTheClass(o, getClassFromName(currentObject), currentObject + "1");
		    		
		    		// Secondly connect both individuals by the current property: Predicate(subject1, object1)
		    		bindIndividualsToTheObjectProperty(o, getObjectPropertyFromName(currentPredicate), currentSubject + "1", currentObject + "1");
				}
				else {
					// If not found, then this property is a DATA property.
						
					// Firstly add Subject(subject1)
					bindIndividualToTheClass(o, getClassFromName(currentSubject), currentSubject + "1");
					
		    		// Secondly connect the subject individual to the object literal by the current property: Predicate(subject1, object)
		    		bindIndividualsToTheDataProperty(o, getDataPropertyFromName(currentPredicate), currentSubject + "1", currentObject);
				}
				scanner.close();
			}			
		}
	    
	    
		//parse "Patient1" from "http://localhost/mediawiki/index.php/Special:URIResolver/Patient1"
		String ABoxesName = ABoxesURI.substring(ABoxesURI.lastIndexOf("/")+1, ABoxesURI.length());
	    
	    //Now we specify the URI our target swrl rule will be reachable through
	  	String ABoxesFile = "http://localhost:8080/CognitiveApp4b/files/output/" + ABoxesName + ".owl";
		
	  	/* Following 2 lines allow to add an annotation property to a RDF node without creation of named individual
	  	 * Use it for specifying the relationship between ABoxes-URI and ABoxes-File
	  	 */
		OWLAnnotationProperty Has_ABoxesFile = factory.getOWLAnnotationProperty(IRI.create(basicIRI + "Property-3AHas_ABoxesFile"));
	    manager.addAxiom(o, factory.getOWLAnnotationAssertionAxiom(Has_ABoxesFile, IRI.create(ABoxesURI), IRI.create(ABoxesFile)));	
	  	
	    
	    //saving new ontology in a new file 
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
	
	public OWLDataProperty getDataPropertyFromName (String propLabel) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
		OWLDataProperty prop = factory.getOWLDataProperty(IRI.create(basicIRI + propLabel));
		return prop; 	
	}
	
	public void bindIndividualToTheClass (OWLOntology o, OWLClass cls, String indLabel) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(indIRI + indLabel));
		manager.addAxiom(o, factory.getOWLClassAssertionAxiom(cls, ind));
		
	}
	
	public void bindIndividualsToTheObjectProperty (OWLOntology o, OWLObjectProperty prop, String ind1Label, String ind2Label) {
		OWLNamedIndividual ind1 = factory.getOWLNamedIndividual(IRI.create(indIRI + ind1Label));
		OWLNamedIndividual ind2 = factory.getOWLNamedIndividual(IRI.create(indIRI + ind2Label));
	    manager.addAxiom(o, factory.getOWLObjectPropertyAssertionAxiom(prop, ind1, ind2));
	}   
	
	public void bindIndividualsToTheDataProperty (OWLOntology o, OWLDataProperty prop, String ind1Label, String literal) {
		OWLNamedIndividual ind1 = factory.getOWLNamedIndividual(IRI.create(indIRI + ind1Label));
		
		// literal is a double number: 36.6
		if (Pattern.matches("\\d+.\\d+", literal)) {
			double literalDouble = Double.valueOf(literal);
			manager.addAxiom(o, factory.getOWLDataPropertyAssertionAxiom(prop, ind1, literalDouble));
		}
		else {
			//literal is an integer number: 90
			if (Pattern.matches("\\d+", literal)) {
				int literalInteger = Integer.valueOf(literal);
				manager.addAxiom(o, factory.getOWLDataPropertyAssertionAxiom(prop, ind1, literalInteger));
			}
			else {
				//literal is a string: "bla"
				manager.addAxiom(o, factory.getOWLDataPropertyAssertionAxiom(prop, ind1, literal));
			}
		}
	}   
}
