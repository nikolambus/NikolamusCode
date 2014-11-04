package cz.makub;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadFromFile {

	public static void main(String[] args) throws IOException {
		
		//we want to read the first four lines
		BufferedReader br = new BufferedReader(new FileReader("D:/DiplArbeit/OurWork/Ideen/RDF_Export+Rules/Export.rdf"));
		System.out.println(br.readLine());
		System.out.println(br.readLine());
		System.out.println(br.readLine());
		System.out.println(br.readLine());
		br.close();
		
        //--------------------------------------------------------------------
		// Method 1 how to read a file line by line till the end of the file |  
		//--------------------------------------------------------------------
		/* BufferedReader br = new BufferedReader(new FileReader("D:/DiplArbeit/OurWork/Ideen/RDF_Export+Rules/Export.rdf"));
        String line;
        while ((line = br.readLine()) != null) {
        	System.out.println(line);
        }
        br.close();
        */
		
		//-------------------------------------------------------------------------------------------------
		// Method 2 how to read a file line by line till the end of the file (for better chars resolving) |  
		//-------------------------------------------------------------------------------------------------
		
        /*FileInputStream fstream = new FileInputStream("D:/DiplArbeit/OurWork/Ideen/RDF_Export+Rules/Export.rdf");
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
        // Print the content on the console
        System.out.println (strLine);
        }
        //Close the input stream
        in.close();      */
	}

}
