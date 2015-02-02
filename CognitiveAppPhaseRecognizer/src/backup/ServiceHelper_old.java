package backup;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class ServiceHelper_old {

	/* The following 3 methods help us to write SPARQL prefixes in the right way. */ 
	/* This method is taken from https://github.com/Data2Semantics/Hubble/blob/master/src/main/java/com/data2semantics/hubble/client/helpers/Helper.java */
	public static String getSparqlPrefixesAsString() {
		return ServiceHelper_old.implode(getSparqlPrefixes(), "\n");
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
		namespaceList.add("PREFIX surgiProp: <http://surgipedia.sfb125.de/wiki/Special:URIResolver/Property-3A>");
		namespaceList.add("PREFIX surgiCat: <http://surgipedia.sfb125.de/wiki/Special:URIResolver/Category-3A>");
		namespaceList.add("PREFIX baseCat: <http://localhost/mediawiki/index.php/Special:URIResolver/Category-3A>");
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
	
	/* Here we define the SPARQL pattern which will serve as the input checker */ 
	public static String getSparqlInputPattern() {
		String startFrame = "\n" + "SELECT * WHERE { ";
		String inputPattern = "?request		surgiProp:Has_phaseContextFile		?phaseContextFile." + "\n" + 
							  "?phaseContextFile		dc:format			\"application/rdf+xml\" ." + "\n" + 
							  "?request		surgiProp:Has_OPType		?opType." + "\n" +
							  "?request		surgiProp:Has_SWRLRule		?ruleURI." + "\n" +
	/*	the category OntologyRule is provided by wiki. 
	 * In case of localhost - we use baseCat
	 * In case of server - we should use surgiCat (see following line): 
	 * "?ruleURI			rdf:type 			surgiCat:OntologyRule ."; 
	 */
							  "?ruleURI			rdf:type 			baseCat:OntologyRule ."; 
		String endFrame = "}";
		
		String together = "\n" + startFrame + "\n" + inputPattern + "\n" + endFrame;
		return together;
	}
	
	public static String getSparqlPatternForSWRLRuleFile(String prefix, String ruleName) {
		
		String sparqlPattern = "PREFIX prefix: <" + prefix + ">" + "\n" + 
				   "PREFIX baseProp: <http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A>" + "\n" +
				   "\n" + "SELECT ?swrlRuleFile WHERE { " + "\n" + 
				   "prefix:" + ruleName + " baseProp:Has_SWRLRuleFile ?swrlRuleFile . }"; 
		
		/* old file architecture
		 * 
		String sparqlPattern = "PREFIX prefix: <" + prefix + ">" + "\n" + 
							   "PREFIX baseProp: <http://localhost/mediawiki/index.php/Special:URIResolver/Property-3A>" + "\n" +
							   "\n" + "SELECT ?swrlRuleFile WHERE { " + "\n" + 
							   "?bnode baseProp:Has_name prefix:" + ruleName + " ." + "\n" + 
							   "?bnode baseProp:Has_SWRLRuleFile ?swrlRuleFile . }"; 
		*/
		return sparqlPattern;
	}
	
	public static QuerySolution evaluationOfSPARQLQueryAgainstModel (String queryString, Model model) {
		
		// that will be our result
		QuerySolution soln = null;
		
		// Create a SPARQL query from the given string
		Query query = QueryFactory.create(queryString);
		
		//Create a QueryExecution to execute over the Model.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
        		
		try {
			
			// Results from a query in a table-like manner for SELECT queries. Each row corresponds to a set of bindings which fulfill the conditions of the query. Access to the results is by variable name.
			ResultSet results = qexec.execSelect();

			while(results.hasNext()){

				//QuerySolution -- A single answer from a SELECT query.
				//results.nextSolution() -- Moves to the next result
				soln = results.nextSolution();

			}		 
		}
		finally {
			qexec.close();
		}
		
		return soln;
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
