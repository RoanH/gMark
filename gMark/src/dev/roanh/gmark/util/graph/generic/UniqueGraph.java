/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.util.graph.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import dev.roanh.gmark.type.IDable;

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
public class UniqueGraph<V, E>{
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
	/**
	 * The ID to assign to the next node added to the graph.
	 */
	private int nextNodeID = 0;
	
	/**
	 * Removes all nodes from the graph that match the given predicate. The returned
	 * nodes still reference the remaining nodes in the graph but remaining graph
	 * nodes no longer reference the removed nodes.
	 * @param predicate The predicate nodes should match to be removed.
	 * @return A list of removed graph nodes.
	 */
	public List<GraphNode<V, E>> removeNodeIf(Predicate<GraphNode<V, E>> predicate){
		List<GraphNode<V, E>> toRemove = nodes.stream().filter(predicate).toList();
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
	 * @return The (newly added) node that is associated
	 *         with the given data.
	 */
	public GraphNode<V, E> addUniqueNode(V data){
		GraphNode<V, E> existing = nodeMap.get(data);
		if(existing != null){
			return existing;
		}else{
			GraphNode<V, E> node = new GraphNode<V, E>(nextNodeID++, this, data);
			nodeMap.put(data, node);
			nodes.add(node);
			return node;
		}
	}
	
	/**
	 * Gets the total number of edges in the graph.
	 * @return The total number of edges in the graph.
	 */
	public int getEdgeCount(){
		return edges.size();
	}
	
	/**
	 * Gets the total number of nodes in the graph.
	 * @return The total number of nodes in the graph.
	 */
	public int getNodeCount(){
		return nodes.size();
	}
	
	/**
	 * Gets the unlabelled edge connecting the node associated
	 * with the given source data and the node associated with
	 * the given target data.
	 * @param source The data to identify the source node.
	 * @param target The data to identify the target node.
	 * @return The edge connecting the source and target node
	 *         or <code>null</code> if no such node exists.
	 */
	public GraphEdge<V, E> getEdge(V source, V target){
		return getEdge(source, target, null);
	}
	
	/**
	 * Gets the edge with the given label data connecting the
	 * node associated with the given source data and the node
	 * associated with the given target data.
	 * @param source The data to identify the source node.
	 * @param target The data to identify the target node.
	 * @param data The edge label data.
	 * @return The edge connecting the source and target node
	 *         or <code>null</code> if no such node exists.
	 */
	public GraphEdge<V, E> getEdge(V source, V target, E data){
		GraphNode<V, E> node = nodeMap.get(source);
		return node == null ? null : node.getEdgeTo(target, data);
	}
	
	/**
	 * Adds a new unique edge with the given label data from the node
	 * associated with the given source data to the existing node
	 * associated with the given target data.
	 * @param source The data to identify the node to connect from with.
	 * @param target The data to identify the node to connect to with.
	 * @param data The edge label data.
	 */
	public void addUniqueEdge(V source, V target, E data){
		addUniqueEdge(nodeMap.get(source), nodeMap.get(target), data);
	}
	
	/**
	 * Adds a new unique edge with the given label data from the given source
	 * node to the existing node associated with the given target data.
	 * @param source The source node to add an edge from.
	 * @param target The data to identify the node to connect to with.
	 * @param data The edge label data.
	 */
	public void addUniqueEdge(GraphNode<V, E> source, V target, E data){
		addUniqueEdge(source, nodeMap.get(target), data);
	}
	
	/**
	 * Adds a new unique edge with the given label data to the given target node from
	 * the existing node associated with the given source data.
	 * @param source The data to identify the node to connect from with.
	 * @param target The target node to add an edge to.
	 * @param data The edge label data.
	 */
	public void addUniqueEdge(V source, GraphNode<V, E> target, E data){
		addUniqueEdge(nodeMap.get(source), target, data);
	}
	
	/**
	 * Adds a new unique edge without any label from the node
	 * associated with the given source data to the existing node
	 * associated with the given target data.
	 * @param source The data to identify the node to connect from with.
	 * @param target The data to identify the node to connect to with.
	 */
	public void addUniqueEdge(V source, V target){
		addUniqueEdge(nodeMap.get(source), nodeMap.get(target));
	}
	
	/**
	 * Adds a new unique edge without any label from the given source
	 * node to the existing node associated with the given target data.
	 * @param source The source node to add an edge from.
	 * @param target The data to identify the node to connect to with.
	 */
	public void addUniqueEdge(GraphNode<V, E> source, V target){
		addUniqueEdge(source, nodeMap.get(target));
	}
	
	/**
	 * Adds a new unique edge without any label to this node from
	 * the existing node associated with the given source data.
	 * @param source The data to identify the node to connect from with.
	 * @param target The target node to add an edge to.
	 */
	public void addUniqueEdge(V source, GraphNode<V, E> target){
		addUniqueEdge(nodeMap.get(source), target);
	}
	
	/**
	 * Adds a new unique edge without any label from the given source
	 * node to the given target node to connect to.
	 * @param source The source node to add an edge from.
	 * @param target The target node to add an edge to.
	 */
	public void addUniqueEdge(GraphNode<V, E> source, GraphNode<V, E> target){
		addUniqueEdge(source, target, null);
	}
	
	/**
	 * Adds a new unique edge with the given label data from the given source
	 * node to the given target node to connect to.
	 * @param source The source node to add an edge from.
	 * @param target The target node to add an edge to.
	 * @param data The edge label data.
	 */
	public void addUniqueEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
		GraphEdge<V, E> edge = new GraphEdge<V, E>(source, target, data);
		if(source.out.add(edge) && target.in.add(edge)){
			edges.add(edge);
		}
	}
	
	/**
	 * Computes the adjacency list representation of this graph. For this the
	 * unique ID of every graph node is used (see {@link GraphNode#getID()}.
	 * The adjacency list is returned as a 2-dimensional array, the first dimension
	 * has as many indices as there were nodes added to the graph. Each of these indices
	 * corresponds to the unique ID of one of the nodes in the graph. Indices for
	 * nodes no longer in the graph will have a value of <code>null</code>. All other
	 * indices have an array with the IDs of all the nodes the node has an edge to.
	 * @return The adjacency list representation of this graph.
	 */
	public int[][] toAdjacencyList(){
		int[][] adj = new int[nextNodeID][];
		for(GraphNode<V, E> node : getNodes()){
			adj[node.getID()] = node.getOutEdges().stream().map(GraphEdge::getTargetNode).mapToInt(GraphNode::getID).toArray();
		}
		return adj;
	}
	
	/**
	 * Makes a deep copy of this graph.
	 * @return The copy of this graph.
	 */
	public UniqueGraph<V, E> copy(){
		UniqueGraph<V, E> copy = new UniqueGraph<V, E>();
		
		for(GraphNode<V, E> node : nodes){
			copy.addUniqueNode(node.getData());
		}
		
		for(GraphEdge<V, E> edge : edges){
			copy.addUniqueEdge(edge.getSource(), edge.getTarget(), edge.getData());
		}
		
		return copy;
	}
	
	/**
	 * Converts this graph to a simple graph. This is a lossy conversion
	 * where information about edge direction and parallel edges is lost.
	 * @param <M> The metadata data type.
	 * @return The constructed simple graph.
	 */
	public <M> SimpleGraph<GraphNode<V, E>, M> toSimpleGraph(){
		SimpleGraph<GraphNode<V, E>, M> graph = new SimpleGraph<GraphNode<V, E>, M>(nodes.stream().mapToInt(n->n.getID() + 1).max().orElse(0));
		
		for(GraphNode<V, E> node : nodes){
			graph.addVertex(node);
		}
		
		for(GraphEdge<V, E> edge : edges){
			graph.addEdge(edge.getSourceNode(), edge.getTargetNode());
		}
			
		return graph;
	}
	
	/**
	 * Represents a single node in the graph that is
	 * uniquely identifiable by some data.
	 * @author Roan
	 * @param <V> The type of data stored at the graph nodes.
	 * @param <E> The type of data stored at the graph edges.
	 */
	public static class GraphNode<V, E> implements IDable{
		/**
		 * The unique ID of this node.
		 * @see #getID()
		 */
		private final int id;
		/**
		 * The graph this node is in.
		 */
		private UniqueGraph<V, E> graph;
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
		 * @param id The ID of this graph node.
		 * @param graph The graph this node belongs to.
		 * @param data The data associated with this
		 *        node that uniquely identifies it in the graph.
		 */
		private GraphNode(int id, UniqueGraph<V, E> graph, V data){
			this.id = id;
			this.graph = graph;
			this.data = data;
		}
		
		/**
		 * Renames this node to the given replacement node.
		 * This procedure will remove this node from the graph
		 * and move all edges originally attached to this node
		 * to the given replacement node.
		 * @param replacement The replacement node.
		 */
		public void rename(GraphNode<V, E> replacement){
			for(GraphEdge<V, E> edge : new ArrayList<GraphEdge<V, E>>(out)){
				edge.source = replacement;
				replacement.out.add(edge);
			}
			
			for(GraphEdge<V, E> edge : new ArrayList<GraphEdge<V, E>>(in)){
				edge.target = replacement;
				replacement.in.add(edge);
			}
			
			graph.nodes.remove(this);
			graph.nodeMap.remove(data);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that the uniqueness of a graph node is still dependent on the
		 * data stored at this node and that therefore two nodes with the same
		 * data but different IDs are in fact still equal. However, this ID
		 * can be useful for other applications where an ordering on
		 * the graph nodes is required. In addition, if no nodes are
		 * ever removed from the graph then the ordered IDs of all nodes
		 * in the graph form an unbroken sequence from 0 to
		 * {@link UniqueGraph#getNodeCount()} - 1.
		 * @return The ID of this node.
		 * @see #getData()
		 */
		@Override
		public int getID(){
			return id;
		}
		
		/**
		 * Removes this node from the graph. After removal it
		 * will still reference other remaining graph nodes.<br>
		 * Note: calling this function while iterating over the
		 * nodes or edges in the graph will cause a co-modification
		 * exception. For mass removal of nodes use
		 * {@link UniqueGraph#removeNodeIf(Predicate)} instead.
		 * @see UniqueGraph#removeNodeIf(Predicate)
		 */
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
		
		/**
		 * Gets the unlabelled edge connecting this node and
		 * the node associated with the given target data.
		 * @param target The data to identify the target node.
		 * @return The edge connecting this node and target node
		 *         or <code>null</code> if no such node exists.
		 */
		public GraphEdge<V, E> getEdgeTo(V target){
			return getEdgeTo(target, null);
		}
		
		/**
		 * Gets the edge with the given label data connecting this
		 * node and the node associated with the given target data.
		 * @param target The data to identify the target node.
		 * @param data The edge label data.
		 * @return The edge connecting this node and target node
		 *         or <code>null</code> if no such node exists.
		 */
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
		
		/**
		 * Gets the total number of outgoing edges this node has.
		 * @return The total number of outgoing edges this node has.
		 */
		public int getOutCount(){
			return out.size();
		}
		
		/**
		 * Gets the total number of incoming edges this node has.
		 * @return The total number of incoming edges this node has.
		 */
		public int getInCount(){
			return in.size();
		}
		
		/**
		 * Gets all the outgoing edges for this node.
		 * @return All the outgoing edges for this node.
		 */
		public Set<GraphEdge<V, E>> getOutEdges(){
			return out;
		}
		
		/**
		 * Gets all the incoming edges for this node.
		 * @return All the incoming edges for this node.
		 */
		public Set<GraphEdge<V, E>> getInEdges(){
			return in;
		}
		
		/**
		 * Adds a new unique edge with the given label data to this node from
		 * the given source node to connect from.
		 * @param source The source node to add an edge from.
		 * @param data The edge label data.
		 */
		public void addUniqueEdgeFrom(GraphNode<V, E> source, E data){
			graph.addUniqueEdge(source, this, data);
		}
		
		/**
		 * Adds a new unique edge with the given label data to this node from
		 * the existing node associated with the given source data.
		 * @param source The data to identify the node to connect from with.
		 * @param data The edge label data.
		 */
		public void addUniqueEdgeFrom(V source, E data){
			graph.addUniqueEdge(source, this, data);
		}
		
		/**
		 * Adds a new unique edge without any label to this node from
		 * the given source node to connect from.
		 * @param source The source node to add an edge from.
		 */
		public void addUniqueEdgeFrom(GraphNode<V, E> source){
			graph.addUniqueEdge(source, this);
		}
		
		/**
		 * Adds a new unique edge without any label to this node from
		 * the existing node associated with the given source data.
		 * @param source The data to identify the node to connect from with.
		 */
		public void addUniqueEdgeFrom(V source){
			graph.addUniqueEdge(source, this);
		}
		
		/**
		 * Adds a new unique edge with the given label data from this node to
		 * the given target node to connect to.
		 * @param target The target node to add an edge to.
		 * @param data The edge label data.
		 */
		public void addUniqueEdgeTo(GraphNode<V, E> target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		/**
		 * Adds a new unique edge with the given label data from this node to
		 * the existing node associated with the given target data.
		 * @param target The data to identify the node to connect to with.
		 * @param data The edge label data.
		 */
		public void addUniqueEdgeTo(V target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		/**
		 * Adds a new unique edge without any label from this node to
		 * the given target node to connect to.
		 * @param target The target node to add an edge to.
		 */
		public void addUniqueEdgeTo(GraphNode<V, E> target){
			graph.addUniqueEdge(this, target);
		}
		
		/**
		 * Adds a new unique edge without any label from this node to
		 * the existing node associated with the given target data.
		 * @param target The data to identify the node to connect to with.
		 */
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
			//nodes are uniquely tied to data
			//so there can never be two distinct equal node objects
			return super.equals(other);
		}
		
		@Override
		public int hashCode(){
			return super.hashCode();
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
		}
		
		/**
		 * Removes this node from the graph. After removal
		 * this node still references remaining graph nodes.
		 */
		public void remove(){
			source.out.remove(this);
			target.in.remove(this);
			source.graph.edges.remove(this);
		}
		
		/**
		 * If this edge was previously removed from the
		 * graph using {@link #remove()} this method can
		 * be used to add the edge back to the graph. If
		 * an equivalent edge was added in the mean time
		 * then the edge is not restored.
		 * @return True if the edge was restored, false
		 *         if an equivalent edge was already present.
		 */
		public boolean restore(){
			if(source.out.add(this) && target.in.add(this)){
				source.graph.edges.add(this);
				return true;
			}else{
				return false;
			}
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
				return edge.source.equals(source) && edge.target.equals(target) && Objects.equals(edge.data, data);
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
