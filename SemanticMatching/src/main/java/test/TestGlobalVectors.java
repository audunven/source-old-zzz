package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.MathUtils;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import wordembedding.VectorExtractor;

public class TestGlobalVectors {

	public static void main(String[] args) throws IOException, OWLOntologyCreationException {


		//create the vector map holding word - embedding vectors		
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(new File("./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt"));
		
		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-301.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-304.rdf");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology targetOntology = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		ArrayList<Double> labelVectors = new ArrayList<Double>();
		
		int sourceCounter = 0;
		int totalSourceCounter = 0;

		for (OWLClass source : sourceOntology.getClassesInSignature()) {
			totalSourceCounter++;
			labelVectors = getLabelVector(source.getIRI().getFragment().toLowerCase(), vectorMap);

			if (labelVectors != null) {
				sourceCounter++;
			System.out.println("There are " + labelVectors.size() + " label vectors for " + source.getIRI().getFragment().toLowerCase());
			} else {
				//System.out.println("There are no label vectors for " + source.getIRI().getFragment().toLowerCase());
			}
			
		}
		
		//System.out.println("\nThere are label vectors for " + sourceCounter + " out of " + totalSourceCounter + " classes in the source ontology");
		
		
		int targetCounter = 0;
		int totalTargetCounter = 0;

		for (OWLClass target : targetOntology.getClassesInSignature()) {
			totalTargetCounter++;
			labelVectors = getLabelVector(target.getIRI().getFragment().toLowerCase(), vectorMap);

			if (labelVectors != null) {
				targetCounter++;
			System.out.println("There are " + labelVectors.size() + " label vectors for " + target.getIRI().getFragment().toLowerCase());
			} else {
				//System.out.println("There are no label vectors for " + target.getIRI().getFragment().toLowerCase());
			}
			
		}
		
		//System.out.println("\nThere are label vectors for " + targetCounter + " out of " + totalTargetCounter + " classes in the target ontology");

		
		double[] globalVectors = null;

		sourceCounter = 0;
		totalSourceCounter = 0;
		for (OWLClass source : sourceOntology.getClassesInSignature()) {
			totalSourceCounter++;
			globalVectors = getGlobalVector(source.getIRI().getFragment().toLowerCase(), OntologyOperations.getClassDefinitionFull(sourceOntology, source), vectorMap);

			if (globalVectors != null) {
				sourceCounter++;
			//System.out.println("There are " + globalVectors.length + " global vectors for " + source.getIRI().getFragment().toLowerCase());
			} else {
				//System.out.println("There are no label vectors for " + source.getIRI().getFragment().toLowerCase());
			}
			
		}

		System.out.println("\nThere are global vectors for " + sourceCounter + " out of " + totalSourceCounter + " classes in the source ontology\n");
		
		targetCounter = 0;
		totalTargetCounter = 0;
		for (OWLClass target : targetOntology.getClassesInSignature()) {
			totalTargetCounter++;
			globalVectors = getGlobalVector(target.getIRI().getFragment().toLowerCase(), OntologyOperations.getClassDefinitionFull(targetOntology, target), vectorMap);

			if (globalVectors != null) {
				targetCounter++;
			//System.out.println("There are " + globalVectors.length + " global vectors for " + target.getIRI().getFragment().toLowerCase());
			} else {
				//System.out.println("There are no label vectors for " + source.getIRI().getFragment().toLowerCase());
			}
			
		}

		//System.out.println("\nThere are global vectors for " + targetCounter + " out of " + totalTargetCounter + " classes in the source ontology\n");



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
		
		ArrayList<Double> commentVectors = null;
		//if the comment simply just repeats the class name, we skip it...
//		System.out.println("\nLabel is " + label);
//		System.out.println("Definition is " + StringUtilities.removeStopWords(def));
		
		if (!StringUtilities.removeStopWords(def).equalsIgnoreCase(label)) {
			commentVectors = getCommentVector(def, vectorMap);
		} else {
			commentVectors = null;
		}

		ArrayList<Double> globalVectors = new ArrayList<Double>();

		ArrayList<Double> globalVector = new ArrayList<Double>();
		
		double[] vectors = new double[300];


		//a fixed dimension of vectors is 300
		int numVectors = 300;

		//if there also are comment vectors, we average the label vector and the comment vector (already averaged between all token vectors for each comment) into a global vector
		//TODO: Simplify this computation of averages
		if (labelVectors != null && !labelVectors.isEmpty() && commentVectors!= null && !commentVectors.isEmpty()) {

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

		
		if (globalVector != null && !globalVector.isEmpty()) {
		
		for (int i = 0; i < vectors.length; i++) {
			vectors[i] = globalVector.get(i);
		}
		
//		System.out.println("\nThe global vectors for " + label + " are: ");
//		
//		for (double d : vectors) {
//			System.out.print(d + " ");
//		}
		
		return vectors;
		
		} else {
			return null;
		}

	}
	
	/**
	 * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned. 
	 * @param cls An input OWL class
	 * @param vectorMap The Map holding words and corresponding vectors
	 * @return a set of vectors (as a string) associated with the label
	 */
	public static ArrayList<Double> getLabelVector(String label, Map<String, ArrayList<Double>> vectorMap) {


		ArrayList<ArrayList<Double>> avgLabelVectors = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> labelVector = new ArrayList<Double>();
		ArrayList<Double> localVectors = new ArrayList<Double>();

		//if the class name is not a compound, turn it into lowercase, 
		if (!StringUtilities.isCompoundWord(label)) {

			String lcLabel = label.toLowerCase();

			//if the class name is in the vectormap, get its vectors
			if (vectorMap.containsKey(lcLabel)) {
				labelVector = vectorMap.get(lcLabel);

			} else {

				labelVector = null;
			}


			//if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from 
			//the compound parts and average them in order to return the vector for the compound class name
		} else if (StringUtilities.isCompoundWord(label)) {
			

			//get the compounds and check if any of them are in the vector file
			String[] compounds = label.split("(?<=.)(?=\\p{Lu})");


			for (int i = 0; i < compounds.length; i++) {
				
				if (vectorMap.containsKey(compounds[i].toLowerCase())) {
					
					localVectors = vectorMap.get(compounds[i].toLowerCase());
					
					avgLabelVectors.add(localVectors);


				} else {
					
					labelVector = null;
				}
			}
			
			//averages all vector arraylists
			labelVector = getAVGVectors(avgLabelVectors, 300);

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
		
		System.out.println("\nThe definition is: " + comment);
		
		ArrayList<ArrayList<Double>> avgCommentVectors = new ArrayList<ArrayList<Double>>();

		ArrayList<Double> commentVector = new ArrayList<Double>();

		ArrayList<Double> commentVectors = new ArrayList<Double>();

		if (comment != null && !comment.isEmpty()) {

			//create tokens from comment
			ArrayList<String> tokens = StringUtilities.tokenize(comment, true);
			
			System.out.println("\nThe tokenized definition is:");
			for (String t : tokens) {
				System.out.println(t);
			}

			if (containedInVectorMap(tokens, vectorMap)) {
			//put all tokens that have an associated vector in the vectorMap in allCommentVectors along with the associated vector
			for (String s : tokens) {
				
				if (vectorMap.containsKey(s)) {
					
					System.out.println("Vectormap contains " + s);

					commentVectors = vectorMap.get(s);
					
					avgCommentVectors.add(commentVectors);

				}
			} 
			
			//create average vector representing all token vectors in each comment
			//averages all vector arraylists
			commentVector = getAVGVectors(avgCommentVectors, 300);
			
			} 

			else {
				commentVector = null;
			}
			
		} else {
			commentVector = null;
		}
		
		if (commentVector != null) {
		System.out.println("The comment vector is: ");
		for (double d : commentVector) {
			System.out.print(d + " ");
		}
		}

		return commentVector;

	}
	
	private static double[] getAVGVectorsToArray(ArrayList<ArrayList<Double>> a_input, int numVectors) {

		ArrayList<Double> avgList = new ArrayList<Double>();

		double[] avgArray = new double[300];

		double[] temp = new double[numVectors];


		for (ArrayList<Double> singleArrayList : a_input) {
			for (int i = 0; i < temp.length; i++) {
				temp[i] += singleArrayList.get(i);
			}
		}

		for (int i = 0; i < temp.length; i++) {
			avgList.add(temp[i]/(double) a_input.size());
		}

		for (int i = 0; i < avgArray.length; i++) {
			avgArray[i] = avgList.get(i);
		}


		return avgArray;
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
	
	private static boolean containedInVectorMap (ArrayList<String> tokens, Map<String, ArrayList<Double>> vectorMap) {
		
		boolean contains = false;
		
		for (String s : tokens) {
			if (vectorMap.containsKey(s)) {
				contains = true;
			}
		}
		
		return contains;
		
	}


}
