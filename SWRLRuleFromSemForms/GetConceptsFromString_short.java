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

public class GetConceptsFromString_short {

	public static OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory df = m.getOWLDataFactory();
	
	//private static final String ontologyIRI = "justSomeIRI";
	public static void fromStringToOWLRDFNotation(String rule, IRI ontologyIRI) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

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
			premises.add(s.propAtom);      
		}
				
		//Working with list we generated null at the beginning of premises. 
		//We should remove it, because there is no null-Atom.
		premises.remove(null);	
		
		//######################## HEAD ######################################################
		
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
        File output = new File("C:/Users/Administrator/Desktop/SWRLRuleFromSemForms2.owl");
        output.createNewFile();
        m.saveOntology(o, IRI.create(output.toURI()));
	
	}
	
	// This property should create classes and properties from given string and IRI
	// We use it once for our body and once for our head
	public static List<SWRLHelpClass> buildRulePart(String part, IRI ontologyIRI) throws OWLOntologyCreationException {
		
		//We want to extract each part-atom. For that we create a list structure that will contain them. 
		List<Atom> partAtoms = new ArrayList<Atom>();
		
		//our first left boundary is just the first symbol
		int leftBoundary = 0;
		int rightBoundary;
		
		//We may iterate the leftBoundary as long as it's smaller than the part-length 
		while (leftBoundary < part.length()) {
			
			//Our first right boundary is the first ')'-symbol
			rightBoundary = part.indexOf(')', leftBoundary);
			
			//Now we cut the first atom from the part
			String help = part.substring(leftBoundary, rightBoundary+1);

			//In the object "newAtomPart" we store information about each atom occurred in part in STRING(!) format
			Atom newAtomPart = new Atom();
			
			if (help.indexOf(',') == -1) {
				
				//If this atom doesn't contain a comma, it's a class	
				newAtomPart.atomType = "CLASS";
				
				//We save this atom's name like this "man"
				newAtomPart.atomName = help.substring(0, help.indexOf('('));
				
				//We save this atom's variable like this "x"
				newAtomPart.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(')'));
			}
			else {
				//If this atom contains a comma, it's a property	
				newAtomPart.atomType = "PROPERTY";
				
				//We save this property's name like this "married"
				newAtomPart.atomName = help.substring(0, help.indexOf('('));
				
				//We save this property's both variables like this "x,y"
				newAtomPart.atomVar = help.substring(help.indexOf('?')+1, help.indexOf(',')) + "," +  help.substring(help.indexOf(',')+2, help.indexOf(')'));
			}
			
			//Now we add the processed atom to our partAtoms list
			partAtoms.add(newAtomPart);
			
			//And shift our leftBoundary
			leftBoundary = rightBoundary+2;
		}
		
		//This list serves for storing all partAtoms in SWRL(!) format
		List<SWRLHelpClass> swrlPartAtoms = new ArrayList<SWRLHelpClass>();
		
		//Within this loop we initialize necessary classes and properties for the part part of the SWRL rule	
		for ( Atom a : partAtoms ) {
			
			SWRLHelpClass SWRLPartAtom = new SWRLHelpClass();
			
			if (a.atomType == "CLASS") {				
				//Get a reference to the needed class so that we can process it with the reasoner.
				SWRLPartAtom.SWRLClassAtomName = df.getOWLClass(IRI.create(ontologyIRI + a.atomName));
				//Create a variable that represents the instance of this class 
				SWRLPartAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar + "_var"));				
			
				//Specify the relationship between a class and a variable
				SWRLPartAtom.classAtom = df.getSWRLClassAtom(SWRLPartAtom.SWRLClassAtomName, SWRLPartAtom.SWRLAtomVar1);

			}
			else {				
				//Get a reference to the needed property so that we can process it with the reasoner.
				SWRLPartAtom.SWRLPropertyAtomName = df.getOWLObjectProperty(IRI.create(ontologyIRI + a.atomName));
				
				//Create 2 variables that represents the instance of this class 
				SWRLPartAtom.SWRLAtomVar1 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(0, a.atomVar.indexOf(',')) + "_var"));
				SWRLPartAtom.SWRLAtomVar2 = df.getSWRLVariable(IRI.create(ontologyIRI + a.atomVar.substring(a.atomVar.indexOf(',')+1,a.atomVar.length()) +"_var"));
		
				SWRLPartAtom.propAtom = df.getSWRLObjectPropertyAtom(SWRLPartAtom.SWRLPropertyAtomName, SWRLPartAtom.SWRLAtomVar1, SWRLPartAtom.SWRLAtomVar2);
			}
		
			//Now we add the processed atom to our swrlPartAtoms list
			swrlPartAtoms.add(SWRLPartAtom);

		}
		
		return swrlPartAtoms;
	}
	
}
