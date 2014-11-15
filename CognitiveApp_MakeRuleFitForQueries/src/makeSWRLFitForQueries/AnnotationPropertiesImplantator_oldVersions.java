package makeSWRLFitForQueries;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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


/* In the SWRLRuleQueryPropertiesImplantation we process the rule data within a rule file, reading file line by line, etc..
 * Here we would like to process the rule on the OWL API level.
 * 
 * We want 0to model a help node which would contain additional info for queries
 * <owl:NamedIndividual rdf:about="http://localhost/mediawiki/index.php/Special:URIResolver/Individual#helpQueriesNode">
   		<Property-3AHas_topic rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Individual#Auffaelliger_Befund"/>
   		<Property-3AHas_topic rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Individual#DRU"/>
   		<Property-3AHas_name rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Individual#RudisRule2"/>
   </owl:NamedIndividual>
 * 
 * */

public class AnnotationPropertiesImplantator_oldVersions {

	static List<String> classNames = new ArrayList<String>(); 
	static String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
  	public static IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
   	public static IRI indIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/Individual#");	

	
	public static void version1(String swrlRule, String outputPath) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
			    
		//getting a SWRL rule from the server
		Scanner scanner = new Scanner(new URL(swrlRule).openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
		
		//going through the OWL rule file line by line 
	    BufferedReader br = new BufferedReader(new StringReader(out));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	
			if (Pattern.matches("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>", line)) {
				
				//parse the class name from regex ([a-zA-Z_0-9:,?\"./]+)
				Pattern pattern = Pattern.compile("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>");
				Matcher mtch = pattern.matcher(line);
				while (mtch.find()) {
					//and add it to the classNames list
					classNames.add(mtch.group(1));
				}
			}	
	    
	    }
	    scanner.close();
	    
		OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
		OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();

		OWLOntology rule = m1.loadOntology(IRI.create(swrlRule));
    	OWLOntology ruleFit = m2.createOntology(basicIRI);
		m2.addAxioms(ruleFit, rule.getAxioms());
		
	    OWLDataFactory factory = m2.getOWLDataFactory();

		OWLNamedIndividual helpInd = factory.getOWLNamedIndividual(IRI.create(indIRI + "helpQueriesNode"));
		OWLObjectProperty propHasTopic = factory.getOWLObjectProperty(IRI.create(basicIRI + "Property-3AHas_topic"));
		OWLObjectProperty propHasName = factory.getOWLObjectProperty(IRI.create(basicIRI + "Property-3AHas_name"));
		
		OWLNamedIndividual topic1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "DRU"));
		OWLNamedIndividual topic2 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Auffaelliger_Befund"));
	    m2.addAxiom(ruleFit, factory.getOWLObjectPropertyAssertionAxiom(propHasTopic, helpInd, topic1));
	    m2.addAxiom(ruleFit, factory.getOWLObjectPropertyAssertionAxiom(propHasTopic, helpInd, topic2));

		OWLNamedIndividual ruleName = factory.getOWLNamedIndividual(IRI.create(indIRI + "RudisRule2"));
	    m2.addAxiom(ruleFit, factory.getOWLObjectPropertyAssertionAxiom(propHasName, helpInd, ruleName));
	    
	    //Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        m2.saveOntology(ruleFit, IRI.create(output.toURI()));
				
	}		
	
/* version 2 - working with lines, Patterns, PrintWriter. Adding properties for queries as ObjectProperties.  */	
public void version2(String swrlRule, String outputPath) throws OWLOntologyCreationException, IOException {
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	    
		//getting a SWRL rule from the server
		Scanner scanner = new Scanner(new URL(swrlRule).openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
		
		//defining the OWL result file
		PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
		
		//help variables
	    int lineCounterForInsert=0;
	    boolean insertFlag = false;
		
		//going through the OWL rule file line by line 
	    BufferedReader br = new BufferedReader(new StringReader(out));
	    String line;
	    while ((line = br.readLine()) != null) {
	    
	    	
	    	
	    	//check
	    	System.out.println(line);
	    	
	    	//add the current line to the result file
			writer.println(line);
	    	
			if (Pattern.matches("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>", line)) {
				
				//parse the class name from regex ([a-zA-Z_0-9:,?\"./]+)
				Pattern pattern = Pattern.compile("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>");
				Matcher mtch = pattern.matcher(line);
				while (mtch.find()) {
					//and add it to the classNames list
					classNames.add(mtch.group(1));
				}
			}

			
			if (Pattern.matches("        <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>", line)) {
				//parse "RudisRule2" from "http://localhost:8080/CognitiveApp2/files/output/RudisRule2.owl"
				String ruleName = swrlRule.substring(swrlRule.lastIndexOf("/")+1, swrlRule.lastIndexOf("."));
				writer.println("        <Property-3AHas_name rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/"+ruleName+"\"/>");
				writer.println("        <Property-3AHas_source_rule rdf:resource=\""+swrlRule+"\"/>");
				for (int i=0; i < classNames.size(); i++)
					writer.println("        <Property-3AHas_topic rdf:resource=\"" + base + classNames.get(i) + "\"/>");
			}
	    	
			//mark the moment when we have reached the line with "Object Properties"
			if (Pattern.matches("    // Object Properties", line))
				insertFlag = true;

			// start to count lines after the "Object Properties" line has been reached
			if (insertFlag == true)
				lineCounterForInsert++;
	    
			// after 4 lines we can define implanted properties as object properties just before the OWL API native ones
			if (lineCounterForInsert == 4) {
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_name -->");
				writer.println(" <owl:ObjectProperty rdf:about=\"" + base + "Property-3AHas_name\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_source_rule -->");
				writer.println(" <owl:ObjectProperty rdf:about=\"" + base + "Property-3AHas_source_rule\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_topic -->");
				writer.println(" <owl:ObjectProperty rdf:about=\"" + base + "Property-3AHas_topic\"/>");
			}
	    }
	    
		writer.close();
	    scanner.close();
	    
	    
	    
	}
}
