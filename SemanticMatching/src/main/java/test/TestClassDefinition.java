package test;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;
import utilities.StringUtilities;

public class TestClassDefinition {
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
		
		String lemmaTest = "HouseArrest";
		
		System.out.println("The lemma of " + lemmaTest + " is " + StringUtilities.getLemma(lemmaTest));


		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		for (OWLClass c : sourceOntology.getClassesInSignature()) {
			if (c.getIRI().getFragment().equalsIgnoreCase("obscuration"))
			System.out.println("Definition for " + c.getIRI().getFragment() + " :" + OntologyOperations.getClassDefinitionFull(sourceOntology, c));
			
		}

	}

}
