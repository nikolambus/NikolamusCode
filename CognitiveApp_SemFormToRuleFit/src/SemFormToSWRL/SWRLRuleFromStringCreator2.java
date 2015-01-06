package SemFormToSWRL;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.clarkparsia.pellet.rules.builtins.BuiltIn;
import com.clarkparsia.pellet.rules.builtins.BuiltInRegistry;
import com.hp.hpl.jena.reasoner.rulesys.builtins.AddOne;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class SWRLRuleFromStringCreator2 {


    // Use here the same IRI as your SMW or Surgipedia
  	public IRI ontologyIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
  	
    // Use special IRI for individuals
   	public IRI indIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/Individual#");	
   	
	public OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	public OWLDataFactory df = m.getOWLDataFactory();
	
	//private static final String ontologyIRI = "justSomeIRI";
	public void fromStringToOWLRDFNotation(String rule, String outputPath, List<String> helpIndividuals) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		
		//this will be our owl ontology
		OWLOntology o = m.createOntology(ontologyIRI);

		//First of all we divide it in two parts: body and head
		String body = rule.substring(0, rule.indexOf('-'));
		String head = rule.substring(rule.indexOf('-')+2, rule.length());
		
		//check
		//System.out.println("body: "+ body); 
		//System.out.println("head: "+ head); 

		List<SWRLHelpClass> swrlBodyAtoms = buildRulePart(body, ontologyIRI);
		List<SWRLHelpClass> swrlHeadAtoms = buildRulePart(head, ontologyIRI);
		
		//######################## BODY ######################################################
		
		//Building this body (premises) of a SWRL rule
		Set<SWRLAtom> premises = new HashSet<SWRLAtom>();
		for (SWRLHelpClass s : swrlBodyAtoms) {
			premises.add(s.classAtom);
			premises.add(s.objPropAtom);      
			premises.add(s.dataPropAtom); 
			premises.add(s.builtinAtom);
		}
				
		//Working with list we generated null at the beginning of premises. 
		//We should remove it, because there is no null-Atom.
		premises.remove(null);	
		
		//######################## HEAD ######################################################
		
		//Building the head (conclusions) of a SWRL rule
		Set<SWRLAtom> conclusions = new HashSet<SWRLAtom>();
		for (SWRLHelpClass s : swrlHeadAtoms) {
			conclusions.add(s.classAtom);
		    conclusions.add(s.objPropAtom);    
		    conclusions.add(s.dataPropAtom);      
		}
			
		//Working with list we generated null at the beginning of premises. 
		//We should remove it, because there is no null-Atom.
		conclusions.remove(null);
		
		//###################### PUT THEM TOGETHER ########################################################
		
		//Now we specify the whole SWRL rule 
        SWRLRule ruleSWRL = df.getSWRLRule(premises, conclusions);
		
        //Apply change
        m.applyChange(new AddAxiom(o, ruleSWRL));
        
        //check
        //System.out.println("File exists: " + new File(outputPath).exists()); 
        
        /* The last step before saving is to add help individuals got from the RDFParser method as parameters.
        * 
        * SWRL rules are designed in a such way that no new(!) variables are allowed at the head side. 
        * So we bring the Head-variable initialization via appropriate class over to the body side
        * Example: Patient(?p), hat_Symptom(?p, ?bis), Blut_im_Stuhl(?bis) -> DRU(?dru), wird_untersucht_durch(?p, ?dru) wird zu
        * Patient(?p), hat_Symptom(?p, ?bis), Blut_im_Stuhl(?bis), DRU(?dru) -> wird_untersucht_durch(?p, ?dru)
        * 
        * It implicates that our patient will need a DRU individual to fire this rule. Though DRU is a conclusion and 
        * it is very possible that our patient will not the DRU data. So we create this help individual here - on the rule's side. 
        */
        if (!helpIndividuals.isEmpty()) {
			 for (String label : helpIndividuals) {
				 OWLClass cls = df.getOWLClass(IRI.create(ontologyIRI + label));
				 OWLNamedIndividual ind = df.getOWLNamedIndividual(IRI.create(indIRI + label + "1"));
			     m.addAxiom(o, df.getOWLClassAssertionAxiom(cls, ind));
			 }
		 }	   
        
        //Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        m.saveOntology(o, IRI.create(output.toURI()));
	
	}
	
	// This property should create classes and properties from given string and IRI
	// We use it once for our body and once for our head
	public List<SWRLHelpClass> buildRulePart(String part, IRI ontologyIRI) throws OWLOntologyCreationException, MalformedURLException, IOException {
		
		//define IRI for variables
	  	IRI varIRI = IRI.create(ontologyIRI + "Variable#");	
		
		//We want to extract each part-atom. For that we create a list structure that will contain them. 
		List<Atom> Atoms = new ArrayList<Atom>();
		
		//our first left boundary is just the first symbol
		int leftBoundary = 0;
		int rightBoundary;
		
		//We may iterate the leftBoundary as long as it's smaller than the part-length 
		while (leftBoundary < part.length()) {
			
			//Our first right boundary is the first ')'-symbol
			rightBoundary = part.indexOf(')', leftBoundary);
			
			//Now we cut the first atom from the part
			String help = part.substring(leftBoundary, rightBoundary+1);

			//In the object "newAtom" we store information about each atom occurred in part in STRING(!) format
			Atom newAtom = new Atom();
			
			if (help.indexOf(',') == -1) {
				
				//If this atom doesn't contain a comma, it's a class	
				newAtom.atomType = "CLASS";
				
				//We save this atom's name like this "man"
				newAtom.atomName = help.substring(0, help.indexOf('('));
				
				//We save this atom's variable like this "?x"
				newAtom.atomArg1 = help.substring(help.indexOf('?'), help.indexOf(')'));
			}
			else {
				if (help.startsWith("swrlb")) {
					
					// if this atom starts with "swrlb:" it's a built-in
					newAtom.atomType = "BUILT-IN";
				
					//We save this atom's name like this "greaterThan"
					newAtom.atomName = help.substring(help.indexOf(':')+1, help.indexOf('('));
				
					//We know: the first argument is a variable and can save it as "?x"
					newAtom.atomArg1 = help.substring(help.indexOf('?'), help.indexOf(','));

					//We don't know rather the second argument is a variable or a number. 
					//If it's a variable, save it like this: "?y". Else - save the literal value: "23".				
					newAtom.atomArg2 = help.substring(help.indexOf(',')+1, help.indexOf(')'));
				}
				else {
				
					//it's a property.	
					newAtom.atomType = "PROPERTY";
					
					newAtom.atomName = help.substring(0, help.indexOf('('));
					
					newAtom.atomArg1 = help.substring(help.indexOf('?'), help.indexOf(','));
					
					newAtom.atomArg2 = help.substring(help.indexOf(',')+1, help.indexOf(')'));
				}
			}	
			//Now we add the processed atom to our Atoms list
			Atoms.add(newAtom);
			
			//And shift our leftBoundary
			leftBoundary = rightBoundary+2;
		}
				
		//This list serves for storing all Atoms in SWRL(!) format
		List<SWRLHelpClass> swrlAtoms = new ArrayList<SWRLHelpClass>();
		
		//Within this loop we initialize appropriate rule atoms (built-ins, classes or properties) for body or head of the SWRL rule	
		for ( Atom a : Atoms ) {
			
			SWRLHelpClass SWRLAtom = new SWRLHelpClass();
			
			if (a.atomType.equalsIgnoreCase("BUILT-IN")) {
				
				//check
				System.out.println("BUILT-IN: " + a.atomName);

				List<SWRLDArgument> builtinArgs = new ArrayList<SWRLDArgument>();
				
				//we know, it's a variable, just take the name, without '?'
				//handle the first argument as a SWRL variable 
				String arg1 = a.atomArg1.substring(1, a.atomArg1.length());
				SWRLAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(varIRI + arg1));
				
				//We don't know rather the second argument is a variable or a literal. 
				
				//variable: we should take the string without '?' and add both variables to the arguments list					
				if (a.atomArg2.startsWith("?")) {
					String arg2 = a.atomArg2.substring(1, a.atomArg2.length());
					SWRLAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(varIRI + arg2));
					
					builtinArgs.add(SWRLAtom.SWRLAtomVar1);
					builtinArgs.add(SWRLAtom.SWRLAtomVar2);
				}
				
				//literal: we should create an OWLLiteral: either double or integer
				else {
					
					// if object is a double literal
					if (Pattern.matches("\\d+.\\d+", a.atomArg2)) {
						double arg2Double = Double.valueOf(a.atomArg2);
						SWRLAtom.SWRLLiteralArg = df.getSWRLLiteralArgument(df.getOWLLiteral(arg2Double));
					}
					
					// if object is an integer literal
					if (Pattern.matches("\\d+", a.atomArg2))  {
			    		int arg2Int = Integer.valueOf(a.atomArg2);
						SWRLAtom.SWRLLiteralArg = df.getSWRLLiteralArgument(df.getOWLLiteral(arg2Int));
					}
			
					builtinArgs.add(SWRLAtom.SWRLAtomVar1);
					builtinArgs.add(SWRLAtom.SWRLLiteralArg);
				}
				
				switch (a.atomName) { 
					case "GreaterThan":
						//specify the relationship between "greaterThan" and arguments
						SWRLAtom.builtinAtom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.GREATER_THAN.getIRI(), builtinArgs);
						break;
			
					case "GreaterThanOrEqual":
						SWRLAtom.builtinAtom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL.getIRI(), builtinArgs);
						break;
						
					case "LessThan":
						SWRLAtom.builtinAtom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.LESS_THAN.getIRI(), builtinArgs);
						break;
						
					case "LessThanOrEqual":
						SWRLAtom.builtinAtom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL.getIRI(), builtinArgs);
						break;
						
					case "Equal":
						SWRLAtom.builtinAtom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.EQUAL.getIRI(), builtinArgs);
						break;
				}
			}
			
			if (a.atomType == "CLASS") {
				
				System.out.println("CLASS: " + a.atomName);
				//Get a reference to the needed class so that we can process it with the reasoner.
				SWRLAtom.SWRLClassAtomName = df.getOWLClass(IRI.create(ontologyIRI + a.atomName));
				
				//we know, it's a variable, just take the name, without '?'
				String arg1 = a.atomArg1.substring(1, a.atomArg1.length());
				
				//Create a variable that represents the instance of this class 
				SWRLAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(varIRI + arg1));				
			
				//Specify the relationship between a class and a variable
				SWRLAtom.classAtom = df.getSWRLClassAtom(SWRLAtom.SWRLClassAtomName, SWRLAtom.SWRLAtomVar1);
			}
							
			if (a.atomType == "PROPERTY") {
				
				System.out.println("PROPERTY: " + a.atomName);
				/* now we should find out which kind of property is it. Therefore we apply the same method as by RDFExportParser.java 
				- we connect to the RDF Export of the appropriate page and extract the type from it */ 

				Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/" + a.atomName).openStream(), "UTF-8").useDelimiter("\\A");
				String currentPropertyPage = scanner.next();

				/* Searching for the line which contains "swivt:type ... wpg" on the RDF Export page. 
				 * "wpg" stands for webpage and if occurs it means that current property is an object property.
				 * For the search line-based search do activate RegEx multiline modus with (?sm) 
				*/
				if (Pattern.matches("(?sm).*^\t\t<swivt:type rdf:resource=\"http://semantic-mediawiki.org/swivt/1.0#_wpg\"/>$.*", currentPropertyPage)) { 	
					
					//---------------------------Object property------------------------------------------------------------------- 
				
					//Get a reference to the needed property so that we can process it with the reasoner.
					SWRLAtom.SWRLObjectPropertyAtomName = df.getOWLObjectProperty(IRI.create(ontologyIRI + a.atomName));
		
					//we know, it's a variable, just take the name, without '?'
					String arg1 = a.atomArg1.substring(1, a.atomArg1.length());
					String arg2 = a.atomArg2.substring(1, a.atomArg2.length());
					
					//Create 2 variables that represents the instance of this class 
					SWRLAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(varIRI + arg1));
					SWRLAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(varIRI + arg2));
						
					SWRLAtom.objPropAtom = df.getSWRLObjectPropertyAtom(SWRLAtom.SWRLObjectPropertyAtomName, SWRLAtom.SWRLAtomVar1, SWRLAtom.SWRLAtomVar2);
				
				}
				else {	
					//---------------------------Data property------------------------------------------------------------------- 
					
					//Get a reference to the needed property so that we can process it with the reasoner.
					SWRLAtom.SWRLDataPropertyAtomName = df.getOWLDataProperty(IRI.create(ontologyIRI + a.atomName));
					
					//we know, it's a variable, just take the name, without '?'
					String arg1 = a.atomArg1.substring(1, a.atomArg1.length());
					
					//Create a variable for subject 
					SWRLAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(varIRI + arg1));
						
					// different cases for object
					if (a.atomArg2.startsWith("?")) {
						
						//variable
						
						String arg2 = a.atomArg2.substring(1, a.atomArg2.length());
						SWRLAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(varIRI + arg2));					
						SWRLAtom.dataPropAtom = df.getSWRLDataPropertyAtom(SWRLAtom.SWRLDataPropertyAtomName, SWRLAtom.SWRLAtomVar1, SWRLAtom.SWRLAtomVar2);
					}
					else {
						
						//literal: we should create an OWLLiteral: double, integer or string
							
						// if object is a double literal
						if (Pattern.matches("\\d+.\\d+", a.atomArg2)) {
							double arg2Double = Double.valueOf(a.atomArg2);
							SWRLAtom.SWRLLiteralArg = df.getSWRLLiteralArgument(df.getOWLLiteral(arg2Double));
						}
						else {	
							// if object is an integer literal
							if (Pattern.matches("\\d+", a.atomArg2))  {
								int arg2Int = Integer.valueOf(a.atomArg2);
								SWRLAtom.SWRLLiteralArg = df.getSWRLLiteralArgument(df.getOWLLiteral(arg2Int));
							}
							// if object is a string literal
							else {
								SWRLAtom.SWRLLiteralArg = df.getSWRLLiteralArgument(df.getOWLLiteral(a.atomArg2));
							}
						}
						SWRLAtom.dataPropAtom = df.getSWRLDataPropertyAtom(SWRLAtom.SWRLDataPropertyAtomName, SWRLAtom.SWRLAtomVar1, SWRLAtom.SWRLLiteralArg);
					}
				}	
			}

			//Now we add the processed atom to our swrlAtoms list
			swrlAtoms.add(SWRLAtom);
		}
		
		return swrlAtoms;
	}
	
	 public static void createHelpIndividuals(List<String> helpIndivids) throws OWLOntologyCreationException {
		 		
	 }
}