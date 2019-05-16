package evaluation.profileweight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import equivalencematching.DefinitionEquivalenceMatcherSigmoid;
import equivalencematching.GraphEquivalenceMatcherSigmoid;
import equivalencematching.LexicalEquivalenceMatcherSigmoid;
import equivalencematching.PropertyEquivalenceMatcherSigmoid;
import equivalencematching.WordEmbeddingMatcherSigmoid;
import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.AlignmentConflictResolution;
import matchercombination.ProfileWeight;
import matchercombination.ProfileWeightSubsumption;
import mismatchdetection.ConceptScopeMismatch;
import mismatchdetection.DomainMismatch;
import mismatchdetection.StructureMismatch;
import net.didion.jwnl.JWNLException;
import ontologyprofiling.OntologyProfiler;
import subsumptionmatching.CompoundMatcherSigmoid;
import subsumptionmatching.ContextSubsumptionMatcherSigmoid;
import subsumptionmatching.DefinitionSubsumptionMatcherSigmoid;
import subsumptionmatching.LexicalSubsumptionMatcherSigmoid;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class EvaluateMergedAlignmentProfileWeightSigmoid {

	final static String dataset = "BIBFRAME-SCHEMAORG";

	//these parameters are used for the sigmoid weight configuration
	final static int slope = 3;
	final static double rangeMin = 0.5;
	final static double rangeMax = 0.7;


	static File ontoFile1 = null;
	static File ontoFile2 = null;
	static String vectorFile = null;
	static String referenceAlignmentEQAndSUB = null;
	static String relationType ="EQ_SUB";


	public static void main(String[] args) throws OWLOntologyCreationException, JWNLException, IOException, AlignmentException, URISyntaxException {

		if (dataset.equalsIgnoreCase("ATMONTO-AIRM")) {
			ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
			ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
			referenceAlignmentEQAndSUB = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQ-SUB.rdf";
			vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/skybrary_trained_ontology_tokens.txt";

		} else if (dataset.equalsIgnoreCase("BIBFRAME-SCHEMAORG")) {
			ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
			ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
			referenceAlignmentEQAndSUB = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQ-SUB.rdf";
			vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
		}

		//compute profile scores
		System.err.println("Computing Profiling Scores");
		Map<String, Double> ontologyProfilingScores = OntologyProfiler.computeOntologyProfileScores(ontoFile1, ontoFile2, vectorFile);

		//compute EQ alignments
		ArrayList<URIAlignment> eqAlignments = computeEQAlignments(ontoFile1, ontoFile2, ontologyProfilingScores, vectorFile);

		//combine EQ alignments into a final EQ alignment
		URIAlignment combinedEQAlignment = combineEQAlignments(eqAlignments);

		//remove mismatches from combined EQ alignment
		URIAlignment combinedEQAlignmentWithoutMismatches = removeMismatches(combinedEQAlignment);

		//compute SUB alignments
		ArrayList<URIAlignment> subAlignments = computeSUBAlignments(ontoFile1, ontoFile2, ontologyProfilingScores, vectorFile);

		//combine SUB alignments into a final SUB alignment
		URIAlignment combinedSUBAlignment = combineSUBAlignments(subAlignments);

		//merge final EQ and final SUB alignment
		URIAlignment mergedEQAndSubAlignment = mergeEQAndSubAlignments(combinedEQAlignmentWithoutMismatches, combinedSUBAlignment);

		//resolve conflicts in merged alignment
		URIAlignment nonConflictedMergedAlignment = AlignmentConflictResolution.resolveAlignmentConflict(mergedEQAndSubAlignment);

		System.err.println("\nThe merged EQ and SUB alignment contains " + nonConflictedMergedAlignment.nbCells() + " relations");

		//store the merged alignment
		File outputAlignment = new File("./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/PROFILEWEIGHT/MERGED_SIGMOID/PROFILEWEIGHT_MERGED_SIGMOID"+dataset+".rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		nonConflictedMergedAlignment.render(renderer);

		writer.flush();
		writer.close();

		//evaluate merged final EQ-SUB alignment
		System.err.println("Evaluating the merged EQ and SUB alignment:");
		Evaluator.evaluateSingleAlignment(nonConflictedMergedAlignment, referenceAlignmentEQAndSUB);

		//evaluate the profile weight alignment at different thresholds
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment refalign = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentEQAndSUB)));
		double[] confidence = {0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();

		for (double conf : confidence) {
			EvaluationScore evalScore = new EvaluationScore();
			nonConflictedMergedAlignment.cut(conf);
			eval = new PRecEvaluator(refalign, nonConflictedMergedAlignment);
			eval.eval(p);
			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());
			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);
			//put the evalation score according to each confidence value in the map
			evaluationMap.put(String.valueOf(conf), evalScore);			
			outputAlignment = new File("./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/PROFILEWEIGHT/MERGED_SIGMOID/PROFILEWEIGHT_MERGED_SIGMOID"+dataset+"_"+conf+".rdf");
			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			nonConflictedMergedAlignment.render(renderer);
			writer.flush();
			writer.close();
			//print evaluation results to console
			Evaluator.evaluateSingleAlignment("Cut Threshold " + conf, nonConflictedMergedAlignment, referenceAlignmentEQAndSUB);
		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/PROFILEWEIGHT/PROFILEWEIGHT_MERGED_SIGMOID");

	}

	private static ArrayList<URIAlignment> computeEQAlignments(File ontoFile1, File ontoFile2, Map<String, Double> ontologyProfilingScores, String vectorFile) throws OWLOntologyCreationException, AlignmentException, URISyntaxException {

		ArrayList<URIAlignment> eqAlignments = new ArrayList<URIAlignment>();

		System.err.println("Computing WEM alignment");
		URIAlignment WEMAlignment = WordEmbeddingMatcherSigmoid.returnWEMAlignment(ontoFile1, ontoFile2, vectorFile, ontologyProfilingScores.get("cc"), slope, rangeMin, rangeMax);	
		eqAlignments.add(WEMAlignment);

		System.err.println("Computing DEM alignment");
		URIAlignment DEMAlignment = DefinitionEquivalenceMatcherSigmoid.returnDEMAlignment(ontoFile1, ontoFile2, vectorFile, ontologyProfilingScores.get("cc"), slope, rangeMin, rangeMax);
		eqAlignments.add(DEMAlignment);

		System.err.println("Computing GEM alignment");
		URIAlignment GEMAlignment = GraphEquivalenceMatcherSigmoid.returnGEMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("sp"), slope, rangeMin, rangeMax);	
		eqAlignments.add(GEMAlignment);

		System.err.println("Computing PEM alignment");
		URIAlignment PEMAlignment = PropertyEquivalenceMatcherSigmoid.returnPEMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("pf"), slope, rangeMin, rangeMax);
		eqAlignments.add(PEMAlignment);

		System.err.println("Computing LEM alignment");
		URIAlignment LEMAlignment = LexicalEquivalenceMatcherSigmoid.returnLEMAlignment(ontoFile1, ontoFile2, (ontologyProfilingScores.get("lc") * ontologyProfilingScores.get("sr")), slope, rangeMin, rangeMax);
		eqAlignments.add(LEMAlignment);

		System.out.println("The arraylist eqAlignments contains " + eqAlignments.size() + " alignments");		


		return eqAlignments;

	}

	private static URIAlignment combineEQAlignments (ArrayList<URIAlignment> inputAlignments) throws AlignmentException, IOException, URISyntaxException {

		URIAlignment combinedEQAlignment = ProfileWeight.computeProfileWeightingEquivalence(inputAlignments);

		System.err.println("\nThe combined EQ alignment contains " + combinedEQAlignment.nbCells() + " relations");
		System.err.println("These relations are: ");

		for (Cell c : combinedEQAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

		return combinedEQAlignment;

	}

	private static ArrayList<URIAlignment> computeSUBAlignments(File ontoFile1, File ontoFile2, Map<String, Double> ontologyProfilingScores, String vectorFile) throws OWLOntologyCreationException, AlignmentException {

		ArrayList<URIAlignment> subAlignments = new ArrayList<URIAlignment>();

		System.err.println("Computing CM alignment");
		URIAlignment CMAlignment = CompoundMatcherSigmoid.returnCMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("cf"), slope, rangeMin, rangeMax);		
		subAlignments.add(CMAlignment);

		System.err.println("Computing CSM alignment");
		URIAlignment CSMAlignment = ContextSubsumptionMatcherSigmoid.returnCSMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("sp"), slope, rangeMin, rangeMax);		
		subAlignments.add(CSMAlignment);

		System.err.println("Computing DSM alignment");
		URIAlignment DSMAlignment = DefinitionSubsumptionMatcherSigmoid.returnDSMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("dc"), slope, rangeMin, rangeMax);		
		subAlignments.add(DSMAlignment);

		System.err.println("Computing LSM alignment");
		URIAlignment LSMAlignment = LexicalSubsumptionMatcherSigmoid.returnLSMAlignment(ontoFile1, ontoFile2, (ontologyProfilingScores.get("lc") * ontologyProfilingScores.get("hr")), slope, rangeMin, rangeMax);		
		subAlignments.add(LSMAlignment);

		return subAlignments;

	}

	private static URIAlignment combineSUBAlignments (ArrayList<URIAlignment> inputAlignments) throws AlignmentException, IOException, URISyntaxException {

		URIAlignment combinedSUBAlignment = ProfileWeightSubsumption.computeProfileWeightingSubsumption(inputAlignments);

		System.err.println("\nThe combined SUB alignment contains " + combinedSUBAlignment.nbCells() + " relations");
		System.err.println("These relations are: ");

		for (Cell c : combinedSUBAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}


		return combinedSUBAlignment;

	}

	private static URIAlignment removeMismatches (URIAlignment combinedEQAlignment) throws AlignmentException, OWLOntologyCreationException, FileNotFoundException, JWNLException {

		URIAlignment conceptScopeMismatchDetection = ConceptScopeMismatch.detectConceptScopeMismatch(combinedEQAlignment);
		URIAlignment structureMismatchDetection = StructureMismatch.detectStructureMismatches(conceptScopeMismatchDetection, ontoFile1, ontoFile2);
		URIAlignment domainMismatchDetection = DomainMismatch.filterAlignment(structureMismatchDetection);

		return domainMismatchDetection;
	}

	private static URIAlignment mergeEQAndSubAlignments (URIAlignment eqAlignment, URIAlignment subAlignment) throws AlignmentException {

		URIAlignment mergedEQAndSubAlignment = AlignmentOperations.combineEQAndSUBAlignments(eqAlignment, subAlignment);

		return mergedEQAndSubAlignment;

	}

}
