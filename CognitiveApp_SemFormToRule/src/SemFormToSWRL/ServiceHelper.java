package SemFormToSWRL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

public class ServiceHelper {

	/* The following 3 methods help us to write SPARQL prefixes in the right way. */ 
	/* This method is taken from https://github.com/Data2Semantics/Hubble/blob/master/src/main/java/com/data2semantics/hubble/client/helpers/Helper.java */
	public static String getSparqlPrefixesAsString() {
		return ServiceHelper.implode(getSparqlPrefixes(), "\n");
	}
	
	/* This method is taken from https://github.com/Data2Semantics/Hubble/blob/master/src/main/java/com/data2semantics/hubble/client/helpers/Helper.java */
	public static ArrayList<String> getSparqlPrefixes() {
		ArrayList<String> namespaceList = new ArrayList<String>();
		namespaceList.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		namespaceList.add("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>");
		namespaceList.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		namespaceList.add("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		namespaceList.add("PREFIX foaf: <http://xmlns.com/foaf/0.1/>");
		namespaceList.add("PREFIX sf: <http://localhost:8080/CognitiveApp2/files/ontologies/sf#>");
		namespaceList.add("PREFIX surgi: <http://surgipedia.sfb125.de/wiki/Special:URIResolver/>");
		namespaceList.add("PREFIX surgiProp: <http://surgipedia.sfb125.de/wiki/Special:URIResolver/Property-3A>");
		namespaceList.add("PREFIX surgiCat: <http://surgipedia.sfb125.de/wiki/Special:URIResolver/Property-3A>");
		namespaceList.add("PREFIX base: <http://localhost/mediawiki/index.php/Special:URIResolver/>");
		namespaceList.add("PREFIX baseProp: <http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A>");
		namespaceList.add("PREFIX baseCat: <http://localhost/mediawiki/index.php/Special:URIResolver/Category-3A>");
		namespaceList.add("PREFIX dc: <http://dublincore.org/documents/2012/06/14/dcmi-terms/?v=elements#>");
		return namespaceList;
	}

	/* This method is taken from https://github.com/Data2Semantics/Hubble/blob/master/src/main/java/com/data2semantics/hubble/client/helpers/Helper.java */
	public static String implode(ArrayList<String> arrayList, String glue) {
		String result = "";
		for (String stringItem: arrayList) {
			if (result.length() > 0) {
				result += glue;
			}
			result += stringItem;
		}
		return result;
	}
	
	/* Here we define the SPARQL pattern which will serve as the input checker*/ 
	public static String getSparqlInputPattern() {
		String startFrame = "\n" + "SELECT * WHERE { ";
		String inputPattern = "?request		surgiProp:Has_OntologyRule ?ruleURI ." + "\n" + 
							  "?ruleURI	rdf:type			baseCat:OntologyRule .";
		String endFrame = "}";
		
		String together = "\n" + startFrame + "\n" + inputPattern + "\n" + endFrame;
		return together;
	}
	
	public static void printRDFDescriptionFromFile(String filepath, HttpServletResponse response, ServletContext context, String contenttype)
			throws Exception {

		response.setContentType(contenttype);
		PrintWriter writer = response.getWriter();

		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line = null;
		while ((line = br.readLine()) != null) {
			writer.println(line);
		}
		br.close();
		writer.close();
	}	
	
}
