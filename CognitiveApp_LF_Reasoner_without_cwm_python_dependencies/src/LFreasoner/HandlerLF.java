package LFreasoner;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HandlerLF extends DefaultHandler {

	//Variables where we gonna store our request parameters
	private String patient = null;
	private String rule = null;
	private String uriRequest = "";
	
	
	//Help flags indicating that we are within a certain element. E.g.: hasPatient=true --> we are processing <lf:hasPatient> bla..
	private boolean hasPatient = false;
	private boolean hasRule = false;

	/* What does SAX mean with ELEMENT?
	 * -------------------------------------------------------------------------------------------------------------------------
	 * <<EXAMPLE 1 with a node as object>>
	 * 
	 * <meanfree:hasInputImage rdf:resource="https://xnat.sfb125.de/data/projects/CAUC/resources/Example_Data/files/T1.nrrd"/>
	 * that's 1 element, thereby:
	 * 
	 * qName = "meanfree:hasInputImage"
	 * localName = "hasInputImage"
	 * atts.getValue("rdf:resource")="https://xnat.sfb125.de/data/projects/CAUC/resources/Example_Data/files/T1.nrrd"
	 *--------------------------------------------------------------------------------------------------------------------------
	 
	 *--------------------------------------------------------------------------------------------------------------------------
	 * <<EXAMPLE 2 with a literal as object>>
	 * 
	 * <lf:hasPatient>universal_patient.ttl</lf:hasPatient>
	 * Thereby: 
	 * qName = "lf:hasPatient"
	 * localName = "hasPatient"
	 * characters processes - "universal_patient.ttl" 
	 *---------------------------------------------------------------------------------------------------------------------------
	 */
	
	// <
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		//IF we are processing <lf:hasPatient> bla.. --> THEN set hasPatient=true 
		if (localName.equalsIgnoreCase("hasPatient"))
			hasPatient = true;
		if (localName.equalsIgnoreCase("hasRule"))
			hasRule = true;
		if (localName.equalsIgnoreCase("description") && !hasPatient && !hasRule) {
			uriRequest = atts.getValue("rdf:about");
		}
	}

	// >
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equalsIgnoreCase("hasPatient"))
			hasPatient = false;
		if (localName.equalsIgnoreCase("hasRule"))
			hasRule = false;
	}
	
	//ALTERNATIVE for memory economical behavior of the characters metho
	//StringBuilder currentText = new StringBuilder();
	
	//for literals
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (hasPatient) 
			patient = new String(ch, start, length);
		//  ALTERNATIVE (by memory leak problems) use Stringbuilder
		//	patient = currentText.append(ch, start, length).toString();
		//  currentText.delete(0, currentText.length()-1);

		if (hasRule) 
			rule = new String(ch, start, length);
	}
	
	public String getRequestURI() {
		return uriRequest;
	}
	
	public String getRule() {
		return rule;
	}
	
	public String getPatient() {
		return patient;
	}
	
}
