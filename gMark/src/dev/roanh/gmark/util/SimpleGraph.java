/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
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
package dev.roanh.gmark.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a simple graph that only supports unlabelled undirected edges.
 * Parallel edges are not supported, but self loops are allowed. Vertices in the
 * graph are uniquely identified by some piece of data.
 * @author Roan
 * @param <T> The vertex data type
 */
public class SimpleGraph<T>{
	/**
	 * Map from identifying data to associated vertex.
	 */
	private Map<T, SimpleVertex<T>> vertexMap = new LinkedHashMap<T, SimpleVertex<T>>();
	/**
	 * Set of all edges in the graph.
	 */
	private Set<SimpleEdge<T>> edges = new HashSet<SimpleEdge<T>>();
	/**
	 * The ID to assign to the next node added to the graph.
	 */
	private int nextNodeID = 0;
	
	/**
	 * Deletes the given vertex from the graph.
	 * @param vertex The vertex to delete
	 */
	public void deleteVertex(SimpleVertex<T> vertex){
		vertexMap.remove(vertex.getData());
		for(SimpleEdge<T> edge : vertex.getEdges()){
			edges.remove(edge);
			edge.getTarget(vertex).edges.remove(edge);
		}
	}
	
	/**
	 * Gets a vertex from this graph using the data that uniquely identifies it.
	 * @param data The data uniquely identifying the requested vertex.
	 * @return The vertex associated with the passed data.
	 */
	public SimpleVertex<T> getVertex(T data){
		return vertexMap.get(data);
	}
	
	/**
	 * Gets all the edges in this graph.
	 * @return All the edges in this graph.
	 */
	public Set<SimpleEdge<T>> getEdges(){
		return edges;
	}
	
	/**
	 * Adds a new vertex to this graph with the given piece of uniquely identifying data.
	 * @param data The data that uniquely identifies this vertex,
	 * @return The newly added vertex or the already existing vertex if a vertex identified
	 *         by the same piece of data was already present in the graph.
	 */
	public SimpleVertex<T> addVertex(T data){
		SimpleVertex<T> v = vertexMap.get(data);
		if(v != null){
			return v;
		}else{
			v = new SimpleVertex<T>(nextNodeID++, data);
			vertexMap.put(data, v);
			return v;
		}
	}
	
	/**
	 * Adds a new edge to this graph between the vertices identified by the given pieces of data.
	 * @param a The data uniquely identifying the first vertex.
	 * @param b The data uniquely identifying the second vertex.
	 * @return The newly added edge.
	 */
	public SimpleEdge<T> addEdge(T a, T b){
		return addEdge(vertexMap.get(a), b);
	}
	
	/**
	 * Adds a new edge to this graph between the given vertex and the vertex associated with the given data.
	 * @param a The first vertex
	 * @param b The data uniquely identifying the second vertex.
	 * @return The newly added edge.
	 */
	public SimpleEdge<T> addEdge(SimpleVertex<T> a, T b){
		return addEdge(a, vertexMap.get(b));
	}
	
	/**
	 * Adds a new edge to this graph between the given two vertices.
	 * @param a The first vertex.
	 * @param b The second vertex.
	 * @return The newly added edge.
	 */
	public SimpleEdge<T> addEdge(SimpleVertex<T> a, SimpleVertex<T> b){
		SimpleEdge<T> edge = new SimpleEdge<T>(a, b);
		b.edges.add(edge);
		a.edges.add(edge);
		edges.add(edge);
		return edge;
	}
	
	/**
	 * Gets the total number of vertices in this graph.
	 * @return The total number of vertices in this graph.
	 */
	public int getVertexCount(){
		return vertexMap.size();
	}
	
	/**
	 * Gets the total number of edges in this graph.
	 * @return The total number of edges in this graph.
	 */
	public int getEdgeCount(){
		return edges.size();
	}
	
	/**
	 * Gets all the vertices in this graph.
	 * @return All the vertices in this graph.
	 */
	public Collection<SimpleVertex<T>> getVertices(){
		return vertexMap.values();
	}
	
	/**
	 * Converts this simple graph to a unique graph instance. For this conversion
	 * edges will remain unlabelled and a directed edge will be added in both directions
	 * between vertices that are connected in this graph.
	 * @return The newly constructed unique graph instance.
	 */
	public UniqueGraph<T, Void> toUniqueGraph(){
		UniqueGraph<T, Void> g = new UniqueGraph<T, Void>();
		vertexMap.keySet().forEach(g::addUniqueNode);
		for(SimpleEdge<T> edge : edges){
			g.addUniqueEdge(edge.getFirstVertex().getData(), edge.getSecondVertex().getData());
			g.addUniqueEdge(edge.getSecondVertex().getData(), edge.getFirstVertex().getData());
		}
		return g;
	}
	
	/**
	 * Contracts the given edge in this graph by removing the given edge and the two
	 * nodes at its ends and then replacing them with the given vertex. All edges
	 * originally connected to either of the two removed vertices will now be
	 * connected to the given vertex.
	 * @param edge The edge to contracted.
	 * @param vertex The vertex to represent the contracted edge and vertices.
	 */
	public void contractEdge(SimpleEdge<T> edge, SimpleVertex<T> vertex){
		SimpleVertex<T> v1 = edge.getFirstVertex();
		SimpleVertex<T> v2 = edge.getSecondVertex();
		
		edges.remove(edge);
		vertexMap.remove(v1.getData());
		vertexMap.remove(v2.getData());
		
		for(SimpleEdge<T> e : edges){
			if(e.getFirstVertex() == v1 || e.getFirstVertex() == v2){
				e.v1 = vertex;
			}else if(e.getSecondVertex() == v1 || e.getSecondVertex() == v2){
				e.v2 = vertex;
			}
		}
	}
	
	/**
	 * Deletes all edges in this graph.
	 */
	public void dropAllEdges(){
		vertexMap.values().forEach(v->v.edges.clear());
		edges.clear();
	}
	
	/**
	 * Represents a single undirected edge between two nodes in the graph.
	 * @author Roan
	 * @param <T> The graph data type.
	 */
	public static final class SimpleEdge<T>{
		/**
		 * The first end point of this edge.
		 */
		private SimpleVertex<T> v1;
		/**
		 * The second end point of this edge.
		 */
		private SimpleVertex<T> v2;
		
		/**
		 * Constructs a new edge between the given two vertices.
		 * @param v1 The first end point of the edge.
		 * @param v2 The second end point of the edge.
		 */
		private SimpleEdge(SimpleVertex<T> v1, SimpleVertex<T> v2){
			this.v1 = v1;
			this.v2 = v2;
		}
		
		/**
		 * Gets the first end point vertex for this end.
		 * @return The first vertex for this edge.
		 */
		public SimpleVertex<T> getFirstVertex(){
			return v1;
		}
		
		/**
		 * Gets the second end point vertex for this end.
		 * @return The second vertex for this edge.
		 */
		public SimpleVertex<T> getSecondVertex(){
			return v2;
		}
		
		/**
		 * Gets the target vertex for this edge given the source vertex. The
		 * given vertex is assumed to be one of the end point vertices of this
		 * edge and the returned vertex will be the other end point vertex of this edge.
		 * @param source The source vertex to use to determine the target vertex.
		 * @return The target vertex of this edge from the point of view of the source vertex.
		 */
		public SimpleVertex<T> getTarget(SimpleVertex<T> source){
			assert v1 == source || v2 == source;
			return v1 == source ? v2 : v1;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof SimpleEdge<?>){
				SimpleEdge<?> edge = (SimpleEdge<?>)obj;
				return (v1.equals(edge.v1) && v2.equals(edge.v2)) || (v1.equals(edge.v2) && v2.equals(edge.v1));
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(v1.hashCode() ^ v2.hashCode());
		}
	}
	
	/**
	 * Represents a single vertex in the graph.
	 * @author Roan
	 * @param <T> The data type of data stored at vertices.
	 */
	public static final class SimpleVertex<T> implements IDable{
		/**
		 * The data stored at this vertex that uniquely identifies it in the graph.
		 */
		private T data;
		/**
		 * The edges this vertex is an end point for.
		 */
		private Set<SimpleEdge<T>> edges = new HashSet<SimpleEdge<T>>();
		/**
		 * The ID of this vertex in the graph.
		 */
		private final int id;
		
		/**
		 * Constructs a new vertex with the given ID and data.
		 * @param id The ID of this vertex.
		 * @param data The data uniquely identifying this vertex.
		 */
		private SimpleVertex(int id, T data){
			this.id = id;
			this.data = data;
		}
		
		/**
		 * Gets a set of all edges that this vertex participates in as an end point.
		 * @return All the edges that this vertex is and end point of.
		 */
		public Set<SimpleEdge<T>> getEdges(){
			return edges;
		}
		
		/**
		 * Gets the data stored at this vertex. This piece of data uniquely
		 * identifies this vertex in the graph.
		 * @return The data associated with this vertex.
		 */
		public T getData(){
			return data;
		}
		
		/**
		 * Gets the degree of this vertex in the graph. This is equal
		 * to the number of edges that this vertex is and end point of.
		 * @return The degree of this vertex in the graph.
		 */
		public int getDegree(){
			return edges.size();
		}
		
		/**
		 * Checks if this vertex has an edge to the given target vertex.
		 * @param target The vertex to check for an edge to.
		 * @return True if this vertex has and edge to the given target vertex.
		 */
		public boolean hasEdge(SimpleVertex<T> target){
			return getEdge(target) != null;
		}
		
		/**
		 * Gets the edge from this vertex to the given target vertex.
		 * @param target The target vertex.
		 * @return The edge from this vertex to the given target vertex
		 *         or <code>null</code> if no such edge exists.
		 */
		public SimpleEdge<T> getEdge(SimpleVertex<T> target){
			for(SimpleEdge<T> edge : edges){
				if(edge.v2 == target || edge.v1 == target){
					return edge;
				}
			}
			return null;
		}
		
		/**
		 * Gets a list of all the neighbour vertices for this vertex, excluding itself.
		 * @return All neighbours of this vertex.
		 */
		public List<SimpleVertex<T>> getNeighbours(){
			return edges.stream().map(e->e.getTarget(this)).collect(Collectors.toList());
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Note that while this ID is unique for a given graph node, the
		 * uniqueness of a node in the graph is still determined by the
		 * data objected used to create the node. Hence two nodes with
		 * different IDs but the same data are in fact equal. Though this
		 * situation could only arise when comparing the IDs of nodes
		 * from different graph.
		 * @see #getData()
		 */
		@Override
		public int getID(){
			return id;
		}
		
		@Override
		public boolean equals(Object obj){
			return obj instanceof SimpleVertex<?> ? ((SimpleVertex<?>)obj).data.equals(data) : false;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(data);
		}
	}
}
