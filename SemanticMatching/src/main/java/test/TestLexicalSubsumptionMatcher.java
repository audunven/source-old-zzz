package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import net.didion.jwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import utilities.LexicalConcept;
import utilities.SimpleRelation;
import utilities.StringUtilities;
import utilities.WordNet;

public class TestLexicalSubsumptionMatcher {

	public static void main(String[] args) throws FileNotFoundException, JWNLException, IOException, AlignmentException, net.sf.extjwnl.JWNLException {

		//String referenceAlignmentPath = "./files/HarmonyAlignmentLexSubMatcher.rdf";
		String referenceAlignmentPath = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-SUBSUMPTION.rdf";

		AlignmentParser parser = new AlignmentParser();		
		URIAlignment referenceAlignment = (URIAlignment) parser.parse(new File(referenceAlignmentPath).toURI().toString());

		String s = null;
		String t = null;

		for (Cell c : referenceAlignment) {

			s = WordNet.getLexicalName(c.getObject1AsURI().getFragment());
			t = WordNet.getLexicalName(c.getObject2AsURI().getFragment());
			
//			s = c.getObject1AsURI().getFragment().toLowerCase();
//			t = c.getObject2AsURI().getFragment().toLowerCase();
		


			System.out.println("\nThe similarity between + " + s + " and " + t);
			System.out.println("Jiang Conrath: " + WordNet.computeJiangConrath(s, t));
			//System.out.println("HirstStOnge: " + WordNet.computeHirstStOnge(s, t));
			System.out.println("Leacock Chodorow: " + WordNet.computeLeacockChodorow(s, t));
			//System.out.println("LESK: " + WordNet.computeLESK(s, t));
			System.out.println("Resnik: " + WordNet.computeResnik(s, t));
			System.out.println("Lin: " + WordNet.computeLin(s, t));
			System.out.println("Wu-Palmer: " + WordNet.computeWuPalmer(s, t));
			System.out.println("Frequency of " + s + " is " + getWordNetFrequency(s));
			System.out.println("Frequency of " + t + " is " + getWordNetFrequency(t));

		}

		String source = "Place";
		String target = "Residence";


		//		//		String source = "Organization";
		//		//		String target = "NGO";
		//
		//		//		String sourceDef = "Corporation or group of persons and/or organizations that acts, or may act, as a unit";
		//		//		String targetDef = "Organization: Non-governmental Organization";
		//
		//		String source = "Place";
		//		String target = "Residence";
		//
		//		//		String sourceDef = "Geographic location";
		//		//		String targetDef = "The place where a person lives";
		//
		//
		//		String sourceLex = WordNet.getLexicalName(source);
		//		String targetLex = WordNet.getLexicalName(target);
		//
		//		System.out.println("The lexical name of " + source + " is " + sourceLex);
		//		System.out.println("The lexical name of " + target + " is " + targetLex);
		//
		//		String[] closeHyponymsSource = WordNet.getHyponyms(sourceLex);
		//		System.out.println("\nClose hyponyms of " + source);
		//		for (int i = 0; i < closeHyponymsSource.length; i++) {
		//			System.out.println(closeHyponymsSource[i]);
		//		}
		//
		//		String[] closeHyponymsTarget = WordNet.getHyponyms(targetLex);
		//		System.out.println("\nClose hyponyms of " + target);
		//		for (int i = 0; i < closeHyponymsTarget.length; i++) {
		//			System.out.println(closeHyponymsTarget[i]);
		//		}
		//
		//		Set<String> allHyponymsSource = WordNet.getAllHyponymsAsSet(sourceLex);
		//		System.out.println("\nAll hyponyms of " + source);
		//		for (String s : allHyponymsSource) {
		//			System.out.println(s);
		//		}
		//
		//		Set<String> allHyponymstarget = WordNet.getAllHyponymsAsSet(targetLex);
		//		System.out.println("\nAll hyponyms of " + target);
		//		for (String t : allHyponymstarget) {
		//			System.out.println(t);
		//		}
	}

	public static int getWordNetFrequency (String input) throws net.sf.extjwnl.JWNLException, FileNotFoundException, JWNLException {

		Dictionary dictionary  = Dictionary.getDefaultResourceInstance();

		IndexWord iWord = dictionary.getIndexWord(POS.NOUN, input);
		int cur = 0;
		int max =  0;
		

		if (iWord != null) {

			for (Synset s : iWord.getSenses()) {

				for (Word w : s.getWords()) {

					if (w.getLemma().equalsIgnoreCase(input)) {

						cur = w.getUseCount();
						if (cur > max) {
							max = cur;

						}

					}

				}

			}

			return max;
		
		} else {
			return 0;
		}
	}

	public static Set<SimpleRelation> align (Set<String> setS, Set<String> setT) throws FileNotFoundException, JWNLException, IOException, net.sf.extjwnl.JWNLException {
		System.out.println("\nStarting Lexical Subsumption Matcher...");

		SimpleRelation relation = new SimpleRelation();
		Set<SimpleRelation> alignment = new HashSet<SimpleRelation>();

		LexicalConcept lc = new LexicalConcept();

		Set<String> hyponyms = new HashSet<String>();
		Set<String> glossTokens = new HashSet<String>();

		Map<String, LexicalConcept> onto1LexicalMap = new HashMap<String, LexicalConcept>();
		Map<String, LexicalConcept> onto2LexicalMap = new HashMap<String, LexicalConcept>();

		String lexicalNameS = null;
		String lexicalNameT = null;

		int wordNetFrequency = 0;

		for (String s : setS) {
			for (String t : setT) {

				lexicalNameS = WordNet.getLexicalName(s).toLowerCase();
				lexicalNameT = WordNet.getLexicalName(t).toLowerCase();

				if (WordNet.containedInWordNet(lexicalNameS)) {
					hyponyms = WordNet.getAllHyponymsAsSet(lexicalNameS);
					glossTokens = StringUtilities.tokenizeToSet(WordNet.getGloss(lexicalNameS), true);
					wordNetFrequency = getWordNetFrequency(lexicalNameS);
					lc = new LexicalConcept(lexicalNameS.replace(" ", ""), URI.create(lexicalNameS), hyponyms, glossTokens, wordNetFrequency);
					onto1LexicalMap.put(lexicalNameS.replace(" ", ""), lc);
				}

				if (WordNet.containedInWordNet(lexicalNameT)) {
					hyponyms = WordNet.getAllHyponymsAsSet(lexicalNameT);
					glossTokens = StringUtilities.tokenizeToSet(WordNet.getGloss(lexicalNameT), true);
					wordNetFrequency = getWordNetFrequency(lexicalNameT);
					lc = new LexicalConcept(lexicalNameT.replace(" ", ""), URI.create(lexicalNameT), hyponyms, glossTokens, wordNetFrequency);
					onto2LexicalMap.put(lexicalNameT.replace(" ", ""), lc);
				}

			}
		}

		System.out.println("Starting matching process...");



		//if neither of the concepts are in WordNet -> give the relation a score of 0
		//if both concepts are in WordNet, we extract their hyponyms and their gloss
		//if the full source is a part of the set of hyponyms of target AND a part of the WordNet gloss of target --> source > target and score 1.0
		//if the compound head of source is a part of the set of hyponyms of target and the full source OR the compound head is a part of the WordNet gloss of target --> source > target and score 0.75
		//if the full source is a part of the set of hyponyms of target: source > target and a score of 0.5
		//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.25

		String sourceEntity = null;
		String targetEntity = null;
		//required to have their representation without lowercase for the compound analysis
		String sourceEntityNormalCase = null;
		String targetEntityNormalCase = null;

		Set<String> hyponymsSource = new HashSet<String>();
		Set<String> hyponymsTarget = new HashSet<String>();
		Set<String> glossSource = new HashSet<String>();
		Set<String> glossTarget = new HashSet<String>();


		for (String s : setS) {
			for (String t : setT) {

				relation = new SimpleRelation();

				//get the entity names for source and target to make the code more readable
				sourceEntity = s.toLowerCase();
				targetEntity = t.toLowerCase();
				sourceEntityNormalCase = s;
				targetEntityNormalCase = t;


				if (sourceEntity.equals(targetEntity)) {

					relation.setSource(s);
					relation.setTarget(t);
					relation.setRelation("=");
					relation.setConfidence(0);
					alignment.add(relation);
					System.out.println(s + " and " + t + " are the same");


				}

				//if source nor target is a lexicalconcept == they are not in wordnet, give the relation between them score 0
				else if (!onto1LexicalMap.containsKey(sourceEntity) || !onto2LexicalMap.containsKey(targetEntity)) {
					relation.setSource(s);
					relation.setTarget(t);
					relation.setRelation("=");
					relation.setConfidence(0);
					alignment.add(relation);
					System.out.println(sourceEntity + " and " + targetEntity + " are not in WordNet");


				} 

				//if both concepts are in WordNet, we retrieve and compare their hyponyms and their gloss
				else if (onto1LexicalMap.containsKey(sourceEntity) && onto2LexicalMap.containsKey(targetEntity)) {
					//get the hyponyms of source and target entities
					hyponymsSource = onto1LexicalMap.get(sourceEntity).getHyponyms();
					hyponymsTarget = onto2LexicalMap.get(targetEntity).getHyponyms();
					//get the glosses of source and target entities
					glossSource = onto1LexicalMap.get(sourceEntity).getGlossTokens();
					glossTarget = onto2LexicalMap.get(targetEntity).getGlossTokens();
				}

				//if either hyponym set is empty -> score is 0
				if ((hyponymsSource == null || hyponymsSource.isEmpty()) || (hyponymsTarget == null || hyponymsTarget.isEmpty())) {
					relation.setSource(s);
					relation.setTarget(t);
					relation.setRelation("=");
					relation.setConfidence(0);
					alignment.add(relation);

					//System.out.println("There are no hyponyms for EITHER " + s + " or " + t);
				}

				else {
					//if the full source is a part of the set of hyponyms of target AND a part of the WordNet gloss of target: source > target and score 1.0
					if (hyponymsTarget.contains(sourceEntity) && glossTarget.contains(sourceEntity)) {
						relation.setSource(s);
						relation.setTarget(t);
						relation.setRelation("&lt;");
						relation.setConfidence(1.0);
						alignment.add(relation);
						System.out.println(s + " is included in the hyponym list of " + t + " and in the gloss of " + t + " -> 1.0");


					}
					//if the compound head of source is a part of the set of hyponyms of target AND the full source OR the compound head of source is a part of the WordNet gloss of target: source > target and score 0.75
					else if (StringUtilities.isCompoundWord(sourceEntityNormalCase)) {
						if (hyponymsTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase))
								&& glossTarget.contains(sourceEntity)
								|| glossTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase).toLowerCase())) {

							relation.setSource(s);
							relation.setTarget(t);
							relation.setRelation("&lt;");
							relation.setConfidence(0.75);
							alignment.add(relation);
							System.out.println("Source is " + s + " and Target is " + t + ". The compound head of " + s + " is included in the hyponym list of " + t + ", and " +  s + " is in the gloss of " + t + " OR the compound head of " +
									s + " is in the gloss of " + t + " -> 0.75");
						}
					}

					//if the full source is a part of the set of hyponyms of target: source > target and a score of 0.5
					else if (hyponymsTarget.contains(sourceEntity)) {
						relation.setSource(s);
						relation.setTarget(t);
						relation.setRelation("&lt;");
						relation.setConfidence(0.5);
						alignment.add(relation);
						System.out.println(s + " is included in the hyponym list of " + t + " -> 0.5");
					}

					//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.25
					else if (StringUtilities.isCompoundWord(sourceEntityNormalCase)) {

						if (hyponymsTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase))) {

							relation.setSource(s);
							relation.setTarget(t);
							relation.setRelation("&lt;");
							relation.setConfidence(0.25);
							alignment.add(relation);
							System.out.println("Source is " + s + " and Target is " + t + ". The compound head of " + s + " is included in the hyponym list of " + t);
						}
					}

					else if (hyponymsSource.contains(targetEntity) && glossSource.contains(targetEntity)) {
						relation.setSource(s);
						relation.setTarget(t);
						relation.setRelation("&gt;");
						relation.setConfidence(1.0);
						alignment.add(relation);
						System.out.println("Source is " + s + " and Target is " + t + "." + t + " is included in the hyponym set of " + s + " AND in the gloss of " + t);
					}

					else if (StringUtilities.isCompoundWord(targetEntityNormalCase)) {
						if (hyponymsSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase))
								&& glossSource.contains(targetEntity) 
								|| glossSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase).toLowerCase())) {
							relation.setSource(s);
							relation.setTarget(t);
							relation.setRelation("&gt;");
							relation.setConfidence(0.75);
							alignment.add(relation);
							System.out.println("Source is " + s + " and Target is " + t + ". The compound head of " + t + " is included in the hyponym set of " + s + " AND EITHER the compound head or the full concept of " + t + " is in the gloss of " + s);
						}
					}

					else if (hyponymsSource.contains(targetEntity)) {
						relation.setSource(s);
						relation.setTarget(t);
						relation.setRelation("&gt;");
						relation.setConfidence(0.5);
						alignment.add(relation);
						System.out.println(t + " is included in the hyponym set of " + s);
					}

					else if (StringUtilities.isCompoundWord(targetEntityNormalCase)) {
						if (hyponymsSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase).toLowerCase())) {
							relation.setSource(s);
							relation.setTarget(t);
							relation.setRelation("&gt;");
							relation.setConfidence(0.25);
							alignment.add(relation);
							System.out.println("Source is " + s + " and Target is " + t + "The compound head of " + t + " is included in the hyponym set of " + s);
						}
					}

					else {
						relation.setSource(s);
						relation.setTarget(t);
						relation.setRelation("=");
						relation.setConfidence(0.0);
						alignment.add(relation);
						//System.out.println("None of the rules apply for " + s + " and " + t);
					}

				}
			}
		}

		return alignment;
	}

}

