package cz.makub;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

import edu.stanford.smi.protegex.owl.swrl.bridge.ArgumentFactory;
import edu.stanford.smi.protegex.owl.swrl.bridge.BuiltInArgument;
import edu.stanford.smi.protegex.owl.swrl.bridge.SWRLBuiltInBridge;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.AbstractSWRLBuiltInLibrary;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.swrlb.*;
import edu.stanford.smi.protegex.owl.swrl.bridge.exceptions.BuiltInException;
import edu.stanford.smi.protegex.owl.swrl.bridge.exceptions.SWRLRuleEngineBridgeException;
import edu.stanford.smi.protegex.owl.swrl.bridge.impl.ArgumentFactoryImpl;


public class MusterOfSWRLRule {

	public static IRI ontologyIRI = IRI.create("http://Niko/test");	

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException, SWRLRuleEngineBridgeException {

		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology(ontologyIRI);
		OWLDataFactory df = m.getOWLDataFactory();
				
		//Get a reference to the needed classes so that we can process it with the reasoner.
		OWLClass phaseClass = df.getOWLClass(IRI.create(ontologyIRI + "#Phase"));
		OWLClass activePhaseClass = df.getOWLClass(IRI.create(ontologyIRI + "#ActivePhase"));
	    
		//Now we initialize a property which will connect our individual in a SWRL rule.
	    OWLObjectProperty previousPhaseProperty = df.getOWLObjectProperty(IRI.create(ontologyIRI + "#PreviousPhase"));

		//Now let's define a simple SWRL rule.
		SWRLVariable p = df.getSWRLVariable(IRI.create(ontologyIRI + "#P"));
		SWRLVariable pre = df.getSWRLVariable(IRI.create(ontologyIRI + "#Pre"));
        
		SWRLObjectPropertyAtom propAtom = df.getSWRLObjectPropertyAtom(previousPhaseProperty, p, pre);
        SWRLClassAtom classAtom = df.getSWRLClassAtom(phaseClass, pre);
        
        
        
        //trying to add built-in; method1  
        //it works only with greaterThan (?p, ?pre) - with two variables
        /* 
         List<SWRLDArgument> swrldArguments = new ArrayList<SWRLDArgument>();
        swrldArguments.add(p);
        swrldArguments.add(pre);
        SWRLBuiltInAtom batom = df.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.GREATER_THAN.getIRI(), swrldArguments);
        System.out.println ("Builtin: " + batom);      
        */
        //trying to add built-in; method2
        /*
        ArgumentFactoryImpl af = (ArgumentFactoryImpl) ArgumentFactory.getFactory();
        List<BuiltInArgument> bargs = new ArrayList<BuiltInArgument>();
        bargs.add(af.createVariableArgument("p"));
        bargs.add(af.createDataValueArgument(9));
        SWRLBuiltInLibraryImpl trial = new SWRLBuiltInLibraryImpl();
        trial.getInvokingBuiltInIndex();

        SWRLBuiltInBridge s = null;
        s.invokeSWRLBuiltIn("myRule", "swrl:greaterThan", 1, false, bargs);
        System.out.println ("Invoke: " + s);*/
        
        
        Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
        antecedent.add(propAtom);
        antecedent.add(classAtom);

        System.out.println (antecedent);
        
        SWRLRule rule = df.getSWRLRule(antecedent, Collections.singleton(df.getSWRLClassAtom(activePhaseClass, p)));
        m.applyChange(new AddAxiom(o, rule));
        
        File output = new File("C:/Dokumente und Einstellungen/VNG/Desktop/musterRule.owl");
        output.createNewFile();
        m.saveOntology(o, IRI.create(output.toURI()));
	}
}
