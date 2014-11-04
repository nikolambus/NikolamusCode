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

//	@GET
//	public String sendRedirectToServiceDescription(@Context final HttpServletResponse response) throws Exception {
//		response.setHeader("Location", "http://141.52.218.34:8080/SFBPrototyp/meanfree/description");
//		response.sendError(HttpServletResponse.SC_SEE_OTHER);
//		return "";
//	}

	@POST
	@Path("/input")
	@Consumes("application/rdf+xml")
	public void postStuff(String rdf, @Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws IOException, SAXException {
		
		//Example of CURL POST!!!
		//curl -H "Content-Type:application/rdf+xml" -X POST -d '/home/schoch/HiFlow3_Project/applications/ElastSimPrototype/ElasticitySimulation/elasticity_Bunny_Scenario_DirBC.xml' http://localhost:8080/MVR-SAS/input
		
		/* STEP5 */
		/*String rdf is read (via POST from Postman) and has a rdf/xml structure like
		
			<rdf:RDF 
				xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
				xmlns:lf="http://localhost:8080/CognitiveApp/files/ontologies/lf#">
	
				<rdf:Description rdf:about="http://www.example.com/Request_01_Example">
					<lf:hasPatient>universal_patient.ttl</lf:hasPatient>
					<lf:hasRule>rule_SF.n3</lf:hasRule>
				</rdf:Description>
			</rdf:RDF>
		
		  a) parse this String with HandlerLF.java, extract RequestData and forward them to  
		  b) cwm command execution
		*/
	
		/*
		System.out.println(rdf);
		
		//we need BufferedReader for more convenient parsing of our String rdf (see getRequestData method)
		BufferedReader input = new BufferedReader(new StringReader(rdf));
		RequestDataLF requestdata = getRequestData(input);
		
		//Variables where we gonna store our request parameters
		String patient = requestdata.getPatient();
		String rule = requestdata.getRule();
		String requestURI = requestdata.getRequestURI();
		
		*/
		
		/* STEP6 */
		/*String rdf is read (via POST from Postman) and has a rdf/xml structure like in STEP5
		  a) check if it satisfies the SPARQL input pattern 
		  b) if yes, pipe it to the HandlerLF
		*/
		
		//Variables where we gonna store our request parameters
		String patient=null;
		String rule=null;
		String requestURI="";
		
		String sparql_prefixes = Helper.getSparqlPrefixesAsString();		
		String sparql_input_pattern = Helper.getSparqlInputPattern();
		String querystring = sparql_prefixes + sparql_input_pattern;
		System.out.println(querystring);
		
		//Encodes this String into a sequence of bytes
		InputStream in = new ByteArrayInputStream(rdf.getBytes());
		
		//Answer a fresh rdf Model with the default specification.
		Model model = ModelFactory.createDefaultModel();
		
	   /* Add statements from a document. This method assumes the concrete syntax is RDF/XML
		* Parameters:
		* in - the input stream
		* "" - the base uri to be used when converting relative URI's to absolute URI's.
		* xml:base="http://example.org/here/"> + rdf:resource="fruit/apple" --> http://example.org/here/fruit/apple
		* if the base is the empty string, then relative URIs will be retained in the model.
		*/
		model.read(in, "");
		
		// Close the Model and free up resources held.
		in.close();// Create a SPARQL query from the given string.
		Query query = QueryFactory.create(querystring);

		// Create a QueryExecution to execute over the Model.
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			
			// Results from a query in a table-like manner for SELECT queries. Each row corresponds to a set of bindings which fulfil the conditions of the query. Access to the results is by variable name.
			ResultSet results = qexec.execSelect();
					
			System.out.println("CHUNGA CHANGA!!!!!!!!!!!");			
			// results.hasNext() is false!!!!
			while(results.hasNext()){
				 
				//QuerySolution -- A single answer from a SELECT query.
				//results.nextSolution() -- Moves on to the next result
				QuerySolution soln = results.nextSolution();

				//Return the value of the named variable in this binding, casting to a Resource.
				requestURI = soln.getResource("request").toString();

				System.out.println(soln);		
				rule = soln.getResource("ruleFile").toString();
				patient = soln.getResource("patientFile").toString(); 
			}
		}
		finally{
			qexec.close();
		}    
		
		System.out.println("Patient: " + patient);
		System.out.println("Rule: " + rule);
		System.out.println("Request URI: " + requestURI);	
						
		Runtime rt = Runtime.getRuntime();
		
		/* Solution 1 (right idea, but..)*/
		/* does not work, because Eclipse finds the Eclipse folder and not the workspace folder */
		/* String currDir = System.getProperty("user.dir");
		String cmd = "cmd /C cwm.py " + patient + " --think=" + rule + " > " + currDir + "/files/output/result_patient.ttl";
		*/

		/* Solution 2 (not working) */
		/* without localhost.properties file, just context */
		// String outputPath = context.getRealPath("/files/output/result_patient.ttl").toString();
		
		/* Solution 3 (safe, but cheatty)*/
		/* finding workspace with help of context, and outputPath has been specified in the localhost.properties file */
		String outputPath = Helper.getProperties(context, "outputPath");
		String cwmPath = Helper.getProperties(context, "cwmPath");
		
		String cmd = "cmd /C" + cwmPath + " cwm.py " + patient + " --think=" + rule + " > " + outputPath + "result_patient.ttl";
		Process proc = rt.exec(cmd);		
	}
	
	/*
	@GET
	@Path("/output")
	@Produces("application/rdf+xml")
	public String getOutput(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) {
        
		return "output a ex:Output;" +
				"      hasMsg 'Execute first'.";
	}*/
	
	@GET
	@Path("/descriptionTTL")
	@Produces("application/rdf+turtle")
	public String getDescription(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws Exception {
        //curl -H "accept:application/rdf+xml" http://localhost:8080/MVR-SAS/description
		Helper.printRDFDescriptionFromFile("/files/descriptions/lf", servletResponse, context, "application/rdf+turtle");
		return "";
	}
	
	@GET
	@Path("/descriptionHTML")
	@Produces(MediaType.TEXT_HTML)
	public String getHTMLDescription() {
		return "<html> " + "<title>" + "HTML Description Head" + "</title>"
	        + "<body><h1>" + "HTML Description Body" + "</body></h1>" + "</html> ";
	}
	
	/*
	 * Necessary for STEP 5 
	//Here parsing of POSTed String rdf takes place
	public RequestDataLF getRequestData (BufferedReader br) throws SAXException, IOException {
		
		//Our implementation of the abstract DefaultHandler Interface - for handling with XML files
		HandlerLF handler = new HandlerLF();
		
		//Simple API XML Parser
		SAXParser parser = new SAXParser();
		
		//supplied with our DefaultHandler (similar to ContentHandler)
		parser.setContentHandler(handler);
		
		//do parsing
		parser.parse(new InputSource(br));
		return new RequestDataLF(handler.getPatient(), handler.getRule(), handler.getRequestURI());
	}	
	*/
}
