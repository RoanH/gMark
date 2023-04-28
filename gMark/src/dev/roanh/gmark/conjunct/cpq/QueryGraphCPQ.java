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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.IDable;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.SimpleGraph;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.Tree;
import dev.roanh.gmark.util.UniqueGraph;
import dev.roanh.gmark.util.Util;

/**
 * Object representing the query graph of a CPQ. This
 * is effectively a visual representation of the CPQ
 * as a graph. The implementation for query graph
 * construction is loosely based on an algorithm
 * proposed by Seiji Maekawa.
 * @author Roan
 */
public class QueryGraphCPQ implements Cloneable{
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
	 * @param label The label being traversed.
	 * @param source The CPQ source vertex where the edge traversal starts.
	 * @param target The CPQ target vertex where the edge traversal ends.
	 */
	protected QueryGraphCPQ(Predicate label, Vertex source, Vertex target){
		this.source = source;
		this.target = target;
		vertices.add(source);
		vertices.add(target);
		
		if(label.isInverse()){
			edges.add(new Edge(target, source, label.getInverse()));
		}else{
			edges.add(new Edge(source, target, label));
		}
		
		source.deg++;
		target.deg++;
	}
	
	/**
	 * No args constructor for use by {@link #clone()}.
	 */
	private QueryGraphCPQ(){
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
		fid.addAll(other.fid);
		for(Edge e : other.edges){
			if(!edges.add(e)){
				e.src.deg--;
				e.trg.deg--;
			}
		}
		
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
	 * @param <M> The graph metadata data types.
	 * @return The incidence graph for this graph.
	 * @see QueryGraphComponent
	 */
	public <M> SimpleGraph<QueryGraphComponent, M> toIncidenceGraph(){
		merge();
		SimpleGraph<QueryGraphComponent, M> g = new SimpleGraph<QueryGraphComponent, M>(vertices.size() + edges.size());
		vertices.forEach(g::addVertex);
		
		for(Edge edge : edges){
			SimpleVertex<QueryGraphComponent, M> v = g.addVertex(edge);
			g.addEdge(v, edge.src);
			g.addEdge(v, edge.trg);
		}

		return g;
	}
	
	/**
	 * Computes if there is a <b>query</b> homomorphism from this query graph <code>G</code>
	 * to the given other graph <code>G'</code>. This implies that any edge traversal made in
	 * this graph can be mimicked in the given other graph. Formally {@code G -> G'} or
	 * <code>G</code> is contained in <code>G'</code> (as a subgraph).
	 * @param graph The other graph to test for query homomorphism to.
	 * @return True when this query graph is query homomorphic to the given graph map.
	 * @see <a href="https://doi.org/10.1016/S0304-3975(99)00220-0">Chandra Chekuri and Anand Rajaraman,
	 *      "Conjunctive query containment revisited", in Theoretical Computer Science, vol. 239, 2000, pp. 211-229</a>
	 */
	public boolean isHomomorphicTo(QueryGraphCPQ graph){
		merge();
		
		//compute base mappings
		RangeList<List<QueryGraphComponent>> known = computeMappings(graph);
		if(known == null){
			return false;
		}
		
		//compute a query decomposition with empty partial maps
		Tree<PartialMap> maps = Util.computeTreeDecompositionWidth2(toIncidenceGraph()).cloneStructure(PartialMap::new);
		
		//join nodes bottom up while computing candidate maps and dependent variables
		return !maps.forEachBottomUp(node->{
			PartialMap map = node.getData();
			expandPartialMap(map, known);
			
			if(!node.isLeaf()){
				for(Tree<PartialMap> child : node.getChildren()){
					map.semiJoin(child.getData());
					if(map.matches.isEmpty()){
						//if any intermediate map is empty the result will be empty
						return true;
					}
				}
			}
			
			//a non empty root implies query homomorphism
			return map.matches.isEmpty();
		});
	}
	
	/**
	 * Computes mappings from the vertices and edges of this graph to similar
	 * vertices and edges in the other given graph.
	 * @param graph The graph to compute mappings to.
	 * @return The mappings between this graph and the given other graph,
	 *         if no mappings are found for some vertex or edge then <code>
	 *         null</code> is returned.
	 */
	private RangeList<List<QueryGraphComponent>> computeMappings(QueryGraphCPQ graph){
		RangeList<List<QueryGraphComponent>> known = new RangeList<List<QueryGraphComponent>>(vertices.size() + edges.size());
		
		for(Vertex vertex : vertices){
			List<QueryGraphComponent> matches = new ArrayList<QueryGraphComponent>();
			for(Vertex other : graph.vertices){
				if((vertex == source) ^ (other == graph.source)){
					continue;
				}
				
				if((vertex == target) ^ (other == graph.target)){
					continue;
				}
				
				//note: it would be possible to force in/out edges here
				//but for the small graphs we usually work with that is
				//more intensive than it is worth (see thesis for more details).
				
				matches.add(other);
			}
			
			if(matches.isEmpty()){
				//if a vertex cannot be mapped there is no homomorphism
				return null;
			}
			
			known.set(vertex, matches);
		}
		
		for(Edge edge : edges){
			List<QueryGraphComponent> matches = new ArrayList<QueryGraphComponent>();
			for(Edge other : graph.edges){
				if((edge.src == source) ^ (other.src == graph.source)){
					continue;
				}
				
				if((edge.trg == target) ^ (other.trg == graph.target)){
					continue;
				}
				
				if(!other.label.equals(edge.label)){
					continue;
				}
				
				if(!known.get(edge.src).contains(other.src)){
					continue;
				}
				
				if(!known.get(edge.trg).contains(other.trg)){
					continue;
				}
				
				matches.add(other);
			}
			
			if(matches.isEmpty()){
				//if an edge cannot be mapped there is no homomorphism
				return null;
			}
			
			known.set(edge, matches);
		}
		
		return known;
	}
	
	/**
	 * Computes the right hand side of the given partial mapping.
	 * Both sides of the mapping are also extended with dependent variables.
	 * @param data The partial map to expand.
	 * @param known A map of know mappings for individual vertices and edges.
	 */
	private void expandPartialMap(PartialMap data, RangeList<List<QueryGraphComponent>> known){
		//sets to compute the Cartesian product of
		List<List<QueryGraphComponent>> sets = new ArrayList<List<QueryGraphComponent>>();
		
		//new left hand side of the mapping with dependent variables
		List<QueryGraphComponent> newLeft = new ArrayList<QueryGraphComponent>();
		
		//reference map stating for each edge where the independent variables for its end points are (else -1 or -2)
		List<int[]> refs = new ArrayList<int[]>();
		
		for(QueryGraphComponent arg : data.left){
			if(arg.isEdge()){
				Edge e = (Edge)arg;
				
				int si = newLeft.indexOf(e.src);
				int ti = newLeft.indexOf(e.trg);
				if(e.src.equals(e.trg)){
					ti = -2;//self loops only generate one dependent variable, signalled with -2 instead of -1
				}
				
				//independent variable
				newLeft.add(e);
				refs.add(new int[]{si, ti});
				sets.add(known.get(arg));
				
				//dependent variables
				if(si == -1){
					newLeft.add(e.src);
				}
				
				if(ti == -1){
					newLeft.add(e.trg);
				}
			}else{
				//dependent vertex variables are only added once and without reference
				if(newLeft.indexOf(arg) == -1){
					newLeft.add(arg);
					refs.add(new int[0]);
					sets.add(known.get(arg));
				}
			}
		}
		data.left = newLeft;
		
		//Cartesian product of all sets
		int size = sets.stream().mapToInt(List::size).reduce(StrictMath::multiplyExact).getAsInt();
		List<List<Object>> product = new ArrayList<List<Object>>(size);
		for(int i = 0; i < size; i++){
			product.add(new ArrayList<Object>());
		}
		
		//if one set is empty we're done
		if(size == 0){
			data.matches = product;
			return;
		}
		
		//build all output sets one with once element from each set at a time
		for(int setIdx = 0; setIdx < sets.size(); setIdx++){
			List<QueryGraphComponent> set = sets.get(setIdx);
			size /= set.size();
			
			int idx = 0;
			for(int o = 0; idx < product.size(); o++){
				List<Object> head = product.get(idx);
				
				//check if the candidate was previous discarded
				if(head == null){
					idx += size;
					continue;
				}
				
				int elemIdx = o % set.size();
				Object obj = set.get(elemIdx);
				int[] ref = refs.get(setIdx);

				if(ref.length != 0){
					//if we have refs we have an edge
					Edge edge = (Edge)obj;

					//check if referenced nodes match and the loop status matches
					if((ref[0] >= 0 && !head.get(ref[0]).equals(edge.src)) || (ref[1] >= 0 && !head.get(ref[1]).equals(edge.trg)) || (ref[1] == -2 && !edge.src.equals(edge.trg))){						
						//if not these candidates are invalid
						for(int i = 0; i < size; i++){
							product.set(idx++, null);
						}
						
						continue;
					}else{
						//valid edge mapping candidate
						for(int i = 0; i < size; i++){
							//add independent variable
							product.get(idx).add(obj);
							
							//add dependent variables
							if(ref[0] == -1){
								product.get(idx).add(edge.src);
							}
							
							if(ref[1] == -1){
								product.get(idx).add(edge.trg);
							}
							
							idx++;
						}
					}
				}else{
					//valid vertex mapping
					for(int i = 0; i < size; i++){
						product.get(idx++).add(obj);
					}
				}
			}
		}
		
		///remove invalid candidates and return
		product.removeIf(Objects::isNull);
		data.matches = product;
	}
	
	/**
	 * Computes the core of this CPQ query graph. The core is the smallest
	 * graph query homomorphically equivalent to this CPQ query graph.
	 * @return The core of this CPQ query graph.
	 */
	public QueryGraphCPQ computeCore(){
		QueryGraphCPQ core = this.clone();
		
		for(Edge edge : new ArrayList<Edge>(core.edges)){
			core.edges.remove(edge);

			if(!isHomomorphicTo(core)){
				core.edges.add(edge);
			}else{
				edge.src.deg--;
				edge.trg.deg--;
			}
		}
		
		core.vertices.removeIf(v->v.deg == 0);
		return core;
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
				if(edge.src == elem.first && edge.trg == elem.first){
					if(edges.add(new Edge(elem.second, elem.second, edge.label))){
						elem.second.deg += 2;
					}

					continue;
				}

				if(edge.src == elem.first){
					if(edges.add(new Edge(elem.second, edge.trg, edge.label))){
						elem.second.deg++;
					}else{
						edge.trg.deg--;
					}
				}

				if(edge.trg == elem.first){
					if(edges.add(new Edge(edge.src, elem.second, edge.label))){
						elem.second.deg++;
					}else{
						edge.src.deg--;
					}
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
		
		//assign numerical identifiers to all components
		int id = 0;
		for(Vertex v : vertices){
			v.id = id++;
		}
		
		for(Edge e : edges){
			e.id = id++;
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
	
	@Override
	protected QueryGraphCPQ clone(){
		QueryGraphCPQ copy = new QueryGraphCPQ();
		copy.source = source;
		copy.target = target;
		copy.edges = new HashSet<Edge>(edges);
		copy.vertices = new HashSet<Vertex>(vertices);
		return copy;
	}
	
	/**
	 * Shared base interface for query graph elements.
	 * Objects of this type are either a {@link Vertex}
	 * or an {@link Edge}.
	 * @author Roan
	 */
	public static abstract interface QueryGraphComponent extends IDable{
		
		/**
		 * Checks if this query graph component is a vertex.
		 * @return True if this is a vertex.
		 * @see Vertex
		 */
		public abstract boolean isVertex();
		
		/**
		 * Checks if this query graph component is an edge.
		 * @return True if this is an edge.
		 * @see Edge
		 */
		public abstract  boolean isEdge();
	}
	
	/**
	 * Represents a vertex in a CPQ query graph.
	 * @author Roan
	 */
	public static class Vertex implements QueryGraphComponent{
		/**
		 * The ID of this vertex.
		 */
		private int id;
		/**
		 * The degree of this vertex, this counts both incoming
		 * and outgoing edges. This means self loops are counted
		 * twice for the degree.
		 */
		private int deg;
		
		@Override
		public String toString(){
			return String.valueOf(id);
		}

		@Override
		public int getID(){
			return id;
		}

		@Override
		public boolean isVertex(){
			return true;
		}

		@Override
		public boolean isEdge(){
			return false;
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
		 * The ID of this edge.
		 */
		private int id;
		
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

		@Override
		public int getID(){
			return id;
		}

		@Override
		public boolean isVertex(){
			return false;
		}

		@Override
		public boolean isEdge(){
			return true;
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
	 * @see QueryGraphCPQ#isHomomorphicTo(QueryGraphCPQ)
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
		 * Performs a natural left semi join of this partial map
		 * with the given other partial map. This is effectively
		 * a filtering operation where anything in this map
		 * is dropped if it does not have any overlap with at
		 * least one list in the given other partial map.
		 * The result of the semi join is stored in this map.
		 * @param other The other partial map to join with.
		 */
		private void semiJoin(PartialMap other){
			int[] map = new int[left.size()];
			for(int i = 0; i < map.length; i++){
				map[i] = other.left.indexOf(left.get(i));
			}
			
			matches.removeIf(match->{
				test: for(List<Object> filter : other.matches){
					for(int i = 0; i < map.length; i++){
						if(map[i] != -1 && !match.get(i).equals(filter.get(map[i]))){
							continue test;
						}
					}
					
					return false;
				}
				
				return true;
			});
		}
	}
}
