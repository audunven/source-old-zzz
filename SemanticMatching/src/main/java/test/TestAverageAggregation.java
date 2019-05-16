package test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class TestAverageAggregation {
	
	public static void main(String[] args) throws AlignmentException, URISyntaxException {
		
		String avgAlignmentFolder = "./files/AverageAggregation";
		String maxAlignmentFolder = "./files/MaxAggregation";
		
		URIAlignment avgAlignment = getAverageAggregatedAlignment(avgAlignmentFolder);
		
//		System.out.println("Printing average aggregation alignment");
//		for (Cell c : avgAlignment) {
//			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
//		}
		
		
		URIAlignment maxAlignment = getMaxAggregatedAlignment(maxAlignmentFolder);

		System.out.println("\nPrinting max aggregation alignment");
		for (Cell c : maxAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}
		
		File alignment = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ALIGNMENTS/PROFILEWEIGHT/EQUIVALENCE/ComputedProfileWeightAlignment_WEIGHTED.rdf");
		AlignmentParser parser = new AlignmentParser();
		URIAlignment inputAlignment = (URIAlignment)parser.parse(alignment.toURI().toString());
		
		URIAlignment normalisedAlignment = normaliseAlignment(inputAlignment);
		System.out.println("\nNormalised alignment:");
		for (Cell c : normalisedAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " " + c.getObject2AsURI().getFragment() + " " + c.getStrength());
		}
		
		System.out.println("\nTesting Naive Descending Extraction");
		URIAlignment nde = naiveDescendingExtraction(inputAlignment);
		for (Cell cell : nde) {
			System.out.println(cell.getObject1AsURI().getFragment() + " " + cell.getObject2AsURI().getFragment() + " " + cell.getRelation().getRelation() + " " + cell.getStrength());
		}
		
	}
	
	public static URIAlignment naiveDescendingExtraction (URIAlignment inputAlignment) throws AlignmentException {
		
		URIAlignment extractedAlignment = new URIAlignment();
		
		URI sourceURI = inputAlignment.getOntology1URI();
		URI targetURI = inputAlignment.getOntology2URI();		
		extractedAlignment.init( sourceURI, targetURI, A5AlgebraRelation.class, BasicConfidence.class );

		ArrayList<Cell> allCells = new ArrayList<Cell>();

			for (Cell c : inputAlignment) {
				allCells.add(c);
			}
		

		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double max;
		Cell currBestCell;
		Cell bestCell;

		
		for (Cell currentCell : allCells) {			
			
			System.out.println("\ncurrentCell is " + currentCell.getObject1() + " " + currentCell.getObject2()+ " " + currentCell.getRelation().getRelation()+ " " + currentCell.getStrength());
			
			//get the strength of currentCell
			thisStrength = 0;
			max = 0;
			currBestCell = null;
			bestCell = null;
			
			if (!processed.contains(currentCell)) {
				
				// get all cells that has the same object1 OR object2 as currentCell
				ArrayList<Cell> sameObj = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1()) || c.getObject2().equals(currentCell.getObject2())) {
						sameObj.add(c);
					}
				}
				
				
							
				//why bigger than 1 and not?
				if (sameObj.size() > 1) {
					
					// placeholder for cells that contains the same object1 and
					// object 2 as currentCell AND that has the same relation type as currentCell
					ArrayList<Cell> sameRel = new ArrayList<Cell>();

					Relation rCurrent = currentCell.getRelation();

					//checking if the cells in sameObj1 also have the same object 2 as "currentCell", AND that their relation type is the same -> if so add the cells to "toCheck"
					for (Cell c2 : sameObj) {
						if (rCurrent.equals(c2.getRelation())) {
							sameRel.add(c2);
						}

					}
										
					//if not null or an empty set
					if (sameRel != null && !sameRel.isEmpty()) {
						
						

						for (Cell c : sameRel) {
							
							System.out.println("This has the same object1 or object2 as currentCell " + c.getObject1() + " " + c.getObject2()+ " " + c.getRelation().getRelation()+ " " + c.getStrength());
							
							thisStrength = max;
							
							if (c.getStrength() >= thisStrength) {
								max = c.getStrength();
								currBestCell = c;
								
							}
														
							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								processed.add(currentCell);
								
							}
						}
						
						bestCell = currBestCell;
						extractedAlignment.addAlignCell(bestCell.getObject1(), bestCell.getObject2(), bestCell.getRelation().getRelation(), bestCell.getStrength());
					}				

				} else {
					
				}
			}
		}

		return extractedAlignment;
		
	}
	
	/**
	 * Normalises an alignment by ensuring that if two cells in an alignment contain the same object1, object2 and the same relation type, the cell with the highest confidence value is retained.
	 * @param inputAlignment
	 * @return normalised alignment holding cells where duplicates (with lower confidence values) are removed
	 * @throws AlignmentException
	   Mar 11, 2019
	 */
	public static URIAlignment normaliseAlignment (URIAlignment inputAlignment) throws AlignmentException {

		URIAlignment normalisedAlignment = new URIAlignment();
		
		URI sourceURI = inputAlignment.getOntology1URI();
		URI targetURI = inputAlignment.getOntology2URI();		
		normalisedAlignment.init( sourceURI, targetURI, A5AlgebraRelation.class, BasicConfidence.class );

		ArrayList<Cell> allCells = new ArrayList<Cell>();

			for (Cell c : inputAlignment) {
				allCells.add(c);
			}
		

		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double max;
		Cell currBestCell;
		Cell bestCell;

		
		for (Cell currentCell : allCells) {			
			
			//get the strength of currentCell
			thisStrength = 0;
			max = 0;
			currBestCell = null;
			bestCell = null;
			
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
								currBestCell = c;
								
							}
														
							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								toKeep.add(c);
								processed.add(currentCell);
								
							}
						}
						
						bestCell = currBestCell;
						normalisedAlignment.addAlignCell(bestCell.getObject1(), bestCell.getObject2(), bestCell.getRelation().getRelation(), bestCell.getStrength());
					}				

				} else {
					
				}
			}
		}

		return normalisedAlignment;
		
		
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

		for (int i = 0; i < filesInDir.length; i++) {

			String URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
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

		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double max;
		double bestStrength;
		
		for (Cell currentCell : allCells) {			
			
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

		ArrayList<Cell> processed = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();
		double thisStrength;
		double averageStrength;
		
		for (Cell currentCell : allCells) {			
			
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
		}


		return aggregatedAlignment;
	}
	


}
