package nl.group9.quicksilver.core.spec;

/**
 * Graph specification used for the graph representing the database data.
 * This interface is intentionally minimal and only specifies the methods
 * strictly required for the rest of the application to work.
 * <p>
 * Some other properties of the database graph:
 * <ul>
 * <li>Vertices, edges and label are represented using integers. This is
 * an abstractation away from the actual data, typically done for performance
 * reasons and for QuickSilver this conversion was done in advance for simplicity.</li>
 * <li>The numerical vertex identifiers are consecutive, that is, if the database
 * graph has <i>V</i> vertices, then all vertices will have an identifier
 * <i>v</i> that satisfies <code>0 <= v < V</code>.</li>
 * <li>The numerical label identifiers are consecutive, that is, if the database
 * graph has <i>L</i> labels, then all labels will have an identifier
 * <i>l</i> that satisfies <code>0 <= l < L</code>.</li>
 * <li>In particular for real world data graph, there is no guarantee that no
 * duplicates exist of some edges.</li>
 * </ul>
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
