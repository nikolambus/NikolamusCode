package apTogether;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class N3_RDF_Mapping2 {

	//here we gonna store the name of the rule file
	//for example: String rule = "boris_natasha.n3";
	static String rule = null;
	static String ruleTransit = null;
	static String ruleRDF = null;
	static List<String> n3BodyTriplesList = new ArrayList<String>(); 

	
	public static void main(String[] args) throws IOException {
		
		
				
		// define the N3 rule file (input from user)
		System.out.println("Please enter your rule file in n3 format");
		Scanner inputScanner = new Scanner(System.in);
		rule = inputScanner.nextLine();
		ruleTransit = "help";
		ruleRDF = "resultec";
		System.out.println("Your rule file is: " + rule);
		System.out.println("Your transit file is: " + ruleTransit);
		System.out.println("Your result file is: " + ruleRDF);


		inputScanner.close();

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
		
		//getting the workspace folder (actually the same thing as System.getProperty("user.dir") + patient)
	    Path rulePath = Paths.get(rule);	   
        File ruleFile = rulePath.toFile();
        
        /* testing
        System.out.println(findNumberOfBodyTriples(ruleFile));
        System.out.println(findNumberOfHeadTriples(ruleFile));
        */
        
		if (!ruleFile.exists())
	        	System.out.println("No such file in " + System.getProperty("user.dir"));
	    else {
	    	 
	    	//define the TTL result rule file (output)
			PrintWriter writer = new PrintWriter(ruleTransit, "UTF-8");
			//add rule independent prefixes to the result file
			writer.println("<rdf:RDF xmlns:ex=\"http://niko/example#\" xmlns:var=\"http://niko/variable#\" xmlns:swrl=\"http://www.w3.org/2003/11/swrl#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">");
			createXMLstructureBody(ruleFile, writer);
			writer.close();

			PrintWriter writer2 = new PrintWriter(ruleRDF, "UTF-8");
			
			//reading a file line by line till the end of the file 
	    	BufferedReader br2 = new BufferedReader(new FileReader(ruleTransit));
	    	String line;
	    	int i=0;
	    	while ((line = br2.readLine()) != null) {
	    		if (Pattern.matches("\\s+Triple\\d+", line)) {	
	    			//find the tab distance till 'T' (from "Triple")
	    			String tab = line.substring(0, line.indexOf('T')-1);
	    			createXMLTripleFromN3(n3BodyTriplesList.get(i), writer2, tab);
	    			i++;
	    		}
	    		else 
	    			writer2.println(line);
	    	}
		    br2.close();
			writer2.close();
	    }		
	}
	
	public static int findNumberOfBodyTriples (File ruleFile) throws IOException  {
    	int bodyTripleCounter=0;
		if (!ruleFile.exists())
        	System.out.println("No such file in " + System.getProperty("user.dir"));
		else {
			//reading a file line by line till the end of the file 
	    	BufferedReader br = new BufferedReader(new FileReader(rule));
	    	String line = "";
	    	while (((line = br.readLine()) != null) && (!line.contains("=>"))) {	
	    		if (Pattern.matches("\\s*([a-zA-Z_0-9:,?\"./]+\\s+){3}\\.?\\s*", line)) {
	    			//System.out.println(line);
	    			n3BodyTriplesList.add(line);
	    			bodyTripleCounter++;
	    		}	
	    	}
		}
    	return bodyTripleCounter;
	}

	public static int findNumberOfHeadTriples (File ruleFile) throws IOException  {
    	int bodyHeadCounter=0;
    	int borderNumber = 0; 
		if (!ruleFile.exists())
        	System.out.println("No such file in " + System.getProperty("user.dir"));
		else {
			//reading a file line by line till the end of the file 
	    	BufferedReader br = new BufferedReader(new FileReader(rule));
	    	String line;
	    	int lineNumber = 0;
	    	while ((line = br.readLine()) != null) {
	    		lineNumber++;
	    		if (line.contains("=>"))
	    			borderNumber = lineNumber;
	    	} 
		    br.close();

		    br = new BufferedReader(new FileReader(rule));
	    	lineNumber = 0;
	    	while ((line = br.readLine()) != null) {
	    		lineNumber++;
	    		if ((lineNumber > borderNumber) && (Pattern.matches("\\s*([a-zA-Z_0-9:,?\"./]+\\s+){3}\\.?\\s*", line)))
	    			bodyHeadCounter++;
	    		
	    		/* testing
	    		System.out.println("linenumber: " + lineNumber);
	    		System.out.println("borderNmber: " + borderNumber);
	    		System.out.println("line: " + line);
	    		*/
	    		
	    	} 	
	    	br.close();
		}
    	return bodyHeadCounter;
	}
	
	public static void createXMLstructureBody (File ruleFile, PrintWriter writer) throws IOException {
		String tabAtomlist = " ";
		String tabFirst = " ";
		String tabTriple = " ";
    	for (int i=0; i<findNumberOfBodyTriples(ruleFile); i++) {
    		writer.println(tabAtomlist + "Atomlist");
    		tabFirst = tabAtomlist + "    ";
    		writer.println(tabFirst + "first");
    		tabTriple = tabFirst + "    ";
    		writer.println(tabTriple + "Triple" + (i+1));
    		writer.println(tabFirst + "/first");
    		writer.println(tabFirst + "rest");
    		tabAtomlist = tabFirst + "    ";
    	}
		writer.println(tabAtomlist + "nil");
		
		String tabRest = " ";
		tabAtomlist = tabFirst.substring(4);
		for (int i=0; i<findNumberOfBodyTriples(ruleFile)-1; i++) {
    		writer.println(tabAtomlist + "/Atomlist");
    		tabRest = tabAtomlist.substring(4);
    		writer.println(tabRest + "/rest");
    		tabAtomlist = tabRest.substring(4);
    	}
		writer.println(tabAtomlist + "/Atomlist");
	}
	
	public static void createXMLTripleFromN3 (String line, PrintWriter writer2, String tab) {
		// remove the leading spaces if any
		line = line.replaceAll("^\\s+", "");
		
		// parse subject, predicate and object from the triple
		String[] tripleArray = line.split("\\s+");
		String subject = tripleArray [0];
		String predicate = tripleArray [1];
		String object = tripleArray [2];
		
		//parse variables
		if (subject.charAt(0)=='?') {
			String ttlVar = "var:" + subject.substring(1, subject.length());
			subject = ttlVar;
		}
		if (object.charAt(0)=='?') {
			String ttlVar = "var:" + object.substring(1, object.length());
			object = ttlVar;
		}
		
		/*
		//parse string literals
		if (object.charAt(0)=='\"')
			object = object + "^^xsd:string";
		
		//parse double literals
		if (Pattern.matches("\\d+.\\d+", object))
			object = object + "^^xsd:double";
		
		//parse integer literals
		if (Pattern.matches("\\d+", object))
			object = object + "^^xsd:integer";
		*/
		
		/* FINDING OUT IndividualPropertyAtom or DatavaluedPropertyAtom!!!!!!  */
		
		//output into the result file
		writer2.println(tab + "[");
		writer2.println(tab + "  rule:subject " + subject + ";"); 
		writer2.println(tab + "  rule:predicate " + predicate + ";"); 
		writer2.println(tab + "  rule:object " + object);
		writer2.println(tab + "] .");
	}
}
