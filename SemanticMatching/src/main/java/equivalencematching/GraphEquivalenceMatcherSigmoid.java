package equivalencematching;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import evaluation.general.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import graph.Graph;
import matchercombination.HarmonyEquivalence;
//import Graph;
//import GraphOperations_delete.RelTypes;
import utilities.ISub;
import utilities.Sigmoid;
import utilities.StringUtilities;

@SuppressWarnings("deprecation")
public class GraphEquivalenceMatcherSigmoid extends ObjectAlignment implements AlignmentProcess {

	//these attributes are used to calculate the weight associated with the matcher's confidence value
	double profileScore;
	int slope;
	double rangeMin;
	double rangeMax;
	OWLOntology sourceOntology;
	OWLOntology targetOntology;

	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto1;
	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto2;

	static GraphDatabaseService db;

	ISub iSubMatcher = new ISub();

	final static String key = "classname";
	private static final double THRESHOLD = 0.9;


	public GraphEquivalenceMatcherSigmoid(String ontology1Name, String ontology2Name, GraphDatabaseService database, double profileScore, int slope, double rangeMin, double rangeMax) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
		this.profileScore = profileScore;
		this.slope = slope;
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	public GraphEquivalenceMatcherSigmoid() {

	}

	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException, IOException {

		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301303/301303-301.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301303/301303-303.rdf");
		String referenceAlignment = "./files/_PHD_EVALUATION/OAEI2011/REFALIGN/301303/301-303-EQ_SUB.rdf";

		//				File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
		//				File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
		//				String referenceAlignment = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQUIVALENCE.rdf";

		//create a new instance of the neo4j database in each run
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);	
		System.out.println("Creating a new NEO4J database");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		System.out.println("Database created");

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		System.out.println("Creating ontology graphs");
		creator = new Graph(db);

		creator.createOntologyGraph(o1, labelO1);
		creator.createOntologyGraph(o2, labelO2);


		double testWeight = 1.0;

		AlignmentProcess a = new GraphEquivalenceMatcherSigmoid(ontologyParameter1, ontologyParameter2, db, testWeight, 3, 0.5, 0.7);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		BasicAlignment graphMatcherAlignment = new BasicAlignment();

		graphMatcherAlignment = (BasicAlignment) (a.clone());

		graphMatcherAlignment.normalise();

		//evaluate the Harmony alignment
		BasicAlignment harmonyAlignment = HarmonyEquivalence.getHarmonyAlignment(graphMatcherAlignment);
		System.out.println("The Harmony alignment contains " + harmonyAlignment.nbCells() + " cells");
		Evaluator.evaluateSingleAlignment(harmonyAlignment, referenceAlignment);

		System.out.println("Printing Harmony Alignment: ");
		for (Cell c : harmonyAlignment) {
			System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

		System.out.println("\nThe alignment contains " + graphMatcherAlignment.nbCells() + " relations");

		System.out.println("Evaluation with no cut threshold:");
		Evaluator.evaluateSingleAlignment(graphMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.2:");
		graphMatcherAlignment.cut(0.2);
		Evaluator.evaluateSingleAlignment(graphMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.4:");
		graphMatcherAlignment.cut(0.4);
		Evaluator.evaluateSingleAlignment(graphMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.6:");
		graphMatcherAlignment.cut(0.6);
		Evaluator.evaluateSingleAlignment(graphMatcherAlignment, referenceAlignment);

		System.out.println("Printing relations at 0.6:");
		for (Cell c : graphMatcherAlignment) {
			System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

		System.out.println("Evaluation with threshold 0.9:");
		graphMatcherAlignment.cut(0.9);
		Evaluator.evaluateSingleAlignment(graphMatcherAlignment, referenceAlignment);

	}


	public static URIAlignment returnGEMAlignment (File ontoFile1, File ontoFile2, double profileScore, int slope, double rangeMin, double rangeMax) throws OWLOntologyCreationException, AlignmentException {

		URIAlignment GEMAlignment = new URIAlignment();

		//create a new instance of the neo4j database in each run
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);	
		System.out.println("Creating a new NEO4J database ( " + dbFile.getPath() + " )");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		System.out.println("Database created");		

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		System.out.println("Creating ontology graphs");
		creator = new Graph(db);

		creator.createOntologyGraph(o1, labelO1);
		creator.createOntologyGraph(o2, labelO2);

		AlignmentProcess a = new GraphEquivalenceMatcherSigmoid(StringUtilities.stripPath(ontoFile1.toString()), StringUtilities.stripPath(ontoFile2.toString()), db, profileScore, slope, rangeMin, rangeMax);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		BasicAlignment GraphEquivalenceMatcherSigmoidAlignment = new BasicAlignment();

		GraphEquivalenceMatcherSigmoidAlignment = (BasicAlignment) (a.clone());

		GraphEquivalenceMatcherSigmoidAlignment.normalise();

		GEMAlignment = GraphEquivalenceMatcherSigmoidAlignment.toURIAlignment();

		GEMAlignment.init( o1.getOntologyID().getOntologyIRI().toURI(), o2.getOntologyID().getOntologyIRI().toURI(), A5AlgebraRelation.class, BasicConfidence.class );

		return GEMAlignment;

	}	


	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {

		int idCounter = 0;
		try {

			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					idCounter++; 

					//using sigmoid function to compute confidence
					addAlignCell("GraphMatcher" + idCounter, cl1,cl2, "=", 
							Sigmoid.weightedSigmoid(slope, computeStructProx(cl1,cl2), Sigmoid.transformProfileWeight(profileScore, rangeMin, rangeMax))); 
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	/**
	 * This method computes the structural proximity of two input classes. 
	 * (1) First it finds the input classes in the corresponding graphs, and measures their distance to root (owl:Thing), 
	 * (2) then it retrieves the list of parent nodes to these two input classes,
	 * (3) then it matches the parent nodes of the corresponding input classes,
	 * (4) if the similarity of parent nodes is above the threshold, the distance to root for these parent nodes is counted,
	 * (5) finally, the structural proximity is computed as:
	 * (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot)
	 * @param o1 an ontology object (OWL entity)
	 * @param o2 an ontology object (OWL entity)
	 * @return measure of similarity between the two input objects (ontology entities)
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	public double computeStructProx(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		//registerShutdownHook(db);		

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

		//get the s1 node from ontology 1
		Node s1Node = getNode(s1, labelOnto1);


		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);

		//get the parent nodes of a class from ontology 1
		ArrayList<Object> onto1Parents = getAllParentNodes(s1Node, labelOnto1);


		//get the parent nodes of a class from ontology 2
		ArrayList<Object> onto2Parents = getAllParentNodes(s2Node,labelOnto2);


		//find distance from s1 node to owl:Thing
		int distanceC1ToRoot = findDistanceToRoot(s1Node);

		//find distance from s2 to owl:Thing
		int distanceC2ToRoot = findDistanceToRoot(s2Node);

		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();

		//map to keep the pair of ancestors matching above the threshold
		Map<Object,Object> matchingMap = new HashMap<Object,Object>();

		//matching the parentnodes
		for (int i = 0; i < onto1Parents.size(); i++) {
			for (int j = 0; j < onto2Parents.size(); j++) {
				iSubSimScore = iSubMatcher.score(onto1Parents.get(i).toString(), onto2Parents.get(j).toString());

				if (iSubSimScore >= THRESHOLD) {

					matchingMap.put(onto1Parents.get(i) , onto2Parents.get(j));
				}	
			}
		}

		double structProx = 0;
		double currentStructProx = 0;
		double avgAncestorDistanceToRoot = 0;


		//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
		for (Entry<Object, Object> entry : matchingMap.entrySet()) {
			Node anc1 = getNode(entry.getKey().toString(), labelOnto1);
			Node anc2 = getNode(entry.getValue().toString(), labelOnto2);

			avgAncestorDistanceToRoot = (findDistanceToRoot(anc1) + findDistanceToRoot(anc2)) / 2;

			currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);

			if (currentStructProx > structProx) {

				structProx = currentStructProx;
			}

		}

		return structProx;
	}

	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();
			}
		} );
	}

	private static enum RelTypes implements RelationshipType
	{
		isA
	}

	/**
	 * Returns a graph node given a label, a property name and property value
	 * @param value
	 * @param label a label represents the graph/ontology to process
	 * @return the node searched for
	 */
	public static Node getNode(String value, Label label) {
		Node testNode = null;

		try ( Transaction tx = db.beginTx() ) {
			testNode = db.findNode(label, key, value);
			tx.success();
		}
		return testNode;	

	}


	/**
	 * Returns the ID of a node given the Node instance as parameter
	 * @param n a Node instance
	 * @return the ID of a node as a long
	 */
	public long getNodeID(Node n) {

		long id = 0;

		try ( Transaction tx = db.beginTx() ) {
			id = n.getId();
			tx.success();	

		}

		return id;	
	}

	/**
	 * Returns a Traverser that traverses the children of a node given a Node instance as parameter
	 * @param classNode a Node instance
	 * @return a traverser
	 */
	public static Traverser getChildNodesTraverser(Node classNode) {

		TraversalDescription td = null;
		try ( Transaction tx = db.beginTx() ) {

			td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());
			tx.success();

		}

		return td.traverse(classNode);
	}

	/**
	 * Returns an ArrayList of all child nodes of a node
	 * @param classNode a Node instance
	 * @param label representing the graph/ontology to process
	 * @return
	 */
	public static ArrayList<Object> getClosestChildNodesAsList(Node classNode, Label label) {

		ArrayList<Object> childNodeList= new ArrayList<Object>();
		Traverser childNodesTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			childNodesTraverser = getChildNodesTraverser(classNode);

			for (Path childNodePath: childNodesTraverser) {
				if(childNodePath.length() == 1 && childNodePath.endNode().hasLabel(label)) {
					childNodeList.add(childNodePath.endNode().getProperty("classname"));
				}
			}

			tx.success();

		}

		return childNodeList;
	}

	/**
	 * Returns a Traverser that traverses the parents of a node given a Node instance as parameter
	 * @param classNode a Node instance
	 * @return a traverser
	 */
	public static Traverser getParentNodeTraverser (Node classNode) {

		TraversalDescription td = null;

		try ( Transaction tx = db.beginTx() ) {

			td = db.traversalDescription()
					.breadthFirst()
					.relationships(RelTypes.isA, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition());

			tx.success();

		}

		return td.traverse(classNode);
	}

	//TO-DO: Why is this an ArrayList and not a Node being returned?
	/**
	 * Returns an ArrayList holding the parent node of the node provided as parameter
	 * @param classNode a node for which the closest parent is to be returned
	 * @param label a label representing the graph (ontology) to process
	 * @return the closest parent node
	 */
	public static ArrayList<Object> getClosestParentNode(Node classNode, Label label) {

		ArrayList<Object> parentNodeList= new ArrayList<Object>();
		Traverser parentNodeTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath: parentNodeTraverser) {
				if(parentNodePath.length() == 1 && parentNodePath.endNode().hasLabel(label)) {
					parentNodeList.add(parentNodePath.endNode().getProperty("classname"));
				}
			}

			tx.success();

		}

		return parentNodeList;
	}

	/**
	 * Returns an ArrayList holding all parent nodes to the Node provided as parameter
	 * @param classNode the Node for which all parent nodes are to be retrieved
	 * @param label representing the graph/ontology to process
	 * @return all parent nodes to node provided as parameter
	 */
	public static ArrayList<Object> getAllParentNodes(Node classNode, Label label) {

		ArrayList<Object> parentNodeList= new ArrayList<Object>();
		Traverser parentNodeTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath: parentNodeTraverser) {
				if (parentNodePath.endNode().hasLabel(label)){
					parentNodeList.add(parentNodePath.endNode().getProperty("classname"));

				}

			}

			tx.success();

		}


		return parentNodeList;
	}



	/**
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the parentNode.
	 * @param parentNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public static Iterable<Path> findShortestPathBetweenNodes(Node parentNode, Node classNode, Label label, RelationshipType rel) {

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		Iterable<Path> paths = finder.findAllPaths( classNode, parentNode );
		return paths;

	}

	/**
	 * Returns the distance from the Node provided as parameter and the root node (i.e. owl:Thing)
	 * We use a Map as a work-around to counting the edges between a given node and the root (owl:Thing). This is possible since a Map only allows
	 * unique keys and a numbered Neo4J path consists of a set of path items <edge-count, node (property)> where all nodes for each edge-count
	 * is listed (e.g. for the node "AcademicArticle" the upwards path is <1, Article>, <2, Document>, <3, owl:Thing>). 
	 * @param classNode
	 * @return
	 */
	public static int findDistanceToRoot(Node classNode) {

		Traverser parentNodeTraverser = null;
		Map<Object, Object> parentNodeMap = new HashMap<>();

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath : parentNodeTraverser) {
				parentNodeMap.put(parentNodePath.length(), parentNodePath.endNode().getProperty("classname"));

			}

			tx.success();

		}
		int distanceToRoot = parentNodeMap.size();

		return distanceToRoot;
	}


}