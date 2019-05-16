package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class TestCellID {

	public static void main(String[] args) throws AlignmentException, IOException {
		
		URIAlignment a = new URIAlignment();
		
		a.addAlignCell("Test", URI.create("https://data.nasa.gov/ontologies/atmonto/ATM#Car"), URI.create("http://www.project-best.eu/owl/airm-mono/airm.owl#Automobile"), "=", 0.9);
		a.addAlignCell("Test2", URI.create("https://data.nasa.gov/ontologies/atmonto/ATM#Boat"), URI.create("http://www.project-best.eu/owl/airm-mono/airm.owl#Ship"), "=", 1.0);
		
		URI onto1URI = URI.create("https://data.nasa.gov/ontologies/atmonto/ATM#");
		URI onto2URI = URI.create("http://www.project-best.eu/owl/airm-mono/airm.owl");
		
		a.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
		
		for (Cell c : a) {
			System.out.println(c.getId() + " " + c.getObject1() + " " + c.getObject2());
		}
		
		String path = "./files/cellIDTest.rdf";
		
		File outputAlignment = new File(path);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		
		a.render(renderer);

		writer.flush();
		writer.close();
		
		//String path = "./files/cellIDTest.rdf";
		String anotherPath = "./files/alignment5.rdf";
		
		AlignmentParser parser = new AlignmentParser();		
		URIAlignment referenceAlignment = (URIAlignment) parser.parse(new File(path).toURI().toString());
		
		System.out.println("Printing referenceAlignment");
		for (Cell c : referenceAlignment) {
			System.out.println(c.getId() + " " + c.getObject1() + " " + c.getObject2());
		}
		

	}

}
