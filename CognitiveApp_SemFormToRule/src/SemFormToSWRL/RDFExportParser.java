package SemFormToSWRL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
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


public class RDFExportParser {
	// here we gonna store our body and head
	public String body = "";
	public String head = "";
	public String rule = "";

   	public String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";

	public List<String> BodySubjectsList = new ArrayList<String>(); 
	public List<String> BodyPredicatesList = new ArrayList<String>(); 
	public List<String> BodyObjectsList = new ArrayList<String>(); 
	
	public List<String> HeadSubjectsList = new ArrayList<String>(); 
	public List<String> HeadPredicatesList = new ArrayList<String>(); 
	public List<String> HeadObjectsList = new ArrayList<String>(); 

	public List<String> varsBank = new ArrayList<String>();
	public List<String> classesBank = new ArrayList<String>();
	public String OPType = null;
	
	public List<String> helpIndividualsList = new ArrayList<String>();
	
  	public String CreateRule(String rdfexport) throws IOException, NumberFormatException, OWLOntologyCreationException {
  		buildBodyLists(rdfexport);
  		buildHeadLists(rdfexport);
  		buildBody();
  		buildHead();
  		rule = body + "->" + head;
		return rule;
  	}

  	public void buildBodyLists(String rdfexport) throws IOException {
  		
		//going through the RDF export line by line 
	    BufferedReader br = new BufferedReader(new StringReader(rdfexport));
	    
	    String line;
	    while ((line = br.readLine()) != null) {
			
	    	// Check all lines
	    	//System.out.println(line);
	    	
	    	// match the line with a concept. Internal shortcuts are used.
	    	if (Pattern.matches("\\s*<property:(HasBodyObject|HasBodySubject|HasBodyPredicate)\\d rdf:resource=\"&wiki;[a-zA-Z_0-9:,?\"./]+\"/>", line)) {
    			
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
	    	else {
	    		/* Predicates with key word "Property" are rdfexported as full resources:
		    	 * 		<property:HasPredicate1 rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Property-3AHas_Age"/>
		    	 * perform appropriate parsing 
		    	 */
	    		if (Pattern.matches("\\s*<property:HasBodyPredicate\\d+ rdf:resource=\"" + base + "Property-3A[a-zA-Z_0-9:,?\"./]+\"/>", line)) {

	    			//Which triple it belongs to?
	    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
	    			
	    			//parse the immediate name of the concept
		    		Pattern patternTab = Pattern.compile("\\s*<property:HasBodyPredicate\\d+ rdf:resource=\"" + base + "Property-3A([a-zA-Z_0-9:,?\"./]+)\"/>");
					Matcher mtchTab = patternTab.matcher(line);
					String conceptName = null;
					while (mtchTab.find()) {
						//store it in the variable conceptName
						conceptName = mtchTab.group(1);
					}
					
					BodyPredicatesList.add(conceptNumber-1, conceptName);
	    		}
	    	}
	    }
	    br.close();	
  	}
  	
  	public void buildBody() throws IOException {
  	
	    for (int i=0; i < BodyPredicatesList.size(); i++) {
			String currentPredicate = BodyPredicatesList.get(i);
		
			/* If the current predicate is "Has_OPType"-relation, then we save the appropriate object to the OPType variable and send it to the AnnotationsPropertiesImplantator2
			 */
			if (currentPredicate.equalsIgnoreCase("Has_OPType")) {
				OPType = BodyObjectsList.get(i);
			}
			else {
			
				/* If the current predicate is "is"-relation, then we handle it in the following way: 
				 * "Patient is HeavyPatient" --> "Patient(?patient) ^ HeavyPatient(?patient)"  
				 */
				if (currentPredicate.equalsIgnoreCase("Is")) {
					body = addRuleClass(body, BodySubjectsList.get(i), BodySubjectsList.get(i));	
					body = addRuleClass(body, BodyObjectsList.get(i), BodySubjectsList.get(i));	
				}
				else {
					// the next possibility - our predicate is a built-in
					if ((currentPredicate.equalsIgnoreCase("GreaterThan")) | (currentPredicate.equalsIgnoreCase("GreaterThanOrEqual")) | (currentPredicate.equalsIgnoreCase("LessThan")) | (currentPredicate.equalsIgnoreCase("LessThanOrEqual")) | (currentPredicate.equalsIgnoreCase("Equal"))) {
						body = addSWRLBuiltin(currentPredicate, BodySubjectsList.get(i), BodyObjectsList.get(i));
					}
					else {
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
							// If not found, then this property is a data property.

							// 1st possibility: DataStringProperty
							if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_str\"/>$.*", currentPredicatePage)) {
						
								// Assume: our i-th triple looks like "DRU untersuchungZeigt Auffaelliger Befund"

								// Firstly add "DRU(?dru)" to the rule 
								body = addRuleClass(body, BodySubjectsList.get(i), BodySubjectsList.get(i));	
					
								// Secondly add "untersuchungZeigt(?dru, "Auffaelliger Befund")" or 
								// "untersuchungZeigt(?dru, ?Auffaelliger_Befund)" if ?Auffaelliger_Befund occurs in other triples
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
								body = addRuleDataNumberProperty(body, currentPredicate, BodySubjectsList.get(i), BodyObjectsList.get(i));
					
							}
						}
						
						//check
						//System.out.println(currentPredicatePage);
						scanner.close();
					}
				}
	    	}	
	    }
	    
		System.out.println("Body Subjects list: " + BodySubjectsList);
		System.out.println("Body Objects list: " + BodyObjectsList);
		System.out.println("Body Predicates list: " + BodyPredicatesList);
		
		//check
		//System.out.println(body);
		
  	}
	
	public void buildHeadLists(String rdfexport) throws NumberFormatException, IOException, OWLOntologyCreationException {
		
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
	    	else {
	    		/* Predicates with key word "Property" are rdfexported as full resources:
		    	 * 		<property:HasPredicate1 rdf:resource="http://localhost/mediawiki/index.php/Special:URIResolver/Property-3AHas_Age"/>
		    	 * perform appropriate parsing 
		    	 */
	    		if (Pattern.matches("\\s*<property:HasHeadPredicate\\d+ rdf:resource=\"" + base + "Property-3A[a-zA-Z_0-9:,?\"./]+\"/>", line)) {

	    			//Which triple it belongs to?
	    			int conceptNumber = Integer.parseInt(line.substring(line.indexOf(" ")-1, line.indexOf(" "))); 
	    			
	    			//parse the immediate name of the concept
		    		Pattern patternTab = Pattern.compile("\\s*<property:HasHeadPredicate\\d+ rdf:resource=\"" + base + "Property-3A([a-zA-Z_0-9:,?\"./]+)\"/>");
					Matcher mtchTab = patternTab.matcher(line);
					String conceptName = null;
					while (mtchTab.find()) {
						//store it in the variable conceptName
						conceptName = mtchTab.group(1);
					}
					
					HeadPredicatesList.add(conceptNumber-1, conceptName);
	    		}
	    	}
	    }
	    br.close();	
	    
	}   
 
	public void buildHead() throws IOException {
 	
	    for (int i=0; i < HeadPredicatesList.size(); i++) {
			String currentPredicate = HeadPredicatesList.get(i);
			
			/* If the current predicate is "Has_OPType"-relation, then we save the appropriate object to the OPType variable and send it to the AnnotationsPropertiesImplantator2
			 */
			if (currentPredicate.equalsIgnoreCase("Has_OPType")) {
				OPType = BodyObjectsList.get(i);
			}
			else {
				/* If we deal with the "is"-relation at the head side, then we handle it in the following way: 
				 * "Patient is HeavyPatient" --> "Patient(?patient) => HeavyPatient(?patient)"  
				 */
				if (currentPredicate.equalsIgnoreCase("Is")) {
					body = addRuleClass(body, HeadSubjectsList.get(i), HeadSubjectsList.get(i));	
					head = addRuleClass(head, HeadObjectsList.get(i), HeadSubjectsList.get(i));	
				
					//check
					//System.out.println("CHECK!!!!!!!!!!!!!!!!!");
				}
				else {
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
							head = addRuleDataNumberProperty(head, currentPredicate, HeadSubjectsList.get(i), HeadObjectsList.get(i));
						}
					}

					//check
					//System.out.println(currentPredicatePage);
					scanner.close();
				}
	    	}
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
    
    
    // DataProperty of type Number
    public String addRuleDataNumberProperty (String rulePart, String name, String arg1, String arg2) {
    	
    	if ((name != "") && (name != null)) {
    		// the case ?A B C (object is a number) --> handle it as a literal
			if ((Pattern.matches("\\d+.\\d+", arg2)) || (Pattern.matches("\\d+", arg2)))  {
	            rulePart = rulePart + name + "(?" + arg1 + "," + arg2 + ")^";
			}
			
    		// the case ?A B ?C (object is a text) --> handle it as a variable 
			else {
				rulePart = rulePart + name + "(?" + arg1 + ",?" + arg2 + ")^";
	    		varsBank.add(arg2);
			}
    	}
		return rulePart;
    }
    
    // DataProperty of type String
    public String addRuleDataStringProperty (String rulePart, String name, String arg1, String arg2) {
    	
    	//check
		//System.out.println("BodyObjectsList-BIBA!!!!!:" + BodyObjectsList);
		//System.out.println("BodySubjectsList-BIBA!!!!!:" + BodySubjectsList);
		//System.out.println("HeadObjectsList-BIBA!!!!!:" + HeadObjectsList);
		//System.out.println("HeadSubjectsList-BIBA!!!!!:" + HeadSubjectsList);
    	
    	//check-1: is it a real concept? and are all lists not empty? 
    	if ((name == "") || (name == null) || (BodyObjectsList.isEmpty()) || (BodySubjectsList.isEmpty()) || (HeadObjectsList.isEmpty()) || (HeadSubjectsList.isEmpty())) 
    		return rulePart;
    	else {
    		
    		//check-2: are we inside of the body? 
    		if (rulePart == body) {
    			
    			//check-3: does arg2 occur in the bodyObjectList twice or does it occur in any other list once more? 
    			if ((Collections.frequency(BodyObjectsList, arg2) > 1) || ((Collections.frequency(BodySubjectsList, arg2) > 0)) || ((Collections.frequency(HeadObjectsList, arg2) > 0)) || ((Collections.frequency(HeadSubjectsList, arg2) > 0))) {
    			
    				// object occurs in other triples --> handle it as a variable (the case ?A B ?C)
    				rulePart = rulePart + name + "(?" + arg1 + ",?" + arg2 + ")^";
	            	varsBank.add(arg2);
    			}
    			else {
    				// object does not occur in other triples --> handle it as a literal (the case ?A B C) 
    				rulePart = rulePart + name + "(?" + arg1 + "," + arg2 + ")^";
				}

    		}	
    		
    		//check-2: are we inside of the head? 
    		if (rulePart == head) {
    			//check-3: does arg2 occur in the bodyObjectList twice or does it occur in any other list once more? 
    			if ((Collections.frequency(HeadObjectsList, arg2) > 1) || ((Collections.frequency(HeadSubjectsList, arg2) > 0)) || ((Collections.frequency(BodyObjectsList, arg2) > 0)) || ((Collections.frequency(BodySubjectsList, arg2) > 0))) {
    			
    				// object occurs in other triples --> handle it as a variable (the case ?A B ?C)
    				rulePart = rulePart + name + "(?" + arg1 + ",?" + arg2 + ")^";
	            	varsBank.add(arg2);
    			}
    			else {
    				// object does not occur in other triples --> handle it as a literal (the case ?A B C) 
    				rulePart = rulePart + name + "(?" + arg1 + "," + arg2 + ")^";
				}

    		}	
    		return rulePart;
    	}		
    }    
   
    // method to add a built-in. Occurs only on the body side. Object can be a variable or a literal. 
    public String addSWRLBuiltin (String name, String arg1, String arg2)
    {
    	if ((name != "") && (name != null)) {
    		
    		/* the case ?A B C (object is a number) 
    		(actually there are some string-built-ins that would have a string as an object 
    		but there are pretty rare and could be omitted by the initial version
    		*/
			if ((Pattern.matches("\\d+.\\d+", arg2)) || (Pattern.matches("\\d+", arg2)))  {
	            body = body + "swrlb:" + name + "(?" + arg1 + "," + arg2 + ")^";
	    		varsBank.add(arg1);
			}
    		// the case ?A B ?C (object is a text and should be handled as a variable) 
			else { 
				body = body + "swrlb:" + name + "(?" + arg1 + ",?" + arg2 + ")^";
	    		varsBank.add(arg1);
	    		varsBank.add(arg2);
			}
		}
    	return body;
    }
}
