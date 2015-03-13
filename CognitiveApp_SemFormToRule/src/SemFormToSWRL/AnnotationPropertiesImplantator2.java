package SemFormToSWRL;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationPropertiesImplantator2 {

	public List<String> classNames = new ArrayList<String>(); 
	public List<String> conclusionClassesNames = new ArrayList<String>(); 

	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
	
	public void action(String inputPath, String ruleName, String OPType, String outputPath) throws IOException {
		
		//defining the OWL result file
		PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
		
		//help variables
	    boolean bodyFlag = false;
	    String currentRuleName = "";
		
	    //Now we specify the URI our target swrl rule will be reachable through
		String ruleSWRLFile = "http://localhost:8080/CognitiveApp2/files/output/" + ruleName + ".owl";
		
		//going through the OWL rule file line by line 
	    BufferedReader br = new BufferedReader(new FileReader(inputPath));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	
	    	//add the current line to the result file
			writer.println(line);
	    	
			/* immediately after prefixes comes the declaration of our help properties for better querying. 
			 * They are described  as annotation properties.
			 */
			if (Pattern.matches("\\s*<owl:Ontology rdf:about=\""+base+"\"/>", line)) {
		
				writer.println("    <!-- ");
				writer.println("    ///////////////////////////////////////////////////////////////////////////////////////");
	    		writer.println("    //");
	    		writer.println("    // Rule-URI");
	    		writer.println("    //");
	    		writer.println("    ///////////////////////////////////////////////////////////////////////////////////////");
	    		writer.println("     -->");
				
				// Rule URI declaration
				writer.println("<rdf:Description rdf:about=\"" + base + ruleName + "\">");
				//writer.println("  <rdf:type rdf:resource=\"" + base + "/Category-3AOntologyRule\"/>");
				writer.println("  <Property-3AHas_SWRLRuleFile rdf:resource=\"" + ruleSWRLFile + "\"/>");
				
				// if our variable OPType isn't empty, it means, we are dealing with a phase recognition rule. 
				// So we specify the corresponding OP type. 
				if (OPType!=null) 
					writer.println("  <Property-3AHas_OPType rdf:resource=\"" + base + OPType + "\"/>");

				writer.println("</rdf:Description>");
				
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
				writer.println(" <!-- " + base + "Property-3AHas_SWRLRuleFile -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_SWRLRuleFile\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AHas_topic -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_topic\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AIs_premise_of -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_premise_of\"/>");
				writer.println("");
				writer.println(" <!-- " + base + "Property-3AIs_conclusion_of -->");
				writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AIs_conclusion_of\"/>");
				// if our variable OPType isn't empty, it means, we are dealing with a phase recognition rule. 
				// So we specify appropriate Annotation property. 
				if (OPType!=null) {
					writer.println("");
					writer.println(" <!-- " + base + "Property-3AHas_OPType -->");
					writer.println(" <owl:AnnotationProperty rdf:about=\"" + base + "Property-3AHas_OPType\"/>");
				}
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

			/*
			 * We have encountered the rule begin if 
			 * firstly: the current line is </rdf:Description> (the end of the variables declaration)
			 * secondly: the next line is <rdf:Description>
			 * thirdly: the next next line is "<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>"
			 */
			
			/* By encountering the rule begin do 
			 * 1. changing the blank node which starts the rule to the ruleName#1 node 
			 * 2. specifying the rule begin with "<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>"
			 * 3. specifying the relation from this node to the Rule-URI via Has_name property
			 * 4. specifying the relation to this node to the topics via Has_topic property
			 */
			
			/*
			 * else: just write the next and the next next lines to the output file 
			 */
			if (Pattern.matches("\\s*</rdf:Description>\\s*", line)) {
				String nextLine = br.readLine();
				String nextnextLine = br.readLine();
				if ((Pattern.matches("\\s*<rdf:Description>\\s*", nextLine)) && (Pattern.matches("        <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>", nextnextLine))) {
					currentRuleName = base + ruleName + "#1";
					
					writer.println("<rdf:Description rdf:about=\"" + currentRuleName + "\">");
					writer.println("        <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#Imp\"/>");
					writer.println("        <Property-3AHas_name rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/"+ruleName+"\"/>");
					
					// decided to move this relation to the separate node RuleURI (see above after Annotation properties)
					//writer.println("		<Property-3AHas_SWRLRuleFile rdf:resource=\"" + ruleSWRL + "\"/>");
					
					for (int i=0; i < classNames.size(); i++)
						writer.println("        <Property-3AHas_topic rdf:resource=\"" + base + classNames.get(i) + "\"/>");
				}
				else {
					/* </rdf:Description> occurs pretty often. So we should take care not to lose some information 
					 * in the nextLine and nextNextLine after "</Description>". 
					 * For example <swrl:head> or <swrl:body> could stand in one of these lines. 
					 * So they need to be processed.  */
					
					//mark the moment when we have reached the line with <swrl:body>
					if (Pattern.matches("\\s*<swrl:body>\\s*", nextLine)) {
						System.out.println("Start of the body!");
						bodyFlag = true;
						writer.println(nextLine);
					}	
					else {
						//mark the moment when we have reached the line with <swrl:head>
						if (Pattern.matches("\\s*<swrl:head>\\s*", nextLine)) {
							System.out.println("Start of the head!");
							bodyFlag = false;
							writer.println(nextLine);
						}
						else 
							writer.println(nextLine);
					}
					
					//mark the moment when we have reached the line with <swrl:body>
					if (Pattern.matches("\\s*<swrl:body>\\s*", nextnextLine)) {
						System.out.println("Start of the body!");
						bodyFlag = true;
						writer.println(nextnextLine);
					}	
					else {
						//mark the moment when we have reached the line with <swrl:head>
						if (Pattern.matches("\\s*<swrl:head>\\s*", nextnextLine)) {
							System.out.println("Start of the head!");
							bodyFlag = false;
							writer.println(nextnextLine);
						}
						else 
							writer.println(nextnextLine);
					}
				}	
			}
			
			//mark the moment when we have reached the line with <swrl:body>
			if (Pattern.matches("\\s*<swrl:body>\\s*", line)) {
				System.out.println("Start of the body!");
				bodyFlag = true;		
			}		
			
			//mark the moment when we have reached the line with <swrl:head>
			if (Pattern.matches("\\s*<swrl:head>\\s*", line)) {
				System.out.println("Start of the head!");
				bodyFlag = false;
			}
			
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
					tab = mtchTab.group(1);
				}
				
				// if we are inside of a body and it the class of the current Atom hasn't been moved from the head side (a kind of exception) --> it's a premise atom
				if ((bodyFlag) && (!conclusionClassesNames.contains(atomClsName))) {

					// body --> premise
					writer.println(tab + "<Property-3AIs_premise_of rdf:resource=\"" + currentRuleName + "\"/>");	
				}
				else {
				
					// head --> conclusion
					writer.println(tab + "<Property-3AIs_conclusion_of rdf:resource=\"" + currentRuleName + "\"/>");	
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
					tab = mtchTab.group(1);
				}
				
				if (bodyFlag) {
					// body --> premise
					writer.println(tab + "<Property-3AIs_premise_of rdf:resource=\"" + currentRuleName + "\"/>");	
				}
				else {
					// head --> conclusion
					writer.println(tab + "<Property-3AIs_conclusion_of rdf:resource=\"" + currentRuleName + "\"/>");	
				}
			}
			
			/* OPTIONAL
			 * There is another possibility to specify SWRL atoms. Instead of
			 * <rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#ClassAtom\"/>
			 * it is specified by 
			 * <swrl:ClassAtom>  
			 * 
			 * Following if-clause processes this variant.
			 */
			/*
			if( (Pattern.matches("\\s*<swrl:ClassAtom>\\s*", line)) || (Pattern.matches("\\s*<swrl:IndividualPropertyAtom>\\s*", line)) || (Pattern.matches("\\s*<swrl:DatavaluedPropertyAtom>\\s*", line)) || (Pattern.matches("\\s*<swrl:BuiltinAtom>\\s*", line)) ){

				//find out which kind of atom we are dealing with
				Pattern patternAtomType = Pattern.compile("\\s*<swrl:([a-zA-Z_0-9:,?\"./]+)>\\s*");
				Matcher mtchAtomType = patternAtomType.matcher(line);
				String atomType = "";
				while (mtchAtomType.find()) {
					//save the atomType
					atomType = mtchAtomType.group(1);
				}
				
				//find the number of blank nodes leading the line with the obtained atom type
				Pattern patternTab = Pattern.compile("(\\s*)<swrl:" + atomType + ">\\s*");
				Matcher mtchTab = patternTab.matcher(line);
				String tab = "";
				while (mtchTab.find()) {
					//and add it to the classNames list
					tab = mtchTab.group(1);
				}
				
				if (bodyFlag) {
					// body --> premise
					writer.println(tab + "	<Property-3AIs_premise_of rdf:resource=\""+ currentRuleName + "\"/>");	
				}
				else {
					// head --> conclusion
					writer.println(tab + "	<Property-3AIs_conclusion_of rdf:resource=\"" + currentRuleName + "\"/>");	
				}
			}*/
	    }
	    
		writer.close();
		br.close();	  
	}
}
