package twittertron;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TwitterParser {
	Set<BigInteger> users;
	Set<String> companies;
	Map<String, PrintWriter> writers;
	
	public TwitterParser(Set<String> companies){
		this.companies = companies;
		users = new HashSet<BigInteger>();
	}
	
	private String getCompany(String tweetText){
		for(String companyName : companies){
			if(tweetText.toLowerCase().contains(companyName)){
				return companyName;
			}
		}
		return null;
	}
	
	private void initFiles(){
		writers = new HashMap<String, PrintWriter>();
		for(String company : companies){
			try{
			    PrintWriter writer = new PrintWriter(company + ".txt", "UTF-8");
			    writers.put(company, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void closeFiles(){
		for(String key : writers.keySet()){
			PrintWriter pw = writers.get(key);
			pw.close();
		}
	}
	
	private void assignToFile(JsonObject tweet){
		String tweetText = tweet.get("twitter:tweet/text").getAsString();
		// IDK if I'll make use of these next two, I could get the tweet id as well but can't think of a use for it
		String language = tweet.get("twitter:tweet/language").getAsString();
		boolean isStatusTweet = tweet.get("twitter:tweet/quote-status?").getAsBoolean();
		
		String company = getCompany(tweetText);
		if(company != null){
			PrintWriter writer = writers.get(company);
			writer.println(tweetText);
		}
	}
	
//	
//	public void parsePage(String pageBody){
//		JsonObject page = new JsonParser().parse(pageBody).getAsJsonObject();
//	    JsonArray usersToTweets = page.get("tweets-list").getAsJsonArray();
//	    
//	}
	
	//[ <user-id>, [ {tweet}, {tweet}, .... ] ]
	public void parseUser(JsonArray userToTweets) {
		initFiles();
		//BigInteger uid = userToTweets.get(0).getAsBigInteger();
		JsonArray tweets = userToTweets.get(1).getAsJsonArray();
		for(int i = 0; i < tweets.size(); i++){
			JsonObject tweet = tweets.get(i).getAsJsonObject();
			assignToFile(tweet);
		}
		closeFiles();
	}
	
}
