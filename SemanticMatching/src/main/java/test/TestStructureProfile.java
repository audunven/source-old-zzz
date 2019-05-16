package test;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;

import org.semanticweb.owlapi.model.OWLClass;

public class TestStructureProfile {

	public static void main(String[] args) throws OWLOntologyCreationException {

		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/302303/302303-302.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/302303/302303-303.rdf");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		double structureProfile = 0;
		
		int numClasses = onto1.getClassesInSignature().size() + onto2.getClassesInSignature().size();
		
		int subclasses = 0;
		int superclasses = 0;
		int counterOnto1 = 0;
		int counterOnto2 = 0;
		
		for (OWLClass c : onto1.getClassesInSignature()) {
			subclasses = OntologyOperations.getEntitySubclasses(onto1, c).size();
			superclasses = OntologyOperations.getEntitySuperclasses(onto1, c).size();
			
			if (subclasses > 0 || superclasses > 0) {
				counterOnto1++;
			}
			
		}
		
		for (OWLClass c : onto2.getClassesInSignature()) {
			subclasses = OntologyOperations.getEntitySubclasses(onto2, c).size();
			superclasses = OntologyOperations.getEntitySuperclasses(onto2, c).size();
			
			if (subclasses > 0 || superclasses > 0) {
				counterOnto2++;
			}
			
		}
		
		structureProfile = ((double) counterOnto1 + (double) counterOnto2) / (double) numClasses;
		
		System.out.println("Structure profile is " + structureProfile);

	}
	
	

}
