package MappingN3toRDF;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

	
public class OneRuleMapper7 {
	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
	public String baseProp = "http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A";
	public String baseVar = "http://localhost/mediawiki/index.php/Special:URIResolver/Variable#";
	public String baseInd = "http://localhost/mediawiki/index.php/Special:URIResolver/Individual#";

	
	public List<String> bodySubjectsList = new ArrayList<String>(); 
	public List<String> bodyPredicatesList = new ArrayList<String>(); 
	public List<String> bodyObjectsList = new ArrayList<String>();

	public List<String> headSubjectsList = new ArrayList<String>(); 
	public List<String> headPredicatesList = new ArrayList<String>(); 
	public List<String> headObjectsList = new ArrayList<String>();
	
	public List<String> variablesBank = new ArrayList<String>();
	
	/**
	 * 
	 * @param oneRuleN3 - one rule snippet from the whole N3 file which can contain multiple rules 
	 * @param ruleName - the name of the rule (if multiple rules within a file, ruleName contains also the number of current partial rule)
	 * @param allPrefixuris - prefixes and their URIs for this rule (needed for "prefixResolver" method below)
	 * @return
	 * @throws IOException
	 */
	public String action(String oneRuleN3, String ruleName, List<String> allPrefixuris) throws IOException {
		
		//the result of this method
		String oneRuleRDF = "";
		
		//building the framework for the rdf rule in a help string "transit"
		String transit = createXMLstructureBody(oneRuleN3, ruleName);
		

		//check
		//System.out.println("TRANSIT: " + transit); 
		
		//reading the transit file line by line till the end of the file 
		BufferedReader br = new BufferedReader(new StringReader(transit));
		String line;
    	int i=0; 
    	int j=0;
    	while ((line = br.readLine()) != null) {
    		
    		/* encountering the string "Triple" (our placeholder for a triple) 
    		 * compute the necessary tab and add the real triple in RDF/XML to the output string oneRuleRDF   
    		 */
    		if (Pattern.matches("\\s+TripleBody\\d+", line)) {	
    			
    			//find the tab distance till 'T' (from "Triple")
    			String tab = line.substring(0, line.indexOf('T')-1);
    			
    			//add the i-th triple in RDF/XML on the distance "tab" to the output string "oneRuleRDF"
    			oneRuleRDF = createXMLTripleFromN3("body", i, oneRuleRDF, ruleName, tab, allPrefixuris);
    			
    			//and iterate the body(!) triples counter
    			i++;
    		}
    		else {
    			if (Pattern.matches("\\s+TripleHead\\d+", line)) {	
    				
    				//find the tab distance till 'T' (from "Triple")
    				String tab = line.substring(0, line.indexOf('T')-1);
    			
    				//add the i-th triple in RDF/XML on the distance "tab" to the output string "oneRuleRDF"
    				oneRuleRDF = createXMLTripleFromN3("head", j, oneRuleRDF, ruleName, tab, allPrefixuris);
    			
    				//and iterate the head(!) triples counter
    				j++;
    			}
        		else {
        			// just copy the actual line from transit else
        			oneRuleRDF = oneRuleRDF + line + "\n";
        		}
    		}
    	}
	    br.close();
	    
	    String oneRuleRDFWithTopics = this.addHasTopic(oneRuleRDF, ruleName, allPrefixuris);
	    //check
	    //System.out.println("oneRuleRDF: ");
	    //System.out.println(oneRuleRDF);

	    return oneRuleRDFWithTopics;
	}

	public String createXMLstructureBody(String oneRuleN3, String ruleName) throws IOException {
		
		//----------------------------------BODY---------------------------------------------------
			
		// that is the string for intermediate step, where we create the framework for our rules 
		String transit = "";
		
		//initialize the tab variables
		String tabBody = "    ";
		String tabAtomlist = " ";
		String tabFirst = " ";
		String tabTriple = " ";
		
		// get the number of body triples		
		int numberOfBodyTriples = findNumberOfBodyTriples(oneRuleN3);
		
		//check
		System.out.println("Number of body triples: " + numberOfBodyTriples);
  	
		if (numberOfBodyTriples == 0)
			System.out.println("No triples found in your rule.");
		else {
			transit = transit + "\n";
			transit = transit + "\n";
			transit = transit + "\n";

			transit = transit + " <swrl:Imp rdf:about=\"" + base + ruleName + "\">" + "\n";
			transit = transit + tabBody + "<base:Property-3AHas_name rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
			transit = transit + tabBody + "<base:Property-3AHas_SWRLRuleFile rdf:resource=\"ToDo........\"/>" + "\n";
			transit = transit + tabBody + "<base:Property-3AHas_N3RuleFile rdf:resource=\"ToDo........\"/>" + "\n";

			// immediate body
			transit = transit + tabBody + "<swrl:body>" + "\n";
			tabAtomlist = tabBody + "    ";
			for (int i=0; i<numberOfBodyTriples; i++) {
				transit = transit + tabAtomlist + "<swrl:AtomList>" + "\n";
				tabFirst = tabAtomlist + "    ";
				transit= transit + tabFirst + "<rdf:first>" + "\n";
				tabTriple = tabFirst + "    ";
				
				//put the string "Triple" just as a placeholder to mark the place where the real triple should be inserted in
				transit = transit + tabTriple + "TripleBody" + (i+1) + "\n";
				transit = transit + tabFirst + "</rdf:first>" + "\n";
				
				// the last rest should contain nil. So we should not start the new line in the last "rest"
				if (i<numberOfBodyTriples-1)
					transit = transit + tabFirst + "<rdf:rest>" + "\n";
				else 
					transit = transit + tabFirst + "<rdf:rest";
				tabAtomlist = tabFirst + "    ";
			}
			transit = transit + " rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>" + "\n";
			
			String tabRest = " ";
			tabAtomlist = tabFirst.substring(4);
			for (int i=0; i<numberOfBodyTriples-1; i++) {
				transit = transit + tabAtomlist + "</swrl:AtomList>" + "\n";
				tabRest = tabAtomlist.substring(4);
				transit = transit + tabRest + "</rdf:rest>" + "\n";
				tabAtomlist = tabRest.substring(4);
			}
			transit = transit + tabAtomlist + "</swrl:AtomList>" + "\n";
			transit = transit + tabBody + "</swrl:body>" + "\n";
		}
		
		//---------------------- HEAD ------------------------------------------
		
		//initialize the tab variables
		tabBody = "    ";
		tabAtomlist = " ";
		tabFirst = " ";
		tabTriple = " ";
		
		int numberOfHeadTriples = findNumberOfHeadTriples(oneRuleN3);
		
		//check
		System.out.println("Number of head triples: " + numberOfHeadTriples);
		System.out.println("");
		
		// <swrl:head> needs the same tab distance as <swrl:body>
		transit = transit + tabBody + "<swrl:head>" + "\n";
		tabAtomlist = tabBody + "    ";
		for (int i=0; i<numberOfHeadTriples; i++) {
			transit = transit + tabAtomlist + "<swrl:AtomList>" + "\n";
			tabFirst = tabAtomlist + "    ";
			transit= transit + tabFirst + "<rdf:first>" + "\n";
			tabTriple = tabFirst + "    ";
			
			//put the string "Triple" just as a placeholder to mark the place where the real triple should be inserted in
			transit = transit + tabTriple + "TripleHead" + (i+1) + "\n";
			transit = transit + tabFirst + "</rdf:first>" + "\n";
			
			// the last rest should contain nil. So we should not start the new line in the last "rest"
			if (i<numberOfHeadTriples-1)
				transit = transit + tabFirst + "<rdf:rest>" + "\n";
			else 
				transit = transit + tabFirst + "<rdf:rest";
			tabAtomlist = tabFirst + "    ";			
		}
		transit = transit + " rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>" + "\n";
		
		String tabRest = " ";
		tabAtomlist = tabFirst.substring(4);
		for (int i=0; i<numberOfHeadTriples-1; i++) {
			transit = transit + tabAtomlist + "</swrl:AtomList>" + "\n";
			tabRest = tabAtomlist.substring(4);
			transit = transit + tabRest + "</rdf:rest>" + "\n";
			tabAtomlist = tabRest.substring(4);
		}
		transit = transit + tabAtomlist + "</swrl:AtomList>" + "\n";
		transit = transit + tabBody + "</swrl:head>" + "\n";
				
		transit = transit + " </swrl:Imp>";
		return transit;
	}
	
	public int findNumberOfBodyTriples(String oneRuleN3) throws IOException {
    	int bodyTripleCounter=0;
		
    	//reading a file line by line till the end of the file 
	    BufferedReader br = new BufferedReader(new StringReader(oneRuleN3));
	    String line = "";
	    	
	    //for every line before we reach "=>"
	    while (((line = br.readLine()) != null) && (!line.contains("=>"))) {	
	    	
	    	/* It is possible that "predicate object ."  or "predicate object ;" are interpreted as "subject predicate object" with object=. or object=;
	    	 * To avoid this kind of errors we should prove the subject-less patterns at first. Additionally we should make the cases mutually exclusive. 
	    	 * For that we propose a if-else-net.
	    	*/
	    	// match a n3-built-in with multiple input channels
			// like (?bil ?al ?asc ?enc ?inr) math:sum ?s .
			if (Pattern.matches("[(]\\s*[?][a-zA-Z0-9_]+(\\s*[?][a-zA-Z0-9_]+)+\\s*[)]\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*[.]*\\s*", line)) {
				
				//check
				//System.out.println("CHECK");				
				
				// extra parsing of subject from '(' to ')'
				bodySubjectsList.add(line.substring(line.indexOf("("), line.indexOf(")") + 1));
				
				// the rest should be discovered for finding out predicate and object
				String lineRest = line.substring(line.indexOf(")")+1);
				
				/* we will split the rest of the line by blanks. 
				to avoid any hypothetical problems we remove the leading and ending blanks 
				*/
				lineRest = lineRest.replaceAll("^\\s+", "");
				lineRest = lineRest.replaceAll("[.]\\s+$", "");

				/* parsing of the rest of the line in a common way: split by blanks
				 the result is stored in some array.
				 handle the 0th element of this array as a predicate and the 1th - as an object. 
				 */
				String[] tripleArray = lineRest.split("\\s+");	
				System.out.println("TripleArray[0]: " + tripleArray[0]);
				System.out.println("TripleArray[1]: " + tripleArray[1]);
				bodyPredicatesList.add(tripleArray[0]);
				bodyObjectsList.add(tripleArray[1]);
	
				bodyTripleCounter++;
			}
			else {
				
				//match " predicate object ; " 
				if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*;\\s*", line)) {
					line = line.replaceAll("^\\s+", "");
					line = line.replaceAll(";\\s*$", "");
					String[] tripleArray = line.split("\\s+");
	
					/* solving the case where an object consists of multiple words. Like: "The death after 1 year"
	    			Therefore look for all split results from the 3rd part by "A B C"-Triples and from the 2nd part by "B C"-Triples 
					 */ 
					int i=0; String fullObject="";
					while (i < (tripleArray.length-1)) {
						fullObject = fullObject + tripleArray[i+1] + " ";
						i++;
					}
					
					//the last combining blank should be removed
					fullObject = fullObject.substring(0, fullObject.length()-1);	
					
					// if we are inside of " predicate object ; " triple take the last subject from the subjects List
					bodySubjectsList.add(bodySubjectsList.get(bodySubjectsList.size()-1));
					bodyPredicatesList.add(tripleArray[0]);
					bodyObjectsList.add(fullObject);
					
					//check
					//System.out.println("Fullobject: " + fullObject);
					bodyTripleCounter++;
				}
				else {
					//match " predicate object . "
					if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*[.]\\s*", line)) {
						line = line.replaceAll("^\\s+", "");
						line = line.replaceAll("[.]\\s*$", "");
						String[] tripleArray = line.split("\\s+");	
						
						// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
						int i=0; String fullObject="";
						while (i < (tripleArray.length-1)) {
							fullObject = fullObject + tripleArray[i+1] + " ";
							i++;
						}	
						
						//the last combining blank should be removed
						fullObject = fullObject.substring(0, fullObject.length()-1);
						
						// if we are inside of " predicate object ; " triple take the last subject from the subjects List
						bodySubjectsList.add(bodySubjectsList.get(bodySubjectsList.size()-1));
						bodyPredicatesList.add(tripleArray[0]);
						bodyObjectsList.add(fullObject);
						
						bodyTripleCounter++;
					}
					
					//match " predicate object " (without closing point. This occurs only as the last triple before body is closed with '}').
					else {
						if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*", line)) {
							line = line.replaceAll("^\\s+", "");
							line = line.replaceAll("[.]\\s*$", "");
							String[] tripleArray = line.split("\\s+");
							
							// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
							int i=0; String fullObject="";
							while (i < (tripleArray.length-1)) {
								fullObject = fullObject + tripleArray[i+1] + " ";
								i++;
							}
							
							//the last combining blank should be removed
							fullObject = fullObject.substring(0, fullObject.length()-1);	
							
							// if we are inside of " predicate object ; " triple take the last subject from the subjects List
							bodySubjectsList.add(bodySubjectsList.get(bodySubjectsList.size()-1));
							bodyPredicatesList.add(tripleArray[0]);
							bodyObjectsList.add(fullObject);
							
							bodyTripleCounter++;
						}
						else {
							//match " subject predicate object ; " 
							if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*;\\s*", line)) {
								
								// remove the leading spaces if any
								line = line.replaceAll("^\\s+", "");
								line = line.replaceAll(";\\s*$", "");
								String[] tripleArray = line.split("\\s+");
								
								/* solving the case where an object consists of multiple words. Like: "The death after 1 year"
	    		    			Therefore look for all split results from the 3rd part by "A B C"-Triples and from the 2nd part by "B C"-Triples 
								 */ 
								int i=0; String fullObject="";
								while (i < (tripleArray.length-2)) {
									fullObject = fullObject + tripleArray[i+2] + " ";
									i++;
								}
								
								//the last combining blank should be removed
								fullObject = fullObject.substring(0, fullObject.length()-1);
								
								bodySubjectsList.add(tripleArray[0]);
								bodyPredicatesList.add(tripleArray[1]);
								bodyObjectsList.add(fullObject);
								
								bodyTripleCounter++;
							}
							
							//match " subject predicate object . "
							else { 
								if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*[.]\\s*", line)) {
									
									line = line.replaceAll("^\\s+", "");
									line = line.replaceAll("[.]\\s*$", "");
									String[] tripleArray = line.split("\\s+");
									
									// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
									int i=0; String fullObject="";
									while (i < (tripleArray.length-2)) {
										fullObject = fullObject + tripleArray[i+2] + " ";
										i++;
									}
	    		    	
									//remove the last combining blank
									fullObject = fullObject.substring(0, fullObject.length()-1);
									
									bodySubjectsList.add(tripleArray[0]);
									bodyPredicatesList.add(tripleArray[1]);
									bodyObjectsList.add(fullObject);
									
									bodyTripleCounter++;
								}	
	    		    	
								//match " subject predicate object " (without closing point. This occurs only as the last triple before body is closed with '}'). 
								else  {
									if (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*", line)) {
										line = line.replaceAll("^\\s+", "");
										line = line.replaceAll("[.]\\s*$", "");
										String[] tripleArray = line.split("\\s+");
										
										// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
										int i=0; String fullObject="";
										while (i < (tripleArray.length-2)) {
											fullObject = fullObject + tripleArray[i+2] + " ";
											i++;
										}

										//remove the last combining blank
										fullObject = fullObject.substring(0, fullObject.length()-1);
	    		    	
										bodySubjectsList.add(tripleArray[0]);
										bodyPredicatesList.add(tripleArray[1]);
										bodyObjectsList.add(fullObject);
										
										bodyTripleCounter++;
									}
								}
							}
						}	
					}
				}
			}
	    }		
    	return bodyTripleCounter;
	}
	public int findNumberOfHeadTriples (String oneRuleN3) throws IOException  {
		int headTripleCounter=0;
		int borderNumber = 0; 
	
		//reading a file line by line till the end of the file 
	    BufferedReader br = new BufferedReader(new StringReader(oneRuleN3));
	    String line;
	    int lineNumber = 0;
	    while ((line = br.readLine()) != null) {
	    	lineNumber++;
	    	// if we've reached the "=>" save the number of this line as a borderNumber
	    	if (line.contains("=>"))
	    		borderNumber = lineNumber;
	    } 
		br.close();

		br = new BufferedReader(new StringReader(oneRuleN3));
	    lineNumber = 0;
	    // go through the rule once more 
	    while ((line = br.readLine()) != null) {
	    	lineNumber++;
	    		
	    	//count only the lines with the number greater than the borderNumber - they are at the head side.
	    	
	    	//match " subject predicate object ; " 
	    	if ((lineNumber > borderNumber) && (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s+[ a-zA-Z_0-9:,?\"./)^(-]+\\s*;\\s*", line))) {
	    		
	    		// remove the leading spaces if any
	    		line = line.replaceAll("^\\s+", "");
	    		line = line.replaceAll(";\\s*$", "");
	    		String[] tripleArray = line.split("\\s+");
	    		
	    		// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
	    		int i=0; String fullObject="";
	    		while (i < (tripleArray.length-2)) {
	    			fullObject = fullObject + tripleArray[i+2] + " ";
	    			i++;
	    		}
	    		
	    		//the last combining blank should be removed
	    		fullObject = fullObject.substring(0, fullObject.length()-1);
	    	
	    		headSubjectsList.add(tripleArray[0]);
	    		headPredicatesList.add(tripleArray[1]);
	    		headObjectsList.add(fullObject);
	    		
	    		headTripleCounter++;
	    		
	    		//check
	    		/*
	    		System.out.println("BIBA-3-Semikolon!!!!!!!!!!!!!!!!!!!!!!!");
	    		System.out.println("Subject: " + tripleArray[0]);
	    		System.out.println("Predicate: " + tripleArray[1]);
	    		System.out.println("Object: " + tripleArray[2]);
	    		System.out.println("helpTripleCounter: " + headTripleCounter);
	    		*/
	    	}
	    	
	    	// There should be no lines in the head which end with blank nodes. So we should not build the if-else-net and keep the special order.
	    	//match " subject predicate object . " 
	    	if ((lineNumber > borderNumber) && (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s+[ a-zA-Z_0-9:,?\"./)^(-]+\\s*[.]\\s*", line))) {
	    		
	    		// remove the leading spaces if any
	    		line = line.replaceAll("^\\s+", "");
	    		line = line.replaceAll("[.]\\s*$", "");
	    		String[] tripleArray = line.split("\\s+");
	    		
	    		// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
	    		int i=0; String fullObject="";
	    		while (i < (tripleArray.length-2)) {
	    			fullObject = fullObject + tripleArray[i+2] + " ";
	    			i++;
	    		}
	    		
	    		//the last combining blank should be removed
	    		fullObject = fullObject.substring(0, fullObject.length()-1);
	    		
	    		//check
	    		//System.out.println("Full object: " + fullObject);
	    		
	    		headSubjectsList.add(tripleArray[0]);
	    		headPredicatesList.add(tripleArray[1]);
	    		headObjectsList.add(fullObject);
	    		
	    		headTripleCounter++;
	    	
	    	}
	    		    	
	    	//match " predicate object ; " 
	    	if ((lineNumber > borderNumber) && (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*;\\s*", line))) {
	    		
	    		// remove the leading spaces if any
	    		line = line.replaceAll("^\\s+", "");
	    		line = line.replaceAll(";\\s*$", "");	    		
	    		String[] tripleArray = line.split("\\s+");
	    	
	    		// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
	    		int i=0; String fullObject="";
	    		while (i < (tripleArray.length-1)) {
	    			fullObject = fullObject + tripleArray[i+1] + " ";
	    			i++;
	    		}
	    		
	    		//the last combining blank should be removed
	    		fullObject = fullObject.substring(0, fullObject.length()-1);
	    		
	    		headSubjectsList.add(headSubjectsList.get(headSubjectsList.size()-1));
	    		headPredicatesList.add(tripleArray[0]);
	    		headObjectsList.add(fullObject);
	    		
	    		headTripleCounter++;
	    		
	    		//check
	    		/*
	    		System.out.println("BIBA-2-Semikolon!!!!!!!!!!!!!!!!!!!!!!!");
	    		System.out.println("Subject: " + headSubjectsList.get(headSubjectsList.size()-1));
	    		System.out.println("Predicate: " + tripleArray[0]);
	    		System.out.println("Object: " + tripleArray[1]);
	    		System.out.println("helpTripleCounter: " + headTripleCounter);
	    		*/
	    	}
	    	
	     	//match " predicate object . " 
	    	if ((lineNumber > borderNumber) && (Pattern.matches("\\s*[a-zA-Z_0-9:,?\"./)^(-]+\\s+[a-zA-Z_0-9:,?\"./)^(-]+\\s*[.]\\s*", line))) {
	    		
	    		// remove the leading spaces if any
	    		line = line.replaceAll("^\\s+", "");
	    		line = line.replaceAll("[.]\\s*$", "");
	    		String[] tripleArray = line.split("\\s+");
	    	
	    		// solving the case where an object consists of multiple words. Like: "The death after 1 year" 
	    		int i=0; 
	    		String fullObject="";
	    		while (i < (tripleArray.length-1)) {
	    			fullObject = fullObject + tripleArray[i+1] + " ";
	    			i++;
	    		}
	   
	    		//the last combining blank should be removed
	    		fullObject = fullObject.substring(0, fullObject.length()-1);
	    		
	    		headSubjectsList.add(headSubjectsList.get(headSubjectsList.size()-1));
	    		headPredicatesList.add(tripleArray[0]);
	    		headObjectsList.add(fullObject);
	    		
	    		headTripleCounter++;

	    	}
	    } 	
	    br.close();
	    
	    return headTripleCounter;
	}  
	
	public String createXMLTripleFromN3(String rulePart, int tripleNumber, String result, String ruleName, String tab, List<String> allPrefixuris) throws MalformedURLException, IOException {
		
		//preparation
		String subjectWithPrefix ="";
		String predicateWithPrefix ="";
		String objectWithPrefix ="";
		
		//the variable "subject" will store the full URI of appropriate concept after prefix resolving (e.g.: "http://localhost/mediawiki/index.php/Special:URIResolver/Variable#o2"
		String subject ="";
		
		//the variable "predicate" will store the meaningful part (the concept) of the predicate (e.g.: "hasPatient")
		String predicate ="";
		
		/* 
		 * Object could be either a node (need namespace) or a literal (no need on namespace) 
		 * the variable "object" will store the meaningful part (the concept) of the predicate (e.g.: "leaflet_sclerosis")
		 * the variable "objectFull" will store the full URI after possible prefix resolving (e.g.: "http://localhost/mediawiki/index.php/Special:URIResolver/leaflet_sclerosis")
		 */
		String object ="";
		String objectFull = ""; 	
		
		// for built-ins with multiple inputs (subjects)
		List<String> multiSubjectList = new ArrayList<String>();

		
		//check: where are we? should we use the body list or the head lists?
		if (rulePart.equalsIgnoreCase("body")) {
			
			// our concepts do not obligatory contain a prefix. A variable for example doesn't have any prefix. Anyway we name them so.
			subjectWithPrefix = bodySubjectsList.get(tripleNumber);
			predicateWithPrefix = bodyPredicatesList.get(tripleNumber);
			objectWithPrefix = bodyObjectsList.get(tripleNumber);
		}
		else {
			subjectWithPrefix = headSubjectsList.get(tripleNumber);
			predicateWithPrefix = headPredicatesList.get(tripleNumber);
			objectWithPrefix = headObjectsList.get(tripleNumber);	
		}
	
		//------------------------------------parse subject---------------------------------------------------------
		//check
		System.out.println("Subject with prefix: " + subjectWithPrefix);
		
		// is it a built-in with multiple inputs (subjects)? e.g.: (?a ?b) math:sum ?c
		if (subjectWithPrefix.charAt(0)=='(') {
			
			/* prepare the subject for further processing. We are going to cut the patterns between '?' and ' '. 
			 * To assure that a blank follows after ?varn, we replace the ')' by ' '
			 * */
			subjectWithPrefix = subjectWithPrefix.replaceAll("[)]", " ");
			
			/* now we should parse the composite expression "(?var1 ?var2 ... ?varn)" to the n separate subjects ?var1, ?var2, ..., ?varn 
			which will be handled as rdf:collection */
			int i = subjectWithPrefix.indexOf("?");
			while ((i > 0) && (i < subjectWithPrefix.length())) {
				
				//check
				System.out.println("Begin of the current subject: " + i);
				System.out.println("End of the current subject: " + subjectWithPrefix.indexOf(" ", i));
				
				multiSubjectList.add(baseVar + subjectWithPrefix.substring(i+1, subjectWithPrefix.indexOf(" ", i)));
				i = subjectWithPrefix.indexOf("?", i+1);
				
				/* choose the smaller index: either the index of a variable or an index of a number 
				 * 
				 * not done yet! 
				 *
				 * i = Math.min(subjectWithPrefix.indexOf("?", i+1), subjectWithPrefix.indexOf("\\d", i+1));
				 */
				
				//check
				System.out.println("MultiSubjectsList: " + multiSubjectList);
			}
		}
		
		//is it a variable?
		if (subjectWithPrefix.charAt(0)=='?') {
			subject = baseVar + subjectWithPrefix.substring(1, subjectWithPrefix.length());
			if (!variablesBank.contains(subject))
				variablesBank.add(subject);
		}
		else {
			//getting the concept labels without a prefix if any
			//here we could apply the method prefixResolver (see below)
			if (subjectWithPrefix.contains(":")) {
				
				//parse only the prefix part of the subject
				String subjectPrefix = subjectWithPrefix.substring(0, subjectWithPrefix.indexOf(":"));
				
				//parse only the concept part of the subject
				String subjectConcept = subjectWithPrefix.substring(subjectWithPrefix.indexOf(":")+1);
				
				//find this prefix under all prefixes and get the appropriate URI. Add to this URI the concept part.
				subject = prefixResolver(subjectPrefix, allPrefixuris) + subjectConcept; 
			}
			
			//if subject hasn't a prefix it should refer to the base namespace
			else 
				subject = base + subjectWithPrefix;
		}
		
		//check
		System.out.println("Subject: " + subject);
		System.out.println("");
		
		//------------------------------------parse predicate---------------------------------------------------------
				
		if (predicateWithPrefix.contains(":")) {
				/* 
				 * We do not resolve predicate prefix at this point because we need firstly the predicate concept (not the full URI but the meaningful part)
				 * for 2 checks 
				 * 1) is it "a"?
				 * 2) access the corresponding page from SMW
				 * 
				 * Then, before outputting the predicate we'll perform predicate prefix resolving
				 */
				predicate = predicateWithPrefix.substring(predicateWithPrefix.indexOf(":")+1, predicateWithPrefix.length());			
			}
			else 
				predicate = predicateWithPrefix;

		//------------------------------------parse object---------------------------------------------------------
		//check
		System.out.println("Object with prefix: " + objectWithPrefix);
		
		//is it a variable?
		if (objectWithPrefix.charAt(0)=='?') {
			objectFull = baseVar + objectWithPrefix.substring(1, objectWithPrefix.length());
			
			//if this variable isn't in the variables bank yet please add it there
			if (!variablesBank.contains(objectFull))
				variablesBank.add(objectFull);
		}
		else {
			/*
			if (objectWithPrefix.contains(":")) {
				
				/* 
				 * we do not resolve the object prefix at this point because 
				 * it could be either a node (need namespace) or a literal (no need on namespace) 
				 * take this decision by the handling of DatavaluedPropertyAtom (see below)
				 * here just save the concept in the variable "object" 
				 */
			/*
				object = objectWithPrefix.substring(objectWithPrefix.indexOf(":")+1, objectWithPrefix.length());	
			}
			else 
				object = objectWithPrefix;
			*/
			
			if (objectWithPrefix.contains(":")) {
			
				//parse only the prefix part of the object
				String objectPrefix = objectWithPrefix.substring(0, objectWithPrefix.indexOf(":"));
			
				//parse only the concept part of the object
				object = objectWithPrefix.substring(objectWithPrefix.indexOf(":")+1, objectWithPrefix.length());	
		
				//find this prefix under all prefixes and get the appropriate URI. Add to this URI the concept part.
				objectFull = prefixResolver(objectPrefix, allPrefixuris) + object; 		
			}
			else {
				object = objectWithPrefix;
			}
			
		}
		
		//check
		System.out.println("ObjectFull: " + objectFull);
		System.out.println("Object: " + object);
		System.out.println("");

		/*------------------- parse current triple as SWRL atom  -------------------------------------- */
		
		/* Possible atoms are "DatavaluedPropertyAtom", "IndividualPropertyAtom", "ClassAtom" */
		
		/* OPTIONAL: trying to parse built-ins with multiple input channels: sum, difference, product, quotient etc.
		  n3 allows the constructs like this: "(?bil ?al) math:sum ?score" 
		  should be translated into SWRL as a collection(!):
		 
		   <swrl:builtin rdf:resource="http://www.w3.org/2003/11/swrlb#add"/>
		   <swrl:arguments rdf:parseType="Collection">
		   	<rdf:Description rdf:about="Variable#score"/>
			<rdf:Description rdf:about="Variable#bil"/>
			<rdf:Description rdf:about="Variable#al
		 */
		if ((predicate.equalsIgnoreCase("sum")) | (predicate.equalsIgnoreCase("difference")) | (predicate.equalsIgnoreCase("product")) | (predicate.equalsIgnoreCase("quotient")) | (predicate.equalsIgnoreCase("exponentiation"))) {
			switch (predicate) {
			case "sum":
				predicate = "add";
				break;
			
			case "difference":
				predicate = "subtract";
				break;
			
			case "product":
				predicate = "multiply";
				break;
			
			case "quotient":
				predicate = "divide";
				break;
				
			case "exponentiation":
				predicate = "pow";
				break;	
			}

			result = result + tab + "<rdf:Description>" + "\n";
			result = result + tab + "	<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#BuiltinAtom\"/>" + "\n";
			result = result + tab + "	<base:Property-3AIs_premise_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
			result = result + tab + "	<swrl:builtin rdf:resource=\"http://www.w3.org/2003/11/swrlb#" + predicate + "\"/>" + "\n";
			result = result + tab + "	<swrl:arguments rdf:parseType=\"Collection\">" + "\n";
			result = result + tab + "		<rdf:Description rdf:about=\"" + objectFull + "\"/>" + "\n";

			//add multiple subjects of a built-in
			for (int i=0; i<multiSubjectList.size(); i++) {
				result = result + tab + "		<rdf:Description rdf:about=\"" + multiSubjectList.get(i) + "\"/>" + "\n";
			}
			
			result = result + tab + "	</swrl:arguments>" + "\n";
			result = result + tab + "</rdf:Description>" + "\n";
		}
		else {
			/* trying to parse binary built-ins: lessThan, greaterThan, etc.
			 */
			if ((predicate.equalsIgnoreCase("lessThan")) | (predicate.equalsIgnoreCase("greaterThan")) | (predicate.equalsIgnoreCase("notLessThan")) | (predicate.equalsIgnoreCase("notGreaterThan")) | (predicate.equalsIgnoreCase("equalTo"))) {
				
				// SWRL and N3 have different magic words for some mathematical operators. "lessThan" and "greaterThan" are equal in both formats. For the rest we specify the transformation.
				switch (predicate) {
				case "notLessThan":
					predicate = "greaterThanOrEqual";
					break;
				case "notGreaterThan" : 
					predicate = "lessThanOrEqual";
					break;
				case "equalTo":
					predicate = "equal";
					break;				
				}
			
				// Further there is a case differentiation by the object datatype. We define a special variable bobject(built-in object) to save this info. 
				String bobject ="";

				// if object is a string		
				if (object.charAt(0)=='\"') 
					bobject = "rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + object + "</rdf:first>";
			
				// if object is a floating-point number
				if (Pattern.matches("\\d+.\\d+", object)) 
					bobject = "rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">" + object + "</rdf:first>";
		
				// if object is an integer
				if (Pattern.matches("\\d+", object))
					bobject = "rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">" + object + "</rdf:first>";
				
				// if object is a boolean
				if ((Pattern.matches("true", object)) || (Pattern.matches("false", object)))
					bobject = "rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">" + object + "</rdf:first>";
						
				// if object is another variable
				if (objectWithPrefix.charAt(0)=='?')
					bobject = "rdf:resource=\"" + object + "\"/>";
			
				result = result + tab + "<rdf:Description>" + "\n";
				result = result + tab + "	<rdf:type rdf:resource=\"http://www.w3.org/2003/11/swrl#BuiltinAtom\"/>" + "\n";
				result = result + tab + "	<swrl:builtin rdf:resource=\"http://www.w3.org/2003/11/swrlb#" + predicate + "\"/>" + "\n";
				result = result + tab + "	<base:Property-3AIs_premise_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
				result = result + tab + "	<swrl:arguments>" + "\n";
				result = result + tab + "		<rdf:Description>" + "\n";
				result = result + tab + "			<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#List\"/>" + "\n";
				result = result + tab + "			<rdf:first rdf:resource=\"" + subject + "\"/>" + "\n"; 
				result = result + tab + "			<rdf:rest>" + "\n"; 
				result = result + tab + "				<rdf:Description>" + "\n";
				result = result + tab + "					<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#List\"/>" + "\n";
				result = result + tab + "					<rdf:first " + bobject + "\n";
				result = result + tab + "					<rdf:rest rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>" + "\n"; ;
				result = result + tab + "				</rdf:Description>" + "\n";
				result = result + tab + "			</rdf:rest>" + "\n"; 
				result = result + tab + "		</rdf:Description>" + "\n";
				result = result + tab + "	</swrl:arguments>" + "\n";
				result = result + tab + "</rdf:Description>" + "\n";
			}
			else {
				//if we are dealing with "subject a object" triple
				if (predicate.equalsIgnoreCase("a")) {
					
					//output ClassAtom into the result file
					result = result + tab + "<swrl:ClassAtom>" + "\n";
					if (rulePart.equalsIgnoreCase("body")) 
						result = result + tab + "  <base:Property-3AIs_premise_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
					else 
						result = result + tab + "  <base:Property-3AIs_conclusion_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
					result = result + tab + "  <swrl:argument1 rdf:resource=\"" + subject + "\"/>" + "\n"; 
					result = result + tab + "  <swrl:classPredicate rdf:resource=\"" + objectFull + "\"/>" + "\n"; 
					result = result + tab + "</swrl:ClassAtom>" + "\n";
					
				}
				
				//if it's a "subject predicate object" triple
				else {
					//get the RDF export of the current predicate
					Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/Property:" + predicate).openStream(), "UTF-8").useDelimiter("\\A");
					String currentPredicatePage = scanner.next();
					//check
					//System.out.println("RDFExport: " + currentPredicatePage);
					scanner.close();
					
					/*------------------- prepare predicate for output  -------------------------------------- */					
					
					String predicateFull = ""; 					
					if (predicateWithPrefix.contains(":")) {

						//parse only the prefix part of the predicate
						String predicatePrefix = predicateWithPrefix.substring(0, predicateWithPrefix.indexOf(":"));
					
						//predicate == predicateConcept (this operation is already done)
						//String predicateConcept = predicateWithPrefix.substring(predicateWithPrefix.indexOf(":")+1);
				
						//find this prefix under all prefixes and get the appropriate URI. Add to this URI the concept part.
						predicateFull = prefixResolver(predicatePrefix, allPrefixuris) + predicate; 		
					}
					else {
						predicateFull = base + predicate;
					}
					
					/*----------------------------------------------------------------------------------------------------------- */					
					
					// Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
					// For this do activate RegEx multiline modus with (?sm)
					if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
						
						//output IndividualPropertyAtom into the result file
						result = result + tab + "<swrl:IndividualPropertyAtom>" + "\n";
						if (rulePart.equalsIgnoreCase("body")) 
							result = result + tab + "  <base:Property-3AIs_premise_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
						else 
							result = result + tab + "  <base:Property-3AIs_conclusion_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
						result = result + tab + "  <swrl:argument1 rdf:resource=\"" + subject + "\"/>" + "\n"; 
						result = result + tab + "  <swrl:propertyPredicate rdf:resource=\"" + predicateFull + "\"/>" + "\n"; 
				
						// if our object is a variable it has got already a namespace 
						if (objectWithPrefix.charAt(0)=='?') {
							result = result + tab + "  <swrl:argument2 rdf:resource=\"" + objectFull + "\"/>" + "\n";
						}
						else 
							result = result + tab + "  <swrl:argument2 rdf:resource=\"" + objectFull + "\"/>" + "\n";
				
						result = result + tab + "</swrl:IndividualPropertyAtom>" + "\n";
					}	
					else {			
						
						//output DatavaluedPropertyAtom into the result file
						result = result + tab + "<swrl:DatavaluedPropertyAtom>" + "\n";
						if (rulePart.equalsIgnoreCase("body")) 
							result = result + tab + "  <base:Property-3AIs_premise_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
						else 
							result = result + tab + "  <base:Property-3AIs_conclusion_of rdf:resource=\"" + base + ruleName + "\"/>" + "\n";
						result = result + tab + "  <swrl:argument1 rdf:resource=\"" + subject + "\"/>" + "\n"; 
						result = result + tab + "  <swrl:propertyPredicate rdf:resource=\"" + predicateFull + "\"/>" + "\n"; 
						
						// if our object is a variable it has got already a namespace 
						if (objectWithPrefix.charAt(0)=='?') {
							result = result + tab + "  <swrl:argument2 rdf:resource=\"" + objectFull + "\"/>" + "\n";
						}
						
						// if our object is a literal it needs no namespace 
						else {
							
							//------------Make up---------------------------------------------------------------------------------------------------------------------------
							//------------parsing the literal datatype------------------------------------------------------------------------------------------------------
						
							//parse string literals					
							if (object.charAt(0)=='\"') {
								object = object.substring(1, object.length()-1);
								result = result + tab + "  <swrl:argument2 rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + object + "</swrl:argument2>" + "\n";
							}
							
							//parse double literals
							if (Pattern.matches("\\d+.\\d+", object)) {
								result = result + tab + "  <swrl:argument2 rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">" + object + "</swrl:argument2>" + "\n";
							}
							
							//parse integer literals
							if (Pattern.matches("\\d+", object)) {
								result = result + tab + "  <swrl:argument2 rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">" + object + "</swrl:argument2>" + "\n";
							}					
							
							//parse boolean literals
							if ((Pattern.matches("true", object)) || (Pattern.matches("false", object)))
								result = result + tab + "  <swrl:argument2 rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">" + object + "</swrl:argument2>" + "\n";
						}
						result = result + tab + "</swrl:DatavaluedPropertyAtom>" + "\n";
					}
				}
			}
		}
		return result;
	}		
	
	public List<String> getVariablesBank() {
		return variablesBank;
	}
	
	/* this method provides the rule root with the "Has_topic" annotation properties. 
	 * The objects of this relationship are all subjects and objects which occur in a rule and which are not variables and literals. 
	 * For example for the rule 	
	 *
	 *	{
 	 *	 ?p1 baseProp:hasFather base:Boris .
     *	 ?p2 baseProp:hasMother base:Natasha .
 	 *	 base:Boris baseProp:marriedTo base:Natasha .
	 *	} => {
	 *		?p1 baseProp:hasSibling "yes" .
	 *	} .
	 *
	 * we get following topics: 
	 * base:Boris
  	 * base:Natasha.
	 */
	public String addHasTopic(String allRulesRDF, String ruleName, List<String> allPrefixuris) throws IOException {			

		BufferedReader br = new BufferedReader(new StringReader(allRulesRDF));
		String result = "";
		String line;
		while ((line = br.readLine()) != null) {
			
			//check
			//System.out.println(line);
			
			result = result + line + "\n";
			if (Pattern.matches("\\s*<swrl:Imp rdf:about=\"http://localhost/mediawiki/index.php/Special:URIResolver/" + ruleName + "\">\\s*", line)) {
				
				//find the number of blank nodes leading the line with the obtained atom type
				Pattern patternTab = Pattern.compile("(\\s*)<swrl:Imp rdf:about=\"" + base + ruleName + "\">\\s*");
				Matcher mtchTab = patternTab.matcher(line);
				String tab = "";
				while (mtchTab.find()) {
					//and add it to the classNames list
					tab = mtchTab.group(1);
				}
				
				List<String> ListForTopic = new ArrayList<String>(); 
				ListForTopic.addAll(bodySubjectsList);
				ListForTopic.addAll(bodyObjectsList);
				ListForTopic.addAll(headSubjectsList);
				ListForTopic.addAll(headObjectsList);

				//remove duplicates from the topics list
				Set<String> concepts = new LinkedHashSet<>(ListForTopic);
				ListForTopic.clear();
				ListForTopic.addAll(concepts);
				
				//check
				System.out.println("ListForTopic: " + ListForTopic);
				
				//remove variables, literals and blank nodes from the topics list
				for (int i=0; i<ListForTopic.size(); i++) {
					if ((ListForTopic.get(i).charAt(0)==('?')) || (ListForTopic.get(i).charAt(0)=='\"') || (Pattern.matches("\\d+", ListForTopic.get(i))) || (Pattern.matches("\\d+.\\d+", ListForTopic.get(i))) || (ListForTopic.get(i).equalsIgnoreCase("true")) || (ListForTopic.get(i).equalsIgnoreCase("false")) || (ListForTopic.get(i).charAt(0)==('_'))) {
						ListForTopic.remove(i);
						i--;
					}	
				}
				
				//check
				System.out.println("ListForTopic2: " + ListForTopic);
				System.out.println("");

				//immediate addition of Has_topic property
				for (int i=0; i<ListForTopic.size(); i++) {
					
					// here we want to add correct object resource. Therefore we should resolve the prefix.
					String topic = ListForTopic.get(i);
					if (topic.contains(":")) {
						String topicPrefix = topic.substring(0, topic.indexOf(":"));
						String topicConcept = topic.substring(topic.indexOf(":")+1);
						result = result + tab + "   <base:Property-3AHas_topic rdf:resource=\"" + prefixResolver(topicPrefix, allPrefixuris) + topicConcept + "\"/>" + "\n";
					}
				}	
			}			
		}
		br.close();
		//writer.close();
		return result;
	}
	
	/* 
	 * Prefixuris are given in following format: 
	 * xnat="http://aifb-ls3-vm2.aifb.kit.edu:8080/xnatwrapper/id/project/Liver_Factors#"
	 * 
	 */
	public String prefixResolver(String prefix, List<String> allPrefixuris) {			
		String fullnamespace = "";
		
		// if we deal with a blank node which looks like "_:bnode" handle it as an individual
		if (prefix.equalsIgnoreCase("_")) {
			fullnamespace = baseInd;
		}
		
		// by encountering a needed @param prefix within allPrefixuris we return the according to this prefix uri.
		for (String a : allPrefixuris) {
			if ((a.substring(0, a.indexOf("=")).equalsIgnoreCase(prefix))) {
				fullnamespace = a.substring(a.indexOf("=")+2, a.length()-1);
			}
		}
		return fullnamespace;
	}

	/* Future Work 
	 * get prefix as input and output the appropriate namespace 
	 */
	
	/*
	public String prefixResolving (File ruleN3) {
		
		if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
		return fullNamespace;
	}
	*/
}


/*
//parse string literals
//in rdf/xml: rdf:datatype="http://www.w3.org/2001/XMLSchema#string">No
if (object.charAt(0)=='\"')
	object = object + "^^xsd:string";

//parse double literals
//in rdf/xml: rdf:datatype="http://www.w3.org/2001/XMLSchema#double">2.0
if (Pattern.matches("\\d+.\\d+", object))
	object = object + "^^xsd:double";
		
//parse integer literals
//in rdf/xml: rdf:datatype="http://www.w3.org/2001/XMLSchema#integer">1
if (Pattern.matches("\\d+", object))
	object = object + "^^xsd:integer";
*/

//testing pattern stuff
/*
System.out.println (Pattern.matches("\\s*[a-zA-Z_0-9:?]+\\s+[a-zA-Z_0-9:?]+\\s+[a-zA-Z_0-9:?]+\\s+\\.", " ?p1 ex:has_Father ?Bor8is ."));
System.out.println (Pattern.matches("\\s*([a-zA-Z_0-9:?]+\\s+){3}\\.\\s*", "	?p1	ex:has_Father   ?Bor8is .   "));
String test = "    ?p1	 	ex:has_Father  ex:Boris .";
test = test.replaceAll("^\\s+", "");
System.out.println(test);

String[] testArray = test.split("\\s+");
System.out.println(testArray[0]);
System.out.println(testArray[1]);
System.out.println(testArray[2]);
System.out.println(testArray[3]);
 */
