package evaluation.sigmoid;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import equivalencematching.DefinitionEquivalenceMatcher;
import equivalencematching.GraphEquivalenceMatcher;
import equivalencematching.LexicalEquivalenceMatcher;
import equivalencematching.PropertyEquivalenceMatcher;
import equivalencematching.WordEmbeddingMatcher;
import evaluation.general.EvaluationScore;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.Graph;
import matchercombination.AverageAggregation;
import utilities.StringUtilities;

public class EvalFindBestSigmoid {
	
	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException, IOException {
		
//		File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
//		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQUIVALENCE.rdf";
//		AlignmentParser refAlignParser = new AlignmentParser(0);
//		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
//		String vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/skybrary_trained_ontology_tokens.txt";
		
		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQUIVALENCE.rdf";
		AlignmentParser refAlignParser = new AlignmentParser(0);
		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
		

//		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-301.rdf");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-304.rdf");
//		String referenceAlignmentFileName = "./files/_PHD_EVALUATION/OAEI2011/REFALIGN/301304/301-304-EQUIVALENCE.rdf";
//		AlignmentParser refAlignParser = new AlignmentParser(0);
//		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
//		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";

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
		BasicAlignment wemAlignment = new BasicAlignment();
		BasicAlignment demAlignment = new BasicAlignment();
		BasicAlignment lemAlignment = new BasicAlignment();
		BasicAlignment pemAlignment = new BasicAlignment();
		BasicAlignment gemAlignment = new BasicAlignment();
		
		Properties p = new Properties();
		
		Map<Integer, ArrayList<URIAlignment>> allAlignments = new HashMap<Integer, ArrayList<URIAlignment>>();
		ArrayList<URIAlignment> alignmentList = new ArrayList<URIAlignment>();
		URIAlignment averageAggAlignment = new URIAlignment();
		
		Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();
		
		for (int i = 0; i < slopes.length; i++) {
			
			System.out.println("Computing the WEM alignment");
			a = new WordEmbeddingMatcher(sourceOntology, targetOntology, vectorFile, 0.80, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			wemAlignment = (BasicAlignment)(a.clone());
			wemAlignment.normalise();
			wemAlignment.cut(confidenceThreshold);	
			alignmentList.add(wemAlignment.toURIAlignment());
			System.out.println("Adding the WEM alignment with " + wemAlignment.nbCells() + " relations into the alignmentList");
			
			System.out.println("Computing the DEM alignment");
			a = new DefinitionEquivalenceMatcher(sourceOntology, targetOntology, vectorFile, 0.80, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			demAlignment = (BasicAlignment)(a.clone());
			demAlignment.normalise();
			demAlignment.cut(confidenceThreshold);
			alignmentList.add(demAlignment.toURIAlignment());
			System.out.println("Adding the DEM alignment with " + demAlignment.nbCells() + " relations into the alignmentList");

			System.out.println("Computing the LEM alignment");
			a = new LexicalEquivalenceMatcher(0.68, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			lemAlignment = (BasicAlignment)(a.clone());
			lemAlignment.normalise();
			lemAlignment.cut(confidenceThreshold);
			alignmentList.add(lemAlignment.toURIAlignment());
			System.out.println("Adding the LEM alignment with " + lemAlignment.nbCells() + " relations into the alignmentList");
			
			System.out.println("Computing the PEM alignment");
			a = new PropertyEquivalenceMatcher(sourceOntology, targetOntology, 0.33, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			pemAlignment = (BasicAlignment)(a.clone());
			pemAlignment.normalise();
			pemAlignment.cut(confidenceThreshold);
			alignmentList.add(pemAlignment.toURIAlignment());
			System.out.println("Adding the PEM alignment with " + pemAlignment.nbCells() + " relations into the alignmentList");
			
			System.out.println("Computing the GEM alignment");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);	
			System.out.println("Creating a new NEO4J database");
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			String ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			String ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());
			Label labelO1 = null;
			Label labelO2 = null;
			Graph creator = null;
			
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );

			System.out.println("Creating ontology graphs");
			creator = new Graph(db);

			creator.createOntologyGraph(sourceOntology, labelO1);
			creator.createOntologyGraph(targetOntology, labelO2);
			
			a = new GraphEquivalenceMatcher(ontologyParameter1, ontologyParameter2, db, 0.51, slopes[i], rangeMin, rangeMax);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			gemAlignment = (BasicAlignment)(a.clone());
			gemAlignment.normalise();
			gemAlignment.cut(confidenceThreshold);
			alignmentList.add(gemAlignment.toURIAlignment());
			System.out.println("Adding the GEM alignment with " + gemAlignment.nbCells() + " relations into the alignmentList");
			
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
			
			Evaluator.evaluateSingleAlignment("AverageAggSigmoidTest_CH_" + slopes[i], averageAggAlignment, referenceAlignmentFileName, "./files/_PHD_EVALUATION/MATCHERTESTING/BEST_SIGMOID_SLOPE/BIBFRAME-SCHEMAORG/AverageAggSigmoidTest_CH_" + slopes[i] + ".txt");

		}
		
		Evaluator.evaluateSingleMatcherThresholds(evaluationMap, "./files/_PHD_EVALUATION/MATCHERTESTING/BIBFRAMESCHEMAORG");
		
	}
	

}
