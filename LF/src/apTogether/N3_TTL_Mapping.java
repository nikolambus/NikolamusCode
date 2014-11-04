package apTogether;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class N3_TTL_Mapping {

	public static void main(String[] args) throws IOException {
		
		//here we gonna store the name of the rule file
		//for example: String rule = "boris_natasha.n3";
		String rule = null;
				
		// define the N3 rule file (input from user)
		System.out.println("Please enter your rule file in n3 format");
		Scanner inputScanner = new Scanner(System.in);
		rule = inputScanner.nextLine();
		System.out.println("Your rule file is: " + rule);
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
		if (!ruleFile.exists())
	        	System.out.println("No such file in " + System.getProperty("user.dir"));
	    else {
	    	 
	    	//define the TTL result rule file (output)
			PrintWriter writer = new PrintWriter("result.ttl", "UTF-8");
			
			//add rule independent prefixes to the result file
			writer.println("@prefix rule: <http://niko/rule#> .");
			writer.println("@prefix var: <http://niko/variable#> .");
			writer.println("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .");

			//reading a file line by line till the end of the file 
	    	BufferedReader br = new BufferedReader(new FileReader(rule));
	    	String line;
	    	while ((line = br.readLine()) != null) {
	    		
	    		//add rule dependent prefixes to the result file 
	    		if (line.contains("@prefix"))
	    			writer.println(line);
				
	    		//'{' indicates the start of the body list 
	    		if (line.equals("{")) {
	    			writer.println("");
	    			writer.println("rule:RuleBoris");
	    			writer.println ("  rule:hasBody (");
	    		}
	    		// if current line matches the triple pattern 
	    		if (Pattern.matches("\\s*([a-zA-Z_0-9:,?\"./]+\\s+){3}\\.?\\s*", line)) {	
	    			
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
	    			
	    			//parse string literals
	    			if (object.charAt(0)=='\"')
	    				object = object + "^^xsd:string";
	    			
	    			//parse double literals
	    			if (Pattern.matches("\\d+.\\d+", object))
	    				object = object + "^^xsd:double";
	    			
	    			//parse integer literals
	    			if (Pattern.matches("\\d+", object))
	    				object = object + "^^xsd:integer";
	    			
	    			//output into the result file
	    			writer.println("	[");
	    			writer.println("	 rule:subject " + subject + ";"); 
	    			writer.println("	 rule:predicate " + predicate + ";"); 
	    			writer.println("	 rule:object " + object);
	    			writer.println("	] .");
	    	    }
	    		
	    		//"=>" indicates the end of body and the begin of head 
	    		if (line.contains("=>")) {
	    			writer.println("  );");
	    			writer.println("  rule:hasHead (");
	    		}
	    		
	    		//end of the rule
	    		if (line.contains("} ."))
	    			writer.println(") .");
	    	}
		    br.close();
			writer.close();
	    }		
	}
}
