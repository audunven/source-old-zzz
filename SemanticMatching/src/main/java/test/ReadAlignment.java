package test;

import java.io.File;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class ReadAlignment {

	public static void main(String[] args) throws AlignmentException {
		File inputAlignmentFile = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ALIGNMENTS/HARMONY_NOWEIGHT/SUBSUMPTION/ATMOntoCoreMerged-airm-mono-AncestorMatcher.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment originalAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());
		
		System.out.println("The alignment contains " + originalAlignment.nbCells() + " relations");
		
		for (Cell c : originalAlignment) {
			System.out.println(c.getObject1AsURI());
		}

	}

}
