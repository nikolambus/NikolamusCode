package SemFormToSWRL;
//import Query;
//import QueryExecution;
//import QuerySolution;
//import ResultSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;




import java.net.URL;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


@Path("/")
public class Service {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Path("/input")
	@Consumes("application/rdf+xml")
	public void postStuff(String rdf, @Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws IOException, SAXException, OWLOntologyCreationException, OWLOntologyStorageException {
		
		/* String rdf is read (via POST from Postman) and has a rdf/xml structure that should satisfy the pattern in Helper.getSparqlInputPattern()
		  a) check if it satisfies the SPARQL input pattern 
		  b) if yes, then extract 
		  - request
		  - patient
		  - rule
		  from the POST
		*/
		
		//Variables where we gonna store our request parameters
		String ruleURI=null;
		String requestURI="";
		
		String sparql_prefixes = ServiceHelper.getSparqlPrefixesAsString();		
		String sparql_input_pattern = ServiceHelper.getSparqlInputPattern();
		String querystring = sparql_prefixes + sparql_input_pattern;
		
		//check
		//System.out.println(querystring);
		
		//Encodes this String into a sequence of bytes
		InputStream in = new ByteArrayInputStream(rdf.getBytes());
		
		//Answer a fresh rdf Model with the default specification.
		Model model = ModelFactory.createDefaultModel();
		
	    //Add statements from a document. This method assumes the concrete syntax is RDF/XML
		model.read(in, "");
		
		//Close the Model and free up resources held.
		in.close();// Create a SPARQL query from the given string.
		Query query = QueryFactory.create(querystring);

		//Create a QueryExecution to execute over the Model.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			
			// Results from a query in a table-like manner for SELECT queries. Each row corresponds to a set of bindings which fulfil the conditions of the query. Access to the results is by variable name.
			ResultSet results = qexec.execSelect();
				
			//check
			//System.out.println("CHUNGA CHANGA!!!!!!!!!!!");			
			
			while(results.hasNext()){
				 
				//QuerySolution -- A single answer from a SELECT query.
				//results.nextSolution() -- Moves on to the next result
				QuerySolution soln = results.nextSolution();

				//check
				System.out.println(soln);		
				
				//Return the value of the named variable in this binding, casting to a Resource. 
				//This solution is a shorter alternative to the string-parsing-HandlerLF-solution. Here we do not need HandlerLF class at all.
				requestURI = soln.getResource("request").toString();
				ruleURI = soln.getResource("ruleURI").toString();
			}
		}
		finally{
			qexec.close();
		}    
		
		if (requestURI=="")
			System.out.println("Input pattern for this Cognitive App hasn't been matched");
		else {
		
			//check
			System.out.println("Rule URI: " + ruleURI);
			System.out.println("Request URI: " + requestURI);	
		
			//parse "RudisRule2" from "http://localhost/mediawiki/index.php/Special:ExportRDF/RudisRule2"
			String ruleName = ruleURI.substring(ruleURI.lastIndexOf("/")+1, ruleURI.length());
		
			//get the output path via ServletContext method "getRealPath" (explanation at the end)
			String outputPath = context.getRealPath("/files/output/") + "\\" + ruleName + "_without_annotations.owl";
		
			//now we should build a bridge between "http://localhost/mediawiki/index.php/Special:URIResolver/RudisRule2" and http://localhost/mediawiki/index.php/Special:RDFExport/RudisRule2
			String rdfExport =  "http://localhost/mediawiki/index.php/Special:ExportRDF/" + ruleName;
			
			//getting the RDF export of the rule's page in SMW
			Scanner scanner = new Scanner(new URL(rdfExport).openStream(), "UTF-8").useDelimiter("\\A");
			String out = scanner.next();
		
			//Wiki rule --> SWRL rule string
			RDFExportParser parser = new RDFExportParser();
			String rule = parser.CreateRule(out);
	    
			scanner.close();

			//check
			System.out.println("Rule: " + rule);
			System.out.println("Classes bank: " + parser.classesBank);
			System.out.println("Vars bank: " + parser.varsBank);

			//SWRL rule string --> SWRL rule file
			SWRLRuleFromStringCreator2 creator = new SWRLRuleFromStringCreator2();
			creator.fromStringToOWLRDFNotation(rule, outputPath, parser.helpIndividualsList);	
			
			//the output of creator is the input for the next step
			String inputPath = outputPath;
			
			System.out.println("");	
			System.out.println("OutputPath-file-without-annotation-properties: " + outputPath);	

			//get the output path via ServletContext method "getRealPath" (explanation at the end)
			outputPath = context.getRealPath("/files/output/") + "\\" + ruleName + ".owl";
			
			System.out.println("");	
			System.out.println("OutputPath-file-with-annotation-properties: " + outputPath);	
			
			//SWRL rule file --> SWRL rule file with annotations
			AnnotationPropertiesImplantator2 implantator = new AnnotationPropertiesImplantator2();
			implantator.action(inputPath, ruleName, outputPath);
		}
	}
	
	@GET
	@Path("/descriptionRDF")
	@Produces("application/rdf+xml")
	public String getDescription(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws Exception {
		//get the descriptions path via ServletContext method "getRealPath" (see explanation how does it work at the end)
		String descriptionsPath = context.getRealPath("/files/descriptions/") + "/";

		//choose the "LF_turtle.ttl" file from the folder with descriptions and output it as response to @GET @Path "/descriptionTTL"
		ServiceHelper.printRDFDescriptionFromFile(descriptionsPath + "SF_description.xml", servletResponse, context, "application/rdf+xml");
		return "";
	}
	
	@GET
	@Path("/descriptionHTML")
	@Produces(MediaType.TEXT_HTML)
	public String getHTMLDescription() {
		return "<html> " + "<title>" + "HTML Description Head" + "</title>"
	        + "<body><h1>" + "HTML Description Body" + "</body></h1>" + "</html> ";
	}
}

/* getting to the "http://localhost:8080/CognitiveApp/files/" via ServletContext.getRealPath
 * 
 ###################################################################################################################################################
 #  If launching Tomcat server via Eclipse this command leads us to                                                                                #
 #  D:\DiplArbeit\OurWork\Eclipse_workSPACE\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\CognitiveApp\files\                     #
 #  which is a copied from "real" Eclipse_workSPACE:                                                                                               #
 #  D:\DiplArbeit\OurWork\Eclipse_workSPACE\CognitiveApp\WebContent\files\                                                                         #
 #  by starting a server.                                                                                                                          #
 #  By reading operations it's all the same where to read from. The files are the same.                                                            #
 #                                                                                                                                                 #
 #  It causes small problems by writing operations, because we write in .metadata/... instead of "real" Eclipse workspace                          #
 #  So new created files will not be visible from Eclipse perspective (associated with "real" Eclipse workspace)                                   #
 #  but they are still accessible on "http://localhost:8080/CognitiveApp/files/" (which is associated with .metadata/...  #
 #                                                                                                                                                 #
 ###################################################################################################################################################
 
 ######################################################################################################
 #  If launching Tomcat server externally (via xampp) it leads us just to the tomcat folder           #
 #  "C:\xampp\tomcat\webapps\CognitiveApp\files\"                                                     #
 #  Access through "http://localhost:8080/CognitiveApp/files/" is possible and refers to this folder. #                                                       #
 ######################################################################################################
  
 With ServletContext.getRealPath imho we have no need in hardcoded paths in the localhost.properties
 But I am not sure about cwm and python pahts. This version assumes that they are executable in every 
 folder.
   		     
*/
