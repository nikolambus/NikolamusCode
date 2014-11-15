package SWRLReasoner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLOntologyChangeFilter;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class Reasoner {

    // Use here the same IRI as your SMW or Surgipedia
  	public IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
  	public OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
  	public OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
  	public OWLOntologyManager m3 = OWLManager.createOWLOntologyManager();

    // these variables will store reserve copies of the phase individual and appropriate class in the method "removePhase"  
    public OWLNamedIndividual currentPhaseInd = null;
    public OWLClass currentPhaseCls = null;
  	
    public void action(String rule, String patient, String outputPath) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

    	/* Firstly we would like to check which rule we are going to deal with: a common rule or a rule_fit version.
    	 * If it's the latter we would like to do a redirect to the former one. Because a rule_fit version is fit for queries, but 
    	 * the common rule is fitter for reasoning. 
    	 * 
    	 *  For that we look inside the rule and search for special property "Property-3AHas_source_rule" which specifies the relation
    	 *  rule_fit --> rule. If found, process the source rule in following. Else - do nothing (it is a common rule)
    	 *  
    	 *  Looking inside is done in 2 steps: 1) get the text from an URI (via Scanner) 2) find the necessary line inside (Pattern Class and its multiline modus (?sm))  
    	 */
    	
    	Scanner scanner = new Scanner(new URL(rule).openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
		
		//check
		//System.out.println(out);       
		
		if (Pattern.matches("(?sm).*^\\s*<Property-3AHas_source_rule rdf:resource=\"([a-zA-Z_0-9:,?\"./]+)\"/>$.*", out)) {
    		//parse the source rule name from regex ([a-zA-Z_0-9:,?\"./]+)
			Pattern pattern = Pattern.compile("(?sm).*^\\s*<Property-3AHas_source_rule rdf:resource=\"([a-zA-Z_0-9:,?\"./]+)\"/>$.*");
			Matcher mtch = pattern.matcher(out);
			while (mtch.find()) {
				//and save the found label to the rule variable
				rule = mtch.group(1);
			}
		}
    	
    	//check
    	System.out.println("RULE after possibly redirect: " + rule);
    	
    	/*load at first the rule and the patient files as 2 ontologies. Note: they have the same basic IRI. 
    	It isn't allowed to load 2 different ontologies with the same IRI into the same manager. So we need different manager:
    	m1 - for ruleOnto, m2 - for patOnto and m3 - for the new mergedOnto*/    
    	OWLOntology ruleOnto = m1.loadOntology(IRI.create(rule));
    	OWLOntology patOnto = m2.loadOntology(IRI.create(patient));
    	OWLOntology mergedOnt = m3.createOntology(basicIRI);
    	
    	/* another idea: we could just preprocess our rule_fit ontology. 
    	 * That means we could find all anonymous individuals and remove them  
    	 * I've just printed it down very fast without debugging it. 
    	 * If it would work there would be no need in above redirection
    	     
    	Set<OWLOntology> ruleHelpSet = new HashSet<OWLOntology>();
    	ruleHelpSet.add(ruleOnto);
    	OWLEntityRemover remover = new OWLEntityRemover(m1, ruleHelpSet);
    	for (OWLNamedIndividual ind : ruleOnto.getIndividualsInSignature()) {
    		if (ind.isAnonymous()) {
    		    ind.accept(remover)
    		}
    	}
    	*/
    	
    	/* Copy all axioms from rule and patient ontologies into the new one */
		m3.addAxioms(mergedOnt, ruleOnto.getAxioms());
		m3.addAxioms(mergedOnt, patOnto.getAxioms());
		
		/* Before we start the reasoning we would like to save some older results that will be obsolete and can only 
		 * confuse us after the new inference results will be added. There is only one method in our case we need to pay attention to: 
		 * BefindetSichInDerPhase. It does not make sense for a patient be in different phases simultaneously. 
		 * So we save the "currentPhase" before reasoning in order to differentiate between the older and the newly inferenced phase.
		 */

		saveCurrentPhase(mergedOnt);		
		
		//check
		System.out.println("OLD_PHASE_INDIVIDUAL: " + currentPhaseInd);
		System.out.println("OLD_PHASE_CLASS: " + currentPhaseCls);

		
		//initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(mergedOnt);		
	
		//Now it should perform reasoning
		reasoner.getKB().realize();
			
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m3, mergedOnt);
		
		/* If a new phase individual is appeared after reasoning, do nothing. 
		 * Else, restore the cut value - it's still patient's current phase. */
		removeOldPhase(mergedOnt);
		
		// remove rules if any
		removeRules(mergedOnt);
		
		//Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        m3.saveOntology(mergedOnt, IRI.create(output.toURI()));
    } 
    
    // removes the current phase Individual and returns its String  
    public void saveCurrentPhase (OWLOntology o) {
    	OWLDataFactory factory = m3.getOWLDataFactory();
		OWLObjectProperty phaseProperty = factory.getOWLObjectProperty(IRI.create(basicIRI + "BefindetSichInDerPhase"));
		OWLClass patientClass = factory.getOWLClass(IRI.create(basicIRI + "Patient"));

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(o);
	    
	    // get all individuals of the "Patient" class
	    NodeSet<OWLNamedIndividual> patientsNodeSet = reasoner.getInstances(patientClass, true);
	    Set<OWLNamedIndividual> patients = patientsNodeSet.getFlattened();
	    
	    // if we have at least one individual of the "Patient" class 
	    if (!patients.isEmpty()) {
	    	
	    	/* actually there should be only one individual of the class "Patient" - use "iterator().next()" */
	    	OWLNamedIndividual patInd = patients.iterator().next();
	   	    	
	        /* check whcih phase individual is combined with our patient individual */
	    	NodeSet<OWLNamedIndividual> phasesNodeSet = reasoner.getObjectPropertyValues(patInd, phaseProperty);        
		    Set<OWLNamedIndividual> phases = phasesNodeSet.getFlattened();

	   	 	// following code creates reserve copies of the phase individual and its class for the case "no new phase was reasoned"
		    if (!phases.isEmpty()) { 
		    	// actually there could be only one phase individual - use "iterator().next()" 
		    	currentPhaseInd = phases.iterator().next();
		    }
		   
		    
		    Set<OWLClassExpression> phaseClsSet = currentPhaseInd.getTypes(o);
		    if (!phaseClsSet.isEmpty()) {
		    	currentPhaseCls = (OWLClass) phaseClsSet.iterator().next();
		    }
    	}    
    }
    
    //check do we have got a new phase. If not, utilize the old one.
    public void removeOldPhase (OWLOntology o) {
    	OWLDataFactory factory = m3.getOWLDataFactory();
		OWLClass patientClass = factory.getOWLClass(IRI.create(basicIRI + "Patient"));
		OWLObjectProperty phaseProperty = factory.getOWLObjectProperty(IRI.create(basicIRI + "BefindetSichInDerPhase"));
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(o);
	    
	    // Get the individual of the Patient class
	    NodeSet<OWLNamedIndividual> patientsNodeSet = reasoner.getInstances(patientClass, true);
	    Set<OWLNamedIndividual> patients = patientsNodeSet.getFlattened();
	    
	    // if we have a patient individual
	    if (!patients.isEmpty()) {
	    	OWLNamedIndividual patInd = patients.iterator().next();

	       	NodeSet<OWLNamedIndividual> phasesNodeSet = reasoner.getObjectPropertyValues(patInd, phaseProperty);        
	    	Set<OWLNamedIndividual> phases = phasesNodeSet.getFlattened();
	    	
	    	// if there are more than one (old) phase
	    	if (phases.size() > 1) {
	    		
	    		System.out.println("We've got a new phase! Removing the old one.");	    		
	    		
	    		// got through the phases (should be 2) and look for the old one
	    		for (OWLNamedIndividual phaseInd : phases) {
	    			if (phaseInd == currentPhaseInd) {
	    				
	    	    		//init a remover-visitor
	    		        OWLEntityRemover remover = new OWLEntityRemover(m3, Collections.singleton(o));
	    		        
	    			    //accept that this phaseInd and its class should be removed
	    			    currentPhaseInd.accept(remover);	    	    
	    				currentPhaseCls.accept(remover);
	    			    m3.applyChanges(remover.getChanges());
	    			}
	    		}
	    	}
	    }
    }
    
    //check do we have rules. If yes, remove them. The result is a patient file.
    public void removeRules (OWLOntology o) {
    	Set<SWRLRule> rulesSet = o.getAxioms(AxiomType.SWRL_RULE);
    	if (!rulesSet.isEmpty()) {
    		for (SWRLRule rule : rulesSet) {
				m3.removeAxiom(o, rule);
    		}
    	}
    } 
}
