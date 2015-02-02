package N3RingReasoner;
//import Query;
//import QueryExecution;
//import QuerySolution;
//import ResultSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;




import java.net.URL;

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

import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;

import org.mindswap.pellet.jena.PelletReasonerFactory;

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
		String patientFile=null;
		String ruleFile=null;
		String requestURI="";
		
		//Base URI (in case of SFB should be changed to Surgipedia)
		String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
		
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
		in.close();
	
		QuerySolution soln = Helper.evaluationOfSPARQLQueryAgainstModel(querystring, model);
	
		if (soln==null) 
			System.out.println("Input SPARQL pattern has not been satisfied.");
		else {
			//Return the value of the named variable in this binding, casting to a Resource. 
			//This solution is a shorter alternative to the string-parsing-HandlerLF-solution. Here we do not need HandlerLF class at all.
			requestURI = soln.getResource("request").toString();
			ruleFile = soln.getResource("ruleFile").toString();
			patientFile = soln.getResource("patientFile").toString(); 
		
			//check
			System.out.println("Patient File: " + patientFile);
			System.out.println("Rule File: " + ruleFile);
			
				//--------------------------------------------------------------------------------------
				// this part describes the immediate reasoning  
				
				Runtime rt = Runtime.getRuntime();
			
				//get the output path via ServletContext method "getRealPath" (see explanation how does it work at the end)
				String outputPath = context.getRealPath("/files/output/") + "/";
			
				// finding the name of patient file 
				String patientName;
				if (patientFile.contains("/")) {
					// we do not need the extension within a patient name
					if (patientFile.contains(".")) 
						patientName = patientFile.substring(patientFile.lastIndexOf("/")+1, patientFile.indexOf("."));
					else 
						patientName = patientFile.substring(patientFile.lastIndexOf("/")+1, patientFile.length());
				}
				else {
					// we do not need the extension within a patient name
					if (patientFile.contains(".")) 
						patientName = patientFile.substring(0, patientFile.indexOf("."));
					else 
						patientName = patientFile;
				}
				
				String cmd = "cmd /C cwm.py " + patientFile + " --think=" + ruleFile + " --n3=qd/ --purge > " + outputPath + patientName + "_new.ttl";
				System.out.println("reasoning command: " + cmd);
				Process proc = rt.exec(cmd);		
			
		}
	}
	
	@GET
	@Path("/descriptionTTL")
	@Produces("application/rdf+turtle")
	public String getDescription(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws Exception {

		//get the descriptions path via ServletContext method "getRealPath" (see explanation how does it work at the end)
		String descriptionsPath = context.getRealPath("/files/descriptions/") + "/";

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

