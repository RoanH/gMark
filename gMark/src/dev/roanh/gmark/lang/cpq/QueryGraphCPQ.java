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
package dev.roanh.gmark.lang.cpq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.roanh.gmark.type.IDable;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.SimpleGraph;
import dev.roanh.gmark.util.graph.generic.Tree;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleVertex;

/**
 * Object representing the query graph of a CPQ. This is effectively a visual representation
 * of the CPQ as a graph. The implementation for query graph construction is loosely based
 * on an algorithm proposed by Seiji Maekawa. The algorithm for query graph core computation
 * and query homomorphism testing are from my Master's thesis.
 * @author Roan
 * @see CPQ
 * @see <a href="https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsection.2.2.3">
 *      Indexing Conjunctive Path Queries for Accelerated Query Evaluation, Section 2.2.3: CPQ Query Graphs</a>
 * @see <a href="https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#chapter.3">
 *      Indexing Conjunctive Path Queries for Accelerated Query Evaluation, Section 3: The Core of a CPQ</a>
 */
public class QueryGraphCPQ{
	/**
	 * The set of vertices for this query graph.
	 */
	private final Set<Vertex> vertices = new HashSet<Vertex>();
	/**
	 * The set of edges for this query graph.
	 */
	private final Set<Edge> edges = new HashSet<Edge>();
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
	 * always null for a fully constructed query graph.
	 */
	private Set<Pair> fid;
	/**
	 * Bit set with true bits corresponding to edge labels
	 * that appear somewhere in this graph.
	 */
	private long[] labelSet;
	
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
		fid = new HashSet<Pair>();
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
		fid = new HashSet<Pair>();
		
		if(label.isInverse()){
			edges.add(new Edge(target, source, label.getInverse()));
		}else{
			edges.add(new Edge(source, target, label));
		}
	}
	
	/**
	 * No args constructor that creates a completely empty query graph.
	 */
	private QueryGraphCPQ(){
		fid = null;
	}
	
	/**
	 * Reverses the query graph by swapping the source and target vertex.
	 * Naturally, this changes nothing for graphs that are a loop.
	 * @see #isLoop()
	 */
	public void reverse(){
		Vertex oldSrc = source;
		source = target;
		target = oldSrc;
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
	 * Converts this query graph to an actual graph instance.
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
	 * Gets the set of edges for this query graph.
	 * @return The set of edges for this query graph.
	 */
	public Set<Edge> getEdges(){
		merge();
		return edges;
	}
	
	/**
	 * Gets the set of vertices for this query graph.
	 * @return The set of vertices for this query graph.
	 */
	public Set<Vertex> getVertices(){
		merge();
		return vertices;
	}
	
	/**
	 * Checks if this query graph is a loop, that is, the
	 * source and target vertex of this query are the same vertex.
	 * @return True if this query graph is a loop.
	 */
	public boolean isLoop(){
		merge();
		return source.equals(target);
	}
	
	/**
	 * Computes the incidence graph of this query graph. The incidence graph of
	 * a query graph is a graph where both vertices and edges from the query graph
	 * are represented as vertices. Edges are only present between an edge nodes
	 * and vertex nodes and only if the vertex node represents a vertex that was
	 * one of the end points of the edge node in the original query graph.
	 * @param <M> The graph metadata data type.
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
	 * @return True when this query graph is query homomorphic to the given graph.
	 * @see <a href="https://doi.org/10.1016/S0304-3975(99)00220-0">Chandra Chekuri and Anand Rajaraman,
	 *      "Conjunctive query containment revisited", in Theoretical Computer Science, vol. 239, 2000, pp. 211-229</a>
	 */
	public boolean isHomomorphicTo(QueryGraphCPQ graph){
		merge();
		
		//check used labels, if we use labels the other graph does not have there is no homomorphism
		if(labelSet.length > graph.labelSet.length){
			return false;
		}
		
		for(int i = 0; i < labelSet.length; i++){
			if((labelSet[i] & graph.labelSet[i]) != labelSet[i]){
				return false;
			}
		}

		//compute base mappings
		RangeList<List<QueryGraphComponent>> known = computeMappings(graph);
		if(known == null){
			return false;
		}

		//compute a query decomposition with empty partial maps
		Tree<PartialMap> maps = Util.computeTreeDecompositionWidth2(toIncidenceGraph()).cloneStructure(PartialMap::new);

		//join nodes bottom up while computing candidate maps and dependent variables
		for(Tree<PartialMap> node : maps){
			PartialMap map = node.getData();
			expandPartialMap(map, known);

			if(!node.isLeaf()){
				for(Tree<PartialMap> child : node.getChildren()){
					map.semiJoinSingle(child.getData());
					if(map.matches.length == 0){
						//if any intermediate map is empty the result will be empty
						return false;
					}
				}
			}
		}
		
		//a non empty root implies query homomorphism
		return maps.getData().matches.length != 0;
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
			check: for(Vertex other : graph.vertices){
				if(vertex == source && other != graph.source){
					continue;
				}
				
				if(vertex == target && other != graph.target){
					continue;
				}
				
				//required labels on incoming edges
				for(int i = 0; i < vertex.in.length; i++){
					if((vertex.in[i] & other.in[i]) != vertex.in[i]){
						continue check;
					}
				}
				
				//required labels on outgoing edges
				for(int i = 0; i < vertex.out.length; i++){
					if((vertex.out[i] & other.out[i]) != vertex.out[i]){
						continue check;
					}
				}
				
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
				if((edge.src == source) && (other.src != graph.source)){
					continue;
				}
				
				if((edge.trg == target) && (other.trg != graph.target)){
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
		Row[] product = new Row[size];
		for(int i = 0; i < size; i++){
			product[i] = new Row(data.left.size());
		}
		
		//if one set is empty we're done
		if(size == 0){
			data.matches = product;
			return;
		}
		
		//build all output sets at once with one element from each set at a time
		int nulls = 0;
		for(int setIdx = 0; setIdx < sets.size(); setIdx++){
			List<QueryGraphComponent> set = sets.get(setIdx);
			size /= set.size();
			
			int idx = 0;
			for(int o = 0; idx < product.length; o++){
				Row head = product[idx];
				
				//check if the candidate was previous discarded
				if(head == null){
					idx += size;
					continue;
				}
				
				QueryGraphComponent obj = set.get(o % set.size());
				int[] ref = refs.get(setIdx);

				if(ref.length != 0){
					//if we have refs we have an edge
					Edge edge = (Edge)obj;

					//check if referenced nodes match and the loop status matches
					if((ref[0] >= 0 && !head.get(ref[0]).equals(edge.src)) || (ref[1] >= 0 && !head.get(ref[1]).equals(edge.trg)) || (ref[1] == -2 && !edge.src.equals(edge.trg))){						
						//if not these candidates are invalid
						for(int i = 0; i < size; i++){
							product[idx++] = null;
						}
						nulls += size;
						
						continue;
					}else{
						//valid edge mapping candidate
						for(int i = 0; i < size; i++){
							//add independent variable
							product[idx].add(obj);
							
							//add dependent variables
							if(ref[0] == -1){
								product[idx].add(edge.src);
							}
							
							if(ref[1] == -1){
								product[idx].add(edge.trg);
							}
							
							idx++;
						}
					}
				}else{
					//valid vertex mapping
					for(int i = 0; i < size; i++){
						product[idx++].add(obj);
					}
				}
			}
		}
		
		///remove invalid candidates and return
		data.matches = filterNull(product, nulls);
		data.sort();
	}
	
	/**
	 * Computes the core of this CPQ query graph. The core is the smallest
	 * graph query homomorphically equivalent to this CPQ query graph.
	 * @return The core of this CPQ query graph.
	 */
	public QueryGraphCPQ computeCore(){
		merge();
		
		//compute base mappings
		RangeList<List<QueryGraphComponent>> known = computeMappings(this);
		
		//compute a query decomposition with empty partial maps
		Tree<PartialMap> maps = Util.computeTreeDecompositionWidth2(toIncidenceGraph()).cloneStructure(PartialMap::new);
		
		//join nodes bottom up while computing candidate maps, dependent variables and full mapping information
		for(Tree<PartialMap> node : maps){
			PartialMap map = node.getData();
			expandPartialMap(map, known);
			
			if(!node.isLeaf()){
				for(Tree<PartialMap> child : node.getChildren()){
					map.semiJoin(child.getData());
				}
			}
		}
		
		//pick the best mapping
		int bestCost = Integer.MAX_VALUE;
		BitSet bestUsage = null;
		for(Row row : maps.getData().matches){
			row.computeBestUsage(vertices.size() + edges.size());
			if(row.cost < bestCost){
				bestCost = row.cost;
				bestUsage = row.best;
			}
		}
		
		//construct the core graph
		QueryGraphCPQ core = new QueryGraphCPQ();
		Vertex[] vMap = new Vertex[vertices.size()];
		for(Vertex vertex : vertices){
			if(bestUsage.get(vertex.id)){
				Vertex v = new Vertex();
				v.in = vertex.in;
				v.out = vertex.out;
				vMap[vertex.id] = v;
				core.vertices.add(v);
			}
		}
		
		core.labelSet = labelSet;
		core.source = vMap[source.getID()];
		core.target = vMap[target.getID()];
		for(Edge edge : edges){
			if(bestUsage.get(edge.id)){
				core.edges.add(new Edge(vMap[edge.getSource().getID()], vMap[edge.getTarget().getID()], edge.getLabel()));
			}
		}
		
		core.assignIdentifiers();
		
		//return the constructed core
		return core;
	}
	
	/**
	 * Executes the final merge step of the query graph construction algorithm,
	 * this step merges vertices that need to be merged together into the same
	 * vertex due to intersections with the identity operation.
	 */
	protected void merge(){
		if(fid == null){
			return;
		}
		
		//essentially picks a pair of vertices that needs to be the same node and
		//replaces all instances of the first node with the second node
		while(!fid.isEmpty()){
			Pair elem = getIdentityPair();
			
			//remove the old vertex
			vertices.remove(elem.first);
			
			//replace edge source/target vertex with the new vertex
			for(Edge edge : edges.stream().toList()){
				if(edge.src == elem.first && edge.trg == elem.first){
					edges.add(new Edge(elem.second, elem.second, edge.label));
					continue;
				}

				if(edge.src == elem.first){
					edges.add(new Edge(elem.second, edge.trg, edge.label));
				}

				if(edge.trg == elem.first){
					edges.add(new Edge(edge.src, elem.second, edge.label));
				}
			}
			edges.removeIf(e->e.src == elem.first || e.trg == elem.first);
			
			//update source/target if required
			source = source == elem.first ? elem.second : source;
			target = target == elem.first ? elem.second : target;
			
			//replace old vertex with the new vertex in all remaining id pairs
			for(Pair pair : fid.stream().toList()){
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
		fid = null;
		assignIdentifiers();
		
		//set in/out properties
		int max = 0;
		for(Edge e : edges){
			max = Math.max(max, e.label.getID());
		}
		
		labelSet = new long[max + 1];
		for(Vertex v : vertices){
			v.in = new long[max + 1];
			v.out = new long[max + 1];
		}
		
		for(Edge e : edges){
			final int id = e.label.getID();
			e.src.out[id >> 6] |= 1L << id;
			e.trg.in[id >> 6] |= 1L << id;
			labelSet[id >> 6] |= 1L << id;
		}
	}
	
	/**
	 * Assigns a unique ID to all components of this query graph.
	 */
	private void assignIdentifiers(){
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
	
	/**
	 * Small utility function that returns a copy of the given
	 * input array with all null entries removed.
	 * @param data The input array containing null values.
	 * @param nulls The number of null values in the passed array,
	 *        if 0 then the input array is returned.
	 * @return An array containing all non-null elements from the
	 *         given input array in the same order.
	 */
	private static final Row[] filterNull(Row[] data, int nulls){
		if(nulls == 0){
			return data;
		}else{
			Row[] rows = new Row[data.length - nulls];
			int i = 0;
			for(Row row : data){
				if(row != null){
					rows[i++] = row;
				}
			}
			return rows;
		}
	}
	
	/**
	 * Shared base interface for query graph elements. Objects of
	 * this type are either a {@link Vertex} or an {@link Edge}.
	 * @author Roan
	 */
	public static abstract sealed class QueryGraphComponent implements IDable, Comparable<QueryGraphComponent> permits Vertex, Edge{
		/**
		 * The ID of this component.
		 */
		protected int id;
		
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
		
		@Override
		public int compareTo(QueryGraphComponent other){
			return Integer.compare(getID(), other.getID());
		}
		
		@Override
		public int getID(){
			return id;
		}
	}
	
	/**
	 * Represents a vertex in a CPQ query graph.
	 * @author Roan
	 */
	public static final class Vertex extends QueryGraphComponent{
		/**
		 * Bit set of label IDs on edges to this vertex.
		 */
		private long[] in;
		/**
		 * Bit set of label IDs on edges from this vertex.
		 */
		private long[] out;
		
		@Override
		public String toString(){
			return String.valueOf(getID());
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
	public static final class Edge extends QueryGraphComponent{
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
		
		/**
		 * Gets the source vertex of this edge.
		 * @return The source vertex of this edge.
		 */
		public Vertex getSource(){
			return src;
		}
		
		/**
		 * Gets the target vertex of this edge.
		 * @return The target vertex of this edge.
		 */
		public Vertex getTarget(){
			return trg;
		}
		
		/**
		 * Gets the label of this edge.
		 * @return The label of this edge.
		 */
		public Predicate getLabel(){
			return label;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(src, trg, label);
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof Edge edge){
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
			if(obj instanceof Pair other){
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
		 * The left hand side of the map, this is the side of the map
		 * with graph parts that need to be matched to equivalent parts
		 * in the other graph. This list essentially represents the attribute
		 * names of the relation for this map.
		 */
		private List<QueryGraphComponent> left;
		/**
		 * The parts of the other graph that are equivalent to the {@link #left}
		 * part of the original graph. This array is essentially a list of rows
		 * in the relation, each representing a valid way of mapping all attributes.
		 */
		private Row[] matches;
		
		/**
		 * Constructs a new partial map with the given set
		 * of graph parts to match to parts of the other graph.
		 * @param left The parts of the graph to match for.
		 */
		private PartialMap(List<QueryGraphComponent> left){
			this.left = left;
		}
		
		/**
		 * Sorts the relation for this map on attribute names.
		 * This enforces a total order on the {@link #left} elements
		 * and sorts the corresponding {@link #matches} at the same time.
		 * This sort takes linear time in the largest distance between
		 * two elements of the {@link #left} array.
		 */
		private void sort(){
			int min = Integer.MAX_VALUE;
			int max = 0;
			
			for(QueryGraphComponent comp : left){
				min = Math.min(min, comp.getID());
				max = Math.max(max, comp.getID());
			}
			
			int[] sorted = new int[max - min + 1];
			for(int i = 0; i < left.size(); i++){
				sorted[left.get(i).getID() - min] = i + 1;
			}
			
			int target = 0;
			for(int idx = 0; idx < sorted.length; idx++){
				int i = sorted[idx];
				if(i != 0){
					sorted[swap(i - 1, target) - min] = i;
					target++;
				}
			}
		}
		
		/**
		 * Swaps the element at the i-th and j-th position in
		 * {@link #left} and all {@link #matches}.
		 * @param i The first element position.
		 * @param j The second element position.
		 * @return The {@link QueryGraphComponent#getID()} of
		 *         the element originally at the j-th position.
		 */
		private int swap(int i, int j){
			QueryGraphComponent elem = left.get(i);
			elem = left.set(j, elem);
			left.set(i, elem);
			
			for(Row r : matches){
				QueryGraphComponent old = r.match[i];
				r.match[i] = r.match[j];
				r.match[j] = old;
			}
			
			return elem.getID();
		}
		
		/**
		 * Performs a natural left semi join of this partial map with the given other
		 * partial map. This is effectively a filtering operation where anything in this
		 * map is dropped if it does not have any overlap with at least one list in the
		 * given other partial map. The result of the semi join is stored in this map.
		 * Both maps are required to be sorted before running this join.
		 * @param other The other partial map to join with.
		 * @see #semiJoin(PartialMap)
		 * @see #sort()
		 */
		private void semiJoinSingle(PartialMap other){
			int nulls = 0;
			outer: for(int r = 0; r < matches.length; r++){
				Row row = matches[r];
				filter: for(Row match : other.matches){
					int ai = 0;
					int bi = 0;
					
					while(bi < other.left.size()){
						if(ai < left.size()){
							QueryGraphComponent first = left.get(ai);
							QueryGraphComponent second = other.left.get(bi);
							
							if(first == second){
								if(row.match[ai] != match.match[bi]){
									continue filter;
								}
								
								ai++;
								bi++;
							}else if(first.getID() > second.getID()){
								bi++;
							}else{
								ai++;
							}
						}else{
							bi++;
						}
					}
					
					//OK
					continue outer;
				}
				
				//remove rows that do not join with anything
				matches[r] = null;
				nulls++;
			}
			
			matches = filterNull(matches, nulls);
		}
		
		/**
		 * Performs a natural left semi join of this partial map with the given other
		 * partial map. This is effectively a filtering operation where anything in this
		 * map is dropped if it does not have any overlap with at least one list in the
		 * given other partial map. The result of the semi join is stored in this map.
		 * Both maps are required to be sorted before running this join.
		 * <p>
		 * Information about attributes from the given other map that did not appear in
		 * this map is also collected and attached to the rows in the {@link #matches}
		 * stored at this map. As a result this join makes it possible to reconstruct
		 * complete graph homomorphism mappings.
		 * @param other The other partial map to join with.
		 * @see #semiJoinSingle(PartialMap)
		 * @see #sort()
		 */
		private void semiJoin(PartialMap other){
			int nulls = 0;
			for(int r = 0; r < matches.length; r++){
				Row row = matches[r];
				boolean hasMatch = false;
				OptionSet options = new OptionSet(row);
				filter: for(Row match : other.matches){
					BitSet maps = new BitSet();
					int ai = 0;
					int bi = 0;
					
					while(bi < other.left.size()){
						if(ai < left.size()){
							QueryGraphComponent first = left.get(ai);
							QueryGraphComponent second = other.left.get(bi);
							
							if(first == second){
								if(row.match[ai] != match.match[bi]){
									continue filter;
								}
								
								ai++;
								bi++;
							}else if(first.getID() > second.getID()){
								maps.set(match.get(bi).id);
								bi++;
							}else{
								ai++;
							}
						}else{
							maps.set(match.get(bi).id);
							bi++;
						}
					}
					
					//OK
					hasMatch = true;
					if(!match.other.isEmpty()){
						options.addAll(maps, match.other);
					}else if(!maps.isEmpty()){
						options.add(maps);
					}
				}
				
				if(!options.options.isEmpty()){
					row.other.add(options);
				}
				
				//remove rows that do not join with anything
				if(!hasMatch){
					matches[r] = null;
					nulls++;
				}
			}
			
			matches = filterNull(matches, nulls);
		}
		
		@Override
		public String toString(){
			return left + " -> " + Arrays.toString(matches);
		}
	}
	
	/**
	 * Represents a set of possible mappings for attributes that
	 * were processed in the past, but will not be seen again in
	 * future tree nodes. These mappings are essentially part of
	 * complete homomorphism mappings for the graph, but are not
	 * relevant to the homomorphism finding algorithm anymore.
	 * Furthermore, this set automatically filters any elements
	 * that are added, keeping only the smallest candidates.
	 * Note that since the added elements represent attributes
	 * that are never seen again we are free to map them in any
	 * way we like without affecting the correct of the of the
	 * algorithm, so we will only keep those candidates that map
	 * to the fewest distinct targets. The row essentially tracks
	 * all the still relevant attributes, while past attributes
	 * are tracked by this option set.
	 * @author Roan
	 */
	private static final class OptionSet{
		/**
		 * All smallest past attribute mappings for this option set.
		 * Note that while each candidate consists of only query graph
		 * components, these are actually mapping from an attribute to
		 * this component. However, we do not actually ever need to use
		 * left hand side attribute of the map, so we do not store it.
		 */
		private final List<BitSet> options = new ArrayList<BitSet>();
		/**
		 * A bit set with the components in the match for the row
		 * this option set belongs to set to true.
		 * The row this option set belongs to.
		 */
		private final BitSet base = new BitSet();
		/**
		 * The current cost of the lowest cost attribute mappings in
		 * {@link #options}. This cost is the number of distinct mapping targets.
		 */
		private int cost = Integer.MAX_VALUE;
		
		/**
		 * Constructs a new empty option set for the given row.
		 * @param row The row to create an option set for.
		 * @see Row
		 */
		private OptionSet(Row row){
			for(QueryGraphComponent c : row.match){
				base.set(c.id);
			}
		}
		
		/**
		 * Adds new mappings to the option set as constructed from the given prefix
		 * and list of option sets to join with. This will attempt to add a new entry
		 * for each unique combination of attribute mappings in the given list of option
		 * sets, essentially computing a Cartesian product. The given prefix targets are
		 * added to every candidate generated in this manner.
		 * @param prefix The prefix mapping targets to add to every candidate encoded as a bit set.
		 * @param toAdd The option sets to compute combinations of.
		 */
		private void addAll(BitSet prefix, List<OptionSet> toAdd){
			prefix.or(base);
			addAll(toAdd, 0, prefix);
		}
		
		/**
		 * Adds new mappings to the option set as constructed from the given prefix
		 * and list of option sets to join with. This will attempt to add a new entry
		 * for each unique combination of attribute mappings in the given list of option
		 * sets, essentially computing a Cartesian product. The work set indicates the
		 * components picked so far and the offset indicates the next option set to pick from.
		 * @param toAdd The option sets to compute combinations of.
		 * @param offset The next option set to pick from.
		 * @param workSet The current set of picked components encoded as a bit set.
		 */
		private void addAll(List<OptionSet> toAdd, int offset, BitSet workSet){
			if(offset >= toAdd.size()){
				addDirect(workSet);
			}else{
				for(BitSet opt : toAdd.get(offset).options){
					BitSet data = new BitSet();
					data.or(workSet);
					data.or(opt);
					addAll(toAdd, offset + 1, data);
				}
			}
		}
		
		/**
		 * Adds a new attribute mapping to this option set. However, if this mapping
		 * has a higher cost than the current options it is discarded. Conversely,
		 * if it has a lower cost all current options are discarded instead. The cost
		 * is computed as the number  of distinct mapping targets when combined with
		 * the mappings at the the row for this option set.
		 * @param maps The new mapping to add (complete with the row mapping targets).
		 * @see #cost
		 */
		private void addDirect(BitSet maps){
			int cost = maps.cardinality();
			if(cost <= this.cost){
				if(cost < this.cost){
					options.clear();
					this.cost = cost;
				}
				
				options.add(maps);
			}
		}
		
		/**
		 * Adds a new attribute mapping to this option set. However, if this mapping
		 * has a higher cost than the current options it is discarded. Conversely,
		 * if it has a lower cost all current options are discarded instead. The cost
		 * is computed as the number  of distinct mapping targets when combined with
		 * the mappings at the the row for this option set.
		 * @param maps The new mapping to add (without the row mapping targets).
		 */
		private void add(BitSet maps){
			maps.or(base);
			addDirect(maps);
		}
		
		@Override
		public String toString(){
			return options.toString();
		}
	}
	
	/**
	 * A row instance represents a single potential homomorphism mapping
	 * for a partial map. A row also tracks information about past attributes
	 * that are required to construct a complete homomorphism mapping.
	 * @author Roan
	 * @see PartialMap
	 */
	private static final class Row{
		/**
		 * The targets that this row maps the attributes from the partial map to.
		 */
		private QueryGraphComponent[] match;
		/**
		 * A list of option sets that complete the entire homomorphism mapping
		 * when combined with the {@link #match} for this row. When constructing
		 * the complete mapping exactly one option from each option set has to be used.
		 */
		private List<OptionSet> other = new ArrayList<OptionSet>();
		/**
		 * Current write index in {@link #match}, this is the next position
		 * that a query graph component will be written to by {@link #add(QueryGraphComponent)}.
		 */
		private int write = 0;
		/**
		 * The best (smallest) complete mapping as represented by this row. This bitset
		 * can be seen as a map from {@link QueryGraphComponent#getID()} to a boolean,
		 * where true means the component was used in the mapping. The cost of this mapping
		 * is given by {@link #cost} and both are computed by {@link #computeBestUsage(int)}.
		 * @see #computeBestUsage(int)
		 * @see #cost
		 */
		private BitSet best;
		/**
		 * The cost of the smallest complete mapping for this row. This value is computed
		 * by {@link #computeBestUsage(int)} and is {@link Integer#MAX_VALUE} before that.
		 * @see #computeBestUsage(int)
		 * @see #best
		 */
		private int cost = Integer.MAX_VALUE;
		
		/**
		 * Constructs a new row with space for the given number of mapping targets.
		 * @param size The number of mapping targets to allocate space for.
		 */
		private Row(int size){
			match = new QueryGraphComponent[size];
		}
		
		/**
		 * Computes the best usage for this row. This is the selection of options
		 * from the option sets in {@link #other} that together with {@link #match}
		 * results in the mapping with the smallest number of distinct targets. For
		 * this computation exactly one option from each option set has to be used.
		 * The result of this computation is stored in #best and its cost in #cost.
		 * @param size The size of the entire original query graph as the combined
		 *        count of vertices and edges.
		 */
		private void computeBestUsage(int size){
			BitSet base = new BitSet(size);
			for(QueryGraphComponent c : match){
				base.set(c.id);
			}
			
			computeBestUsage(0, base, size);
		}
		
		/**
		 * Computes the best usage for this row. This is the selection of options
		 * from the option sets in {@link #other} that together with {@link #match}
		 * results in the mapping with the smallest number of distinct targets. For
		 * this computation exactly one option from each option set has to be used.
		 * The result of this computation is stored in #best and its cost in #cost.
		 * @param offset The current option set to pick from.
		 * @param workSet The set of picked mappings so far encoded as a bit set
		 *        with bits set based on component IDs.
		 * @param size The size of the entire original query graph as the combined
		 *        count of vertices and edges.
		 */
		private void computeBestUsage(int offset, BitSet workSet, int size){
			if(offset >= other.size()){
				updateBest(workSet);
			}else{
				for(BitSet opt : other.get(offset).options){
					BitSet data = new BitSet(size);
					data.or(workSet);
					data.or(opt);
					computeBestUsage(offset + 1, data, size);
				}
			}
		}
		
		/**
		 * Updates best mapping candidate for this row with the given candidate if
		 * this candidate has a lower cost than the current best candidate. The cost
		 * is evaluated as the total number of distinct mapping targets.
		 * @param used A set of picked mappings that together form the complete candidate
		 *        encoded as a bit set with bits set based on component IDs.
		 */
		private void updateBest(BitSet used){
			int cost = used.cardinality();
			if(cost < this.cost){
				best = used;
				this.cost = cost;
			}
		}
		
		/**
		 * Gets the match component at the given index.
		 * @param idx The index to get.
		 * @return The match component at the given index.
		 */
		public QueryGraphComponent get(int idx){
			return match[idx];
		}
		
		/**
		 * Adds a new match component to this row.
		 * @param comp The component to append.
		 */
		public void add(QueryGraphComponent comp){
			match[write++] = comp;
		}
		
		@Override
		public String toString(){
			return Arrays.toString(match) + " | " + other;
		}
	}
}
