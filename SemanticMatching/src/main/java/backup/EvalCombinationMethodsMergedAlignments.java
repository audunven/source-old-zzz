package backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import matchercombination.AverageAggregation;
import matchercombination.CutThreshold;
import matchercombination.HarmonyEquivalence;
import matchercombination.HarmonySubsumption;
import matchercombination.MajorityVote;
import matchercombination.NaiveDescendingExtraction;
import matchercombination.ProfileWeight;
import matchercombination.ProfileWeightSubsumption;
import net.didion.jwnl.JWNLException;
import utilities.StringUtilities;

public class EvalCombinationMethodsMergedAlignments {

	//ATMONTO-AIRM || BIBFRAME-SCHEMAORG || OAEI2011
	final static String dataset = "BIBFRAME-SCHEMAORG";

	//EQUIVALENCE || SUBSUMPTION
	final static String relationType = "SUBSUMPTION";

	//WEIGHT || NOWEIGHT || SIGMOID
	final static String weightType = "SIGMOID";
	//final static String slope = "2";
	
	
	static boolean weighted;


	//IF OAEI
	static String onto1 = "303";
	static String onto2 = "304";

	static File ontoFile1 = null;
	static File ontoFile2 = null;
	static String wiki_vectorFile_normal = null;

	final static String prefix = "file:";

	static String storePath = null;
	static String evalPath = null;

	static String storePath_harmony = null;
	static String storePath_profileWeight = null;
	static String storePath_averageAgg = null;
	static String storePath_cutThreshold = null;
	static String storePath_majorityVote = null;

	static String referenceAlignment = null;

	static String evaluationOutput_harmony = null;
	static String evaluationOutput_profileWeight = null;
	static String evaluationOutput_averageAgg = null;
	static String evaluationOutput_cutThreshold = null;
	static String evaluationOutput_majorityVote = null;

	static String evaluationOutputExcel_harmony = null;
	static String evaluationOutputExcel_profileWeight = null;
	static String evaluationOutputExcel_averageAgg = null;
	static String evaluationOutputExcel_cutThreshold = null;
	static String evaluationOutputExcel_majorityVote = null;

	static String alignmentOutput_harmony = null;
	static String alignmentOutput_profileWeight = null;
	static String alignmentOutput_averageAgg= null;
	static String alignmentOutput_cutThreshold = null;
	static String alignmentOutput_majorityVote = null;

	static String alignmentFileName = null;


	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException, JWNLException {

		if (dataset.equalsIgnoreCase("ATMONTO-AIRM")) {

			ontoFile1 = new File("./files/_PHD_EVALUATION/"+dataset+"/ONTOLOGIES/ATMOntoCoreMerged.owl");
			ontoFile2 = new File("./files/_PHD_EVALUATION/"+dataset+"/ONTOLOGIES/airm-mono.owl");
			wiki_vectorFile_normal = "./files/_PHD_EVALUATION/EMBEDDINGS/skybrary_trained_ontology_tokens.txt";
			referenceAlignment = "./files/_PHD_EVALUATION/"+dataset+"/REFALIGN/ReferenceAlignment-"+dataset+"-" + relationType + ".rdf";

			storePath = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/INDIVIDUAL_ALIGNMENTS/"+ relationType + "_" +weightType;
			evalPath = storePath + "/EXCEL";

			storePath_harmony = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/HARMONY/"+relationType+"_"+weightType;
			storePath_profileWeight = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/PROFILEWEIGHT/"+relationType+"_"+weightType;
			storePath_averageAgg = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/"+relationType+"_"+weightType;
			storePath_cutThreshold = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/CUT_THRESHOLD/"+relationType+"_"+weightType;
			storePath_majorityVote = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/MAJORITYVOTE/"+relationType+"_"+weightType;

			evaluationOutput_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType+".txt";
			evaluationOutput_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType+".txt";
			evaluationOutput_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType+".txt";
			evaluationOutput_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType+".txt";
			evaluationOutput_majorityVote = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType+".txt";

			evaluationOutputExcel_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType;
			evaluationOutputExcel_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType;
			evaluationOutputExcel_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType;
			evaluationOutputExcel_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType;
			evaluationOutputExcel_majorityVote = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType;

			alignmentOutput_harmony = storePath_harmony + "/ALIGNMENTS/";
			alignmentOutput_profileWeight = storePath_profileWeight+ "/ALIGNMENTS/";
			alignmentOutput_averageAgg= storePath_averageAgg + "/ALIGNMENTS/";
			alignmentOutput_cutThreshold = storePath_cutThreshold + "/ALIGNMENTS/";
			alignmentOutput_majorityVote = storePath_majorityVote + "/ALIGNMENTS/";

			evalHarmony(storePath);
			evalProfileWeight(storePath);
			evalAverageAggregation(storePath);
			evalCutThreshold(storePath);
			evalMajorityVote(storePath);


		} else if (dataset.equalsIgnoreCase("BIBFRAME-SCHEMAORG")) {

			ontoFile1 = new File("./files/_PHD_EVALUATION/"+dataset+"/ONTOLOGIES/bibframe.rdf");
			ontoFile2 = new File("./files/_PHD_EVALUATION/"+dataset+"/ONTOLOGIES/schema-org.owl");
			wiki_vectorFile_normal = "./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
			referenceAlignment = "./files/_PHD_EVALUATION/"+dataset+"/REFALIGN/ReferenceAlignment-"+dataset+"-" + relationType + ".rdf";

			storePath = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ALIGNMENTS/INDIVIDUAL_ALIGNMENTS/"+ relationType + "_" +weightType;
			evalPath = storePath + "/EXCEL";

			storePath_harmony = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/HARMONY/"+relationType+"_"+weightType;
			storePath_profileWeight = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/PROFILEWEIGHT/"+relationType+"_"+weightType;
			storePath_averageAgg = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/AVERAGE/"+relationType+"_"+weightType;
			storePath_cutThreshold = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/CUT_THRESHOLD/"+relationType+"_"+weightType;
			storePath_majorityVote = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/MAJORITYVOTE/"+relationType+"_"+weightType;

			evaluationOutput_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType+".txt";
			evaluationOutput_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType+".txt";
			evaluationOutput_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType+".txt";
			evaluationOutput_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType+".txt";
			evaluationOutput_majorityVote = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType+".txt";

			evaluationOutputExcel_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType;
			evaluationOutputExcel_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType;
			evaluationOutputExcel_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType;
			evaluationOutputExcel_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType;
			evaluationOutputExcel_majorityVote = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType;

			alignmentOutput_harmony = storePath_harmony + "/ALIGNMENTS/";
			alignmentOutput_profileWeight = storePath_profileWeight+ "/ALIGNMENTS/";
			alignmentOutput_averageAgg= storePath_averageAgg + "/ALIGNMENTS/";
			alignmentOutput_cutThreshold = storePath_cutThreshold + "/ALIGNMENTS/";
			alignmentOutput_majorityVote = storePath_majorityVote + "/ALIGNMENTS/";

			evalHarmony(storePath);
			evalProfileWeight(storePath);
			evalAverageAggregation(storePath);
			evalCutThreshold(storePath);
			evalMajorityVote(storePath);

		} else if (dataset.equalsIgnoreCase("OAEI2011")) {

			ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + onto1+onto2 + "/" + onto1+onto2 + "-" + onto1 + ".rdf");
			ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + onto1+onto2 + "/" + onto1+onto2 + "-" + onto2 + ".rdf");
			wiki_vectorFile_normal = "./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
			referenceAlignment ="./files/_PHD_EVALUATION/OAEI2011/REFALIGN/" + onto1+onto2 + "/" + onto1 + "-" + onto2 + "-" +relationType+".rdf";

			storePath = "./files/_PHD_EVALUATION/OAEI2011/ALIGNMENTS/" + onto1+onto2+ "/INDIVIDUAL_ALIGNMENTS/"+ relationType + "_" +weightType;
			evalPath = storePath + "/EXCEL";

			storePath_harmony = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/" + onto1+onto2 + "/HARMONY/"+relationType+"_"+weightType;
			storePath_profileWeight = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/" + onto1+onto2 + "/PROFILEWEIGHT/"+relationType+"_"+weightType;
			storePath_averageAgg = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/" + onto1+onto2 +"/AVERAGE/"+relationType+"_"+weightType;
			storePath_cutThreshold = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/" + onto1+onto2 + "/CUT_THRESHOLD/"+relationType+"_"+weightType;
			storePath_majorityVote = "./files/_PHD_EVALUATION/"+dataset+"/ALIGNMENTS/" + onto1+onto2 + "/MAJORITYVOTE/"+relationType+"_"+weightType;

			evaluationOutput_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType+".txt";
			evaluationOutput_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType+".txt";
			evaluationOutput_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType+".txt";
			evaluationOutput_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType+".txt";
			evaluationOutput_cutThreshold = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType+".txt";

			evaluationOutputExcel_harmony = storePath_harmony+"/eval_HARMONY"+relationType+"_"+weightType;
			evaluationOutputExcel_profileWeight = storePath_profileWeight+"/eval_PROFILEWEIGHT"+relationType+"_"+weightType;
			evaluationOutputExcel_averageAgg = storePath_averageAgg+"/eval_AVERAGEAGGREGATION"+relationType+"_"+weightType;
			evaluationOutputExcel_cutThreshold = storePath_cutThreshold+"/eval_CUT_THRESHOLD"+relationType+"_"+weightType;
			evaluationOutputExcel_majorityVote = storePath_majorityVote+"/eval_MAJORITYVOTE"+relationType+"_"+weightType;

			alignmentOutput_harmony = storePath_harmony + "/ALIGNMENTS/";
			alignmentOutput_profileWeight = storePath_profileWeight+ "/ALIGNMENTS/";
			alignmentOutput_averageAgg= storePath_averageAgg + "/ALIGNMENTS/";
			alignmentOutput_cutThreshold = storePath_cutThreshold + "/ALIGNMENTS/";
			alignmentOutput_majorityVote = storePath_majorityVote + "/ALIGNMENTS/";

			evalHarmony(storePath);
			evalProfileWeight(storePath);
			evalAverageAggregation(storePath);
			evalCutThreshold(storePath);
			evalMajorityVote(storePath);

		}

	}



	public static void evalHarmony(String storePath) throws AlignmentException, IOException, URISyntaxException {

		URIAlignment harmonyAlignment = new URIAlignment();

		boolean normalise = false;

		if (relationType.equalsIgnoreCase("EQUIVALENCE")) {
			harmonyAlignment = HarmonyEquivalence.computeHarmonyAlignmentFromFolder(storePath);
			normalise = true;
		} else {
			harmonyAlignment = HarmonySubsumption.computeHarmonyAlignmentFromFolder(storePath);
		}

		URIAlignment storedHarmonyAlignment = new URIAlignment();

		//if the alignment is an equivalence alignment we run the Naive Descending Extraction to enforce 1-1 relations, otherwise if the alignment holds subsumption relations we just keep the alignment as it is. 
		if (normalise == true) {
			storedHarmonyAlignment = NaiveDescendingExtraction.extractOneToOneRelations(harmonyAlignment);
		} else {
			storedHarmonyAlignment = (URIAlignment) harmonyAlignment.clone();
		}
		
		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URI onto1URI = harmonyAlignment.getOntology1URI();
		URI onto2URI = harmonyAlignment.getOntology2URI();
		storedHarmonyAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		//evaluate Harmony alignment
		double[] confidences = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		URIAlignment evalAlignment = new URIAlignment();

		//parse the reference alignment
		AlignmentParser parser = new AlignmentParser();
		URIAlignment refalign = (URIAlignment) parser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		//storing the alignment
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;


		for (int i = 0; i < confidences.length; i++) {
			EvaluationScore evalScore = new EvaluationScore();

			evalAlignment = (URIAlignment)(storedHarmonyAlignment.clone());			
			evalAlignment.cut(confidences[i]);

			//perform the evaluation here...				
			eval = new PRecEvaluator(refalign, evalAlignment);
			eval.eval(p);
			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());
			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);

			//put the evalation score according to each confidence value in the map
			evaluationMap.put(String.valueOf(confidences[i]), evalScore);

			//store the alignment file for this confidence value
			outputAlignment = new File(alignmentOutput_harmony + "HARMONY_" + relationType + "_" + weightType + "_" + confidences[i] + ".rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evalAlignment.render(renderer);
			writer.flush();
			writer.close();

			Evaluator.evaluateSingleAlignment("HADAPT with confidence " + confidences[i], evalAlignment, referenceAlignment, alignmentOutput_harmony + "HARMONY_" + relationType + "_" + weightType + "_" + confidences[i] + ".txt");

		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationOutputExcel_harmony);

	}


	public static void evalProfileWeight(String storePath) throws AlignmentException, URISyntaxException, IOException {

		URIAlignment profileWeightAlignment = new URIAlignment();

		boolean normalise = false;

		if (relationType.equalsIgnoreCase("EQUIVALENCE")) {
			profileWeightAlignment = ProfileWeight.computeProfileWeightingEquivalence(storePath);
			normalise = true;
		} else {
			profileWeightAlignment = ProfileWeightSubsumption.computeProfileWeightingSubsumption(storePath);
		}



		URIAlignment storedProfileWeightAlignment = new URIAlignment();

		if (normalise == true) {
			
			storedProfileWeightAlignment = NaiveDescendingExtraction.extractOneToOneRelations(profileWeightAlignment);

		} else {
			
			storedProfileWeightAlignment = (URIAlignment) profileWeightAlignment.clone();
		}
		
		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URI onto1URI = profileWeightAlignment.getOntology1URI();
		URI onto2URI = profileWeightAlignment.getOntology2URI();
		storedProfileWeightAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		//evaluate ProfileWeight alignment
		double[] confidences = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		URIAlignment evalAlignment = new URIAlignment();

		//parse the reference alignment
		AlignmentParser parser = new AlignmentParser();
		URIAlignment refalign = (URIAlignment) parser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		//storing the alignment
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;


		for (int i = 0; i < confidences.length; i++) {
			EvaluationScore evalScore = new EvaluationScore();

			evalAlignment = (URIAlignment)(storedProfileWeightAlignment.clone());
			evalAlignment.cut(confidences[i]);

			System.out.println("At confidence  " + confidences[i] + " the profileWeightAlignment contains " + evalAlignment.nbCells() + " relations");

			//perform the evaluation here...				
			eval = new PRecEvaluator(refalign, evalAlignment);

			eval.eval(p);

			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);
			evaluationMap.put(String.valueOf(confidences[i]), evalScore);

			//store the alignment file for this confidence value
			outputAlignment = new File(alignmentOutput_profileWeight + "PROFILEWEIGHT_" + relationType + "_" + weightType + "_" + confidences[i] + ".rdf");


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evalAlignment.render(renderer);
			writer.flush();
			writer.close();

			Evaluator.evaluateSingleAlignment("Profile Weight with confidence " + confidences[i], evalAlignment, referenceAlignment, alignmentOutput_profileWeight + "PROFILEWEIGHT_" + relationType + "_" + weightType + "_" + confidences[i] + ".txt");

		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationOutputExcel_profileWeight);



	}

	public static void evalAverageAggregation (String storePath) throws AlignmentException, URISyntaxException, IOException {

		//map that holds an average aggregated alignment per confidence score (from 0.6, ..., 1.0)
		Map<Double, URIAlignment> averageAggregatedAlignments = AverageAggregation.getAverageAggregatedAlignmentMap(storePath);

		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		URIAlignment evalAlignment = new URIAlignment();

		//parse the reference alignment
		AlignmentParser parser = new AlignmentParser();
		URIAlignment refalign = (URIAlignment) parser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		//storing the alignment
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		for (Entry<Double, URIAlignment> e : averageAggregatedAlignments.entrySet()) {

			EvaluationScore evalScore = new EvaluationScore();
			
			if (relationType.equalsIgnoreCase("EQUIVALENCE")) {
				
				evalAlignment = NaiveDescendingExtraction.extractOneToOneRelations((URIAlignment) e.getValue());
				
			} else {
				
				evalAlignment = (URIAlignment)(e.getValue().clone());
				
			}

			evalAlignment.cut(e.getKey());

			//perform the evaluation here...				
			eval = new PRecEvaluator(refalign, evalAlignment);

			eval.eval(p);

			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);
			evaluationMap.put(String.valueOf(e.getKey()), evalScore);

			//store the alignment file for this confidence value
			outputAlignment = new File(alignmentOutput_averageAgg + "AVERAGE_AGG_" + relationType + "_" + weightType + "_" + e.getKey() + ".rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evalAlignment.render(renderer);
			writer.flush();
			writer.close();

			Evaluator.evaluateSingleAlignment("Average Aggregation with confidence " + e.getKey(), evalAlignment, referenceAlignment, alignmentOutput_averageAgg + "AVERAGE_AGG_" + relationType + "_" + weightType + "_" + e.getKey() + ".txt");


		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationOutputExcel_averageAgg);		


	}

	public static void evalCutThreshold (String storePath) throws AlignmentException, IOException, URISyntaxException {

		//map that holds an average aggregated alignment per confidence score (from 0.6, ..., 1.0)
		Map<Double, URIAlignment> cutTresholdAlignments = CutThreshold.cutThresholdAlignment(storePath);

		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		URIAlignment evalAlignment = new URIAlignment();

		//parse the reference alignment
		AlignmentParser parser = new AlignmentParser();
		URIAlignment refalign = (URIAlignment) parser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		//storing the alignment
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		for (Entry<Double, URIAlignment> e : cutTresholdAlignments.entrySet()) {

			EvaluationScore evalScore = new EvaluationScore();
			

			if (relationType.equalsIgnoreCase("EQUIVALENCE")) {
				evalAlignment = NaiveDescendingExtraction.extractOneToOneRelations((URIAlignment) e.getValue());	
			} else {
				evalAlignment = (URIAlignment)(e.getValue().clone());
			}

			evalAlignment.cut(e.getKey());

			//perform the evaluation here...				
			eval = new PRecEvaluator(refalign, evalAlignment);

			eval.eval(p);

			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);
			evaluationMap.put(String.valueOf(e.getKey()), evalScore);

			//store the alignment file for this confidence value
			outputAlignment = new File(alignmentOutput_cutThreshold + "CUTTHRESHOLD_" + relationType + "_" + weightType + "_" + e.getKey() + ".rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evalAlignment.render(renderer);
			writer.flush();
			writer.close();

			Evaluator.evaluateSingleAlignment("Cut Threshold with confidence " + e.getKey(), evalAlignment, referenceAlignment, alignmentOutput_cutThreshold + "CUTTHRESHOLD_" + relationType + "_" + weightType + "_" + e.getKey() + ".txt");


		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationOutputExcel_cutThreshold);	

	}

	public static void evalMajorityVote (String storePath) throws AlignmentException, IOException, URISyntaxException {

		//map that holds an average aggregated alignment per confidence score (from 0.6, ..., 1.0)
		Map<Double, URIAlignment> majorityVotedAlignments = MajorityVote.getMajorityVotes(storePath);		
		double precision = 0;
		double recall = 0;
		double fMeasure = 0;
		PRecEvaluator eval = null;
		Properties p = new Properties();
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		URIAlignment evalAlignment = new URIAlignment();

		//parse the reference alignment
		AlignmentParser parser = new AlignmentParser();
		URIAlignment refalign = (URIAlignment) parser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignment)));

		//storing the alignment
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		for (Entry<Double, URIAlignment> e : majorityVotedAlignments.entrySet()) {

			EvaluationScore evalScore = new EvaluationScore();
			

			if (relationType.equalsIgnoreCase("EQUIVALENCE")) {
				
				evalAlignment = NaiveDescendingExtraction.extractOneToOneRelations((URIAlignment) e.getValue());	
				
			} else {
				
				evalAlignment = (URIAlignment)(e.getValue().clone());
				
			}

			evalAlignment.cut(e.getKey());

			//perform the evaluation here...				
			eval = new PRecEvaluator(refalign, evalAlignment);

			eval.eval(p);

			precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
			recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

			evalScore.setPrecision(precision);
			evalScore.setRecall(recall);
			evalScore.setfMeasure(fMeasure);
			evaluationMap.put(String.valueOf(e.getKey()), evalScore);

			//store the alignment file for this confidence value
			outputAlignment = new File(alignmentOutput_majorityVote + "MAJORITYVOTE" + relationType + "_" + weightType + "_" + e.getKey() + ".rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evalAlignment.render(renderer);
			writer.flush();
			writer.close();

			Evaluator.evaluateSingleAlignment("Majority Vote with confidence " + e.getKey(), evalAlignment, referenceAlignment, alignmentOutput_majorityVote+ "MAJORITYVOTE" + relationType + "_" + weightType + "_" + e.getKey() + ".txt");


		}

		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationOutputExcel_majorityVote);	

	}



}


