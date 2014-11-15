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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class AnnotationPropertiesImplantator {

	public List<String> classNames = new ArrayList<String>(); 
	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
	
	public void action(String swrlRule, String outputPath) throws OWLOntologyCreationException, IOException {
		
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
	    	
			/* immediately after prefixes comes the declaration of our help properties for better querying. 
			 * They are described  as annotation properties.
			 */
			if (Pattern.matches("\\s*<owl:Ontology rdf:about=\""+base+"\"/>", line)) {
				writer.println("    <!-- ");
				writer.println("    ///////////////////////////////////////////////////////////////////////////////////////");
	    		writer.println("    //");
	    		writer.println("    // Annotation Properties");
	    		writer.println("    //");
	    		writer.println("    ///////////////////////////////////////////////////////////////////////////////////////");
	    		writer.println("     -->");
				writer.println("");
				writer.println("");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_name -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_name\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_source_rule -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_source_rule\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_topic -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_topic\"/>");
	    	}
			
			// if we encounter a class declaration save the class name - each class name is the value of the "Property-3AHas_topic" property
			if (Pattern.matches("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>", line)) {
				
				//parse the class name from regex ([a-zA-Z_0-9:,?\"./]+)
				Pattern pattern = Pattern.compile("\\s*<owl:Class rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>");
				Matcher mtch = pattern.matcher(line);
				while (mtch.find()) {
					//and add it to the classNames list
					classNames.add(mtch.group(1));
				}
			}

			// By encountering the rule begin do implanting our annotation properties as properties of the blank node that starts the rule  
			if (Pattern.matches("        <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>", line)) {
				//parse "RudisRule2" from "http://localhost:8080/CognitiveApp2/files/output/RudisRule2.owl"
				String ruleName = swrlRule.substring(swrlRule.lastIndexOf("/")+1, swrlRule.lastIndexOf("."));
				writer.println("        <Property-3AHas_name rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/"+ruleName+"\"/>");
				writer.println("        <Property-3AHas_source_rule rdf:resource=\""+swrlRule+"\"/>");
				for (int i=0; i < classNames.size(); i++)
					writer.println("        <Property-3AHas_topic rdf:resource=\"" + base + classNames.get(i) + "\"/>");
			}
	    }
	    
		writer.close();
	    scanner.close();
	  
	}

}
