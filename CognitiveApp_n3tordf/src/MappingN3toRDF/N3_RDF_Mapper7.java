package MappingN3toRDF;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

public class N3_RDF_Mapper7 {

	public String ruleTransit = null;

	public  List<String> internPrefixuris = new ArrayList<String>();
	public List<String> globalPrefixuris = new ArrayList<String>(Arrays.asList("rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"", "rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"", "owl=\"http://www.w3.org/2002/07/owl#\"", "swrl=\"http://www.w3.org/2003/11/swrl#\"", "xsd=\"http://www.w3.org/2001/XMLSchema#\"", "base=\"http://localhost/mediawiki/index.php/Special:URIResolver/\"", "baseVar=\"http://localhost/mediawiki/index.php/Special:URIResolver/Variable#\"", "baseProp=\"http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A\""));
	public List<String> allPrefixuris = new ArrayList<String>();

	public List<String> totalVariablesBank = new ArrayList<String>();
	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";

	/*
	 * @param
	 * ruleN3 - URI of the n3 rule file (e.g.: http://localhost:8080/CognitiveApp6/files/input/bn.n3)
	 * ruleName - String (e.g.: bla)
	 * outputPath - application-sensitive path to the result file, which 
	 * 				corresponds to the tomcat directory with appropriate cognitive app (e.g.: D:\DiplArbeit\OurWork\Eclipse_workSPACE\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\CognitiveApp6\files\output/bn.xml)
	 * 				is reachable via appropriate URI (http://localhost:8080/CognitiveApp6/files/output/bn.xml)
	 */
	public void action(String ruleN3, String ruleName, String outputPath) throws IOException {
				
		// here we gonna store all rules mapped to RDF from this file
		String allRulesRDF = "";
		
		if (ruleN3.isEmpty())
	        	System.out.println("No such file in " + System.getProperty("user.dir"));
	    else {
	
	    	//build the list of all prefix-URIs (we'll need it later)
			allPrefixuris.addAll(globalPrefixuris);
	    	
			//produce connection to the n3 file
			URL url = new URL(ruleN3);			
		    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			
		    String line;
			String currentRule = "";
			boolean currentRuleFlag = false;
			int ruleCounter = 1;
			
			//go through the file with rules and divide it in the snippets "1 rule -> 1 snippet". 			
			while ((line = br.readLine()) != null) {

		    	// looking for n3 prefix declaration
				if (Pattern.matches("^\\s*@prefix\\s*[a-zA-Z0-9_]+:\\s*<[a-zA-Z_0-9:,?\"./)^(#-]+>\\s*[.]\\s*$", line)) {
					
					// the part from the first blank till the ':' is the namespace
					String prefixnamespace = line.substring(line.indexOf(" ")+1, line.indexOf(":"));
					
					// there are leading blanks or leading blanks + @prefix possible - remove them to have the clear namespace name
					prefixnamespace = prefixnamespace.replaceAll("^\\s*(@prefix)?\\s+","");
					
					// the part from '<' till '>' is the immediate prefix uri
					String justPrefixuri = line.substring(line.indexOf("<")+1, line.indexOf(">"));
				
					/* we save in the variable "prefixuri" the prefix-uri-pair in following format: 
					 * xnat="http://aifb-ls3-vm2.aifb.kit.edu:8080/xnatwrapper/id/project/Liver_Factors#"
					 */
					String prefixuri = prefixnamespace + "=\"" + justPrefixuri + "\"";
					
					//add intern prefix if it isn't a duplicate of already set global prefix
					if (!globalPrefixuris.contains(prefixuri))
						internPrefixuris.add(prefixuri);

					allPrefixuris.addAll(internPrefixuris);
					//check
					//System.out.println("PREFIXNAMESPACE: " + prefixnamespace);
					//System.out.println("PREFIXURI: " + prefixuri);
					//System.out.println(internPrefixes);
				}
				
				//mark a rule begin with the setting flag to true
	    		if (Pattern.matches("\\s*[{]\\s*", line)) {	
	    			currentRuleFlag = true;	
	    		}
	    		
	    		//as long as we are within a certain rule store its lines in a String "currentRule"
	    		if (currentRuleFlag)
	    			currentRule = currentRule + line + "\n"; 
	    		
	    		//reached the end of the current rule -> set the flag to false, send the snippet with current rule to the further processing by OneRuleMapper
	    		if (Pattern.matches("\\s*[}]\\s*[.]\\s*", line)) {	
	    			currentRuleFlag = false;
	    			
	       			//check
	    			//System.out.println("Rule: " + currentRule);
	    			
	    			OneRuleMapper7 mapper = new OneRuleMapper7();
	    			String  currentRuleRDF = mapper.action(currentRule, ruleName + ruleCounter, allPrefixuris);

	    			/* alternative method to add hasTopic-properties within this class.
	    			 * gather all concepts of the ruleCounter-th rule in the ruleCounter-th list of the ListForTopic.
	    			/*
	    			ListForTopic.add(ruleCounter, new ArrayList<String>());
	    			ListForTopic.get(ruleCounter).addAll(mapper.bodySubjectsList);
	    			ListForTopic.get(ruleCounter).addAll(mapper.bodyObjectsList);
	    			*/
	    			
	    			//writer the result of the mapping for currentRule into the output file ruleRDF
	    			allRulesRDF = allRulesRDF + currentRuleRDF + "\n";
	    			currentRule = "";
	    			
	    			totalVariablesBank.addAll(mapper.getVariablesBank());
	    			ruleCounter++;
	    		}
	    	}
			
			br.close();
			
			//remove duplicates from the totalVariablesBank
			Set<String> vars = new LinkedHashSet<>(totalVariablesBank);
			totalVariablesBank.clear();
			totalVariablesBank.addAll(vars);
			
			//check
			//System.out.println("total variables 2: " + totalVariablesBank);

			//----------------------- Writing the result to the output file -------------------------------
			
			PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
			
			//add rule independent prefixes to the result file
			writer.println("<rdf:RDF xmlns:base=\"http://localhost/mediawiki/index.php/Special:URIResolver/\" xmlns:baseVar=\"http://localhost/mediawiki/index.php/Special:URIResolver/Variable#\" xmlns:baseProp=\"http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:swrl=\"http://www.w3.org/2003/11/swrl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");

			//add rule dependent prefixes into the result file
			for (int i=0; i < internPrefixuris.size(); i++) {
				writer.print("xmlns:" + internPrefixuris.get(i) + " ");
			}
			writer.println(">");			
			//add annotation properties which are necessary for queries
			writer.println("");
			writer.println("<!-- ");
			writer.println("///////////////////////////////////////////////////////////////////////////////////////");
    		writer.println("//");
    		writer.println("// Annotation Properties");
    		writer.println("//");
    		writer.println("///////////////////////////////////////////////////////////////////////////////////////");
    		writer.println("-->");
			writer.println("");
			writer.println("");
			writer.println(" <!-- " + base + "Property-3AHas_name -->");
			writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_name\"/>");
			writer.println("");
			writer.println(" <!-- " + base + "Property-3AHas_rule_file -->");
			writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_rule_file\"/>");
			writer.println("");
			writer.println(" <!-- " + base + "Property-3AHas_topic -->");
			writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_topic\"/>");
			writer.println("");
			writer.println(" <!-- " + base + "Property-3AIs_premise_of -->");
			writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_premise_of\"/>");
			writer.println("");
			writer.println(" <!-- " + base + "Property-3AIs_conclusion_of -->");
			writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_conclusion_of\"/>");
			writer.println("    ");
			
			//variables declaration
			for (int i=0; i<totalVariablesBank.size(); i++) {
				writer.println("<rdf:Description rdf:about=\"" + totalVariablesBank.get(i) + "\">");
				writer.println("  <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Variable\"/>");
				writer.println("</rdf:Description>");
			}
			
			//add hasTopic properties to the all rules
			//String ruleswithTopics = addHasTopic(allRulesRDF);
	
			//write the rules with topics to the result file
			writer.println(allRulesRDF);
		
			
			//the end
			writer.println("</rdf:RDF>");
			writer.close();	
		}
		//scanner.close();
	}	
		
	/*
	 * Alternative method to add hasTopic-properties
	 * 
	public static String addHasTopic(String allRulesRDF) throws IOException {			

		BufferedReader br = new BufferedReader(new StringReader(allRulesRDF));
		String result = "";
		String line;
		int ruleCounter = 1;		 
		while ((line = br.readLine()) != null) {

			result = result + line + "\n";
			if (Pattern.matches("\\s*<swrl:Imp rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + ruleName + "[0-9]*\">\\s*", line)) {

				//find the number of blank nodes leading the line with the obtained atom type
				Pattern patternTab = Pattern.compile("(\\s*)<swrl:Imp rdf:about=\"" + base + ruleName + "[0-9]*\">\\s*");
				Matcher mtchTab = patternTab.matcher(line);
				String tab = "";
				while (mtchTab.find()) {
					//and add it to the classNames list
					tab = mtchTab.group(1);
				}
				
				//remove duplicates from the ListForTopic's i-th list
				Set<String> concepts = new LinkedHashSet<>(ListForTopic.get(ruleCounter));
				ListForTopic.get(ruleCounter).clear();
				ListForTopic.get(ruleCounter).addAll(concepts);
				
				for (int i=0; i<ListForTopic.get(ruleCounter).size(); i++)
					result = result + tab + "   <base:Property-3AHas_topic rdf:resource=\"" + base + ListForTopic.get(ruleCounter).get(i) + "\"/>" + "\n";
					//writer.println(tab + "   <base:Property-3AHas_topic rdf:resource=\"" + base + ListForTopic.get(ruleCounter).get(i) + "\"/>");
			}			
		}
		br.close();
		//writer.close();
		return result;
	}
	*/
}