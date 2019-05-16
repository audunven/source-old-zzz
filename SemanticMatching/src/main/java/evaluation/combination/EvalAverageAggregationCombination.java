package evaluation.combination;

import java.io.BufferedWriter;
import java.io.File;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.AlignmentConflictResolution;
import matchercombination.AverageAggregation;
import matchercombination.NaiveDescendingExtraction;
import mismatchdetection.MismatchDetection;
import net.didion.jwnl.JWNLException;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class EvalAverageAggregationCombination {

	public static void main(String[] args) throws AlignmentException, URISyntaxException, OWLOntologyCreationException, JWNLException, IOException {

		//ATMONTO-AIRM || BIBFRAME-SCHEMAORG || OAEI2011
		String dataset = "ATMONTO-AIRM";

		//the two ontology files whose alignments are combined
//		File sourceOntologyFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
//		File targetOntologyFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
//		String referenceAlignment = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQ-SUB.rdf";

		File sourceOntologyFile = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
		File targetOntologyFile = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
		String referenceAlignment = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQ-SUB.rdf";
		
		//folder holding all individual EQ and SUB alignments at threshold 0.1
		String EQ_folder = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/MERGED_NOWEIGHT/EQ";
		String SUB_folder = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/MERGED_NOWEIGHT/SUB";

		//put all EQ alignments in the EQ_folder into an ArrayList after enforcing 1-1 relations and removing mismatches
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment refalign = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		File folder = new File(EQ_folder);
		File[] filesInDir = folder.listFiles();
		URIAlignment thisAlignment = null;

		String URI = null;

		ArrayList<URIAlignment> eqAlignments = new ArrayList<URIAlignment>();

		URIAlignment ndaAlignment = new URIAlignment();
		URIAlignment noMIsmatchAlignment = new URIAlignment();

		for (int i = 0; i < filesInDir.length; i++) {
			URI = StringUtilities.convertToFileURL(EQ_folder) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			thisAlignment = (URIAlignment) aparser.parse(new URI(URI));

			//enforce 1-1 relations
			ndaAlignment = NaiveDescendingExtraction.extractOneToOneRelations(thisAlignment);

			//remove mismatches
			noMIsmatchAlignment = MismatchDetection.removeMismatches(ndaAlignment, sourceOntologyFile, targetOntologyFile);

			eqAlignments.add(noMIsmatchAlignment);

		}

		//put all SUB alignments in the SUB_folder into an ArrayList
		folder = new File(SUB_folder);
		filesInDir = folder.listFiles();
		ArrayList<URIAlignment> subAlignments = new ArrayList<URIAlignment>();

		for (int i = 0; i < filesInDir.length; i++) {
			URI = StringUtilities.convertToFileURL(SUB_folder) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			thisAlignment = (URIAlignment) aparser.parse(new URI(URI));
			subAlignments.add(thisAlignment);

		}

		URIAlignment averageAggEQAlignment = AverageAggregation.getAverageAggregatedAlignment(eqAlignments);
		URIAlignment averageAggSUBAlignment = AverageAggregation.getAverageAggregatedAlignment(subAlignments);

		//merge the "merged" EQ alignment and SUB alignment 
		URIAlignment mergedEQAndSubAlignment = AlignmentOperations.combineEQAndSUBAlignments(averageAggEQAlignment, averageAggSUBAlignment);

		//resolve potential conflicts in the merged EQ and SUB alignment
		URIAlignment nonConflictedMergedAlignment = AlignmentConflictResolution.resolveAlignmentConflict(mergedEQAndSubAlignment);
		
		//store the merged alignment
		File outputAlignment = null;

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		
		double[] confidence = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		
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
			outputAlignment = new File("./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/MERGED_NOWEIGHT/AverageAggregation"+dataset+"_"+conf+".rdf");
			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			nonConflictedMergedAlignment.render(renderer);
			writer.flush();
			writer.close();
			//print evaluation results to console
			Evaluator.evaluateSingleAlignment("Cut Threshold " + conf, nonConflictedMergedAlignment, referenceAlignment);
		}
		
		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/MERGED_NOWEIGHT");
		

	}

}
