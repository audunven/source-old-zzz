package evaluation.sigmoid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.ProfileWeightSubsumption;
import utilities.StringUtilities;

public class EvalAllSigmoidSub {
	
	final static double rangeMin = 0.5;
	final static double rangeMax = 0.7;
	
	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException {
		
		int[] slopeParam = {11,12,13,14,15,16,17,18,19,20};
 
		
		/* ATM */
//		Map<String, Double> profileScores = new HashMap<String, Double>();
//		profileScores.put("cf", 0.93);
//		profileScores.put("cc", 0.84);
//		profileScores.put("dc", 0.98);
//		profileScores.put("pf", 0.78);
//		profileScores.put("sp", 0.58);
//		profileScores.put("lc", 0.72);
		
		/* CH */
		Map<String, Double> profileScores = new HashMap<String, Double>();
		profileScores.put("cf", 0.65);
		profileScores.put("cc", 0.83);
		profileScores.put("dc", 0.75);
		profileScores.put("pf", 0.41);
		profileScores.put("sp", 0.91);
		profileScores.put("lc", 0.75);
		
		String folderName = "./files/_PHD_EVALUATION/MATCHERTESTING/SIGMOID/CH_SUB";
		File folder = new File(folderName);		
		URIAlignment a = new URIAlignment();
		
		File[] filesInDir = folder.listFiles();

		double profileWeight = 0;
		
		URIAlignment combinationAlignment = new URIAlignment();
		
		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-SUBSUMPTION.rdf";
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
		PRecEvaluator eval = null;
		Properties p = new Properties();
		

		Map<Integer, ArrayList<URIAlignment>> alignmentMap = new HashMap<Integer, ArrayList<URIAlignment>>();
				
		String URI = null;
		
		
		
		//for each slope parameter
		for (int i = 0; i < slopeParam.length; i++) {
			
			ArrayList<URIAlignment> alignmentList = new ArrayList<URIAlignment>();
			
			long start = System.currentTimeMillis();
			
			System.out.println("\nRefining weights for slope parameter " + slopeParam[i]);
			
			//implement relevant weight for on each alignment
			for (int j = 0; j < filesInDir.length; j++) {
			
			if (filesInDir[j].getName().contains("CompoundMatcher")) {
				profileWeight = profileScores.get("cf");
				URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[j].toString());
				a = (URIAlignment) aparser.parse(new URI(URI));
				
				implementWeight(a, profileWeight, slopeParam[i]);
				alignmentList.add(a);

			} else if (filesInDir[j].getName().contains("ContextSubsumptionMatcher")) {
				profileWeight = profileScores.get("sp");
				URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[j].toString());
				a = (URIAlignment) aparser.parse(new URI(URI));
				
				implementWeight(a, profileWeight, slopeParam[i]);
				alignmentList.add(a);
				
			} else if (filesInDir[j].getName().contains("DefinitionSubsumptionMatcher")) {
				profileWeight = profileScores.get("dc");
				URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[j].toString());
				a = (URIAlignment) aparser.parse(new URI(URI));
				
				implementWeight(a, profileWeight, slopeParam[i]);
				alignmentList.add(a);
				
			} else if (filesInDir[j].getName().contains("LexicalSubsumptionMatcher")) {
				profileWeight = profileScores.get("lc");
				URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[j].toString());
				a = (URIAlignment) aparser.parse(new URI(URI));
				
				implementWeight(a, profileWeight, slopeParam[i]);
				alignmentList.add(a);
				
			} 
			
//			long end = System.currentTimeMillis();
//			long elapsedTime = end - start;
//			System.out.println("Refining the weights for slope parameter " + slopeParam[i] + " took " + elapsedTime/1000 + " seconds");
//			
//			//add weighted alignment to set
//			alignmentList.add(a);
			
			}
			
			long end = System.currentTimeMillis();
			long elapsedTime = end - start;
			System.out.println("Refining the weights for slope parameter " + slopeParam[i] + " took " + elapsedTime/1000 + " seconds");
			
			//add weighted alignment to set
			//alignmentList.add(a);
			
			//add slope parameter and associated list of alignments in alignmentMap
			System.out.println("Adding " + slopeParam[i] + " and alignmentList with " + alignmentList.size() + " alignments to alignmentMap");
			alignmentMap.put(slopeParam[i], alignmentList);
		}
		
		System.out.println("\nCombining alignments");
		
		for (Entry<Integer, ArrayList<URIAlignment>> e : alignmentMap.entrySet()) {
			
//			System.out.println("Running Profile Weight");
			
			//for each slope parameter and associated alignment list compute the profile weight alignment
			combinationAlignment = ProfileWeightSubsumption.computeProfileWeightingSubsumption(e.getValue());
			
//			System.out.println("Finished running Profile Weight");
			
			//evaluate combination alignment at different thresholds and print to csv file
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			
			System.out.print("\n");
			System.out.print(e.getKey());
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.2);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.3);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.4);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.5);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.6);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.7);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.8);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(0.9);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
			combinationAlignment.cut(1.0);
			eval = new PRecEvaluator((Alignment) referenceAlignment, combinationAlignment);
			eval.eval(p);
			System.out.print(";");
			System.out.print(round(eval.getFmeasure(),2));
			
		}
			
		}

	
	/**
	 * Adds weight to the confidence according to the profile score associated to the matcher producing the alignment. If the initial
	 * confidence is 0.0, no weight is implemented.
	 * @param inputAlignment
	 * @param weight
	 * @param slope
	   Apr 25, 2019
	 */
	private static void implementWeight(BasicAlignment inputAlignment, double weight, int slope) {
		
		double confidence = 0;
				
		for (Cell c : inputAlignment) {
			
			confidence = c.getStrength();			
			
			if (confidence != 0.0)	{
			c.setStrength(weightedSigmoid(slope, confidence, transformProfileWeight(weight, rangeMin, rangeMax)));
			} 
		}		
	}
		
	public static double weightedSigmoid(int slope, double confidence, double profileWeight) {
		return (1/( 1 + Math.pow(Math.E,(-slope*(confidence-profileWeight)))));
	}
	

	public static double transformProfileWeight (double profileWeight, double rangeMin, double rangeMax) {
		
		return ((1.0 - profileWeight) * (rangeMax - rangeMin) / (1.0 - 0.0)) + rangeMin;
		
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
