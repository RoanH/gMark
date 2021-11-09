package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
	/**
	 * List of all nodes in this graph.
	 */
	private List<GraphNode<V, E>> nodes = new ArrayList<GraphNode<V, E>>();
	/**
	 * List of all edges in this graph.
	 */
	private List<GraphEdge<V, E>> edges = new ArrayList<GraphEdge<V, E>>();
	
	//note, returned edges detached from the graph they still ref the graph but the rest of the graph does not ref them
	public List<GraphNode<V, E>> removeNodeIf(Predicate<GraphNode<V, E>> predicate){
		List<GraphNode<V, E>> toRemove = nodes.stream().filter(predicate).collect(Collectors.toList());
		toRemove.forEach(GraphNode::remove);
		return toRemove;
	}
	
	/**
	 * Gets a list of all nodes in this graph.
	 * @return All nodes in this graph.
	 */
	public List<GraphNode<V, E>> getNodes(){
		return nodes;
	}
	
	/**
	 * Gets a list of all edges in this graph.
	 * @return All edges in this graph.
	 */
	public List<GraphEdge<V, E>> getEdges(){
		return edges;
	}
	
	/**
	 * Gets a single node from this graph by the
	 * data that is stored at the node to find.
	 * Note that nodes are assumed to store information
	 * that uniquely identifies them, meaning the
	 * given data uniquely identifies a single node.
	 * @param data The data stored at the node to find.
	 * @return The node that stores the given data or
	 *         <code>null</code> if no such node exists.
	 */
	public GraphNode<V, E> getNode(V data){
		return nodeMap.get(data);
	}
	
	/**
	 * Adds a new unique node to this graph that is associated
	 * with the given data. The data given is assumed to uniquely
	 * identify a single node in the whole graph. If this graph
	 * already contains a node associated with the same data then
	 * a new node is not added to the graph and the existing node
	 * is returned instead.
	 * @param data The data to associated with the node.
	 * @return The (newly added) node that is assocaited
	 *         with the given data.
	 */
	public GraphNode<V, E> addUniqueNode(V data){
		GraphNode<V, E> existing = nodeMap.get(data);
		if(existing != null){
			return existing;
		}else{
			GraphNode<V, E> node = new GraphNode<V, E>(this, data);
			nodeMap.put(data, node);
			nodes.add(node);
			return node;
		}
	}
	
	public int getEdgeCount(){
		return edges.size();
	}
	
	public int getNodeCount(){
		return nodes.size();
	}
	
	public GraphEdge<V, E> getEdge(V source, V target){
		return getEdge(source, target, null);
	}
	
	public GraphEdge<V, E> getEdge(V source, V target, E data){
		GraphNode<V, E> node = nodeMap.get(source);
		return node == null ? null : node.getEdgeTo(target, data);
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
	
	public void addUniqueEdge(V source, V target){
		addUniqueEdge(nodeMap.get(source), nodeMap.get(target));
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, V target){
		addUniqueEdge(source, nodeMap.get(target));
	}
	
	public void addUniqueEdge(V source, GraphNode<V, E> target){
		addUniqueEdge(nodeMap.get(source), target);
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, GraphNode<V, E> target){
		addUniqueEdge(source, target, null);
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
	
	/**
	 * Represents a single node in the graph that is
	 * uniquely identifiable by some data.
	 * @author Roan
	 * @param <V> The type of data stored at the graph nodes.
	 * @param <E> The type of data stored at the graph edges.
	 */
	public static class GraphNode<V, E>{
		/**
		 * The graph this node is in.
		 */
		private Graph<V, E> graph;
		/**
		 * The edges departing from this node.
		 */
		private Set<GraphEdge<V, E>> out = new HashSet<GraphEdge<V, E>>();
		/**
		 * The edges ending at this node.
		 */
		private Set<GraphEdge<V, E>> in = new HashSet<GraphEdge<V, E>>();
		/**
		 * The data stored at this node, this also
		 * uniquely identifies this node in the entire graph.
		 */
		private V data;
		
		/**
		 * Constructs a new graph node for the given graph
		 * and with the given data that uniquely identifies
		 * this node in the entire graph.
		 * @param graph The graph this node belongs to.
		 * @param data The data associated with this
		 *        node that uniquely identifies it in the graph.
		 */
		private GraphNode(Graph<V, E> graph, V data){
			this.graph = graph;
			this.data = data;
		}
		
		//note about comod
		//note all edges too
		public void remove(){
			//manual edge removal to prevent co-modification
			for(GraphEdge<V, E> edge : out){
				edge.target.in.remove(edge);
			}
			for(GraphEdge<V, E> edge : in){
				edge.source.out.remove(edge);
			}
			graph.edges.removeAll(out);
			graph.edges.removeAll(in);
			graph.nodes.remove(this);
			graph.nodeMap.remove(data);
		}
		
		public GraphEdge<V, E> getEdgeTo(V target){
			return getEdgeTo(target, null);
		}
		
		public GraphEdge<V, E> getEdgeTo(V target, E data){
			for(GraphEdge<V, E> edge : out){
				if(edge.target.data.equals(target)){
					if(edge.data == null){
						if(data == null){
							return edge;
						}
					}else{
						if(edge.data.equals(data)){
							return edge;
						}
					}
				}
			}
			return null;
		}
		
		public Set<GraphEdge<V, E>> getOutEdges(){
			return out;
		}
		
		public Set<GraphEdge<V, E>> getInEdges(){
			return in;
		}
		
		public void addUniqueEdgeFrom(GraphNode<V, E> source, E data){
			graph.addUniqueEdge(source, this, data);
		}
		
		public void addUniqueEdgeFrom(V source, E data){
			graph.addUniqueEdge(source, this, data);
		}
		
		public void addUniqueEdgeFrom(GraphNode<V, E> source){
			graph.addUniqueEdge(source, this);
		}
		
		public void addUniqueEdgeFrom(V source){
			graph.addUniqueEdge(source, this);
		}
		
		public void addUniqueEdgeTo(GraphNode<V, E> target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		public void addUniqueEdgeTo(V target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		public void addUniqueEdgeTo(GraphNode<V, E> target){
			graph.addUniqueEdge(this, target);
		}
		
		public void addUniqueEdgeTo(V target){
			graph.addUniqueEdge(this, target);
		}
		
		/**
		 * Gets the data stored at this node. The
		 * returned data uniquely identifies this
		 * node in the entire graph.
		 * @return The data stored at this node.
		 */
		public V getData(){
			return data;
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
	
	/**
	 * Represents a single directed edge in the graph that
	 * can have associated data.
	 * @author Roan
	 * @param <V> The type of data stored at the graph nodes.
	 * @param <E> The type of data stored at the graph edges.
	 */
	public static class GraphEdge<V, E>{
		/**
		 * The source node for this edge.
		 */
		private GraphNode<V, E> source;
		/**
		 * The target node for this edge.
		 */
		private GraphNode<V, E> target;
		/**
		 * The data associated with this edge, can be <code>null</code>.
		 */
		private E data;
		
		/**
		 * Constructs a new edge with the given source,
		 * target and associated data.
		 * @param source The source node for this edge.
		 * @param target The target node for this edge.
		 * @param data The data associated with this edge,
		 *        allowed to be <code>null</code>.
		 */
		private GraphEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
			this.source = source;
			this.target = target;
			this.data = data;
			source.out.add(this);
			target.in.add(this);
		}
		
		public void remove(){
			source.out.remove(this);
			target.in.remove(this);
			source.graph.edges.remove(this);
		}
		
		/**
		 * Gets the source node for this edge.
		 * @return The source node for this edge.
		 * @see #getSource()
		 */
		public GraphNode<V, E> getSourceNode(){
			return source;
		}
		
		/**
		 * Gets the target node for this edge.
		 * @return The target node for this edge.
		 * @see #getTarget()
		 */
		public GraphNode<V, E> getTargetNode(){
			return target;
		}
		
		/**
		 * Gets the data stored at the source node for this edge.
		 * @return The data stored at the source node for this edge.
		 * @see #getSourceNode()
		 */
		public V getSource(){
			return source.data;
		}
		
		/**
		 * Gets the data stored at the target node for this edge.
		 * @return The data stored at the target node for this edge.
		 * @see #getTargetNode()
		 */
		public V getTarget(){
			return target.data;
		}
		
		/**
		 * Gets the data associated with this edge.
		 * @return The data associated with this edge,
		 *         possibly <code>null</code>.
		 */
		public E getData(){
			return data;
		}
		
		@Override
		public String toString(){
			return data == null ? "GraphEdge[data=null]" : data.toString();
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof GraphEdge){
				GraphEdge<?, ?> edge = (GraphEdge<?, ?>)other;
				return edge.source.equals(source) && edge.target.equals(target) && edge.data.equals(data);
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(source, target, data);
		}
	}
}
