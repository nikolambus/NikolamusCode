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
	public List<String> conclusionClassesNames = new ArrayList<String>(); 

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
	    boolean bodyFlag = false;
	    String currentRuleName = "";
		
		//going through the OWL rule file line by line 
	    BufferedReader br = new BufferedReader(new StringReader(out));
	    String line;
	    while ((line = br.readLine()) != null) {
	    
	    	//check
	    	//System.out.println(line);
	    	
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
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AIs_premise_of -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_premise_of\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AIs_conclusion_of -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_conclusion_of\"/>");
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
				currentRuleName = ruleName;
				writer.println("        <Property-3AHas_name rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/"+ruleName+"\"/>");
				writer.println("        <Property-3AHas_source_rule rdf:resource=\""+swrlRule+"\"/>");
				for (int i=0; i < classNames.size(); i++)
					writer.println("        <Property-3AHas_topic rdf:resource=\"" + base + classNames.get(i) + "\"/>");
			}
			
			//mark the moment when we have reached the line with <swrl:body>
			if (Pattern.matches("\\s*<swrl:body>\\s*", line))
				bodyFlag = true;
			
			//mark the moment when we have reached the line with <swrl:head>
			if (Pattern.matches("\\s*<swrl:head>\\s*", line))
				bodyFlag = false;
			
			/* if we find the line where some individual is asserted to a class -> 
			it means, this is a help individual and its class has been moved here from the head side 
			(SWRL rules do not allow to use new variables on the head side). 
			So this class should be treated as a conclusion. 
			Add it to the special list of those classes. 	
			*/
			if (Pattern.matches("\\s*<rdf:type rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>", line)) {
				
				//parse the class name from regex ([a-zA-Z_0-9:,?\"./]+)
				Pattern pattern = Pattern.compile("\\s*<rdf:type rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>");
				Matcher mtch = pattern.matcher(line);
				while (mtch.find()) {
					conclusionClassesNames.add(mtch.group(1));
				}
				//check
				System.out.println("Conclusion Classes: " + conclusionClassesNames);
			}
			
			/* if we encounter a blank node of type ClassAtom, IndividualPropertyAtom, DatavaluedPropertyAtom or BuiltinAtom 
			   we add the "Property-3AIs_premise_of" or the "Property-3AIs_conclusion_of" property 
			   that builds the relationship from the current node to the rule node
			*/
			if (Pattern.matches("\\s*<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#ClassAtom\"/>\\s*", line)) {
				
				/*now we should find out: is it a "real" body class or it has come from the head. 
				Therefore we check the next line where the name of the class is specified,
				extract this name and prove: does it occur within our conclusionsClasses list 
				*/
				String nextLine = br.readLine();
				//parse the class name from regex ([a-zA-Z_0-9:,?\"./]+)

				Pattern patternCls = Pattern.compile("\\s*<swrl:classPredicate rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/([a-zA-Z_0-9:,?\"./]+)\"/>");
				Matcher mtchCls = patternCls.matcher(nextLine);
				String atomClsName = "";
				while (mtchCls.find()) {
					//and add it to the classNames list
					atomClsName = mtchCls.group(1);
				}
				
				//check
				System.out.println("Current AtomClsName: " + atomClsName);

				//find the number of blank nodes leading the line
				Pattern patternTab = Pattern.compile("(\\s*)<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#ClassAtom\"/>\\s*");
				Matcher mtchTab = patternTab.matcher(line);
				String tab = "";
				while (mtchTab.find()) {
					//and add it to the classNames list
					tab = mtchTab.group(1);
				}
				
				// if we are inside of a body and it the class of the current Atom hasn't been moved from the head side (a kind of exception) --> it's a premise atom
				if ((bodyFlag) && (!conclusionClassesNames.contains(atomClsName))) {

					// body --> premise
					writer.println(tab + "<Property-3AIs_premise_of rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + currentRuleName + "\"/>");	
				}
				else {
				
					// head --> conclusion
					writer.println(tab + "<Property-3AIs_conclusion_of rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + currentRuleName + "\"/>");	
				}
				
				// BufferedReader has moved one line further. Do not forget to write it into the new file. 
				writer.println(nextLine);
				
			}
			
			// now process 2 property atoms and a built-in atom. 
			if( (Pattern.matches("\\s*<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#IndividualPropertyAtom\"/>\\s*", line)) || (Pattern.matches("\\s*<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#DatavaluedPropertyAtom\"/>\\s*", line)) || (Pattern.matches("\\s*<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#BuiltinAtom\"/>\\s*", line)) ){

				//find out which kind of atom we are dealing with
				Pattern patternAtomType = Pattern.compile("\\s*<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#([a-zA-Z_0-9:,?\"./]+)\"/>\\s*");
				Matcher mtchAtomType = patternAtomType.matcher(line);
				String atomType = "";
				while (mtchAtomType.find()) {
					//save the atomType
					atomType = mtchAtomType.group(1);
				}
				
				//find the number of blank nodes leading the line with the obtained atom type
				Pattern patternTab = Pattern.compile("(\\s*)<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#" + atomType + "\"/>\\s*");
				Matcher mtchTab = patternTab.matcher(line);
				String tab = "";
				while (mtchTab.find()) {
					//and add it to the classNames list
					tab = mtchTab.group(1);
				}
				
				if (bodyFlag) {
					// body --> premise
					writer.println(tab + "<Property-3AIs_premise_of rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + currentRuleName + "\"/>");	
				}
				else {
					// head --> conclusion
					writer.println(tab + "<Property-3AIs_conclusion_of rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + currentRuleName + "\"/>");	
				}
			}
	    }
	    
		writer.close();
	    scanner.close();
	  
	}
}
