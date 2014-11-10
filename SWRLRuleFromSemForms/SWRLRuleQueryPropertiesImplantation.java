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


public class SWRLRuleQueryPropertiesImplantation {

	static List<String> classNames = new ArrayList<String>(); 
	static String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	    
		//getting a SWRL rule from the server
		Scanner scanner = new Scanner(new URL("http://localhost:8080/CognitiveApp2/files/output/swrl_rule.owl").openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
		
		//defining the OWL result file
		String resultFile = "C:/Users/Administrator/Desktop/swrl_rule_fit.owl";
		PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
		
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
				writer.println("        <Property-3AHas_name rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/RudisRule2\"/>");
				for (int i=0; i < classNames.size(); i++)
					writer.println("        <Property-3AHas_topic rdf:resource=\"" + base + classNames.get(i) + "\"/>");
			}
	    	
	    }
	    
		writer.close();
	    scanner.close();
	    
	    
	    
	}

}
