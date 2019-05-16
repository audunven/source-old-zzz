package test;

import java.io.File;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import equivalencematching.WordEmbeddingMatcher;
import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import org.semanticweb.owl.align.Cell;

public class TestMatchers {
	
	static File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
	static File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
	static String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";
	static String referenceAlignmentEQ = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQUIVALENCE.rdf";
	
	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException {

		
		System.err.println("Computing WEM alignment");
		URIAlignment WEMAlignment = WordEmbeddingMatcher.returnWEMAlignment(ontoFile1, ontoFile2, vectorFile, 0.83);
		
		System.out.println("The WEMAlignment contains " + WEMAlignment.nbCells() + " relations");
		
		WEMAlignment.cut(0.6);
		
		System.out.println("The WEMAlignment now contains " + WEMAlignment.nbCells() + " relations");
		
		for (Cell c : WEMAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}
		
		//evaluate
		Evaluator.evaluateSingleAlignment(WEMAlignment, referenceAlignmentEQ);
		
		
	}

}
