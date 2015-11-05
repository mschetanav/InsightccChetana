import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class TweetCleaner {

	//Ideally there would be unit tests for file not found and parsing errors, but these are taken care of with the throws exceptions below
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		//to parse the JSON lines
		JSONParser parser = new JSONParser();
		
		//counts the number of tweets with unicode escaped
		int countUni = 0;
		
		//reading and writing into the specified files
		Scanner sc = new Scanner(new File("tweet_input/tweets.txt"));
		PrintWriter pwr = new PrintWriter("tweet_output/ft1.txt");
		
		//loops through the input and prints the output per line while counting the number of tweets with unicodes escaped
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			
			JSONObject json = (JSONObject) parser.parse(line);
			String created_at = (String) json.get("created_at");
			String text = (String) json.get("text");
			
			//sometimes there are lines that don't contain either of these. Just skip in such situations
			if(created_at==null || text==null) continue;
			
			if(text.matches(".*[^\\p{ASCII}].*")) 
				countUni++;
			text = text.replaceAll("[^\\p{ASCII}]", "")// removes the unicode asked to be removed
					//replaces escape characters as requested. The first two escape sequences are taken care of by the json reader, but left for completion.
						//.replaceAll("\\/", "/")
						//.replaceAll("\\\\", "\\")
						.replaceAll("\\'", "'")
						.replaceAll("\\\"", "\"")
						.replaceAll("\n", " ")
						.replaceAll("\t", " ")
						.replaceAll("\\s+", " ")
					;
			pwr.println(text+" (timestamp: "+created_at+")");	
		}
		
		//printing the last line
		pwr.println("\n"+countUni+" tweets contained unicode.");
		
		//closing everything 
		sc.close();
		pwr.close();
		
		System.out.println("Tweet cleaning complete.");
	}

}
