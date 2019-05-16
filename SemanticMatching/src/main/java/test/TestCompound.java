package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import net.didion.jwnl.JWNLException;
import utilities.WordNet;

public class TestCompound {
	
	public static void main(String[] args) throws FileNotFoundException, JWNLException, OWLOntologyCreationException {
		
		//test by running all Bibframe ontology concepts through the isCompoundWord method
		File ontoFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		for (OWLClass c : onto.getClassesInSignature()) {
			System.out.println("Is " + c.getIRI().getFragment() + " a compound: " + isCompoundWord(c.getIRI().getFragment().toLowerCase()));
		}
		
//		String word1 = "lskjdff";
//		System.out.println("Is " + word1 + " a compound: " + isCompoundWord(word1));
		
	}

//	public static boolean isCompoundWord(String w) throws FileNotFoundException, JWNLException{
//		int N = w.length();
//		for(int i=1;i<N-1;i++){
//			String x = w.substring(0, i);
//			String y = w.substring(i+1, N-1);
//			
//			System.out.println("x is " + x);
//			System.out.println("y is " + y);
//			
//			//if WordNet contains
//			if (WordNet.containedInWordNet(x) && WordNet.containedInWordNet(y)) {
//				System.out.println("x is " + x);
//				System.out.println("y is " + y);
//				return true;
//			}
//		}
//		return false;
//	}
	

	public static boolean isCompoundWord(String s) throws FileNotFoundException, JWNLException {
		List<String> compounds = new LinkedList<String>();
	    int[] pos = new int[s.length()+1];
	 
	    Arrays.fill(pos, -1);
	 
	    pos[0]=0;
	 
	    for(int i=0; i<s.length(); i++){
	        if(pos[i]!=-1){
	            for(int j=i+1; j<=s.length(); j++){
	                String sub = s.substring(i, j);
	                if (sub.length() > 2 && WordNet.containedInWordNet(sub)) {
	                	System.out.println("\nAdding " + sub + " to the compounds list");
	                	compounds.add(sub);
	                	System.out.println(sub + " is contained in WordNet");
	                    pos[j]=i;
	                }
	            } 
	        }
	    }
	    
	    System.out.println("There are " + compounds.size() + " words in the compounds list");
	    
	    if (compounds.size() > 1) {
	    	return true;
	    } else {
	    	return false;
	    }
	 
	    //return pos[s.length()]!=-1;
	}

	  
	// returns true if string can be segmented into space  
	// separated words, otherwise returns false 
	public static boolean isCompound(String str) throws FileNotFoundException, JWNLException 
	{ 
	    int size = str.length(); 
	  
	    // Base case 
	    if (size == 0)  return true; 
	  
	    // Try all prefixes of lengths from 1 to size 
	    for (int i=1; i<=size; i++) 
	    { 

	    	System.out.println("Is " + str.substring(0, i) + " and " + str.substring(i, size-i) + " in wordnet?");
	        if (WordNet.containedInWordNet(str.substring(0, i) ) && 
	        		isCompound( str.substring(i, size-i) )) 
	            return true; 
	    } 

	    return false; 
	} 
	
}