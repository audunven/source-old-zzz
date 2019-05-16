package evaluation.combination.oaei;

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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.AlignmentConflictResolution;
import matchercombination.NaiveDescendingExtraction;
import mismatchdetection.MismatchDetection;
import net.didion.jwnl.JWNLException;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class EvalCutThresholdCombinationOAEI {

	public static void main(String[] args) throws AlignmentException, URISyntaxException, OWLOntologyCreationException, JWNLException, IOException {

		String dataset = "OAEI2011";
		String EQ_folder = null;
		String SUB_folder = null;
		File sourceOntologyFile = null;
		File targetOntologyFile = null;

		String referenceAlignment = null;

		String[] ontos = new String[] {"301302", "301303", "301304", "302303", "302304", "303304"};

		AlignmentParser aparser = null;
		URIAlignment refalign = null;
		URIAlignment eq_alignments = null;
		URIAlignment nda_eq_alignment = null;
		URIAlignment noMismatchEQAlignment = null;
		URIAlignment sub_alignment = null;
		URIAlignment mergedEQAndSubAlignment = null;
		URIAlignment nonConflictedMergedAlignment = null;
		File outputAlignment = null;

		for (int i = 0; i < ontos.length; i++) {

			sourceOntologyFile = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + ontos[i] + "/" + ontos[i] + "-" + ontos[i].substring(0, 3) + ".rdf");
			targetOntologyFile = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + ontos[i] + "/" + ontos[i] + "-" + ontos[i].substring(3, ontos[i].length()) + ".rdf");
			referenceAlignment ="./files/_PHD_EVALUATION/OAEI2011/REFALIGN/" + ontos[i] + "/" + ontos[i].substring(0, 3) + "-" + ontos[i].substring(3, ontos[i].length()) + "-EQ_SUB.rdf";
			EQ_folder = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/"+ontos[i]+"/CUT_THRESHOLD/MERGED_NOWEIGHT/EQ";
			SUB_folder = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/"+ontos[i]+"/CUT_THRESHOLD/MERGED_NOWEIGHT/SUB";

			aparser = new AlignmentParser(0);
			refalign = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

			//combine EQ alignments into a single alignment
			eq_alignments = combineAlignments(EQ_folder);

			//enforce 1-1 eq relations using naive descending extraction
			nda_eq_alignment = NaiveDescendingExtraction.extractOneToOneRelations(eq_alignments);

			//filter out potential mismatches
			noMismatchEQAlignment = MismatchDetection.removeMismatches(nda_eq_alignment, sourceOntologyFile, targetOntologyFile);

			//combine all SUB alignments into a single alignment
			sub_alignment = combineAlignments(SUB_folder);

			//merge the "merged" EQ alignment and SUB alignment 
			mergedEQAndSubAlignment = AlignmentOperations.combineEQAndSUBAlignments(noMismatchEQAlignment, sub_alignment);

			//resolve potential conflicts in the merged EQ and SUB alignment
			nonConflictedMergedAlignment = AlignmentConflictResolution.resolveAlignmentConflict(mergedEQAndSubAlignment);


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

				outputAlignment = new File("./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/"+ontos[i]+"/CUT_THRESHOLD/MERGED_NOWEIGHT/CutThreshold"+dataset+"_"+ontos[i]+"_"+conf+".rdf");

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

			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/"+ontos[i]+"/CUT_THRESHOLD/MERGED_NOWEIGHT_"+ontos[i]);

		}

	}

	/**
	 * Combines (by union) all individual alignments in a folder into a combined alignment
	 * @param inputAlignments
	 * @return
	 * @throws AlignmentException
	 * @throws URISyntaxException
	   May 3, 2019
	 */
	private static URIAlignment combineAlignments (String alignmentFolder) throws AlignmentException, URISyntaxException {

		ArrayList<Alignment> inputAlignments = new ArrayList<Alignment>();
		URIAlignment combinedAlignment = new URIAlignment();

		AlignmentParser aparser = new AlignmentParser(0);

		File folder = new File(alignmentFolder);
		File[] filesInDir = folder.listFiles();
		URIAlignment thisAlignment = null;
		String URI = null;

		for (int i = 0; i < filesInDir.length; i++) {

			URI = StringUtilities.convertToFileURL(alignmentFolder) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			thisAlignment = (URIAlignment) aparser.parse(new URI(URI));
			inputAlignments.add(thisAlignment);
		}

		URI onto1URI = null;
		URI onto2URI = null;

		for (Alignment a : inputAlignments) {

			onto1URI = a.getOntology1URI();
			onto2URI = a.getOntology2URI();

			for (Cell c : a) {

				combinedAlignment.addAlignCell(c.getId(), c.getObject1(), c.getObject2(), c.getRelation(), c.getStrength());
			}
		}

		combinedAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		return combinedAlignment;

	}

}
