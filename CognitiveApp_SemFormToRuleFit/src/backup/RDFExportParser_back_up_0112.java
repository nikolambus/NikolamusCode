package backup;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class RDFExportParser_back_up_0112 {
	// here we gonna store our body and head
	public String body = "";
	public String head = "";

	
	public List<String> BodySubjectsList = new ArrayList<String>(); 
	public List<String> BodyPredicatesList = new ArrayList<String>(); 
	public List<String> BodyObjectsList = new ArrayList<String>(); 
	
	public List<String> HeadSubjectsList = new ArrayList<String>(); 
	public List<String> HeadPredicatesList = new ArrayList<String>(); 
	public List<String> HeadObjectsList = new ArrayList<String>(); 

	public List<String> varsBank = new ArrayList<String>();
	public List<String> classesBank = new ArrayList<String>();
	
	public List<String> helpIndividualsList = new ArrayList<String>();
      	
  	public void buildBody(String rdfexport) throws IOException {
  		
		//going through the RDF export line by line 
	    BufferedReader br = new BufferedReader(new StringReader(rdfexport));
	    
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	//System.out.println(line);
	    	
	    	// match the line with a concept. Internal shortcuts are used.
	    	if (Pattern.matches("		<property:(HasBodyObject|HasBodySubject|HasBodyPredicate)\\d rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
    			
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
    			  case "HasBodySubject":
    				BodySubjectsList.add(conceptNumber-1, conceptName);
        			break;
    			  case "HasBodyObject":
    				BodyObjectsList.add(conceptNumber-1, conceptName);
    			    break;
    			  case "HasBodyPredicate":
      				BodyPredicatesList.add(conceptNumber-1, conceptName);
      			    break;
    			  default:
    			    System.err.println( "Unknown concept type: " + conceptType );
    			}
	    	}	    	
	    }
	    br.close();	
	    
	    for (int i=0; i < BodyPredicatesList.size(); i++) {
			String currentPredicate = BodyPredicatesList.get(i);
			
			//get the RDF export of the current predicate
			Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/" + currentPredicate).openStream(), "UTF-8").useDelimiter("\\A");
			String currentPredicatePage = scanner.next();
			
			// Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
			// For this do activate RegEx multiline modus with (?sm)
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
				
				// If found, then this property is an object property.
				// Assume: our i-th triple looks like "Patient wirdUntersuchtDurch DRU"
				
				// Firstly add "Patient(?patient)" to the rule 
				body = addRuleClass(body, BodySubjectsList.get(i), BodySubjectsList.get(i));	
				
				// Secondly add "DRU(?dru)" to the rule 
				body = addRuleClass(body, BodyObjectsList.get(i), BodyObjectsList.get(i));
				
				// Thirdly add "wirdUntesuchtDurch (?patient, ?dru)"
				body = addRuleObjectProperty(body, currentPredicate, BodySubjectsList.get(i), BodyObjectsList.get(i));
				
			}
			else {
				
			}

			// If not found, then this property is a data property.

			// 1st possibility: DataStringProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_str\"/>$.*", currentPredicatePage)) {
				
			// Assume: our i-th triple looks like "DRU untersuchungZeigt Auffaelliger Befund"

			// Firstly add "DRU(?dru)" to the rule 
			body = addRuleClass(body, BodySubjectsList.get(i), BodySubjectsList.get(i));	
			
			// Secondly add "untersuchungZeigt (?dru, "Auffaelliger Bedfund")"
			body = addRuleDataStringProperty(body, currentPredicate, BodySubjectsList.get(i), BodyObjectsList.get(i));
			
			}

			// 2nd possibility: DataNumberProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_num\"/>$.*", currentPredicatePage)) {
			
				// Assume: our i-th triple looks like "Patient hasAge Age"
				// The handling of this case is similar to the object property, with one difference: we do not create a class for Age  
				// It makes sense, since one uses DataNumberProperties to bind some value to a variable and then
				// to compare this variable against some threshold within a swrl built-in like: 
				// "Patient(?p), hasAge(?p, ?age), greaterThan(?age, 70)".
				
				body = addRuleClass(body, BodySubjectsList.get(i), BodySubjectsList.get(i));	
				body = addRuleObjectProperty(body, currentPredicate, BodySubjectsList.get(i), BodyObjectsList.get(i));
			
			}

			//check
			//System.out.println(currentPredicatePage);
			scanner.close();

		}
		
		System.out.println("Body Subjects list: " + BodySubjectsList);
		System.out.println("Body Objects list: " + BodyObjectsList);
		System.out.println("Body Predicates list: " + BodyPredicatesList);
		
		//check
		//System.out.println(body);
		
  	}
	
	public void buildHead(String rdfexport) throws NumberFormatException, IOException, OWLOntologyCreationException {
		
		//going through the RDF export line by line 
	    BufferedReader br = new BufferedReader(new StringReader(rdfexport));
	    
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	//System.out.println(line);
	    	
	    	// match the line with a concept. Internal shortcuts are used.
	    	if (Pattern.matches("		<property:(HasHeadObject|HasHeadSubject|HasHeadPredicate)\\d rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
    			
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
    			  case "HasHeadSubject":
        			HeadSubjectsList.add(conceptNumber-1, conceptName);
        			break;
    			  case "HasHeadObject":
    				HeadObjectsList.add(conceptNumber-1, conceptName);
    			    break;
    			  case "HasHeadPredicate":
      				HeadPredicatesList.add(conceptNumber-1, conceptName);
      			    break;
    			  default:
    			    System.err.println( "Unknown concept type: " + conceptType );
    			}
	    	}	    	
	    }
	    br.close();	
	    
	    for (int i=0; i < HeadPredicatesList.size(); i++) {
			String currentPredicate = HeadPredicatesList.get(i);
			
			//get the RDF export of the current predicate
			Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/" + currentPredicate).openStream(), "UTF-8").useDelimiter("\\A");
			String currentPredicatePage = scanner.next();
			
			// Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
			// For this do activate RegEx multiline modus with (?sm)
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPredicatePage)) {
				
				// If found, then this property is an object property.
				// Assume: our i-th triple looks like "Patient wirdUntersuchtDurch DRU"
				
				/*  SWRL rules are designed in a such way that no new(!) variables are allowed at the head side. 
				   	So we bring the Head-variable initialization via appropriate class over to the body side
					Example: Patient(?p), hat_Symptom(?p, ?bis), Blut_im_Stuhl(?bis) -> DRU(?dru), wird_untersucht_durch(?p, ?dru) wird zu
					Patient(?p), hat_Symptom(?p, ?bis), Blut_im_Stuhl(?bis), DRU(?dru) -> wird_untersucht_durch(?p, ?dru)
				*/
				body = addRuleClass(body, HeadSubjectsList.get(i), HeadSubjectsList.get(i));
				body = addRuleClass(body, HeadObjectsList.get(i), HeadObjectsList.get(i));

				/* If we are dealing with the concepts that do not occur in the body we need to create help individuals for them
				 * See GetConceptsFromString_short method for explanation  
				 */
				if (!BodySubjectsList.contains(HeadSubjectsList.get(i))) {
					helpIndividualsList.add(HeadSubjectsList.get(i));
				}
				
				if (!BodyObjectsList.contains(HeadObjectsList.get(i))) {
					helpIndividualsList.add(HeadObjectsList.get(i));
				}	

				// add "wirdUntesuchtDurch (?p, ?dru)" to the head side
				head = addRuleObjectProperty(head, currentPredicate, HeadSubjectsList.get(i), HeadObjectsList.get(i));
				
			}
			else {
				
			}

			// If not found, then this property is a data property.

			// 1st possibility: DataStringProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_str\"/>$.*", currentPredicatePage)) {
				
			// Assume: our i-th triple looks like "DRU untersuchungZeigt Auffaelliger Befund"

			// Firstly add "DRU(?dru)" at the body side (see explanation above - by object property handling) 
			body = addRuleClass(body, HeadSubjectsList.get(i), HeadSubjectsList.get(i));	
			
			// Secondly add "untersuchungZeigt (?dru, "Auffaelliger Bedfund")"
			head = addRuleDataStringProperty(head, currentPredicate, HeadSubjectsList.get(i), HeadObjectsList.get(i));
			
			}

			// 2nd possibility: DataNumberProperty
			if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_num\"/>$.*", currentPredicatePage)) {
			
				// Assume: our i-th triple looks like "Patient hasAge Age"
				// The handling of this case is similar to the object property, with one difference: we do not create a class for Age  
				// It makes sense, since one uses DataNumberProperties to bind some value to a variable and then
				// to compare this variable against some threshold within a swrl built-in like: 
				// "Patient(?p), hasAge(?p, ?age), greaterThan(?age, 70)".
				
				body = addRuleClass(body, HeadSubjectsList.get(i), HeadSubjectsList.get(i));	
				head = addRuleObjectProperty(head, currentPredicate, HeadSubjectsList.get(i), HeadObjectsList.get(i));
			
			}

			//check
			//System.out.println(currentPredicatePage);
			scanner.close();

		}
		
		System.out.println("Head Subjects list: " + HeadSubjectsList);
		System.out.println("Head Objects list: " + HeadObjectsList);
		System.out.println("Head Predicates list: " + HeadPredicatesList);
		System.out.println("");
		System.out.println("Helpindividuals: " + helpIndividualsList);

	    
		//Now we can be sure that the body processing has been finished and we can cut the last conjugation symbol
		body = body.substring(0, body.length()-1);
		
		//cut the last conjugation symbol for head
		head = head.substring(0, head.length()-1);
		
		//check
		//System.out.println(head);
	    
	}
	
	
	public String addRuleClass (String rulePart, String name, String var)
    {
    	if ((name != "") && (name != null) && (!classesBank.contains(name))) {
    		rulePart = rulePart + name + "(?" + var + ")^";
    		classesBank.add(name);
    		varsBank.add(var);
    	}
    	return rulePart;
    }
	
    public String addRuleObjectProperty (String rulePart, String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rulePart = rulePart + name + "(?" + var1 + ",?" + var2 + ")^";
    	return rulePart;
    }
    
    // Version1: for String values
    public String addRuleDataStringProperty (String rulePart, String name, String var, String value)
    {
    	if ((name != "") && (name != null))
        rulePart = rulePart + name + "(?" + var + ",\"" + value + "\")^";
    	return rulePart;
    }
    
    // Version2: for Number values
    public String addRuleDataNumberProperty (String rulePart, String name, String var1, String var2)
    {
    	if ((name != "") && (name != null))
        rulePart = rulePart + name + "(?" + var1 + ",?" + var2 + ")^";
		varsBank.add(var2);
		return rulePart;
    }

}
