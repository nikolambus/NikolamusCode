package apTogether;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

public class ReasonRules2 {
	
	public static void main(String[] args) throws IOException, InterruptedException {

		//here we gonna store the name of the patient file
		//for example: String patient = "universal_patient_diff_dates.ttl";
		String patient = null;
		
		// define the patient file (input from user)
		System.out.println("Please enter your patient file");
		Scanner inputScanner = new Scanner(System.in);
		patient = inputScanner.nextLine();
		System.out.println("Your patient file is: " + patient);
		inputScanner.close();
		
		/*find applicable rules for a certain patient via a n3-rule "show_applicable_rules". 
		 * It consumes the patient data and outputs a helpfile which contains among others applicable rules names. */
		String commandShowRules = "cwm.py " + patient + " --filter=show_applicable_rules.n3 --n3=q > helpfile";
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("cmd /C" + commandShowRules);
		
		//Here we gonna store the obtained applicable rules
	    List<String> applicableRules = new ArrayList<String>();

	    //waiting till the file "helpfile" via cmd and cwm.py is created
	    Thread.sleep(1000);
	    
	    //reading a file line by line till the end of the file and picking out the rule names 
	    BufferedReader br = new BufferedReader(new FileReader("helpfile"));
	    String line;
	    String rule = null;
	    while ((line = br.readLine()) != null) {
	    	if (line.contains(":rule_")) {
	    		int ulineindex = line.lastIndexOf(':');
	    		rule = line.substring(ulineindex + 1, line.length()-1);
	    		if (rule.charAt(rule.length()-1) == ' ') 
	    			rule = rule.substring(0, rule.length()-1);
		    	
	    		//add the obtained rule name to the list of applicable rules
	    		applicableRules.add(rule);
	    	}
		}
	    br.close();
	    
	    //here our command will land
	    String commandExecuteRules = "";
	    
	    //here a list of applicable rules will land
    	PrintWriter writer = new PrintWriter("applicable_rules.txt", "UTF-8");
	    
	    //define the name of the result file (input from user)
	    String result = "resultPatient.ttl";
	    
	    //firstly determine the number of applicable rules
	    int rulesNum = applicableRules.size();
	    
	    //getting the workspace folder (actually the same thing as System.getProperty("user.dir") + patient)
	    Path patientPath = Paths.get(patient);	   
        File patientFile = patientPath.toFile();
        
        //if the specified file exists
        if (!patientFile.exists())
        	System.out.println("No such file in " + System.getProperty("user.dir"));
        else {
        	
    	    //copy patient data to the 0.all in order to get into the loop
        	Path pathTarget = Paths.get("0.all");
        	Files.copy(patientPath, pathTarget, StandardCopyOption.REPLACE_EXISTING);
	    
        	//now we start by the index 0 and create a chained command to execute all the rules subsequently  
        	//i - source patient index
        	int i=0;
        	while (i < rulesNum) {
	    	
        		//j - result patient index
        		int j=i+1;
        		commandExecuteRules = commandExecuteRules + "cwm.py " + i + ".all" + " --think=Rules_all/" + applicableRules.get(i) + "_all.n3" + " --n3=q > " + j + ".all" + " && cmd /C ";
        		
        		//parallel we add each rule in the file "applicable_rules.txt"
        		writer.println(applicableRules.get(i));        		
        		i++;
        	}
        	
        	//close writing into the file applicable_rules.txt
        	writer.close();

        	System.out.println(commandExecuteRules);
        	//this command goes through the list of the found rules and execute for each of them its ALL version on the given patient  
        	runtime.exec("cmd /C" + commandExecuteRules);

        	//waiting till cmd and cwm are ready
        	Thread.sleep(5000);
        	
        	//copy the last created file (result) to the specified result file
        	Path pathResultDigit = Paths.get(i + ".all");
        	Path pathResultName = Paths.get(result);
        	Files.copy(pathResultDigit, pathResultName, StandardCopyOption.REPLACE_EXISTING);
        	File resultFile = pathResultName.toFile();
        
        	Cleaner c = new Cleaner();
        		c.clean(resultFile, rulesNum);
        	System.out.println("");	
        	System.out.println("Success! You find applicable rules in \"applicable_rules.txt\" and the result patient after reasoning in \"" + result + "\"");
        }  	
	}
}

class Cleaner extends Thread {
	public synchronized void clean(File resultFile, int rulesNum) throws InterruptedException, IOException {
		
		//wait till the resultFile is created
		while (!resultFile.exists()) {
	            wait();
	        }
		 
		//afterwards remove all interim results
		
		//remove "helpfile"
		Path helpfilePath = Paths.get("helpfile");
    	Files.delete(helpfilePath);
  
    	//remove *.all
		int k=0;
     	while (k <= rulesNum) {
	    	Path helpPath = Paths.get(k + ".all");
	    	Files.delete(helpPath);
	    	k++;
	    }
     	notify();
	}
}
