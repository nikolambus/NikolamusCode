import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class BuildRule {
	
    public static String rule = "";    
  	public static IRI basicIRI = IRI.create("http://localhost/mediawiki/index.php/Special:URIResolver/");	

    
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
		
		//getting the RDF export of the rule's page in SMW
		Scanner scanner = new Scanner(new URL("http://localhost/mediawiki/index.php/Special:ExportRDF/RudisRule2").openStream(), "UTF-8").useDelimiter("\\A");
		String out = scanner.next();
	    		
		Helper.buildBody(out);
		Helper.buildHead(out);
		
		rule = Helper.body + "->" + Helper.head;
	    scanner.close();

	    //check
		System.out.println("Rule: " + rule);
		System.out.println("Classes bank: " + Helper.classesBank);
		System.out.println("Vars bank: " + Helper.varsBank);
		
		GetConceptsFromString_short.fromStringToOWLRDFNotation(rule, basicIRI);

	}
	
}
