package SemFormToSWRL;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

public class SWRLHelpClass {
	
	OWLClass SWRLClassAtomName;
	OWLObjectProperty SWRLObjectPropertyAtomName;
	OWLDataProperty SWRLDataPropertyAtomName;
	//SWRLBuiltInsVocabulary SWRLBuiltinVocab;
	
	SWRLVariable SWRLAtomVar1;
	SWRLVariable SWRLAtomVar2;
	SWRLLiteralArgument SWRLLiteralArg;
	
	SWRLClassAtom classAtom;
	SWRLObjectPropertyAtom objPropAtom;
	SWRLDataPropertyAtom dataPropAtom;
	SWRLBuiltInAtom builtinAtom;
}
