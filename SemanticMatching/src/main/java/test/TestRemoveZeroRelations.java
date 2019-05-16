package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class TestRemoveZeroRelations {
	
	public static void main(String[] args) throws AlignmentException, IOException {
		
		File alignmentFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ALIGNMENTS/MAX/EQUIVALENCE/bibframe-schema-org-PropertyMatcher.rdf");
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment alignment = (BasicAlignment) parser.parse(alignmentFile.toURI().toString());
		System.out.println("The old alignment contains " + alignment.nbCells());
		URIAlignment newAlignment = removeZeroConfidenceRelations(alignment);
		System.out.println("The new alignment contains " + newAlignment.nbCells());
		
		AlignmentVisitor renderer = null;	
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ALIGNMENTS/MAX/NONZERO/bibframe-schema-org-PropertyMatcher.rdf")), true); 
		renderer = new RDFRendererVisitor(writer);
		
		newAlignment.render(renderer);
		writer.flush();
		writer.close();
		
	}
	
	public static URIAlignment removeZeroConfidenceRelations(BasicAlignment inputAlignment) throws AlignmentException {
		
		URIAlignment alignmentWithNonZeroRelations = new URIAlignment();
		
		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		
		System.out.println("onto1URI is " + onto1URI);
		System.out.println("onto2URI is " + onto2URI);
		
		alignmentWithNonZeroRelations.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
		
		
		for (Cell c : inputAlignment) {
			if (c.getStrength() != 0.0) {
				
				alignmentWithNonZeroRelations.addAlignCell(c.getId(), c.getObject1(), c.getObject2(), c.getRelation(), c.getStrength());
				
			} 
		}
		
		return alignmentWithNonZeroRelations;
		

	}

}
