package nl.group9.quicksilver.core.spec;

/**
 * Graph specification used for the graph representing the database data.
 * This interface is intentionally minimal and only specifies the methods
 * strictly required for the rest of the application to work.
 * @author Roan
 */
@FunctionalInterface
public abstract interface DatabaseGraph{

	/**
	 * Adds a new edge to this graph.
	 * @param source The source vertex for the edge.
	 * @param target The target vertex for the edge.
	 * @param label The edge label of the edge.
	 */
	public abstract void addEdge(int source, int target, int label);
}
