import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
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
import org.semanticweb.owlapi.model.SWRLClassAtom;
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

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class GetConceptsFromString {

	//private static final String ontologyIRI = "justSomeIRI";
	public static void fromStringToOWLRDFNotation(String rule, IRI ontologyIRI) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology(ontologyIRI);
		OWLDataFactory df = m.getOWLDataFactory();
				
		//Assumed, our rule is the following one 
		//String rule ="Phase(?p)^CurrentPhase(?pre)^previousPhase(?p,?pre)^Instrument(?i)^Structure(?struct)^Activity(?i,?struct)->DetectedPhase(?p)";
		
		//First of all we divide it in two parts: body and head
		String body = rule.substring(0, rule.indexOf('-'));
		String head = rule.substring(rule.indexOf('-')+2, rule.length());
		
		//check
		System.out.println("body: "+ body); 
		System.out.println("head: "+ head); 

		//######################## BODY ######################################################
		
		//Now we will deal with the body part.
		//We want to extract each body-atom. For that we create a list structure that will contain them. 
		List<Atom> bodyAtoms = new ArrayList<Atom>();
		
		//our first left boundary is just the first symbol
		int leftBoundary = 0;
		int rightBoundary;
		
		//We may iterate the leftBoundary as long as it's smaller than the body-length 
		while (leftBoundary < body.length()) {
			
			//Our first right boundary is the first ')'-symbol
			rightBoundary = body.indexOf(')', leftBoundary);
			
			//Now we cut the first atom from the body
			String help = body.substring(leftBoundary, rightBoundary+1);

			//In the object "newAtomBody" we store information about each atom occurred in body in STRING(!) format
			Atom newAtomBody = new Atom();
			
			if (help.indexOf(',') == -1) {
				
				//If this atom doesn't contain a comma, it's a class	
				newAtomBody.atomType = "CLASS";
				
				//We save this atom's name like this "man"
				newAtomBody.atomName = help.substring(0, help.indexOf('('));
				
				//We save this atom's variable like this "x"
				newAtomBody.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(')'));
			}
			else {
				//If this atom contains a comma, it's a property	
				newAtomBody.atomType = "PROPERTY";
				
				//We save this property's name like this "married"
				newAtomBody.atomName = help.substring(0, help.indexOf('('));
				
				//We save this property's both variables like this "x,y"
				newAtomBody.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(',')) + "," +  help.substring(help.indexOf(',')+2, help.indexOf(')'));
			}
			
			//Now we add the processed atom to our bodyAtoms list
			bodyAtoms.add(newAtomBody);
			
			//And shift our leftBoundary
			leftBoundary = rightBoundary+2;
		}
		
		//This list serves for storing all bodyAtoms in SWRL(!) format
		List<SWRLHelpClass> swrlBodyAtoms = new ArrayList<SWRLHelpClass>();
		
		//Within this loop we initialize necessary classes and properties for the body part of the SWRL rule	
		for ( Atom a : bodyAtoms ) {
			
			SWRLHelpClass SWRLBodyAtom = new SWRLHelpClass();
			
			if (a.atomType == "CLASS") {				
				//Get a reference to the needed class so that we can process it with the reasoner.
				SWRLBodyAtom.SWRLClassAtomName = df.getOWLClass(IRI.create(ontologyIRI + a.atomName));
				//Create a variable that represents the instance of this class 
				SWRLBodyAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar));				
			
				//Specify the relationship between a class and a variable
				SWRLBodyAtom.classAtom = df.getSWRLClassAtom(SWRLBodyAtom.SWRLClassAtomName, SWRLBodyAtom.SWRLAtomVar1);

			}
			else {				
				//Get a reference to the needed property so that we can process it with the reasoner.
				SWRLBodyAtom.SWRLPropertyAtomName = df.getOWLObjectProperty(IRI.create(ontologyIRI + a.atomName));
				
				//Create 2 variables that represents the instance of this class 
				SWRLBodyAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(0, a.atomVar.indexOf(','))));
				SWRLBodyAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(a.atomVar.indexOf(',')+1,a.atomVar.length())));
		
				SWRLBodyAtom.propAtom = df.getSWRLObjectPropertyAtom(SWRLBodyAtom.SWRLPropertyAtomName, SWRLBodyAtom.SWRLAtomVar1, SWRLBodyAtom.SWRLAtomVar2);
			}
		
			//Now we add the processed atom to our swrlBodyAtoms list
			swrlBodyAtoms.add(SWRLBodyAtom);

		}
			
		//Building the body (premises) of a SWRL rule
		Set<SWRLAtom> premises = new HashSet<SWRLAtom>();
		for (SWRLHelpClass s : swrlBodyAtoms) {
	        premises.add(s.classAtom);
	        premises.add(s.propAtom);      
		}
		
		//Working with list we generated null at the beginning of premises. 
		//We should remove it, because there is no null-Atom.
		premises.remove(null);
		
		//check
		//System.out.println (premises);
		
		//######################## HEAD ######################################################

		//We want to extract each head-atom. For that we create a list structure that will contain them. 
		List<Atom> headAtoms = new ArrayList<Atom>();
		
		//our first left boundary is just the first symbol
		leftBoundary = 0;
		
		//We may iterate the leftBoundary as long as it's smaller than the head-length 
		while (leftBoundary < head.length()) {
			
			//Our first right boundary is the first ')'-symbol
			rightBoundary = head.indexOf(')', leftBoundary);
			
			//Now we cut the first atom from the head
			String help = head.substring(leftBoundary, rightBoundary+1);

			//In the object "newAtomHead" we store information about each atom occurred in head in STRING(!) format
			Atom newAtomHead = new Atom();
			
			if (help.indexOf(',') == -1) {
			
				//If this atom doesn't contain a comma, it's a class	
				newAtomHead.atomType = "CLASS";
				
				//We save this atom's name like this "man"
				newAtomHead.atomName = help.substring(0, help.indexOf('('));
				
				//We save this atom's variable like this "x"
				newAtomHead.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(')'));
			}
			else {
				
				//If this atom contains a comma, it's a property	
				newAtomHead.atomType = "PROPERTY";
				
				//We save this property's name like this "married"
				newAtomHead.atomName = help.substring(0, help.indexOf('('));
				
				//We save this property's both variables like this "x,y"
				newAtomHead.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(',')) + "," +  help.substring(help.indexOf(',')+2, help.indexOf(')'));	
				
				
			}
			
			//Now we add the processed atom to our headAtoms list
			headAtoms.add(newAtomHead);
			
			//And shift our leftBoundary
			leftBoundary = rightBoundary+2;
			
		}	
			
		//This list serves for storing all headAtoms in SWRL(!) format
		List<SWRLHelpClass> swrlHeadAtoms = new ArrayList<SWRLHelpClass>();
			
		//Within this loop we initialize necessary classes and properties for the head part of the SWRL rule	
		for ( Atom a : headAtoms ) {
			
			SWRLHelpClass SWRLHeadAtom = new SWRLHelpClass();
			
			if (a.atomType == "CLASS") {				
				
				//Get a reference to the needed class so that we can process it with the reasoner.
				SWRLHeadAtom.SWRLClassAtomName = df.getOWLClass(IRI.create(ontologyIRI + a.atomName));
				//Create a variable that represents the instance of this class 
				SWRLHeadAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar));				
			
				//Specify the relationship between a class and a variable
				SWRLHeadAtom.classAtom = df.getSWRLClassAtom(SWRLHeadAtom.SWRLClassAtomName, SWRLHeadAtom.SWRLAtomVar1);
			}	
		    else {				
				//Get a reference to the needed property so that we can process it with the reasoner.
				SWRLHeadAtom.SWRLPropertyAtomName = df.getOWLObjectProperty(IRI.create(ontologyIRI + a.atomName));
				
				//Create 2 variables that represents the instance of this class 
				SWRLHeadAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(0, a.atomVar.indexOf(','))));
				SWRLHeadAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(a.atomVar.indexOf(',')+1,a.atomVar.length())));
			
				SWRLHeadAtom.propAtom = df.getSWRLObjectPropertyAtom(SWRLHeadAtom.SWRLPropertyAtomName, SWRLHeadAtom.SWRLAtomVar1, SWRLHeadAtom.SWRLAtomVar2);
			}
			
			//Now we add the processed atom to our swrlHeadAtoms list
			swrlHeadAtoms.add(SWRLHeadAtom);

		}
			
		//Building the head (conclusions) of a SWRL rule
		Set<SWRLAtom> conclusions = new HashSet<SWRLAtom>();
		for (SWRLHelpClass s : swrlHeadAtoms) {
			conclusions.add(s.classAtom);
		    conclusions.add(s.propAtom);      
		}
			
		//Working with list we generated null at the beginning of premises. 
		//We should remove it, because there is no null-Atom.
		conclusions.remove(null);
	
		//###################### PUT THEM TOGETHER ########################################################

		//Now we specify the whole SWRL rule 
        SWRLRule ruleSWRL = df.getSWRLRule(premises, conclusions);
		
        //Apply change
        m.applyChange(new AddAxiom(o, ruleSWRL));

        //Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File("C:/Users/Administrator/Desktop/SWRLRuleFromSemForms.owl");
        output.createNewFile();
        m.saveOntology(o, IRI.create(output.toURI()));
		
		//----------------------------------------------------------------------------------------------------		        
        /*  REASONING 
         *  It makes sense only if we filled our ontology with individuals
        
        //initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(o);
		
		//Now it should perform reasoning
		reasoner.getKB().realize();
		
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m, o);
		
		//Now we can save our ontology after reasoning in an another external file 
		File file = new File("D:/DiplArbeit/OurWork/USECASE/RULES/GenericSWRLPhaseRuleReasoned.owl");
		file.createNewFile();
		m.saveOntology(o, IRI.create(file.toURI()));
		
		*/
	}
}
