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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SimpleGraph;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.Tree;
import dev.roanh.gmark.util.UniqueGraph;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;
import dev.roanh.gmark.util.Util;

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
	 * @param label The label being traversed. Should not be a negated predicate.
	 * @param source The CPQ source vertex where the edge traversal starts.
	 * @param target The CPQ target vertex where the edge traversal ends.
	 */
	protected QueryGraphCPQ(Predicate label, Vertex source, Vertex target){
		this.source = source;
		this.target = target;
		vertices.add(source);
		vertices.add(target);
		edges.add(new Edge(source, target, label));
	}
	
	/**
	 * Gets the number of vertices in this query graph.
	 * @return The number of vertices in this query graph.
	 */
	public int getVertexCount(){
		merge();
		return vertices.size();
	}
	
	/**
	 * Gets the number of edges in this query graph.
	 * @return The number of edges in this query graph.
	 */
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
	public UniqueGraph<Vertex, Predicate> toUniqueGraph(){
		merge();
		UniqueGraph<Vertex, Predicate> graph = new UniqueGraph<Vertex, Predicate>();
		vertices.forEach(graph::addUniqueNode);
		for(Edge edge : edges){
			graph.addUniqueEdge(edge.src, edge.trg, edge.label);
		}
		return graph;
	}
	
	/**
	 * Computes the incidence graph of this query graph. The incidence graph of
	 * a query graph is a graph where both vertices and edges from the query graph
	 * are represented as vertices. Edges are only present between an edge nodes
	 * and vertex nodes and only if the vertex node represents a vertex that was
	 * one of the end points of the edge node in the original query graph.
	 * @return The incidence graph for this graph.
	 * @see QueryGraphComponent
	 */
	public SimpleGraph<QueryGraphComponent> toIncidenceGraph(){
		merge();
		SimpleGraph<QueryGraphComponent> g = new SimpleGraph<QueryGraphComponent>();
		vertices.forEach(g::addVertex);
		
		for(Edge edge : edges){
			SimpleVertex<QueryGraphComponent> v = g.addVertex(edge);
			g.addEdge(v, edge.src);
			g.addEdge(v, edge.trg);
		}

		return g;
	}
	
	/**
	 * Computes if there is a <b>query</b> homomorphism from this query graph G to the given
	 * other graph G'. This implies that any edge traversal made in this graph can be mimicked
	 * in the given other graph. Formally G -> G' or G is contained in G' (as a subgraph).
	 * <p>
	 * Note: This method tests for <b>query</b> homomorphism, as such the identity of the source
	 * and target vertices is extremely important. Specifically, the given other graph should use
	 * the exact same source and target vertices as this query graph. If not, there will never be
	 * a query homomorphism. To achieve this it is either possible to reuse the vertices from this
	 * graph or to manually pass source and target vertices when creating the query graph using
	 * the {@link CPQ#toQueryGraph(Vertex, Vertex)} method.
	 * @param graph The other graph to test for query homomorphism to.
	 * @return True when this query graph is query homomorphic to the given graph.
	 * @see <a href="https://doi.org/10.1016/S0304-3975(99)00220-0">Chandra Chekuri and Anand Rajaraman,
	 *      "Conjunctive query containment revisited", in Theoretical Computer Science, vol. 239, 2000, pp. 211-229</a>
	 */
	public boolean isHomomorphicTo(UniqueGraph<Vertex, Predicate> graph){
		merge();
		
		//compute a query decomposition
		Tree<List<QueryGraphComponent>> decomp = Util.computeTreeDecompositionWidth2(toIncidenceGraph());
		
		//pre compute mappings
		Map<QueryGraphComponent, List<Object>> known = new HashMap<QueryGraphComponent, List<Object>>();
		Map<Vertex, List<Edge>> outEdges = new HashMap<Vertex, List<Edge>>();
		Map<Vertex, List<Edge>> inEdges = new HashMap<Vertex, List<Edge>>();
		
		for(Vertex vertex : vertices){
			outEdges.put(vertex, new ArrayList<Edge>());
			inEdges.put(vertex, new ArrayList<Edge>());
		}
		
		for(Edge edge : edges){
			List<Object> matches = new ArrayList<Object>();
			for(GraphEdge<Vertex, Predicate> other : graph.getEdges()){
				if((edge.src == source) ^ (other.getSource() == source)){
					continue;
				}
				
				if((edge.trg == target) ^ (other.getTarget() == target)){
					continue;
				}
				
				if(other.getData().equals(edge.label)){
					matches.add(other);
				}
			}
			
			outEdges.get(edge.src).add(edge);
			inEdges.get(edge.trg).add(edge);
			known.put(edge, matches);
		}
		
		for(Vertex vertex : vertices){
			List<Object> matches = new ArrayList<Object>();
			for(GraphNode<Vertex, Predicate> other : graph.getNodes()){
				if((vertex == source) ^ (other.getData() == source)){
					continue;
				}
				
				if((vertex == target) ^ (other.getData() == target)){
					continue;
				}
				
				List<Edge> out = outEdges.get(vertex);
				if(!checkEquivalent(out, other.getOutEdges())){
					continue;
				}
				
				List<Edge> in = inEdges.get(vertex);
				if(!checkEquivalent(in, other.getInEdges())){
					continue;
				}
				
				matches.add(other);
			}
			
			known.put(vertex, matches);
		}
		
		//copy structure & compute candidate maps
		Tree<PartialMap> maps = decomp.cloneStructure(PartialMap::new);
		
		maps.forEach(node->{
			List<List<Object>> sets = new ArrayList<List<Object>>();
			for(QueryGraphComponent arg : node.getData().left){
				sets.add(known.get(arg));
			}
			
			node.getData().matches = Util.cartesianProduct(sets);
		});
		
		//join nodes bottom up
		maps.forEachBottomUp(node->{
			if(!node.isLeaf()){
				PartialMap map = node.getData();
				for(Tree<PartialMap> child : node.getChildren()){
					map.semiJoin(child.getData());
				}
			}
		});
		
		//a non empty root implies query homomorphism
		return !maps.getData().matches.isEmpty();
	}
	
	/**
	 * Executes the final merge step of the query graph construction algorithm,
	 * this step merges vertices that need to be merged together into the same
	 * vertex due to intersections with the identity operation.
	 */
	protected void merge(){
		//essentially picks a pair of vertices that needs to be the same node and
		//replaces all instances of the first node with the second node
		while(!fid.isEmpty()){
			Pair elem = getIdentityPair();
			
			//remove the old vertex
			vertices.remove(elem.first);
			
			//replace edge source/target vertex with the new vertex
			for(Edge edge : edges.stream().collect(Collectors.toList())){
				if(edge.src == elem.first){
					edges.add(new Edge(elem.second, edge.trg, edge.label));
				}
				
				if(edge.trg == elem.first){
					edges.add(new Edge(edge.src, elem.second, edge.label));
				}
				
				if(edge.src == elem.first && edge.trg == elem.first){
					edges.add(new Edge(elem.second, elem.second, edge.label));
				}
			}
			edges.removeIf(e->e.src == elem.first || e.trg == elem.first);
			
			//update source/target if required
			source = source == elem.first ? elem.second : source;
			target = target == elem.first ? elem.second : target;
			
			//replace old vertex with the new vertex in all remaining id pairs
			for(Pair pair : fid.stream().collect(Collectors.toList())){
				if(pair.second == elem.first){
					fid.add(new Pair(pair.first, elem.second));
				}
				
				if(pair.first == elem.first){
					fid.add(new Pair(elem.second, pair.second));
				}
			}
			fid.removeIf(p->p.first == elem.first || p.second == elem.first);
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
	
	@Override
	public String toString(){
		return "QueryGraphCPQ[V=" + vertices + ",E=" + edges + ",src=" + source + ",trg=" + target + ",Fid=" + fid + "]";
	}
	
	/**
	 * Helper method to check if the predicates on the edges in the given
	 * list all occur at least onces in the given second set of edges.
	 * @param first The set of edges whose predicates to find.
	 * @param second The set of edges where the same predicates need to
	 *        exist at least once.
	 * @return True if the second set contains all the predicates from
	 *         the first set at least once.
	 */
	private static boolean checkEquivalent(List<Edge> first, Set<GraphEdge<Vertex, Predicate>> second){
		Iterator<Predicate> edges = first.stream().map(e->e.label).sorted().distinct().iterator();
		Iterator<Predicate> other = second.stream().map(GraphEdge::getData).sorted().distinct().iterator();
		
		while(edges.hasNext()){
			Predicate p = edges.next();
			
			while(true){
				if(!other.hasNext()){
					return false;
				}else if(p.equals(other.next())){
					break;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Shared base interface for query graph elements.
	 * Objects of this type are either a {@link Vertex}
	 * or an {@link Edge}.
	 * @author Roan
	 */
	public static abstract interface QueryGraphComponent{
		
		/**
		 * Checks if this query graph component is a vertex.
		 * @return True if this is a vertex.
		 * @see Vertex
		 */
		public default boolean isVertex(){
			return this instanceof Vertex;
		}
		
		/**
		 * Checks if this query graph component is an edge.
		 * @return True if this is an edge.
		 * @see Edge
		 */
		public default boolean isEdge(){
			return this instanceof Edge;
		}
	}
	
	/**
	 * Represents a vertex in a CPQ query graph.
	 * @author Roan
	 */
	public static class Vertex implements QueryGraphComponent{
		
		@Override
		public String toString(){
			return String.valueOf((char)('a' + (this.hashCode() % 26)));
		}
	}
	
	/**
	 * Represents a directed edge in a CPQ query graph.
	 * @author Roan
	 */
	public static class Edge implements QueryGraphComponent{
		/**
		 * The edge source vertex.
		 */
		private final Vertex src;
		/**
		 * The edge target vertex.
		 */
		private final Vertex trg;
		/**
		 * The edge label.
		 */
		private final Predicate label;
		
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
		public int hashCode(){
			return Objects.hash(src, trg, label);
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Edge){
				Edge edge = (Edge)obj;
				return src == edge.src && trg == edge.trg && label.equals(edge.label);
			}else{
				return false;
			}
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
		private final Vertex first;
		/**
		 * The second vertex.
		 */
		private final Vertex second;
		
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
		public int hashCode(){
			return Objects.hash(first.hashCode() ^ second.hashCode());
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Pair){
				Pair other = (Pair)obj;
				return (other.first == first && other.second == second) || (other.first == second && other.second == first);
			}else{
				return false;
			}
		}
		
		@Override
		public String toString(){
			return "(" + first + "," + second + ")";
		}
	}
	
	/**
	 * Object used to store partial mapping required for the
	 * query homomorphism testing algorithm.
	 * @author Roan
	 * @see QueryGraphCPQ#isHomomorphicTo(UniqueGraph)
	 */
	private static final class PartialMap{
		/**
		 * The left hand side of the map, this is the
		 * side of the map with graph parts that need
		 * to be matched to equivalent parts in the other graph. 
		 */
		private List<QueryGraphComponent> left;
		/**
		 * The parts of the other graph that are equivalent
		 * to the {@link #left} part of the original graph.
		 */
		private List<List<Object>> matches;
		
		/**
		 * Constructs a new partial map with the given set
		 * of graph parts to match to parts of the other graph.
		 * @param left The parts of the graph to match for.
		 */
		private PartialMap(List<QueryGraphComponent> left){
			this.left = left;
		}
		
		/**
		 * Performs a natural semi join of this partial map
		 * with the given other partial map. This is effectively
		 * a filtering operation where anything in this map
		 * is dropped if it does not have any overlap with at
		 * least one list in the given other partial map.
		 * The result of the semi join is stored in this map.
		 * @param other The other partial map to join with.
		 */
		private void semiJoin(PartialMap other){
			matches.removeIf(match->{
				for(List<Object> filter : other.matches){
					for(Object obj : filter){
						if(mapContains(match, obj)){
							return false;
						}
					}
				}
				
				return true;
			});
		}
		
		/**
		 * Helper method to check if the given map match contains
		 * the given item. This method will make sure to extract
		 * vertices from edges as required. An edge is contained
		 * if at least one of its vertices is present and similarly
		 * a vertex is present if it is part of an edge.
		 * @param map The matching map to search.
		 * @param item The item to search for.
		 * @return True if the partial map contains the given item.
		 */
		private static boolean mapContains(List<Object> map, Object item){
			if(item instanceof GraphEdge){
				GraphEdge<?, ?> edge = (GraphEdge<?, ?>)item;
				return mapContains(map, edge.getSourceNode()) || mapContains(map, edge.getTargetNode());
			}else{
				for(Object elem : map){
					if(elem == item){
						return true;
					}else if(elem instanceof GraphEdge){
						GraphEdge<?, ?> edge = (GraphEdge<?, ?>)elem;
						if(edge.getSourceNode() == item || edge.getTargetNode() == item){
							return true;
						}
					}
				}
				return false;
			}
		}
	}
}
