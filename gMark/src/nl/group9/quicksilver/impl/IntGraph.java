package nl.group9.quicksilver.impl;

import java.util.ArrayList;
import java.util.List;

import dev.roanh.gmark.data.SourceLabelPair;
import dev.roanh.gmark.data.TargetLabelPair;
import dev.roanh.gmark.util.RangeList;

/**
 * Implementation of a simple directed labelled graph using adjacency lists.
 * @author Roan
 * @see <a href="https://en.wikipedia.org/wiki/Adjacency_list">Adjacency List Based Graph</a>
 */
public class IntGraph{
	/**
	 * The (maximum) number of distinct labels in this graph.
	 */
	private final int labelCount;
	/**
	 * The number of vertices in this graph.
	 */
	private final int vertexCount;
	/**
	 * An adjacency list encoding edges in the forward direction.
	 * For vertex <i>i</i> its outgoing edges with their target
	 * vertex and label are stored at the <i>i</i>-th position of the list.
	 */
	private final RangeList<List<TargetLabelPair>> adjacenyList;
	/**
	 * An adjacency list encoding edges in the inverse direction.
	 * For vertex <i>i</i> its incoming edges with their source
	 * vertex and label are stored at the <i>i</i>-th position of the list.
	 */
	private final RangeList<List<SourceLabelPair>> reverseAdjacencyList;
	
	/**
	 * Constructs a new simple graph with the given size.
	 * @param vertexCount The number of vertices to allocate space for.
	 * @param labelCount The number of labels for the graph.
	 */
	public IntGraph(int vertexCount, int labelCount){
		this.vertexCount = vertexCount;
		this.labelCount = labelCount;
		
		adjacenyList = new RangeList<List<TargetLabelPair>>(vertexCount, ArrayList::new);
		reverseAdjacencyList = new RangeList<List<SourceLabelPair>>(vertexCount, ArrayList::new);
	}
	
	/**
	 * Gets all the outgoing edges from the given source vertex.
	 * @param source The source vertex to get outgoing edges for.
	 * @return All outgoing edges for the given source vertex.
	 * @see TargetLabelPair
	 */
	public List<TargetLabelPair> getOutgoingEdges(int source){
		return adjacenyList.get(source);
	}
	
	/**
	 * Gets all the incoming edges to the given target vertex.
	 * @param target The target vertex to get incoming edges for.
	 * @return All incoming edges for the given target vertex.
	 * @see SourceLabelPair
	 */
	public List<SourceLabelPair> getIncomingEdges(int target){
		return reverseAdjacencyList.get(target);
	}
	
	/**
	 * Checks if this graph contains the given edge.
	 * @param source The source vertex for the edge.
	 * @param target The label of the edge.
	 * @param label The target vertex for the edge.
	 * @return True if the specified edge exists in the graph.
	 */
	public boolean hasEdge(int source, int target, int label){
		return adjacenyList.get(source).contains(new TargetLabelPair(target, label));
	}
	
	/**
	 * Gets the number of edges in this graph.
	 * @return The number of edges in this graph.
	 */
	public int getEdgeCount(){
		int count = 0;
		for(List<TargetLabelPair> out : adjacenyList){
			count += out.size();
		}
		
		return count;
	}
	
	/**
	 * Gets the number of vertices in this graph.
	 * @return The number of vertices in this graph.
	 */
	public int getVertexCount(){
		return vertexCount;
	}
	
	public int getLabelCount(){
		return labelCount;
	}
	
	public void addEdge(int source, int target, int label){
		if(source >= vertexCount || target >= vertexCount || label >= labelCount){
			throw new IllegalArgumentException("Edge data out of bounds: (source=%d, target=%d, label=%d)".formatted(source, target, label));
		}
		
		adjacenyList.get(source).add(new TargetLabelPair(target, label));
		reverseAdjacencyList.get(target).add(new SourceLabelPair(source, label));
	}
}