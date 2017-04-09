
import java.io.*;
import java.util.*;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class DocumentSentimentAnalysis {

  /** Usage: java -cp "*" StanfordCoreNlpDemo [inputFile [outputTextFile [outputXmlFile]]] */
  public static void main(String[] args) throws IOException {
    // set up optional output files
    PrintWriter out;
    if (args.length > 1) {
      out = new PrintWriter(args[1]);
    } else {
      out = new PrintWriter(System.out);
    }
    PrintWriter xmlOut = null;
    if (args.length > 2) {
      xmlOut = new PrintWriter(args[2]);
    }

    // Add in sentiment
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");

    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
    Annotation annotation;
    if (args.length > 0) {
      annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
    } else {
      annotation = new Annotation("Colton went to the mall");
    }

    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
    if (sentences != null && ! sentences.isEmpty()) {
    	double docSentiment = 0;
    	for(CoreMap sentence : sentences){
    		double sentimentScore = getSentimentScore(sentence);
    		docSentiment += sentimentScore;
    		out.println("sentiment score: " + sentimentScore);
    		out.println(sentence.get(SentimentCoreAnnotations.SentimentClass.class) + ": " + sentence);
    	}
    	docSentiment = docSentiment/sentences.size();
    	out.println("document sentiment: " + docSentiment);
    }
    IOUtils.closeIgnoringExceptions(out);
    IOUtils.closeIgnoringExceptions(xmlOut);
  }
  
  /**
	 * gets the sentiment score for that sentence where -2 is very negative and 2 is very positive
	 * @param sentence
	 * @return the sentiment score for the sentence
	 */
	private static double getSentimentScore(CoreMap sentence) {
		if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Very negative")){
			return -2;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Negative")){
			return -1;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Neutral")){
			return 0;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Positive")){
			return 1;
		}
		else if(sentence.get(SentimentCoreAnnotations.SentimentClass.class).equals("Very positive")){
			return 2;
		}
		else{
			return 0;
		}
	}

}

