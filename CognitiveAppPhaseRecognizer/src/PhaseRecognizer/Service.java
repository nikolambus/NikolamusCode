package PhaseRecognizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
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

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
		String phaseContextURI=null;
		String OPType=null;
		String requestURI="";
		
		//Base URI (in case of SFB should be changed to Surgipedia)
		String base = "http://localhost/mediawiki/index.php/Special:URIResolver/";
		
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
		in.close();
	
		QuerySolution soln = ServiceHelper.evaluationOfSPARQLQueryAgainstModel(querystring, model);
	
		if (soln==null) 
			System.out.println("Input SPARQL pattern has not been satisfied.");
		else {
			//Return the value of the named variable in this binding, casting to a Resource. 
			//This solution is a shorter alternative to the string-parsing-HandlerLF-solution. Here we do not need HandlerLF class at all.
			requestURI = soln.getResource("request").toString();
			phaseContextURI = soln.getResource("phaseContextURI").toString(); 
			OPType = soln.getResource("OPType").toString();
			
			//check
			System.out.println("Context: " + phaseContextURI);
			System.out.println("OP-Type: " + OPType);
		
			//-------------------------------------------------------------------------------------			
			/* Now we should find the rules files which corresponds to the obtained OPType. 
			 *  Therefore we evaluate another query over the global triple store.
			 *  In case of localhost we just add some RDF/XML files to the model. 
			 *  In case of server we should add the RDF export of the SFB Triple Store. Smth. like this: 
			 *  
			 *  // loading triples from SFB Triple Store 
	        	//model2.read("http://aifb-ls3-vm2.aifb.kit.edu/rdfData/surgipediaExport.owl", "RDF/XML");
	        	//model2.read("http://aifb-ls3-vm2.aifb.kit.edu/rdfData/rdfData.txt", "N-TRIPLE");
			 *
			 */
			
			Model model2 = ModelFactory.createDefaultModel();
			
			//loading local files and triples form them  (using them as a local triple store)
		    model2.read("http://localhost:8080/CognitiveApp6/files/output/rule_CHILDPUGH.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp6/files/output/rule_SF.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp6/files/output/rule_FONG.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp6/files/output/rule_RINGSELECTION.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RulePancreas1.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RulePancreas2.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RulePancreas3.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RulePancreas4.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RulePancreas5.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RuleCCE1.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RuleCCE2.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/RuleCCE3.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/TestRule0.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/TestRule1.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/TestRule2.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/TestRule3.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp2/files/output/TestRule4.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp4b/files/output/ContextPancreas1.owl", "RDF/XML");
	        model2.read("http://localhost:8080/CognitiveApp4b/files/output/ContextCCE1.owl", "RDF/XML");

	        //-------------------------------------------------------------------------------------
	        //------------------------------------Handling the phaseContext------------------------
	        
	        String phaseContextName;
			String phaseContextPrefix;
			/* finding out the prefix and the immediate phaseContextURI name without a prefix. 
			 * E.g.: http://localhost/mediawiki/index.php/Special:URIResolver/ContextPancreas1 -> 
			 * phaseContextPrefix = http://localhost/mediawiki/index.php/Special:URIResolver/
			 * phaseContextName = ContextPancreas1
			 */
			if (phaseContextURI.contains("/")) {
				phaseContextPrefix = phaseContextURI.substring(0, phaseContextURI.lastIndexOf("/")+1);
				phaseContextName = phaseContextURI.substring(phaseContextURI.lastIndexOf("/")+1, phaseContextURI.length());
			}
			else { 
				phaseContextPrefix = base;
				phaseContextName = phaseContextURI;
			}				        
	        
	        // construct a query which would find ABoxes file given ABoxes URI
	        String querystring1 = ServiceHelper.getSparqlPatternForPhaseContextFile(phaseContextPrefix, phaseContextName);
	        QuerySolution soln1 = ServiceHelper.evaluationOfSPARQLQueryAgainstModel(querystring1, model2);
	        if (soln1 == null) {
				System.out.println("Could not find the phase context owl file from phase context URI \"" + phaseContextURI + "\"");
			}
	        else {
	        	String phaseContextFile = soln1.getResource("phaseContextFile").toString();
				System.out.println("phaseContextFile: " + phaseContextFile);	     
				
				//-------------------------------------------------------------------------------------			
				//---------------------------- now handling the OPType--------------------------------- 
				
		        String OPTypeName;
				String OPTypePrefix;
				
				/* finding out the prefix and the immediate OPType name without a prefix. 
				 * E.g.: http://localhost/mediawiki/index.php/Special:URIResolver/PancreasResection -> 
				 * OPTypePrefix = http://localhost/mediawiki/index.php/Special:URIResolver/
				 * OPTypeName  = PancreasResection
				 */
				if (OPType.contains("/")) {
					OPTypePrefix = OPType.substring(0, OPType.lastIndexOf("/")+1);
					OPTypeName = OPType.substring(OPType.lastIndexOf("/")+1, OPType.length());
				}
				else { 
					OPTypePrefix = base;
					OPTypeName = OPType;
				}				
				
				//check
				//System.out.println("OPType Prefix: " + OPTypePrefix);
				//System.out.println("OPType name: " + OPTypeName);
				//System.out.println("Request URI: " + requestURI);	
		        
		        // construct a query which would select swrl rules files given OPType
		        String querystring2 = ServiceHelper.getSparqlPatternRulesForOPType(OPTypePrefix, OPTypeName);
		        
		        //check
		        //System.out.println("SPARQL query looking for rules corresponding to a specific OPType: " + querystring2);

		        // evaluate that query against our artificial triple store
		        List<QuerySolution> solutions = ServiceHelper.evaluationOfSPARQLQueryAgainstModelMulti(querystring2, model2);
		        
		        if (solutions.isEmpty()) 
					System.out.println("Could not find any swrl rules file for this OPType: \"" + OPType + "\"");
		        else {
		        	
		        	//false by default, true if phase detection has been performed
		        	Boolean phaseDetected = false;
		        	for (QuerySolution solnRule : solutions) {
				        String swrlRuleFile = solnRule.getResource("swrlRuleFile").toString();
						System.out.println("swrlRuleFile: " + swrlRuleFile);	     
						
						
						//--------------------------------------------------------------------------------------
						// this part describes the immediate reasoning 
					
						//get the output path via ServletContext method "getRealPath" (see explanation how does it work at the end)
						String outputPath = context.getRealPath("/files/output/") + "\\" + phaseContextName + "_new.owl";
						
						//perform reasoning
						Reasoner reasoner = new Reasoner();
						//if some DetectedPhase has been obtained quit the loop, else try the next rule for this context
						phaseDetected = reasoner.action(swrlRuleFile, phaseContextFile, phaseContextURI, OPTypeName, outputPath);
						if (phaseDetected)
							break;
					}
		        	if (!phaseDetected)
		        		System.out.println("No phase could be detected.");
		        }	
	        }   
		}
	}
	
	/*
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
	*/
	
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
