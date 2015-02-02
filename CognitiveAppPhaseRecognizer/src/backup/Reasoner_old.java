package backup;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;


public class Reasoner_old {

    // Use here the same IRI as your SMW or Surgipedia
  	public IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
    // Use special IRI for individuals
   	public IRI indIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/Individual#");	
   	
  	public OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
  	public OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
  	public OWLOntologyManager m3 = OWLManager.createOWLOntologyManager();

	public OWLObjectProperty previousPhase;
	public OWLNamedIndividual detected = null;

    // these variables will store reserve copies of the phase individual and appropriate class in the method "removePhase"  
    //public OWLNamedIndividual currentPhaseInd = null;
    //public OWLClass currentPhaseCls = null;
  	
    public void action(String rule, String context, String opType, String outputPath) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
    	
    	/*load at first the rule and the patient files as 2 ontologies. Note: they have the same basic IRI. 
    	It isn't allowed to load 2 different ontologies with the same IRI into the same manager. So we need different manager:
    	m1 - for ruleOnto, m2 - for patOnto and m3 - for the new mergedOnto*/    
    	OWLOntology ruleOnto = m1.loadOntology(IRI.create(rule));
    	OWLOntology contextOnto = m2.loadOntology(IRI.create(context));
    	OWLOntology mergedOnt = m3.createOntology(basicIRI);
    	
    	/* Copy all logical axioms (discard annotation properties) from the rule ontology and all axioms from the patient ontology into the new one */
		m3.addAxioms(mergedOnt, ruleOnto.getLogicalAxioms());
		m3.addAxioms(mergedOnt, contextOnto.getAxioms());
		
		// Now we would like to set OP type specific PreviousPhase properties and create appropriate individuals
		// Therefore we create an instance of a special class which is responsible for it
		PhaseAboxesCreator creator = new PhaseAboxesCreator();

		if (opType.equalsIgnoreCase("PancreasResection")) {
			//create classes, individuals and previousPhase-relationships for phases 
		    Set<OWLAxiom> phases = creator.setPancreasIndividualsAndPreviousPhaseProperties();
		    m3.addAxioms(mergedOnt, phases);
		}

		//CCE = Cholecystectomy
		if (opType.equalsIgnoreCase("CCE")) {
		    Set<OWLAxiom> phases = creator.setCCEIndividualsAndPreviousPhaseProperties();
		    m3.addAxioms(mergedOnt, phases);
		}
		
		//AE = Adrenoectomy
		if (opType.equalsIgnoreCase("AE")) {
			Set<OWLAxiom> phases = creator.setAEIndividualsAndPreviousPhaseProperties();
		    m3.addAxioms(mergedOnt, phases);
		}
		
		/* Before we start the reasoning we would like to save some older results that can become obsolete after 
		 * the new inference results will be added. If so they will only confuse us. 
		 * There is only one method in our case we need to pay attention to: "BefindetSichInDerPhase". 
		 * It does not make sense for a patient to be in different phases simultaneously. 
		 * So we save the "currentPhase" before reasoning in order to differentiate between the older and the newly inferenced phase.
		 */

		//saveCurrentPhase(mergedOnt);		
		
		//check
		//System.out.println("OLD_PHASE_INDIVIDUAL: " + currentPhaseInd);
		//System.out.println("OLD_PHASE_CLASS: " + currentPhaseCls);

		
		//initialize the reasoner with help of the Pellet-package
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(mergedOnt);		
	
		//Now it should perform reasoning
		reasoner.getKB().realize();
			
		//Now let us fill the results of the reasoning in our ontology variable
		InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner);
		generator.fillOntology(m3, mergedOnt);
		
		/* If a new phase individual is appeared after reasoning, do nothing. 
		 * Else, restore the cut value - it's still patient's current phase. */
		//removeOldPhase(mergedOnt);
		
		// remove rules if any
		removeRules(mergedOnt);
			
		//Now we can assure that our rule was added into the ontology by saving ontology in a new file 
        File output = new File(outputPath);
        output.createNewFile();
        m3.saveOntology(mergedOnt, IRI.create(output.toURI()));
        
    	//Now we just finding out which individual was asserted to the DetectedPhase-class 
	    
        //1.step: we need one reasoner more - an OWLreasoner
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner owlreasoner = reasonerFactory.createReasoner(mergedOnt);
	    
	    //2.step: defining the class DetectedPhase
	    OWLDataFactory factory = m3.getOWLDataFactory();
	    OWLClass detectedPhase = factory.getOWLClass(IRI.create(basicIRI + "DetectedPhase"));
        
	    //3.step: get the instances of the class DetectedPhase
	    NodeSet<OWLNamedIndividual> individualsNodeSet = owlreasoner.getInstances(detectedPhase, true);
        Set<OWLNamedIndividual> detectedIndis = individualsNodeSet.getFlattened();
        
        //If there are some
        if (!detectedIndis.isEmpty()) {
        	//Get the first instance from the class currentPhase with help of "Iterator" and "next" command
            Iterator<OWLNamedIndividual> i = detectedIndis.iterator();
            detected = i.next();
            System.out.println ("Detected phase is: \n" + detected);
        }
        else {
        	System.out.println("No Phase could be detected.");
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
    
    // removes the current phase Individual and returns its String  
    /*
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
	    	
	    	// actually there should be only one individual of the class "Patient" - use "iterator().next()"
	    	OWLNamedIndividual patInd = patients.iterator().next();
	   	    	
	        // check which phase individual is combined with our patient individual
	    	NodeSet<OWLNamedIndividual> phasesNodeSet = reasoner.getObjectPropertyValues(patInd, phaseProperty);        
		    Set<OWLNamedIndividual> phases = phasesNodeSet.getFlattened();

	   	 	// following code creates reserve copies of the phase individual and its class for the case "no new phase was reasoned"
		    if (!phases.isEmpty()) { 
		    	// actually there could be only one phase individual - use "iterator().next()" 
		    	currentPhaseInd = phases.iterator().next();
		    }
		   
		    if (!phases.isEmpty()) { 
			    Set<OWLClassExpression> phaseClsSet = currentPhaseInd.getTypes(o);
		    
		    	if (!phaseClsSet.isEmpty()) {
		    		currentPhaseCls = (OWLClass) phaseClsSet.iterator().next();
		    	}
		    }
		}    
    }
    */
    
    //check do we have got a new phase. If not, utilize the old one.
    /*
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
    */   
}
