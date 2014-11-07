package LFreasoner;
//import Query;
//import QueryExecution;
//import QuerySolution;
//import ResultSet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.http.HTTPException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;

@Path("/")
public class ReasonerService {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Path("/input")
	@Consumes("application/rdf+xml")
	public void postStuff(String rdf, @Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws IOException, SAXException {
		
		/* String rdf is read (via POST from Postman) and has a rdf/xml structure that should satisfy the pattern in Helper.getSparqlInputPattern()
		  a) check if it satisfies the SPARQL input pattern 
		  b) if yes, then extract 
		  - request
		  - patient
		  - rule
		  from the POST
		*/
		
		//Variables where we gonna store our request parameters
		String patient=null;
		String rule=null;
		String requestURI="";
		
		String sparql_prefixes = Helper.getSparqlPrefixesAsString();		
		String sparql_input_pattern = Helper.getSparqlInputPattern();
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
				//System.out.println(soln);		
				
				//Return the value of the named variable in this binding, casting to a Resource. 
				//This solution is a shorter alternative to the string-parsing-HandlerLF-solution. Here we do not need HandlerLF class at all.
				requestURI = soln.getResource("request").toString();
				rule = soln.getResource("ruleFile").toString();
				patient = soln.getResource("patientFile").toString(); 
			}
		}
		finally{
			qexec.close();
		}    
		
		//check
		System.out.println("Patient: " + patient);
		System.out.println("Rule: " + rule);
		System.out.println("Request URI: " + requestURI);	
						
		Runtime rt = Runtime.getRuntime();

		// Use the output path hardcoded in "localhost.properties" file 
		String outputPath = Helper.getProperties(context, "outputPath");
		
		String cmd = "cmd /C cwm.py " + patient + " --think=" + rule + " --n3=qd > " + outputPath + "result_patient.ttl";
		Process proc = rt.exec(cmd);		
	}
	
	@GET
	@Path("/descriptionTTL")
	@Produces("application/rdf+turtle")
	public String getDescription(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws Exception {
        
		//get the path to the folder which stores all our descriptions
		String descriptionsPath = Helper.getProperties(context, "descriptionsPath");
		
		//choose the "LF_turtle.ttl" file from the folder with descriptions and output it as response to @GET @Path "/descriptionTTL"
		Helper.printRDFDescriptionFromFile(descriptionsPath + "LF_turtle.ttl", servletResponse, context, "application/rdf+turtle");
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
