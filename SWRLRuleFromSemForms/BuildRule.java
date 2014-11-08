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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class BuildRule {
	
    public static String rule = "";
	static List<String> SubjectsList = new ArrayList<String>(); 
	static List<String> PredicatesList = new ArrayList<String>(); 
	static List<String> ObjectsList = new ArrayList<String>(); 

	static List<String> varsBank = new ArrayList<String>();
	static List<String> classesBank = new ArrayList<String>();

    
    // Use here the same IRI as your SMW or Surgipedia
  	public static IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
    
    
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
		
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
    			
	    		//check
	    		//System.out.println(line);
    			
    			//Is it an object/predicate/subject ?
    			String conceptType = line.substring(line.indexOf(":")+1, line.indexOf(" ")-1);
    			
    			//Which triple it belongs to?
    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
    			
    			String conceptName = line.substring(line.indexOf(";")+1, line.indexOf("\"", line.indexOf(";")));
    			
    			//check
    			//System.out.println(conceptType + "  " + conceptNumber);
    			
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
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
				
				// If found, then this property is an object property.
				// Assume: our i-th triple looks like "Patient wirdUntersuchtDurch DRU"
				
				// Firstly add "Patient(?patient)" to the rule 
				addRuleClass(SubjectsList.get(i), SubjectsList.get(i));	
				
				// Secondly add "DRU(?dru)" to the rule 
				addRuleClass(ObjectsList.get(i), ObjectsList.get(i));
				
				// Thirdly add "wirdUntesuchtDurch (?patient, ?dru)"
				addRuleObjectProperty(currentPredicate, SubjectsList.get(i), ObjectsList.get(i));
				
			}
			else {
				
			}

			// If not found, then this property is a data property.

			// 1st possibility: DataStringProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_str\"/>$.*", currentPredicatePage)) {
				
			// Assume: our i-th triple looks like "DRU untersuchungZeigt Auffaelliger Befund"

			// Firstly add "DRU(?dru)" to the rule 
			addRuleClass(SubjectsList.get(i), SubjectsList.get(i));	
			
			// Secondly add "untersuchungZeigt (?dru, "Auffaelliger Bedfund")"
			addRuleDataStringProperty(currentPredicate, SubjectsList.get(i), ObjectsList.get(i));
			
			}

			// 2nd possibility: DataNumberProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_num\"/>$.*", currentPredicatePage)) {
			
				// Assume: our i-th triple looks like "Patient hasAge Age"
				// The handling of this case is similar to the object property, with one difference: we do not create a class for Age  
				// It makes sense, since one uses DataNumberProperties to bind some value to a variable and then
				// to compare this variable against some threshold within a swrl built-in like: 
				// "Patient(?p), hasAge(?p, ?age), greaterThan(?age, 70)".
				
				addRuleClass(SubjectsList.get(i), SubjectsList.get(i));	
				addRuleObjectProperty(currentPredicate, SubjectsList.get(i), ObjectsList.get(i));
			
			}

			//check
			//System.out.println(currentPredicatePage);

		}
		scanner.close();

		// cut the last conjugation symbol
		rule = rule.substring(0, rule.length()-1);
		
		System.out.println("Subjects list: " + SubjectsList);
		System.out.println("Objects list: " + ObjectsList);
		System.out.println("Predicates list: " + PredicatesList);

		System.out.println("Rule: " + rule);
		System.out.println("Classes bank: " + classesBank);
		System.out.println("Vars bank: " + varsBank);
		
		//add some head just for testing
		rule = rule + "->Biopsie(?biopsie)";
		
		GetConceptsFromString.fromStringToOWLRDFNotation(rule, basicIRI);

	}
	
	public static void addRuleClass (String name, String var)
    {
    	if ((name != "") && (name != null) && (!classesBank.contains(name))) {
    		rule = rule + name + "(?" + var + ")^";
    		classesBank.add(name);
    		varsBank.add(var);
    	}
    }
	
    public static void addRuleObjectProperty (String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var1 + ",?" + var2 + ")^";
    }
    
    // Version1: for String values
    public static void addRuleDataStringProperty (String name, String var, String value)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var + ",\"" + value + "\")^";
    }
    
    // Version2: for Number values
    public static void addRuleDataNumberProperty (String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rule = rule + name + "(?" + var1 + ",?" + var2 + ")^";
		varsBank.add(var2);
    }

}
