package LFreasoner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;



import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

public class Helper {
	
	/* The following 3 methods help us to write SPARQL prefixes in the right way. */ 
	/* This method is taken from https://github.com/Data2Semantics/Hubble/blob/master/src/main/java/com/data2semantics/hubble/client/helpers/Helper.java */
	public static String getSparqlPrefixesAsString() {
		return Helper.implode(getSparqlPrefixes(), "\n");
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
		String inputPattern = "?request		lf:hasPatientFile		?patientFile." + "\n" + 
							  "?request		lf:hasRuleFile		?ruleFile." + "\n" +
				
							  "OPTIONAL { ?patientFile		dc:format			\"text/turtle\" } ." + "\n" + 
							  "OPTIONAL { ?patientFile		rdf:type			lf:PatientFile } ." + "\n" +
							  "OPTIONAL { ?ruleFile 		dc:format 			\"text/n3\" } ." + "\n" +
							  "OPTIONAL { ?ruleFile			rdf:type 			lf:RuleFile } ."; 
	
		String endFrame = "}";
		
		String together = "\n" + startFrame + "\n" + inputPattern + "\n" + endFrame;
		return together;
	}
	
	public static void printRDFDescriptionFromFile(String filepath, HttpServletResponse response, ServletContext context, String contenttype)
			throws Exception {

		response.setContentType(contenttype);
		PrintWriter writer = response.getWriter();

		BufferedReader br = new BufferedReader(new FileReader(context.getRealPath(filepath)));
		System.out.println(context.getRealPath(filepath));
		String line = null;
		while ((line = br.readLine()) != null) {
			writer.println(line);
		}
		br.close();
		writer.close();
	}

	public static String getProperties(ServletContext context, String key) throws IOException {			
		Reader reader = new FileReader(context.getRealPath("/files/localhost.properties"));
				
		Properties prop = new Properties();
		prop.load(reader);
		return prop.getProperty(key);
		//reader.close();	

		
	}
	
}
