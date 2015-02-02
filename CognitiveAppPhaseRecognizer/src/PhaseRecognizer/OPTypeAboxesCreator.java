package PhaseRecognizer;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OPTypeAboxesCreator {
	
    // Use here the same IRI as your SMW or Surgipedia
  	public IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	
    // Use special IRI for individuals
   	public IRI indIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/Individual#");	

   	public OWLObjectProperty previousPhase;

	public Set<OWLAxiom> setPancreasIndividualsAndPreviousPhaseProperties() throws OWLOntologyCreationException {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		    OWLDataFactory factory = manager.getOWLDataFactory();
		    
		    System.out.println("creating Pancreas phase classes...");
		    OWLClass phaseStart = factory.getOWLClass(IRI.create(basicIRI + "Start"));
		    OWLClass phaseMobilisation = factory.getOWLClass(IRI.create(basicIRI + "Mobilisation"));
		    OWLClass phasePort_placement = factory.getOWLClass(IRI.create(basicIRI + "Port_placement"));
		    OWLClass phaseDissection = factory.getOWLClass(IRI.create(basicIRI + "Dissection"));
		    OWLClass phaseResection = factory.getOWLClass(IRI.create(basicIRI + "Resection"));
		    OWLClass phaseClosure = factory.getOWLClass(IRI.create(basicIRI + "Closure"));
		    OWLClass phaseDrain = factory.getOWLClass(IRI.create(basicIRI + "Drain"));
		    OWLClass phaseEnd = factory.getOWLClass(IRI.create(basicIRI + "End"));

		    System.out.println("creating Pancreas phase individuals...");
			OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Start1"));
			OWLNamedIndividual port_placement1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Port_placement1"));
			OWLNamedIndividual mobilisation1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Mobilistation1"));
			OWLNamedIndividual dissection1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Dissection1"));
			OWLNamedIndividual resection1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Resection1"));
			OWLNamedIndividual closure1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Closure1"));
			OWLNamedIndividual drain1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Drain1"));
			OWLNamedIndividual end1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "End1"));
			
		    System.out.println("creating Pancreas class-individual relationships for phases...");
		    Set<OWLAxiom> OurAxioms = new HashSet<OWLAxiom>();
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseStart, start1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phasePort_placement, port_placement1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseMobilisation, mobilisation1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDissection, dissection1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection, resection1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseClosure, closure1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDrain, drain1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseEnd, end1));
		    
		    System.out.println("creating Pancreas previous phase relations....");
		    previousPhase = factory.getOWLObjectProperty(IRI.create(basicIRI + "PreviousPhase"));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, start1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, start1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, port_placement1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, port_placement1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, port_placement1));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, mobilisation1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, dissection1));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, closure1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, resection1));
		  
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, closure1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, closure1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, closure1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, drain1));
		    
		    return OurAxioms;
		}
	    
	    
	    public Set<OWLAxiom> setCCEIndividualsAndPreviousPhaseProperties() throws OWLOntologyCreationException {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		    OWLDataFactory factory = manager.getOWLDataFactory();
		    
		    System.out.println("creating CCE phase classes...");
		    OWLClass phaseStart = factory.getOWLClass(IRI.create(basicIRI + "Start"));
		    OWLClass phaseMobilisation = factory.getOWLClass(IRI.create(basicIRI + "Mobilisation"));
		    OWLClass phasePort_placement = factory.getOWLClass(IRI.create(basicIRI + "Port_placement"));
		    OWLClass phaseDissection = factory.getOWLClass(IRI.create(basicIRI + "Dissection"));
		    OWLClass phaseResection_cystic_Artery = factory.getOWLClass(IRI.create(basicIRI + "Resection_cystic_artery"));
		    OWLClass phaseResection_cystic_duct = factory.getOWLClass(IRI.create(basicIRI + "Resection_cystic_duct"));
		    OWLClass phaseResection_gallbladder = factory.getOWLClass(IRI.create(basicIRI + "Resection_gallbladder"));
		    OWLClass phaseClosure = factory.getOWLClass(IRI.create(basicIRI + "Closure"));
		    OWLClass phaseDrain = factory.getOWLClass(IRI.create(basicIRI + "Drain"));
		    OWLClass phaseEnd = factory.getOWLClass(IRI.create(basicIRI + "End"));

		    System.out.println("creating CCE phase individuals...");
			OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Start1"));
			OWLNamedIndividual port_placement1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Port_placement1"));
			OWLNamedIndividual mobilisation1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Mobilistation1"));
			OWLNamedIndividual dissection1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Dissection1"));
			OWLNamedIndividual resection_cystic_artery1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Resection_cystic_artery1"));
			OWLNamedIndividual resection_cystic_duct1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Resection_cystic_duct1"));
			OWLNamedIndividual resection_gallbladder1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Resection_gallbladder1"));
			OWLNamedIndividual closure1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Closure1"));
			OWLNamedIndividual drain1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Drain1"));
			OWLNamedIndividual end1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "End1"));
			
		    System.out.println("creating CCE class-individual relationships for phases...");
		    Set<OWLAxiom> OurAxioms = new HashSet<OWLAxiom>();
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseStart, start1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phasePort_placement, port_placement1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseMobilisation, mobilisation1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDissection, dissection1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_cystic_Artery, resection_cystic_artery1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_cystic_duct, resection_cystic_duct1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection_gallbladder, resection_gallbladder1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseClosure, closure1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDrain, drain1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseEnd, end1));
		    
		    System.out.println("creating CCE previous phase relations....");
		    previousPhase = factory.getOWLObjectProperty(IRI.create(basicIRI + "PreviousPhase"));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, start1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, start1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, start1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, start1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, port_placement1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, port_placement1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, mobilisation1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_artery1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_duct1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, dissection1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_duct1, resection_cystic_artery1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, resection_cystic_artery1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_cystic_artery1, resection_cystic_duct1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection_gallbladder1, resection_cystic_duct1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, resection_gallbladder1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, closure1, resection_gallbladder1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, closure1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, closure1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, drain1));
		    
		    return OurAxioms;
		}
	    
	    public Set<OWLAxiom> setAEIndividualsAndPreviousPhaseProperties() throws OWLOntologyCreationException {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		    OWLDataFactory factory = manager.getOWLDataFactory();
		    
		    System.out.println("creating AE phase classes...");
		    OWLClass phaseStart = factory.getOWLClass(IRI.create(basicIRI + "Start"));
		    OWLClass phaseMobilisation = factory.getOWLClass(IRI.create(basicIRI + "Mobilisation"));
		    OWLClass phasePort_placement = factory.getOWLClass(IRI.create(basicIRI + "Port_placement"));
		    OWLClass phaseDissection = factory.getOWLClass(IRI.create(basicIRI + "Dissection"));
		    OWLClass phaseResection = factory.getOWLClass(IRI.create(basicIRI + "Resection"));
		    OWLClass phaseClosure = factory.getOWLClass(IRI.create(basicIRI + "Closure"));
		    OWLClass phaseDrain = factory.getOWLClass(IRI.create(basicIRI + "Drain"));
		    OWLClass phaseEnd = factory.getOWLClass(IRI.create(basicIRI + "End"));

		    System.out.println("creating AE phase individuals...");
			OWLNamedIndividual start1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Start1"));
			OWLNamedIndividual port_placement1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Port_placement1"));
			OWLNamedIndividual mobilisation1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Mobilistation1"));
			OWLNamedIndividual dissection1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Dissection1"));
			OWLNamedIndividual resection1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Resection1"));
			OWLNamedIndividual closure1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Closure1"));
			OWLNamedIndividual drain1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "Drain1"));
			OWLNamedIndividual end1 = factory.getOWLNamedIndividual(IRI.create(indIRI + "End1"));
			
		    System.out.println("creating AE class-individual relationships for phases...");
		    Set<OWLAxiom> OurAxioms = new HashSet<OWLAxiom>();
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseStart, start1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phasePort_placement, port_placement1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseMobilisation, mobilisation1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDissection, dissection1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseResection, resection1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseClosure, closure1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseDrain, drain1));
		    OurAxioms.add(factory.getOWLClassAssertionAxiom(phaseEnd, end1));
		    
		    System.out.println("creating AE previous phase relations....");
		    previousPhase = factory.getOWLObjectProperty(IRI.create(basicIRI + "PreviousPhase"));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, start1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, start1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, port_placement1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, port_placement1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, port_placement1));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, mobilisation1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, mobilisation1));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, port_placement1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, mobilisation1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, resection1, dissection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, dissection1));

		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, dissection1, resection1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, closure1, resection1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, drain1, closure1));
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, closure1));
		    
		    OurAxioms.add(factory.getOWLObjectPropertyAssertionAxiom(previousPhase, end1, drain1));
		    
		    return OurAxioms;
		}
}
