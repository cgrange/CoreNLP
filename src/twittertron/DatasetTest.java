package twittertron;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.*;

public class DatasetTest {
	
	static String readFile(String path, Charset encoding) throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
	}
	
	private static void testDataset() throws IOException{
		PrintWriter check;
		check = new PrintWriter("check.txt");
		String datasetStr = readFile("instances2.txt", StandardCharsets.UTF_8);
		JsonArray instances = new JsonParser().parse(datasetStr).getAsJsonArray();
		for(int i = 0; i < instances.size(); i++){
			JsonObject instance = instances.get(i).getAsJsonObject();
			check.println(instance.get("label"));
		}
		check.close();
	}
	
	public static void main(String args[]){
		try {
			testDataset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
