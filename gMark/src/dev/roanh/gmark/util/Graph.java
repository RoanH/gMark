package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to model a graph with directed
 * edges. Both edges and nodes are assumed to be
 * unique, that is, no two edges or nodes exist
 * that cannot be distinguished. Both vertices
 * and edges support storing some form of data.
 * The data stored at nodes is assumed to uniquely
 * identify a given node. This class does not
 * perform a lot of integrity checks in order
 * to stay performant.
 * @author Roan
 * @param <V> Type of the data stored at vertices.
 * @param <E> Type of the data stored at edges.
 */
public class Graph<V, E>{
	/**
	 * Map for efficient graph node lookup from
	 * the data stored a specific node. Note that
	 * it is assumed that there are no nodes that
	 * store the same data.
	 */
	private Map<V, GraphNode<V, E>> nodeMap = new HashMap<V, GraphNode<V, E>>();
	private List<GraphNode<V, E>> nodes = new ArrayList<GraphNode<V, E>>();
	private List<GraphEdge<V, E>> edges = new ArrayList<GraphEdge<V, E>>();
	
	public List<GraphNode<V, E>> getNodes(){
		return nodes;
	}
	
	public List<GraphEdge<V, E>> getEdges(){
		return edges;
	}
	
	public GraphNode<V, E> getNode(V data){
		return nodeMap.get(data);
	}
	
	//TODO assumed to be unique but not checked
	//therefore data is also assumed to be unique
	public GraphNode<V, E> addUniqueNode(V data){
		GraphNode<V, E> node = new GraphNode<V, E>(this, data);
		if(nodeMap.put(data, node) != null){
			//TODO
			throw new IllegalStateException("TODO");
		}
		nodes.add(node);
		return node;
	}
	
	public void addUniqueEdge(V source, V target, E data){
		addUniqueEdge(nodeMap.get(source), nodeMap.get(target), data);
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, V target, E data){
		addUniqueEdge(source, nodeMap.get(target), data);
	}
	
	public void addUniqueEdge(V source, GraphNode<V, E> target, E data){
		addUniqueEdge(nodeMap.get(source), target, data);
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
		//TODO this duplicate check is fairly expensive as is
		for(GraphEdge<V, E> edge : source.out){
			if(edge.target.equals(target)){
				return;
			}
		}
		edges.add(new GraphEdge<V, E>(source, target, data));
	}
	
	public static class GraphNode<V, E>{
		private Graph<V, E> graph;
		private Set<GraphEdge<V, E>> out = new HashSet<GraphEdge<V, E>>();
		private Set<GraphEdge<V, E>> in = new HashSet<GraphEdge<V, E>>();
		private V data;
		
		private GraphNode(Graph<V, E> graph, V data){
			this.graph = graph;
			this.data = data;
		}
		
		public Set<GraphEdge<V, E>> getOutEdges(){
			return out;
		}
		
		public Set<GraphEdge<V, E>> getInEdges(){
			return in;
		}
		
		public void addUniqueEdgeTo(GraphNode<V, E> target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		public void addUniqueEdgeTo(V target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		@Override
		public String toString(){
			return data.toString();
		}
		
		@Override
		public boolean equals(Object other){
			return other instanceof GraphNode ? ((GraphNode<?, ?>)other).data.equals(data) : false;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(data);
		}
	}
	
	public static class GraphEdge<V, E>{
		private GraphNode<V, E> source;
		private GraphNode<V, E> target;
		private E data;
		
		private GraphEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
			this.source = source;
			this.target = target;
			this.data = data;
			source.out.add(this);
			target.in.add(this);
		}
		
		public GraphNode<V, E> getSourceNode(){
			return source;
		}
		
		public GraphNode<V, E> getTargetNode(){
			return target;
		}
		
		public V getSource(){
			return source.data;
		}
		
		public V getTarget(){
			return target.data;
		}
		
		public E getData(){
			return data;
		}
		
		@Override
		public String toString(){
			return data == null ? null : data.toString();
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof GraphEdge){
				GraphEdge<?, ?> edge = (GraphEdge<?, ?>)other;
				return edge.source.equals(source) && edge.target.equals(target);
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(source, target);
		}
	}
}
