package MappingN3toRDF;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;




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
		namespaceList.add("PREFIX lf: <http://localhost:8080/CognitiveApp/files/ontologies/lf#>");
		namespaceList.add("PREFIX dc: <http://purl.org/dc/elements/1.1/>");
		namespaceList.add("PREFIX baseProp: <http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A>");
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
	
	/* Here we define the SPARQL pattern which will serve as an input checker*/ 
	public static String getSparqlInputPattern() {
		String startFrame = "\n" + "SELECT * WHERE { ";
		String inputPattern = "?request		baseProp:Has_N3RuleFile		?n3ruleFile." + "\n" +
							  "?n3ruleFile 		dc:format 			\"text/n3\" ."; 
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
	
	//actually not used 
	/*
	public static String getProperties(ServletContext context, String key) throws IOException {			
		
		Reader reader = new FileReader(context.getRealPath("/files/localhost.properties"));
		
		Properties prop = new Properties();
		prop.load(reader);
		return prop.getProperty(key);
		//reader.close();	
	}
	*/
}
