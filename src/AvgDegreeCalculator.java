import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



class Graph{
	//stores nodes, edges and their timestamps respectively
	ArrayList<String> nodes = new ArrayList<>();
	HashMap<String, HashSet<String>> edges = new HashMap<>();
	
	//need to make sure timestamp is replaced with the latest version using has value
	HashMap<String, LocalDateTime> edgeTimeStamps = new HashMap<>();
	//HashMap<LocalDateTime, String> timeStampsEdges = new HashMap<>();//use this for efficiency
	//convenience datastructure for searching from beginning
	ArrayList<LocalDateTime> timeStamps = new ArrayList<>();
	
	//stores latest time
	LocalDateTime latestTime;
	
	public String avgDegree() {
		float sum=0;
		for (String node : this.nodes) {
			sum+=this.edges.get(node).size();
		}

		//default score when the graph is empty is 0
		float avg=nodes.isEmpty()?0:sum/nodes.size();
		
		return String.format("%.2f", avg);
	}
}


public class AvgDegreeCalculator {

	public static void main(String[] args) throws FileNotFoundException, ParseException {
		Graph g = new Graph();
		
		
		//to parse the JSON lines
		JSONParser parser = new JSONParser();

		//reading and writing into the specified files
		Scanner sc = new Scanner(new File("tweet_input/tweets.txt"));
		PrintWriter pwr = new PrintWriter("tweet_output/ft2.txt");

		//loops through the input and prints the output per line while counting the number of tweets with unicodes escaped
		while(sc.hasNextLine()){
			String line = sc.nextLine();

			JSONObject json = (JSONObject) parser.parse(line);
			String created_at = (String) json.get("created_at");
			String text = (String) json.get("text");

			//sometimes there are lines that don't contain either of these. Just skip in such situations
			if(created_at==null || text==null){
				pwr.println(g.avgDegree());
				continue;
			}
			
			text = text.replaceAll("[^\\p{ASCII}]", "") // removes the unicode asked to be removed
					//replaces escape characters as requested. The first two escape sequences are taken care of by the json reader, but left for completion.
					//.replaceAll("\\/", "/")
					//.replaceAll("\\\\", "\\")
					.replaceAll("\\'", "'")
					.replaceAll("\\\"", "\"")
					.replaceAll("\n", " ")
					.replaceAll("\t", " ")
					.replaceAll("\\s+", " ")
					;
			
			//extracting hashtags and storing unique hastags into a list
			ArrayList<String> hashTags = new ArrayList<>();
			for (String string : text.split(" |, |\\.")) {
				if(string.startsWith("#") && !hashTags.contains(string))
					hashTags.add(string);
			}
			
			if(!hashTags.isEmpty()){
				//System.out.println(hashTags+" (timestamp: "+created_at+")");
				
				DateTimeFormatter f = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss +0000 yyyy");//created the pattern based on the examples for data (Thu Oct 29 17:51:01 +0000 2015)
				LocalDateTime dateTime = LocalDateTime.from(f.parse(created_at));
				
				//updating latest time
				g.latestTime = dateTime;
				g.timeStamps.add(dateTime);
				
				//updating nodes
				for (String string : hashTags) {
					if(!g.nodes.contains(string)){
						g.nodes.add(string);
						g.edges.put(string, new HashSet<>());
					}
				}
				
				//remove nodes and edges based on time lapse
				LocalDateTime latestMinus60 = g.latestTime.minusSeconds(60);
				HashSet<LocalDateTime> timeStampsToRemove = new HashSet<>();
				for(LocalDateTime time: g.timeStamps){
					if(time.isBefore(latestMinus60))
						timeStampsToRemove.add(time);
					else
						break;//because the timestamps are in order. Saves visiting all the times
				}
				if(!timeStampsToRemove.isEmpty()){//most of the times it is not empty. so, it will save some time
					g.timeStamps.removeAll(timeStampsToRemove);
					for(String edge: g.edgeTimeStamps.keySet()){
						if(timeStampsToRemove.contains(g.edgeTimeStamps.get(edge))){
							String[] nodes = edge.split("___");
							g.edges.get(nodes[0]).remove(nodes[1]);//reverse is also true, but that gets executed with the reverse edge
							if(g.edges.get(nodes[0]).isEmpty()){
								g.nodes.remove(nodes[0]);//saves space
								g.edges.remove(nodes[0]);
							}
						}
					}
				}
				
				
				
				for(int i=0;i<hashTags.size();i++){
					for(int j=i+1;j<hashTags.size();j++){
						g.edges.get(hashTags.get(i)).add(hashTags.get(j));//it's a set. so, there are no duplicates
						g.edges.get(hashTags.get(j)).add(hashTags.get(i));
						g.edgeTimeStamps.put(hashTags.get(i)+"___"+hashTags.get(j), dateTime);//the time stamp gets updated, if it is not already present
						g.edgeTimeStamps.put(hashTags.get(j)+"___"+hashTags.get(i), dateTime);
						
						// use reverse map for faster search (O(1) instead of O(n))
						
					}
				}

				
			}
			
			pwr.println(g.avgDegree());
			//System.out.println();
		}


		//closing everything 
		sc.close();
		pwr.close();


		System.out.println("Average degree calculation complete.");
	}

}
