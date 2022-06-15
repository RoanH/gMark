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
package dev.roanh.gmark.conjunct.cpq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Graph;

/**
 * Object representing the query graph of a CPQ. This
 * is effectively a visual representation of the CPQ
 * as a graph. The implementation is loosely based on
 * an algorithm proposed by Seiji Maekawa.
 * @author Roan
 */
public class QueryGraphCPQ{
	/**
	 * The set of vertices for this query graph.
	 */
	private Set<Vertex> vertices = new HashSet<Vertex>();
	/**
	 * The set of edges for this query graph.
	 */
	private Set<Edge> edges = new HashSet<Edge>();
	/**
	 * The source vertex of the CPQ.
	 */
	private Vertex source;
	/**
	 * The target vertex of the CPQ.
	 */
	private Vertex target;
	/**
	 * Set of identity pair that still need to be processed,
	 * always empty for a fully constructed query graph.
	 */
	private Set<Pair> fid = new HashSet<Pair>();
	
	/**
	 * Constructs a new query graph containing only the
	 * given source and target vertex. This effectively
	 * constructs a query graph for the identity CPQ.
	 * @param source The CPQ source vertex.
	 * @param target The CPQ target vertex.
	 */
	protected QueryGraphCPQ(Vertex source, Vertex target){
		this.source = source;
		this.target = target;
		vertices.add(source);
		vertices.add(target);
		fid.add(new Pair(source, target));
	}
	
	/**
	 * Constructs a new query graph for the CPQ containing
	 * only a single edge label traversal.
	 * @param cpq The CPQ describing the labelled edge traversal.
	 * @param source The CPQ source vertex where the edge traversal starts.
	 * @param target The CPQ target vertex where the edge traversal ends.
	 */
	protected QueryGraphCPQ(EdgeCPQ cpq, Vertex source, Vertex target){
		this.source = source;
		this.target = target;
		vertices.add(source);
		vertices.add(target);
		edges.add(new Edge(source, target, cpq.getLabel()));
	}
	
	public int getVertexCount(){
		merge();
		return vertices.size();
	}
	
	public int getEdgeCount(){
		merge();
		return edges.size();
	}
	
	/**
	 * Computes the union of this query graph and the
	 * given other query graph by merging the vertex,
	 * edge and identity sets. Note that this method
	 * does not create a new query graph and instead
	 * updates this graph with the data from the given
	 * other graph. 
	 * @param other The graph to merge with.
	 * @return The computed union graph (equal to this graph).
	 */
	protected QueryGraphCPQ union(QueryGraphCPQ other){
		vertices.addAll(other.vertices);
		edges.addAll(other.edges);
		fid.addAll(other.fid);
		return this;
	}
	
	/**
	 * Gets the query graph source vertex.
	 * @return The source vertex.
	 */
	public Vertex getSourceVertex(){
		return source;
	}
	
	/**
	 * Gets the query graph target vertex.
	 * @return The target vertex.
	 */
	public Vertex getTargetVertex(){
		return target;
	}
	
	/**
	 * Sets the target vertex of this CPQ query graph.
	 * @param target The new target vertex.
	 */
	protected void setTarget(Vertex target){
		this.target = target;
	}
	
	/**
	 * Converts this query graph to an actual
	 * graph instance.
	 * @return The constructed graph instance.
	 */
	public Graph<Vertex, Predicate> toGraph(){
		merge();
		Graph<Vertex, Predicate> graph = new Graph<Vertex, Predicate>();
		vertices.forEach(graph::addUniqueNode);
		for(Edge edge : edges){
			graph.addUniqueEdge(edge.src, edge.trg, edge.label);
		}
		return graph;
	}
	
	/**
	 * Executes the final merge step of the query graph construction algorithm,
	 * this step merges vertices that need to be merged together into the same
	 * vertex due to intersections with the identity operation.
	 */
	protected void merge(){
		while(!fid.isEmpty()){
			Pair elem = getIdentityPair();
			
			vertices.remove(elem.first);
			
			for(Edge edge : edges.stream().collect(Collectors.toList())){
				if(edge.src == elem.first){
					edges.add(new Edge(elem.second, edge.trg, edge.label));
				}
				
				if(edge.trg == elem.first){
					edges.add(new Edge(edge.src, elem.second, edge.label));
				}
			}
			edges.removeIf(e->e.src == elem.first || e.trg == elem.first);
			
			source = source == elem.first ? source : elem.second;
			target = target == elem.first ? target : elem.second;
			
			for(Pair pair : fid.stream().collect(Collectors.toList())){
				if(pair.second == elem.first){
					fid.add(new Pair(elem.second, pair.second));
				}
				
				if(pair.first == elem.first){
					fid.add(new Pair(pair.first, elem.second));
				}
			}
		}
	}
	
	/**
	 * Gets a single identity pair that still
	 * needs to be processed and removes it from the set.
	 * @return The retrieved identity pair.
	 */
	private Pair getIdentityPair(){
		Iterator<Pair> iter = fid.iterator();
		Pair elem = iter.next();
		iter.remove();
		return elem;
	}
	
	@Override
	public String toString(){
		return "QueryGraphCPQ[V=" + vertices + ",E=" + edges + ",src=" + source + ",trg=" + target + ",Fid=" + fid + "]";
	}
	
	/**
	 * Represents a vertex in a CPQ query graph.
	 * @author Roan
	 */
	public static class Vertex{
		
		@Override
		public String toString(){
			return String.valueOf((char)('a' + (this.hashCode() % 26)));
		}
	}
	
	/**
	 * Computes the label for a vertex in a CPQ query graph. Note
	 * this graph is technically not labelled but a source and target
	 * vertex can be identified (and might be identical).
	 * @param vertex The vertex to get the label for.
	 * @return The label for the given vertex.
	 */
	public String getVertexLabel(Vertex vertex){
		if(vertex == source){
			return vertex == target ? "src,trg" : "src";
		}else{
			return vertex == target ? "trg" : "";
		}
	}
	
	/**
	 * Represents a directed edge in a CPQ query graph.
	 * @author Roan
	 */
	private static class Edge{
		/**
		 * The edge source vertex.
		 */
		private Vertex src;
		/**
		 * The edge target vertex.
		 */
		private Vertex trg;
		/**
		 * The edge label.
		 */
		private Predicate label;
		
		/**
		 * Constructs a new edge with the given source
		 * and target vertex and edge label.
		 * @param src The edge source vertex.
		 * @param trg The edge target vertex.
		 * @param label The edge label.
		 */
		private Edge(Vertex src, Vertex trg, Predicate label){
			this.src = src;
			this.trg = trg;
			this.label = label;
		}
		
		@Override
		public String toString(){
			return "(" + src + "," + trg + "," + label.getAlias() + ")";
		}
	}
	
	/**
	 * Represents a pair of vertices used for identity processing.
	 * @author Roan
	 */
	private static class Pair{
		/**
		 * The first vertex.
		 */
		private Vertex first;
		/**
		 * The second vertex.
		 */
		private Vertex second;
		
		/**
		 * Constructs a new pair of vertices.
		 * @param first The first vertex.
		 * @param second The second vertex.
		 */
		private Pair(Vertex first, Vertex second){
			this.first = first;
			this.second = second;
		}
		
		@Override
		public String toString(){
			return "(" + first + "," + second + ")";
		}
	}
}
