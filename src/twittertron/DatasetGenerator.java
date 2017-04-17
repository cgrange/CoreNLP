package twittertron;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.*;

public class DatasetGenerator {
	
	static String url = "http://54.183.211.5/";
	static String charset = StandardCharsets.UTF_8.name();
	static PrintWriter out = new PrintWriter(System.out);
	static PrintWriter instancesFile;
	static PrintWriter pagesFile;
	static DocumentSentimentAnalysis dsa;
	
	private static String getPage(int pageNum) throws IOException{
		String query = String.format("page=%s", URLEncoder.encode(String.valueOf(pageNum), charset));
		URLConnection connection = new URL(url + "?" + query).openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		InputStream response = connection.getInputStream();
		Scanner scanner = new Scanner(response);
	    String responseBody = scanner.useDelimiter("\\A").next();
	    //out.println(responseBody);
		response.close();
		return responseBody;
	}	
	
	private static Map<String, Boolean> getFollowingMap(JsonObject userObj, Set<String> companies){
		//System.out.println(userObj.toString());
		JsonArray followees = userObj.get("twitter:user/followees").getAsJsonArray();
		//System.out.println(followees.toString());
		Map<String, Boolean> followingMap = new HashMap<String, Boolean>();
		for(String company : companies){
			for(int i = 0; i < followees.size(); i++){
				String screen_name = followees.get(i).getAsJsonObject().get("twitter:user/screen-name").getAsString();
				if(screen_name.toLowerCase().equals(company)){
					followingMap.put(company, true);
				}
			}
			if(!followingMap.containsKey(company)){
				followingMap.put(company, false);
			}
		}
		return followingMap;
	}
	
	private static JsonObject getLabel(Set<String> companies, JsonObject userObj){
		JsonObject label = null;
		try {
			label = dsa.getLabel(companies, getFollowingMap(userObj, companies));
			//System.out.println(label.toString());
		} catch (IOException e) {
			//System.out.println("GOT HEEM! line 62 of dataset Generator");
			e.printStackTrace();
		}
		return label;
	}
	
	private static JsonObject generateInstance(JsonArray userToTweets, JsonObject userObj, Set<String> companies){
		JsonObject instance = new JsonObject();
		TwitterParser tParser = new TwitterParser(companies);
		tParser.parseUser(userToTweets);
		JsonObject label = getLabel(companies, userObj);
		//System.out.println("label: " + label.toString());
		instance.add("user", userObj);
		instance.add("label", label);
		
		return instance;
	}
	
	// {"tweets-list": [[<user-id> [<tweet> ...]], "users-map": {<user-id>: <user> ...}}
	// each page has 100 users
	private static JsonArray pageToInstances(String page, Set<String> companies){
		JsonObject jsonPage = new JsonParser().parse(page).getAsJsonObject();
		JsonArray usersToTweets = jsonPage.get("tweets-list").getAsJsonArray();
		JsonObject users_map = jsonPage.get("users-map").getAsJsonObject();
		JsonArray instances = new JsonArray();
		for(int i = 0; i < usersToTweets.size(); i++){
	    	JsonArray userToTweets = usersToTweets.get(i).getAsJsonArray();
	    	//System.out.println("userToTweets " + i + ": " + userToTweets.toString());
	    	BigInteger uid = userToTweets.get(0).getAsBigInteger();
	    	JsonObject userObj = users_map.get(uid.toString()).getAsJsonObject();
	    	//System.out.println("userObj " + i + " " + userObj.toString());
	    	if(userObj.get("twitter:user/language").getAsString().equals("en")){
	    		JsonObject instance = generateInstance(userToTweets, userObj, companies);
	    		System.out.println("instance: " + i + " " + instance.toString());
		    	instances.add(instance);
	    	}
	    	System.out.println("done with " + i);
	    }
		return instances;
	}
	
	public static void main(String[] args){
		dsa = new DocumentSentimentAnalysis();
		try {
			instancesFile = new PrintWriter("instances.txt");
			instancesFile.print("[ ");
//			pagesFile = new PrintWriter("pages.txt");
//			pagesFile.print("testing 1, 2");
		} catch (FileNotFoundException e1) {
			System.out.println("NO!!! the instances.txt file could not open");
			e1.printStackTrace();
		}
		Set<String> companies = new HashSet<String>();
		String[] companiesArr = {"apple", "android", "nike", "microsoft", "xbox", "playstation"};
		for(String company : companiesArr){
			companies.add(company);
		}
		try {
			int page = 0;
			int numPages = 100;
			while(page < numPages){
				if(page == 3){
					page = 4;
				}
				JsonArray instances = new JsonArray();
				
				/*
				
				For Java, you can call `java.util.concurrent.Threadpools.newFixedThreadpool(...)`
				and then submit as many `java.util.concurrent.Future`s to it as you want concurrent threads. 
				Inside of each `Future` you make the HTTP call and `.add` the result to a 
				`java.util.concurrent.ConcurrentHashMap` with the key as the page number
				Should work nicely
				 
				 */
				String pageStr = getPage(page);
				
				instances = pageToInstances(pageStr, companies);
				for(int i = 0; i < instances.size(); i++){
					JsonObject instance = instances.get(i).getAsJsonObject();
					instancesFile.print(instance.toString());
					if(i == instances.size()-1 && page == numPages-1){
						//don't print comma
						break;
					}
					else{
						instancesFile.print(", ");
					}
				}
				page++;
			}	
			instancesFile.print(" ] ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		instancesFile.close();
	}
	
}
