package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import utilities.MathUtils;
import utilities.StringUtilities;
import wordembedding.VectorExtractor;

public class TestGlobalVectorsArray {

	public static void main(String[] args) throws IOException {

		String testClass = "AboutPage";
		String definition = "This is a test to see if the comment vector approach works";
		
		//create the vector map holding word - embedding vectors		
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(new File("./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt"));
		
		//ArrayList<Double> labelVectors = getLabelVector(testClass, vectorMap);

		//ArrayList<Double> commentVectors = getCommentVector(definition, vectorMap);

		double[] globalVectors = getGlobalVector(testClass, definition, vectorMap);
		
		for (int i = 0; i < globalVectors.length; i++) {
			System.out.print(globalVectors[i]);
		}
		
		

	}
	
	/**
	 * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned. 
	 * @param cls An input OWL class
	 * @param vectorMap The Map holding words and corresponding vectors
	 * @return a set of vectors (as a string) associated with the label
	 */
	public static ArrayList<Double> getLabelVector(String label, Map<String, ArrayList<Double>> vectorMap) {

		ArrayList<Double> labelVectors = new ArrayList<Double>();
		Map<String, ArrayList<Double>> compoundVectors = new HashMap<String, ArrayList<Double>>();
		ArrayList<ArrayList<Double>> avgLabelVectors = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> labelVector = new ArrayList<Double>();

		//if the class name is not a compound, turn it into lowercase, 
		if (!StringUtilities.isCompoundWord(label)) {

			String lcLabel = label.toLowerCase();

			//if the class name is in the vectormap, get its vectors
			if (vectorMap.containsKey(lcLabel)) {
				labelVectors = vectorMap.get(lcLabel);

			} else {

				labelVectors = null;
			}

			labelVector = labelVectors;

			//if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from 
			//the compound parts and average them in order to return the vector for the compound class name
		} else if (StringUtilities.isCompoundWord(label)) {

			//get the compounds and check if any of them are in the vector file
			String[] compounds = label.split("(?<=.)(?=\\p{Lu})");


			for (int i = 0; i < compounds.length; i++) {
				if (vectorMap.containsKey(compounds[i].toLowerCase())) {
					
					labelVectors = vectorMap.get(compounds[i].toLowerCase());
				
					
					avgLabelVectors.add(labelVectors);
					compoundVectors.put(compounds[i].toLowerCase(), labelVectors);


				} else {
					labelVectors = null;
				}
			}

			//averages all vector arraylists
			labelVector = getAVGVectors(avgLabelVectors, 300);

		}
		
		System.out.println("\n Label vectors");
		for (double d : labelVector) {
			System.out.print(d + ";");
		}
		return labelVector;


	}
	
	/**
	 * Returns the average vector of all tokens represented in the RDFS comment for an OWL class
	 * @param onto The ontology holding the OWL class
	 * @param cls The OWL class
	 * @param vectorMap The map of vectors from en input vector file
	 * @return An average vector for all (string) tokens in an RDFS comment
	 * @throws IOException
	 */
	public static ArrayList<Double> getCommentVector(String comment, Map<String, ArrayList<Double>> vectorMap) throws IOException {
		
		ArrayList<ArrayList<Double>> avgCommentVectors = new ArrayList<ArrayList<Double>>();

		ArrayList<Double> commentVector = new ArrayList<Double>();

		ArrayList<Double> commentVectors = new ArrayList<Double>();

		if (comment != null && !comment.isEmpty()) {

			//create tokens from comment
			ArrayList<String> tokens = StringUtilities.tokenize(comment, true);

			//put all tokens that have an associated vector in the vectorMap in allCommentVectors along with the associated vector
			for (String s : tokens) {
				if (vectorMap.containsKey(s)) {

					commentVectors = vectorMap.get(s);
					
					avgCommentVectors.add(commentVectors);

				} else {
					
					commentVectors = null;
				}

			}

			//create average vector representing all token vectors in each comment
			//averages all vector arraylists
			commentVector = getAVGVectors(avgCommentVectors, 300);
			
		} else {
			commentVector = null;
		}
		
		System.out.println("\n Comment vector");
		for (double d : commentVector) {
			System.out.print(d + ";");
		}
		return commentVector;

	}
	
	
	/**
	 * Returns a "global vector", that is an average of a label vector and a comment vector
	 * @param labelVector The average vector for an OWL class´ label
	 * @param commentVector The average vector for all (string) tokens in the OWL class´ RDFS comment
	 * @return a set of vectors averaged between label vectors and comment vectors
	 * @throws IOException 
	 */
	public static double[] getGlobalVector(String label, String def, Map<String, ArrayList<Double>> vectorMap) throws IOException {

		ArrayList<Double> labelVectors = getLabelVector(label, vectorMap);
		ArrayList<Double> commentVectors = getCommentVector(def, vectorMap);

		ArrayList<Double> globalVectors = new ArrayList<Double>();

		ArrayList<Double> globalVector = new ArrayList<Double>();
		
		double[] vectors = new double[300];



		//a fixed dimension of vectors is 300
		int numVectors = 300;

		//if there also are comment vectors, we average the label vector and the comment vector (already averaged between all token vectors for each comment) into a global vector
		if (commentVectors!= null && !commentVectors.isEmpty()) {

			double average = 0;
			for (int i = 0; i < numVectors; i++) {
				if (labelVectors.size() < 1 && commentVectors.size() < 1) {
					return null;
				} else if (labelVectors.size() < 1 && commentVectors.size() > 0) {
					average = commentVectors.get(i);
				} else if (labelVectors.size() > 0 && commentVectors.size() < 1) { 
					average = labelVectors.get(i);
				} else {

					if (labelVectors.get(i) == 0.0) {
						average = commentVectors.get(i);
					} else if (commentVectors.get(i) == 0.0) {
						average = labelVectors.get(i);
					} else {

						average = (labelVectors.get(i) + commentVectors.get(i)) / 2;
					}
				}
				globalVectors.add(average);

			}

		} else {
			globalVector = labelVectors;
		}

		//round the vector value to 6 decimals
		for (double d : globalVectors) {
			globalVector.add(MathUtils.round(d, 6));
		}


		System.out.println("\n Global vector");
		for (double d : globalVector) {
			System.out.print(d + ";");
		}
		
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = globalVector.get(i);
		}
		
		return vectors;

	}
	
	private static ArrayList<Double> getAVGVectors(ArrayList<ArrayList<Double>> a_input, int numVectors) {

		ArrayList<Double> avgList = new ArrayList<Double>();
		
		double[] temp = new double[numVectors];
		
		
		for (ArrayList<Double> singleArrayList : a_input) {
			for (int i = 0; i < temp.length; i++) {
				temp[i] += singleArrayList.get(i);
			}
		}
		
		for (int i = 0; i < temp.length; i++) {
			avgList.add(temp[i]/(double) a_input.size());
		}
		
		
		
		return avgList;
	}
	

}
