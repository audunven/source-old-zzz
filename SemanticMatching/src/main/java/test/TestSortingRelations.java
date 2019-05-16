package test;

import java.util.ArrayList;
import java.util.Collections;

import utilities.Relation;
import utilities.RelationComparatorConfidence;

public class TestSortingRelations {
	
	public static void main(String[] args) {
	
	ArrayList<Relation> relationsList = new ArrayList<Relation>();
	relationsList.add(new Relation("ID1", "Car", "Vehicle", "<", 1.0));
	relationsList.add(new Relation("ID2", "Boat", "Vehicle", "<", 1.0));
	relationsList.add(new Relation("ID3", "Ship", "Vehicle", "<", 1.0));
	relationsList.add(new Relation("ID4", "Vehicle", "Vehicle", "<", 0.3));
	relationsList.add(new Relation("ID5", "Transport", "Vehicle", "<", 0.6));
	
	System.out.println("Printing unsorted list");
	for (Relation r : relationsList) {
		System.out.println(r.getConcept1Fragment() + " " + r.getConcept2Fragment() + " " + r.getRelationType() + " " + r.getConfidence());
	}
	
	Collections.sort(relationsList, new RelationComparatorConfidence());
	
	System.out.println("Printing sorted list");
	for (Relation r : relationsList) {
		System.out.println(r.getConcept1Fragment() + " " + r.getConcept2Fragment() + " " + r.getRelationType() + " " + r.getConfidence());
	}
	
	
	
	
	}

}
