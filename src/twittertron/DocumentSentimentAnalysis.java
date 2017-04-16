package twittertron;

import java.io.*;
import java.util.*;

import com.google.gson.JsonObject;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class DocumentSentimentAnalysis {

	public static JsonObject getLabel(Set<String> companies, Map<String, Boolean> followingMap) throws IOException {
		JsonObject label = new JsonObject();
		PrintWriter out = new PrintWriter(System.out);
		
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		for(String company : companies){
			Annotation annotation;
			annotation = new Annotation(IOUtils.slurpFileNoExceptions(company + ".txt"));
			pipeline.annotate(annotation);
			
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			if (sentences != null && ! sentences.isEmpty()) {
				//System.out.println("file has sentence(s)");
				double docSentiment = 0;
				for(CoreMap sentence : sentences){
					double sentimentScore = getSentimentScore(sentence);
					docSentiment += sentimentScore;
				}
				docSentiment = docSentiment/sentences.size();
				docSentiment *= 1.5;
				if(followingMap.get(company)){
					docSentiment *= 2;
				}
				label.addProperty(company + "_sentiment", docSentiment);
			}
			else{
				//System.out.println("empty file");
				if(followingMap.get(company)){
					label.addProperty(company + "_sentiment", 1);
				}
				else{
					label.addProperty(company + "_sentiment", 0);
				}
					
			}
		}
		//System.out.println("in dsa label: " + label.toString());
		//IOUtils.closeIgnoringExceptions(out);
		return label;
	}
  
  /**
	 * gets the sentiment score for that sentence where -2 is very negative and 2 is very positive
	 * @param sentence
	 * @return the sentiment score for the sentence
	 */
	private static double getSentimentScore(CoreMap sentence) {
		double score;
		if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Very negative")){
			score = -2;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Negative")){
			score = -1;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Neutral")){
			score = 0;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Positive")){
			score = 1;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Very positive")){
			score = 2;
		}
		else{
			score = 0;
		}
		//System.out.println("Sentiment for sentence: " + score);
		return score;
	}

}

