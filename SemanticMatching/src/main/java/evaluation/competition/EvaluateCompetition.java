package evaluation.competition;

import java.io.File;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;

import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class EvaluateCompetition {
	
	public static void main(String[] args) throws AlignmentException, URISyntaxException {
		
		String referenceAlignment = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-SUBSUMPTION.rdf";
		
		File singleAlignment = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ALIGNMENTS/COMPETITION/EVALUATION_COMPETITION/SUBSUMPTION/bibframe-schemaorg-STROMA-SUB.rdf");
		AlignmentParser parser = new AlignmentParser();
		URIAlignment alignmentFile = (URIAlignment) parser.parse(singleAlignment.toURI().toString());
		
		Evaluator.evaluateSingleAlignment(alignmentFile, referenceAlignment);
		
	}

}
