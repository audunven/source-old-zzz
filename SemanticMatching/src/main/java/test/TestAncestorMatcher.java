package test;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;

public class TestAncestorMatcher {

	public static void main(String[] args) throws OWLOntologyCreationException {

		File ontoFile1 = new File("./files/SATest1.owl");
		File ontoFile2 = new File("./files/SATest2.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology targetOntology = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Map<String, Set<String>> sourceMap = OntologyOperations.getClassesAndAllSuperClasses(sourceOntology);
		Map<String, Set<String>> targetMap = OntologyOperations.getClassesAndAllSuperClasses(targetOntology);
		
		for (Entry<String, Set<String>> e : sourceMap.entrySet()) {
			System.out.println("\nClass: " + e.getKey());
			for (String ancestor : e.getValue()) {
				System.out.println(ancestor);
			}
			
		}
	}

}
