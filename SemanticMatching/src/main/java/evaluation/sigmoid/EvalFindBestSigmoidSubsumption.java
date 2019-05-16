package evaluation.sigmoid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.AverageAggregation;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.ContextSubsumptionMatcher;
import subsumptionmatching.DefinitionSubsumptionMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher;
import utilities.StringUtilities;

public class EvalFindBestSigmoidSubsumption {
	
	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException, IOException {
		
//		File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
//		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-SUBSUMPTION.rdf";
//		AlignmentParser refAlignParser = new AlignmentParser(0);
//		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
//		String vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/skybrary_trained_ontology_tokens.txt";
		
//		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
//		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-SUBSUMPTION.rdf";
//		AlignmentParser refAlignParser = new AlignmentParser(0);
//		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
//		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
		

		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-301.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-304.rdf");
		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/OAEI2011/REFALIGN/301304/301-304-SUBSUMPTION.rdf";
		AlignmentParser refAlignParser = new AlignmentParser(0);
		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology targetOntology = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		int slopes[] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		double confidenceThreshold = 0.6;
		double rangeMin = 0.5;
		double rangeMax = 0.7;
		
		Properties params = new Properties();
		PRecEvaluator eval = null;
		String fMeasure = null;
		AlignmentProcess a = null;
		BasicAlignment cmAlignment = new BasicAlignment();
		BasicAlignment csmAlignment = new BasicAlignment();
		BasicAlignment lsmAlignment = new BasicAlignment();
		BasicAlignment dsmAlignment = new BasicAlignment();
		
		Properties p = new Properties();
		
		Map<Integer, ArrayList<URIAlignment>> allAlignments = new HashMap<Integer, ArrayList<URIAlignment>>();
		ArrayList<URIAlignment> alignmentList = new ArrayList<URIAlignment>();
		URIAlignment averageAggAlignment = new URIAlignment();
		
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		
		for (int i = 0; i < slopes.length; i++) {
			
			System.out.println("Computing the CM alignment");
			a = new CompoundMatcher(0.14, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			cmAlignment = (BasicAlignment)(a.clone());
			cmAlignment.cut(confidenceThreshold);	
			alignmentList.add(cmAlignment.toURIAlignment());
			System.out.println("Adding the CM alignment with " + cmAlignment.nbCells() + " relations into the alignmentList");
			
			System.out.println("Computing the CSM alignment");
			a = new ContextSubsumptionMatcher(sourceOntology, targetOntology, 0.51, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			csmAlignment = (BasicAlignment)(a.clone());
			csmAlignment.cut(confidenceThreshold);
			alignmentList.add(csmAlignment.toURIAlignment());
			System.out.println("Adding the CSM alignment with " + csmAlignment.nbCells() + " relations into the alignmentList");

			System.out.println("Computing the LSM alignment");
			a = new LexicalSubsumptionMatcher(0.68, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			lsmAlignment = (BasicAlignment)(a.clone());
			lsmAlignment.cut(confidenceThreshold);
			alignmentList.add(lsmAlignment.toURIAlignment());
			System.out.println("Adding the LSM alignment with " + lsmAlignment.nbCells() + " relations into the alignmentList");
			
			System.out.println("Computing the DSM alignment");
			a = new DefinitionSubsumptionMatcher(sourceOntology, targetOntology, 0.61, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			dsmAlignment = (BasicAlignment)(a.clone());
			dsmAlignment.cut(confidenceThreshold);	
			alignmentList.add(dsmAlignment.toURIAlignment());
			System.out.println("Adding the DSM alignment with " + dsmAlignment.nbCells() + " relations into the alignmentList");
			
			
			System.out.println("Adding " + alignmentList.size() + " alignments into the map for slope " + slopes[i]);
			allAlignments.put(slopes[i], alignmentList);
			averageAggAlignment = AverageAggregation.getAverageAggregatedAlignment(alignmentList);

			eval = new PRecEvaluator(referenceAlignment, averageAggAlignment);
			eval.eval(p);
			fMeasure = eval.getResults().getProperty("fmeasure").toString();
			
			EvaluationScore evalScore = new EvaluationScore();

			evalScore.setfMeasure(eval.getFmeasure());
			evalScore.setPrecision(eval.getPrecision());
			evalScore.setRecall(eval.getRecall());

			//put the evalation score according to each confidence value in the map
			evaluationMap.put(String.valueOf(slopes[i]), evalScore);
			
			System.out.println("The F-measure at slope " + slopes[i] + " and with confidence cut " + confidenceThreshold + " is " + fMeasure);
			
			Evaluator.evaluateSingleAlignment("AverageAggSigmoidTest_ATM_" + slopes[i], averageAggAlignment, referenceAlignmentFileName, "./files/_PHD_EVALUATION/MATCHERTESTING/BEST_SIGMOID_SLOPE/OAEI2011/SUBSUMPTION/AverageAggSigmoidTest_OAEI2011_SUB" + slopes[i] + ".txt");

		}
		
		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, "./files/_PHD_EVALUATION/MATCHERTESTING/OAEI2011_301304-SUB");
		
	}
	

}
