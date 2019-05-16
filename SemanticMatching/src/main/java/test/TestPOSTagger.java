package test;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TestPOSTagger {
	
	public static void main(String[] args) {
	
	MaxentTagger tagger = new MaxentTagger("./files/taggers/english-left3words-distsim.tagger");
	
	String sample = "This is a sample text";
	
	String tagged = tagger.tagString(sample);
	
	System.out.println(tagged);

	}
}
