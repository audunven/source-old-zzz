package keod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import equivalencematching.DefinitionEquivalenceMatcher;
import equivalencematching.GraphEquivalenceMatcher;
import equivalencematching.LexicalEquivalenceMatcher;
import equivalencematching.PropertyEquivalenceMatcher;
import equivalencematching.WordEmbeddingMatcher;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import matchercombination.ProfileWeight;
import matchercombination.ProfileWeightSubsumption;
import mismatchdetection.ConceptScopeMismatch;
import mismatchdetection.DomainMismatch;
import mismatchdetection.StructureMismatch;
import net.didion.jwnl.JWNLException;
import ontologyprofiling.OntologyProfiler;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.ContextSubsumptionMatcher;
import subsumptionmatching.DefinitionSubsumptionMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher;
import utilities.AlignmentOperations;

public class EvaluateMergedAlignmentBasicWeighting {

	static File ontoFile1 = new File("./files/KEOD18/datasets_refined/d1/ontologies/aixm_airportheliport.owl");
	static File ontoFile2 = new File("./files/KEOD18/datasets_refined/d1/ontologies/aerodromeinfrastructure.owl");

	static String referenceAlignmentEQAndSUB = "./files/KEOD18/datasets_refined/d1/refalign/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure.rdf";
	static String referenceAlignmentEQ = "./files/KEOD18/datasets_refined/d1/refalign/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure-Equivalence.rdf";
	static String referenceAlignmentSUB = "./files/KEOD18/datasets_refined/d1/refalign/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure-Subsumption.rdf";
	static String vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/skybrary_trained_ontology_tokens.txt";


	public static void main(String[] args) throws OWLOntologyCreationException, JWNLException, IOException, AlignmentException, URISyntaxException {


		//compute profile scores
		System.err.println("Computing Profiling Scores");
		Map<String, Double> ontologyProfilingScores = OntologyProfiler.computeOntologyProfileScores(ontoFile1, ontoFile2, vectorFile);

		//compute EQ alignments
		ArrayList<URIAlignment> eqAlignments = computeEQAlignments(ontoFile1, ontoFile2, ontologyProfilingScores, vectorFile);

		//combine EQ alignments into a final EQ alignment
		URIAlignment combinedEQAlignment = combineEQAlignments(eqAlignments);

		//remove mismatches from combined EQ alignment
		URIAlignment combinedEQAlignmentWithoutMismatches = removeMismatches(combinedEQAlignment);

		//evaluate the combined EQ alignment
		System.err.println("Evaluating the combined EQ alignment:");
		Evaluator.evaluateSingleAlignment(combinedEQAlignmentWithoutMismatches, referenceAlignmentEQ);

		//compute SUB alignments
		ArrayList<URIAlignment> subAlignments = computeSUBAlignments(ontoFile1, ontoFile2, ontologyProfilingScores, vectorFile);

		//combine SUB alignments into a final SUB alignment
		URIAlignment combinedSUBAlignment = combineSUBAlignments(subAlignments);

		//evaluate the combined SUB alignment
		System.err.println("Evaluating the combined SUB alignment:");
		Evaluator.evaluateSingleAlignment(combinedSUBAlignment, referenceAlignmentSUB);

		//merge final EQ and final SUB alignment
		URIAlignment mergedEQAndSubAlignment = mergeEQAndSubAlignments(combinedEQAlignmentWithoutMismatches, combinedSUBAlignment);

		System.err.println("\nThe merged EQ and SUB alignment contains " + mergedEQAndSubAlignment.nbCells() + " relations");

		//store the merged alignment
		File outputAlignment = new File("./files/KEOD18/datasets_refined/d1/April2019/aixm_airportheliport_aerodromeinfrastructure_basicweight.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		mergedEQAndSubAlignment.render(renderer);

		writer.flush();
		writer.close();

		//evaluate merged final EQ-SUB alignment
		System.err.println("Evaluating the merged EQ and SUB alignment:");
		Evaluator.evaluateSingleAlignment(mergedEQAndSubAlignment, referenceAlignmentEQAndSUB);

	}

	private static ArrayList<URIAlignment> computeEQAlignments(File ontoFile1, File ontoFile2, Map<String, Double> ontologyProfilingScores, String vectorFile) throws OWLOntologyCreationException, AlignmentException, URISyntaxException {

		ArrayList<URIAlignment> eqAlignments = new ArrayList<URIAlignment>();

		System.err.println("Computing WEM alignment");
		URIAlignment WEMAlignment = WordEmbeddingMatcher.returnWEMAlignment(ontoFile1, ontoFile2, vectorFile, ontologyProfilingScores.get("cc"));	
		eqAlignments.add(WEMAlignment);

		System.err.println("Computing DEM alignment");
		URIAlignment DEMAlignment = DefinitionEquivalenceMatcher.returnDEMAlignment(ontoFile1, ontoFile2, vectorFile, ontologyProfilingScores.get("cc"));
		eqAlignments.add(DEMAlignment);

		System.err.println("Computing GEM alignment");
		URIAlignment GEMAlignment = GraphEquivalenceMatcher.returnGEMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("sp"));	
		eqAlignments.add(GEMAlignment);

		System.err.println("Computing PEM alignment");
		URIAlignment PEMAlignment = PropertyEquivalenceMatcher.returnPEMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("pf"));
		eqAlignments.add(PEMAlignment);

		System.err.println("Computing LEM alignment");
		URIAlignment LEMAlignment = LexicalEquivalenceMatcher.returnLEMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("lc"));
		eqAlignments.add(LEMAlignment);

		System.out.println("The arraylist eqAlignments contains " + eqAlignments.size() + " alignments");		


		return eqAlignments;

	}

	private static URIAlignment combineEQAlignments (ArrayList<URIAlignment> inputAlignments) throws AlignmentException, IOException, URISyntaxException {

		URIAlignment combinedEQAlignment = ProfileWeight.computeProfileWeightingEquivalence(inputAlignments);

		System.err.println("\nThe combined EQ alignment contains " + combinedEQAlignment.nbCells() + " relations");

		return combinedEQAlignment;

	}

	private static ArrayList<URIAlignment> computeSUBAlignments(File ontoFile1, File ontoFile2, Map<String, Double> ontologyProfilingScores, String vectorFile) throws OWLOntologyCreationException, AlignmentException {

		ArrayList<URIAlignment> subAlignments = new ArrayList<URIAlignment>();

		System.err.println("Computing CM alignment");
		URIAlignment CMAlignment = CompoundMatcher.returnCMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("cf"));		
		subAlignments.add(CMAlignment);

		System.err.println("Computing CSM alignment");
		URIAlignment CSMAlignment = ContextSubsumptionMatcher.returnCSMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("sp"));		
		subAlignments.add(CSMAlignment);

		System.err.println("Computing DSM alignment");
		URIAlignment DSMAlignment = DefinitionSubsumptionMatcher.returnDSMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("dc"));		
		subAlignments.add(DSMAlignment);

		System.err.println("Computing LSM alignment");
		URIAlignment LSMAlignment = LexicalSubsumptionMatcher.returnLSMAlignment(ontoFile1, ontoFile2, ontologyProfilingScores.get("lc"));		
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
