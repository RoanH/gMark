package nl.group9.quicksilver.core.spec;

/**
 * Simple bridging facility deliver the right concrete implementation
 * of the evaluator and database graph that should be used in QuickSilver.
 * @author Roan
 * @param <G> The database graph type.
 * @param <R> The result graph type.
 * @see DatabaseGraph
 * @see Evaluator
 */
public abstract interface EvaluatorProvider<G extends DatabaseGraph, R extends ResultGraph>{

	/**
	 * Constructs a new evaluator instance to be used to evaluate queries.
	 * @return The constructed evaluator instance.
	 * @see Evaluator
	 */
	public abstract Evaluator<G, R> createEvaluator();
	
	/**
	 * Constructs a new empty graph to use for for the main database graph. After construction
	 * the graph will be built up using {@link DatabaseGraph#addEdge(int, int, int)}.
	 * @param vertexCount The number of vertices the allocated graph has to have space for.
	 * @param edgeCount The number of edges the allocated graph has to have space for.
	 * @param labelCount The number of labels the allocated has to have space for.
	 * @return The (empty) constructed database graph instance.
	 * @see DatabaseGraph
	 */
	public abstract G createGraph(int vertexCount, int edgeCount, int labelCount);
}
