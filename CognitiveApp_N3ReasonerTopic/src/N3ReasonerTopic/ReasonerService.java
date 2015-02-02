package N3ReasonerTopic;
//import Query;
//import QueryExecution;
//import QuerySolution;
//import ResultSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;




import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
		String patient=null;
		String topic=null;
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
			topic = soln.getResource("topic").toString();
			patient = soln.getResource("patientFile").toString(); 
		
			//check
			System.out.println("Patient: " + patient);
			System.out.println("Topic: " + topic);
			
			//--------------------------------------------------------------------------------------------------
			//------------------------------------- handling patient -------------------------------------------
			// finding the name of patient file 
			String patientName;
			if (patient.contains("/")) {
				// we do not need the extension within a patient name
				if (patient.contains(".")) 
					patientName = patient.substring(patient.lastIndexOf("/")+1, patient.indexOf("."));
				else 
					patientName = patient.substring(patient.lastIndexOf("/")+1, patient.length());
			}
			else {
				// we do not need the extension within a patient name
				if (patient.contains(".")) 
					patientName = patient.substring(0, patient.indexOf("."));
				else 
					patientName = patient;
			}

			//check
			System.out.println("Patient name: " + patientName);
			
			//-----------------------------------------------------------------------------------------------
			//------------------------------------- handling topic ------------------------------------------
			
			String topicName;
			String topicPrefix;

			/* finding out the the prefix and the immediate topic name without a prefix. 
			 * E.g.: http://localhost/mediawiki/index.php/Special:URIResolver/bla -> 
			 * topicPprefix = http://localhost/mediawiki/index.php/Special:URIResolver/
			 * topicName  = bla
			 */
			if (topic.contains("/")) {
				topicPrefix = topic.substring(0, topic.lastIndexOf("/")+1);
				topicName = topic.substring(topic.lastIndexOf("/")+1, topic.length());
			}
			else { 
				topicPrefix = base;
				topicName = topic;
			}				
			
			//check
			//System.out.println("Topic Prefix: " + topicPrefix);
			//System.out.println("Topic name: " + topicName);
		
			//-------------------------------------------------------------------------------------			
			/* Now we should find the rule files which corresponds to the obtained topic. 
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
	       	  
	        // construct a query which would select swrl rules given a topic
	        String querystring2 = ServiceHelper.getSparqlPatternRulesForTopic(topicPrefix, topicName);

	        //check
	        System.out.println("Our SPARQL query: " + querystring2);
	        
	        // evaluate that query against our artificial triple store
	        List<QuerySolution> solutions = ServiceHelper.evaluationOfSPARQLQueryAgainstModelMulti(querystring2, model2);
	        
	        if (solutions.isEmpty()) 
				System.out.println("Could not find any swrl rule files for this topic: \"" + topic + "\"");
			else {
				
				//get the n3 rule file for each found rule and store it in a special list "stringSolutions"
				List<String> stringSolutions = new ArrayList<String>(); 
				for (QuerySolution solnRule : solutions) {
					String n3RuleFile = solnRule.getResource("n3RuleFile").toString();
					stringSolutions.add(n3RuleFile);					
					//check
					//System.out.println("n3RuleFile: " + n3RuleFile);
				}
				
				//remove duplicates from the stringSolutions list via transfer to a set, which does not allow duplicates
				Set<String> setStringSolutions = new LinkedHashSet<>(stringSolutions);
				stringSolutions.clear();
				stringSolutions.addAll(setStringSolutions);
				
				//check
				System.out.println("There were found following rule files for this topic: " + stringSolutions);
				
				//--------------------------------------------------------------------------------------
				// this part describes the immediate algorithm (in our case - the reasoner)
				
				//this variable will store our cwm command
				String cwmCommand = "cmd /C cwm.py " + patient; 
				
				//go through all rule files found by topic and add each rule to the rule command
	          	for (String rule : stringSolutions) {
					cwmCommand = cwmCommand + " --think=" + rule + " --purge";
	          	}

				//get the output path via ServletContext method "getRealPath" (see explanation how does it work at the end)
				String outputPath = context.getRealPath("/files/output/") + "/";	
				
				//complete the reasoning command
	          	cwmCommand = cwmCommand + " --n3=qd/ --purge > " + outputPath + patientName + "_new.ttl";
			
				//create the runtime environment to access cmd
				Runtime rt = Runtime.getRuntime();	
					
				//check				
				System.out.println("reasoning command: " + cwmCommand);
					
				//immediate reasoning
				Process proc = rt.exec(cwmCommand);
					
	          			
			}
		}
	}
	
	@GET
	@Path("/descriptionTTL")
	@Produces("application/rdf+turtle")
	public String getDescription(@Context final HttpServletResponse servletResponse, @Context final HttpServletRequest servletRequest, @Context final ServletContext context) throws Exception {

		//get the descriptions path via ServletContext method "getRealPath" (see explanation how does it work at the end)
		String descriptionsPath = context.getRealPath("/files/descriptions/") + "/";

		//choose the "LF_turtle.ttl" file from the folder with descriptions and output it as response to @GET @Path "/descriptionTTL"
		ServiceHelper.printRDFDescriptionFromFile(descriptionsPath + "LF_turtle.ttl", servletResponse, context, "application/rdf+turtle");
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

