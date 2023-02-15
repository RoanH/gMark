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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.GraphPanel;
import dev.roanh.gmark.util.SimpleGraph;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.Tree;
import dev.roanh.gmark.util.UniqueGraph;
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
		SimpleGraph<QueryGraphComponent> g = new SimpleGraph<QueryGraphComponent>();
		vertices.forEach(g::addVertex);
		
		for(Edge edge : edges){
			SimpleVertex<QueryGraphComponent> v = g.addVertex(edge);
			g.addEdge(v, edge.src);
			g.addEdge(v, edge.trg);
		}

		return g;
	}
	
	public static void main(String[] args){
		Predicate l1 = new Predicate(1, "1");
		Predicate l2 = new Predicate(2, "2");
		Predicate l3 = new Predicate(3, "3");
		Predicate l4 = new Predicate(4, "4");
		
		//QueryGraphCPQ cpq = CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.labels(l2, l4), CPQ.labels(l3, l2))).toQueryGraph();
		QueryGraphCPQ cpq = CPQ.labels(l1, l2, l4).toQueryGraph();
		
		UniqueGraph<Vertex, Predicate> self = cpq.toUniqueGraph();
		
		JFrame f = new JFrame();
		
		f.add(new GraphPanel<Vertex, Predicate>(self, Object::toString, Predicate::getAlias));
		f.setSize(300, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		f = new JFrame();
		GraphPanel<QueryGraphComponent, Void> gp = new GraphPanel<QueryGraphComponent, Void>(cpq.toIncidenceGraph().toUniqueGraph());
		gp.setDirected(false);
		f.add(gp);
		f.setSize(300, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		
		cpq.isHomomorphicTo(self);//totally should be true
		
	}
	
	public boolean isHomomorphicTo(UniqueGraph<Vertex, Predicate> other){
		//compute a query decomposition
		Tree<List<QueryGraphComponent>> decomp = Util.computeTreeDecompositionWidth2(toIncidenceGraph());
		
		decomp.forEach(t->System.out.println(t.getDepth() + ": " + t.getData()));
		
		//expand each list with dependent variables
		decomp.forEach(t->{
			List<QueryGraphComponent> data = t.getData();
			final int size = data.size();
			for(int i = 0; i < size; i++){
				if(data.get(i).isEdge()){
					Edge edge = (Edge)data.get(i);
					
					if(!data.contains(edge.src)){
						data.add(edge.src);
					}
					
					if(!data.contains(edge.trg)){
						data.add(edge.trg);
					}
				}
			}
		});
		
		System.out.println("================");
		decomp.forEach(t->System.out.println(t.getDepth() + ": " + t.getData()));

		
		
		
		return false;//TODO
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
}
