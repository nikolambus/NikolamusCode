import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;

public class BuildRule {
	
    public String rule = "";
	static List<String> SubjectsList = new ArrayList<String>(); 
	static List<String> PredicatesList = new ArrayList<String>(); 
	static List<String> ObjectsList = new ArrayList<String>(); 

    
    // Use here the same IRI as your SMW or Surgipedia
  	public static IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
    
    
	public static void main(String[] args) throws IOException {
		
		//getting the RDF export of the rule's page in SMW
		Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/RudisRule1").openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
	    		
		//going through the RDF export line by line 
	    BufferedReader br = new BufferedReader(new StringReader(out));
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	// System.out.println(line);
	    	
	    	// 1st possibility to match the line with a concept 
	    	// full path does not work. Internal shortcuts are used. Look the 2nd possibility
			/*
			if (Pattern.matches("		<property:HasObject1 rdf:resource=\"http://localhost/mediawiki/index.php/Special:URIResolver/DRU\"/>", line)) {	
	    			System.out.println(line);
    		}*/
	    	
	    	// 2nd possibility to match the line with a concept. Internal shortcuts are used.
	    	if (Pattern.matches("		<property:(HasObject|HasSubject|HasPredicate)\\d rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
    			System.out.println(line);
    			
    			//Is it an object/predicate/subject ?
    			String conceptType = line.substring(line.indexOf(":")+1, line.indexOf(" ")-1);
    			
    			//Which triple it belongs to?
    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
    			
    			String conceptName = line.substring(line.indexOf(";")+1, line.indexOf("\"", line.indexOf(";")));
    			
    			System.out.println(conceptType + "  " + conceptNumber);
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

    			System.out.println(SubjectsList);
    			System.out.println(ObjectsList);
    			System.out.println(PredicatesList);
    			
	    	}	    	
	    }
	    br.close();	
	    scanner.close();
	    
		for (int i=0; i < PredicatesList.size(); i++) {
			String currentPredicate = PredicatesList.get(i);
			
			//get the RDF export of the current predicate
			scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/" + currentPredicate).openStream(), "UTF-8").useDelimiter("\\A");
			String currentPredicatePage = scanner.next();
			
			// Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
			// For this do activate RegEx multiline modus with (?sm)
			System.out.println(Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage));

			// If found, then this property is an object property.
			//HIER WEITER
			
			//System.out.println(currentPredicatePage);

		}
		scanner.close();
	}
	
	public void setRuleClass (String name, String var)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var + ")^";
    }

}
