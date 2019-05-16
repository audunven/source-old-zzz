package test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class TestMax {

	//test method
	public static void main(String[] args) throws AlignmentException, URISyntaxException {

		
		
		//	String avgAlignmentFolder = "./files/AverageAggregation";	
		//	URIAlignment avgAlignment = getAverageAggregatedAlignment(avgAlignmentFolder);

		//		System.out.println("Printing average aggregation alignment");
		//		for (Cell c : avgAlignment) {
		//			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		//		}


		String maxAlignmentFolder = "./files/MaxAggregation";
		URIAlignment maxAlignment = getMaxAggregatedAlignment(maxAlignmentFolder);

		System.out.println("\nPrinting max aggregation alignment");
		for (Cell c : maxAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

		//Evaluating
		String referenceAlignment = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQUIVALENCE.rdf";
		Evaluator.evaluateSingleAlignment(maxAlignment, referenceAlignment);


	}


	/**
	 * Returns an alignment that consists of a set of relations where the strength of each relation is the maximum of this cell´s strength across all input alignments 
	 * @param folderName
	 * @return
	 * @throws AlignmentException
	 * @throws URISyntaxException
	   Mar 11, 2019
	 */
	public static URIAlignment getMaxAggregatedAlignment(String folderName) throws AlignmentException, URISyntaxException {
		URIAlignment maxAlignment = new URIAlignment();

		//create an ArrayList of all alignments in folder
		ArrayList<URIAlignment> inputAlignments = new ArrayList<URIAlignment>();
		AlignmentParser aparser = new AlignmentParser(0);

		File folder = new File(folderName);
		File[] filesInDir = folder.listFiles();
		URIAlignment thisAlignment = null;

		URI sourceURI = null;
		URI targetURI = null;
		
		String URI = null;

		for (int i = 0; i < filesInDir.length; i++) {

			URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			
			thisAlignment = (URIAlignment) aparser.parse(new URI(URI));
			inputAlignments.add(thisAlignment);

			sourceURI = thisAlignment.getOntology1URI();
			targetURI = thisAlignment.getOntology2URI();

		}

		maxAlignment.init( sourceURI, targetURI, A5AlgebraRelation.class, BasicConfidence.class );

		ArrayList<Cell> allCells = new ArrayList<Cell>();

		// get all cells in all alignments and put them in a set
		for (Alignment a : inputAlignments) {
			// for all cells C in each input alignment
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		int allCellsSize = allCells.size();
		System.out.println("\nThere are " + allCellsSize + " in allCells");
		
		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double max;
		double bestStrength;
		int counter = 0;

		for (Cell currentCell : allCells) {			
			counter++;
			System.out.println("Processing cell " + counter + " of " + allCellsSize);

			//get the strength of currentCell
			thisStrength = 0;
			max = 0;
			bestStrength = 0;

			if (!processed.contains(currentCell)) {

				// get all cells that has the same object1 as currentCell
				ArrayList<Cell> sameObj1 = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1())) {
						sameObj1.add(c);
					}
				}

				//why bigger than 1 and not?
				if (sameObj1.size() > 1) {

					// placeholder for cells that contains the same object1 and
					// object 2 as currentCell AND that has the same relation type as currentCell
					ArrayList<Cell> sameObj2 = new ArrayList<Cell>();

					Object o2 = currentCell.getObject2();
					Relation rCurrent = currentCell.getRelation();

					//checking if the cells in sameObj1 also have the same object 2 as "currentCell", AND that their relation type is the same -> if so add the cells to "toCheck"
					for (Cell c2 : sameObj1) {
						if (o2.equals(c2.getObject2()) && rCurrent.equals(c2.getRelation())) {
							sameObj2.add(c2);
						}

					}

					//if toCheck is not null or an empty set
					if (sameObj2 != null && !sameObj2.isEmpty()) {

						for (Cell c : sameObj2) {
							thisStrength = max;

							if (c.getStrength() >= thisStrength) {
								max = c.getStrength();
							}

							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								toKeep.add(c);
								processed.add(currentCell);

							}
						}

						bestStrength = max;
						maxAlignment.addAlignCell(currentCell.getObject1(), currentCell.getObject2(), currentCell.getRelation().getRelation(), bestStrength);
					}				

				} else {

				}
			}
			
		}

		counter++;
		return maxAlignment;
	}

	/**
	 * Returns an alignment that consists of a set of relations where the strength is averaged across all input alignments 
	 * @param folderName
	 * @return
	 * @throws AlignmentException
	 * @throws URISyntaxException
	   Mar 11, 2019
	 */
	public static URIAlignment getAverageAggregatedAlignment(String folderName) throws AlignmentException, URISyntaxException {
		URIAlignment aggregatedAlignment = new URIAlignment();

		int numCellsWithSameRel = 0;

		//create an ArrayList of all alignments in folder
		ArrayList<URIAlignment> inputAlignments = new ArrayList<URIAlignment>();
		AlignmentParser aparser = new AlignmentParser(0);

		File folder = new File(folderName);
		File[] filesInDir = folder.listFiles();
		URIAlignment thisAlignment = null;

		URI sourceURI = null;
		URI targetURI = null;

		for (int i = 0; i < filesInDir.length; i++) {

			String URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			thisAlignment = (URIAlignment) aparser.parse(new URI(URI));
			inputAlignments.add(thisAlignment);

			sourceURI = thisAlignment.getOntology1URI();
			targetURI = thisAlignment.getOntology2URI();

		}

		aggregatedAlignment.init( sourceURI, targetURI, A5AlgebraRelation.class, BasicConfidence.class );

		ArrayList<Cell> allCells = new ArrayList<Cell>();

		// get all cells in all alignments and put them in a set
		for (Alignment a : inputAlignments) {
			// for all cells C in each input alignment
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		int allCellsSize = allCells.size();
		int counter = 0;
		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double averageStrength;

		for (Cell currentCell : allCells) {			
			counter++;
			System.out.println("Processing cell " + counter + " of " + allCellsSize);
			
			//get the strength of currentCell
			thisStrength = 0;
			averageStrength = 0;

			if (!processed.contains(currentCell)) {

				// get all cells that has the same object1 as currentCell
				ArrayList<Cell> sameObj1 = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1())) {
						sameObj1.add(c);
					}
				}

				//why bigger than 1 and not?
				if (sameObj1.size() > 1) {

					// placeholder for cells that contains the same object1 and
					// object 2 as currentCell AND that has the same relation type as currentCell
					ArrayList<Cell> sameObj2 = new ArrayList<Cell>();

					Object o2 = currentCell.getObject2();
					Relation rCurrent = currentCell.getRelation();

					//checking if the cells in sameObj1 also have the same object 2 as "currentCell", AND that their relation type is the same -> if so add the cells to "toCheck"
					for (Cell c2 : sameObj1) {
						if (o2.equals(c2.getObject2()) && rCurrent.equals(c2.getRelation())) {
							sameObj2.add(c2);
						}

					}

					//if toCheck is not null or an empty set
					if (sameObj2 != null && !sameObj2.isEmpty()) {

						//how many other cells have the same relation as currentCell?
						numCellsWithSameRel = sameObj2.size();
						for (Cell c : sameObj2) {
							thisStrength += c.getStrength();

							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								toKeep.add(c);
								processed.add(currentCell);

							}
						}

						averageStrength = thisStrength / (double) numCellsWithSameRel;
						aggregatedAlignment.addAlignCell(currentCell.getObject1(), currentCell.getObject2(), currentCell.getRelation().getRelation(), averageStrength);
					}				

				} else {

				}
			}
			counter++;
		}


		return aggregatedAlignment;
	}

}
