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

import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INTERSECTION;
import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INVERSE;
import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_JOIN;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.generic.GenericParser;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

/**
 * Parser for CPQs (Conjunctive Path Queries).
 * @author Roan
 * @see CPQ
 */
public final class ParserCPQ extends GenericParser{
	
	/**
	 * Prevent instantiation.
	 */
	private ParserCPQ(){
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query) throws IllegalArgumentException{
		return parse(query, CHAR_JOIN, CHAR_INTERSECTION, CHAR_INVERSE);
	}

	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @param labels The label set to use, new labels will <b>not</b> be created if labels are found in the
	 *        input that are not covered by the given list.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ or contains unknown labels.
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return parse(query, labels, CHAR_JOIN, CHAR_INTERSECTION, CHAR_INVERSE);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 */
	public static CPQ parse(String query, char join, char intersect, char inverse) throws IllegalArgumentException{
		return parse(query, new HashMap<String, Predicate>(), join, intersect, inverse);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param labels The label set to use, new labels will <b>not</b> be created if labels are found in the
	 *        input that are not covered by the given list.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ or contains unknown labels.
	 */
	public static CPQ parse(String query, List<Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		return parse(query, mapPredicates(labels), join, intersect, inverse);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param labels A map with predicates found so far.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 */
	private static CPQ parse(String query, Map<String, Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		List<String> parts = split(query, join);
		if(parts.size() > 1){
			return CPQ.concat(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
		}
		
		parts = split(query, intersect);
		if(parts.size() > 1){
			return CPQ.intersect(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
		}
		
		if(query.equals("id")){
			return CPQ.IDENTITY;
		}
		
		if(query.startsWith("(") && query.endsWith(")")){
			return parse(query.substring(1, query.length() - 1), labels, join, intersect, inverse);
		}
		
		if(query.indexOf('(') == -1 && query.indexOf(')') == -1 && query.indexOf(join) == -1 && query.indexOf(intersect) == -1){
			return CPQ.label(parsePredicate(query, labels, inverse));
		}

		throw new IllegalArgumentException("Invalid CPQ.");
	}
	
	/**
	 * Parses the given graph with the indicated source and target vertices into a CPQ instance.
	 * <p>
	 * The constructed CPQ instance is guaranteed to have a query graph that is query homomorphically
	 * equivalent to the input query graph and also has the same number of vertices and edges.
	 * @param <V> The vertex data type.
	 * @param queryGraph The CPQ query graph to parse.
	 * @param sourceVertex The source vertex of the CPQ query graph.
	 * @param targetVertex The target vertex of the CPQ query graph.
	 * @return The parsed CPQ instance.
	 * @throws IllegalArgumentException When the given source and target vertices do not exist in the
	 *         given query graph, or when the given query graph does not represent a valid CPQ instance.
	 * @see QueryGraphCPQ
	 */
	public static <V> CPQ parse(UniqueGraph<V, Predicate> queryGraph, V sourceVertex, V targetVertex) throws IllegalArgumentException{
		if(!queryGraph.containsNode(sourceVertex) || !queryGraph.containsNode(targetVertex)){
			throw new IllegalArgumentException("The given source and target vertex do not belong to the query graph.");
		}
		
		ReductionGraph graph = new ReductionGraph(queryGraph, sourceVertex, targetVertex);
		boolean changed;
		do{
			changed = false;
			
			//reduce degree 1 vertices
			for(GraphNode<VertexData, EdgeData> v : graph.getNodes()){
				if(!graph.isTerminal(v) && v.getDegree() == 1){
					graph.contractVertexDegree1(v);
					changed = true;
				}
			}
			
			//handle fully reduced loops by moving them to vertex metadata
			for(GraphEdge<VertexData, EdgeData> edge : graph.getEdges()){
				if(edge.getSourceNode().equals(edge.getTargetNode())){
					edge.getSource().addLoop(edge.getData());
					edge.remove();
					changed = true;
				}
			}
			
			//collapse (intersect) edges between the same source and target vertex
			for(GraphNode<VertexData, EdgeData> v : graph.getNodes()){
				if(v.getOutEdges().size() >= 2){
					Iterator<GraphEdge<VertexData, EdgeData>> edges = v.getOutEdges().stream().sorted(Comparator.comparingInt(e->e.getTargetNode().getID())).iterator();

					GraphEdge<VertexData, EdgeData> last = edges.next();
					while(edges.hasNext()){
						GraphEdge<VertexData, EdgeData> next = edges.next();
						if(next.getTargetNode().getID() == last.getTargetNode().getID()){
							last.getData().addParallel(next.getData().getPath());
							next.remove();
							changed = true;
						}else{
							last = next;
						}
					}
				}
			}
			
			//reduce degree 2 vertices that aren't articulation points
			for(GraphNode<VertexData, EdgeData> v : graph.getNodes()){
				if(!graph.isTerminal(v) && v.getDegree() == 2 && !graph.isArticulationPoint(v)){
					graph.contractVertexDegree2(v);
					changed = true;
				}
			}
			
			//reduce degree 2 vertices that are articulation points only if there are no alternative actions
			if(!changed){
				for(GraphNode<VertexData, EdgeData> v : graph.getNodes()){
					if(!graph.isTerminal(v) && v.getDegree() == 2 && graph.isArticulationPoint(v)){
						graph.contractVertexDegree2(v);
						changed = true;
						break;
					}
				}
			}
		}while(changed);
		
		if(sourceVertex.equals(targetVertex) && graph.graph.getNodeCount() == 1 && graph.graph.getEdgeCount() == 0){
			return graph.graph.getNodes().getFirst().getData().loops;
		}else if(!sourceVertex.equals(targetVertex) && graph.graph.getNodeCount() == 2 && graph.graph.getEdgeCount() == 1){
			CPQ path = graph.graph.getEdges().getFirst().getData().getPath();
			if(graph.source.getInCount() == 1){
				//flip the edges as we walked it from target to source
				path = path.inverse();
			}
			
			//src loops -> path -> trg loops
			return concat(
				graph.source.getData().loops,
				path,
				graph.target.getData().loops
			);
		}
		
		throw new IllegalArgumentException("The given input graph does not represent a valid CPQ.");
	}
	
	/**
	 * Concatenates the given CPQs ignoring CPQs that are null.
	 * @param first The first CPQ.
	 * @param second The second CPQ.
	 * @param third The third CPQ.
	 * @return The concatenation in order of the given non-null CPQs.
	 */
	private static CPQ concat(CPQ first, CPQ second, CPQ third){
		CPQ q = first;
		
		if(second != null){
			q = q == null ? second : CPQ.concat(q, second);
		}
		
		if(third != null){
			q = q == null ? third : CPQ.concat(q, third);
		}
		
		return q;
	}
	
	/**
	 * CPQ Query Graph that is being reduced for parsing.
	 * @author Roan
	 * @see VertexData
	 * @see EdgeData
	 */
	private static class ReductionGraph{
		/**
		 * The actual graph data.
		 */
		private final UniqueGraph<VertexData, EdgeData> graph = new UniqueGraph<VertexData, EdgeData>();
		/**
		 * The source vertex of the CPQ query graph.
		 */
		private final GraphNode<VertexData, EdgeData> source;
		/**
		 * The target vertex of the CPQ query graph.
		 */
		private final GraphNode<VertexData, EdgeData> target;
		/**
		 * The articulation points in the graph. That is, the cut vertices
		 * of the graph, removing one of these vertices causes the graph becomes disconnected.
		 */
		private final Set<GraphNode<VertexData, EdgeData>> articulation;
		
		/**
		 * Constructed a new reduction graph for the given query graph.
		 * @param <V> The graph vertex data type.
		 * @param queryGraph The query graph.
		 * @param source The source vertex of the query graph.
		 * @param target The target vertex of the query graph.
		 */
		private <V> ReductionGraph(UniqueGraph<V, Predicate> queryGraph, V source, V target){
			Map<V, VertexData> transform = new HashMap<V, VertexData>();
			
			for(GraphNode<V, Predicate> node : queryGraph.getNodes()){
				V v = node.getData();
				VertexData data = new VertexData();
				graph.addUniqueNode(data);
				transform.put(v, data);
			}
			
			this.source = graph.getNode(transform.get(source));
			this.target = graph.getNode(transform.get(target));
			
			for(GraphEdge<V, Predicate> edge : queryGraph.getEdges()){
				addEdge(
					graph.getNode(transform.get(edge.getSource())),
					graph.getNode(transform.get(edge.getTarget())),
					CPQ.label(edge.getData())
				);
			}
			
			articulation = Set.copyOf(Util.computeArticulationPoints(graph));
		}
		
		/**
		 * Checks if the given vertex is the source or target vertex of this graph.
		 * @param vertex The vertex to check.
		 * @return True if the given vertex is a terminal node.
		 */
		private boolean isTerminal(GraphNode<VertexData, EdgeData> vertex){
			return vertex.equals(source) || vertex.equals(target);
		}
		
		/**
		 * Checks if the given vertex is an articulation vertex of the graph.
		 * @param vertex The vertex to check.
		 * @return True if the given vertex is an articulation point.
		 */
		private boolean isArticulationPoint(GraphNode<VertexData, EdgeData> vertex){
			return articulation.contains(vertex);
		}
		
		/**
		 * Reduces the given degree 1 vertex by moving all paths through it to a loop
		 * on the only vertex it has an edge to.
		 * @param vertex The vertex to reduce.
		 */
		private void contractVertexDegree1(GraphNode<VertexData, EdgeData> vertex){
			if(vertex.getInCount() == 1){
				//base --path-> v loops --path inv-> base
				GraphEdge<VertexData, EdgeData> edge = vertex.getInEdges().iterator().next();
				EdgeData data = edge.getData();
				if(data.paths.size() == 1){
					edge.getSource().addLoop(concat(
						data.getPath(),
						vertex.getData().loops,
						data.getPath().inverse()
					));
				}else{
					edge.getSource().addLoop(concat(
						data.getBundledPath(),
						vertex.getData().loops,
						data.getSinglePath().inverse()
					));
				}
			}else{
				//base --path inv-> v loops --path-> base
				GraphEdge<VertexData, EdgeData> edge = vertex.getOutEdges().iterator().next();
				EdgeData data = edge.getData();
				if(data.paths.size() == 1){
					edge.getTarget().addLoop(concat(
						data.getPath().inverse(),
						vertex.getData().loops,
						data.getPath()
					));
				}else{
					edge.getTarget().addLoop(concat(
						data.getSinglePath().inverse(),
						vertex.getData().loops,
						data.getBundledPath()
					));
				}
			}

			vertex.remove();
		}
		
		/**
		 * Reduces the given degree 2 vertex by moving the paths through it to a new direct edge
		 * between the two vertices it is connected to.
		 * @param vertex The vertex to reduce.
		 */
		private void contractVertexDegree2(GraphNode<VertexData, EdgeData> vertex){
			if(vertex.getInCount() == 2){
				//from -> v loops -> to inverse
				Iterator<GraphEdge<VertexData, EdgeData>> iter = vertex.getInEdges().iterator();
				GraphEdge<VertexData, EdgeData> from = iter.next();
				GraphEdge<VertexData, EdgeData> to = iter.next();
				addEdge(
					from.getSourceNode(),
					to.getSourceNode(),
					concat(
						from.getData().getPath(),
						vertex.getData().loops,
						to.getData().getPath().inverse()
					)
				);
			}else if(vertex.getOutCount() == 2){
				//from inverse -> v loops -> to
				Iterator<GraphEdge<VertexData, EdgeData>> iter = vertex.getOutEdges().iterator();
				GraphEdge<VertexData, EdgeData> from = iter.next();
				GraphEdge<VertexData, EdgeData> to = iter.next();
				addEdge(
					from.getTargetNode(),
					to.getTargetNode(),
					concat(
						from.getData().getPath().inverse(),
						vertex.getData().loops,
						to.getData().getPath()
					)
				);
			}else{
				//from -> v loops -> to
				GraphEdge<VertexData, EdgeData> from = vertex.getInEdges().iterator().next();
				GraphEdge<VertexData, EdgeData> to = vertex.getOutEdges().iterator().next();
				addEdge(
					from.getSourceNode(),
					to.getTargetNode(),
					concat(
						from.getData().getPath(),
						vertex.getData().loops,
						to.getData().getPath()
					)
				);
			}
			
			vertex.remove();
		}
		
		/**
		 * Gets a list of all the vertices remaining in the graph.
		 * @return A list of all graph nodes.
		 */
		private List<GraphNode<VertexData, EdgeData>> getNodes(){
			//this has to be co-mod safe
			return List.copyOf(graph.getNodes());
		}
		
		/**
		 * Gets a list of all the edges remaining in the graph.
		 * @return A list of all graph edges.
		 */
		private List<GraphEdge<VertexData, EdgeData>> getEdges(){
			//this has to be co-mod safe
			return List.copyOf(graph.getEdges());
		}
		
		/**
		 * Adds a new edge to this reduction graph, edges are created such that
		 * the path is always oriented from low ID to high ID to facilitate easier searches.
		 * @param from The source node for the path (low ID node).
		 * @param to The target node for the path (high ID node).
		 * @param path The path between the from and to nodes, will be inverted and the from
		 *        and to nodes swapped if the input does not follow the ID requirements.
		 */
		private void addEdge(GraphNode<VertexData, EdgeData> from, GraphNode<VertexData, EdgeData> to, CPQ path){
			if(from.getID() <= to.getID()){
				from.addUniqueEdgeTo(to, new EdgeData(path));
			}else{
				to.addUniqueEdgeTo(from, new EdgeData(path.inverse()));
			}
		}
	}
	
	/**
	 * Vertex metadata instance tracking accumulated self loops.
	 * @author Roan
	 */
	private static class VertexData{
		/**
		 * The looping paths present on this vertex
		 */
		private CPQ loops;
		
		private VertexData(){
		}
		
		private void addLoop(EdgeData edge){
			addLoop(edge.getPath());
		}
		
		private void addLoop(CPQ path){
			if(loops == null){
				loops = CPQ.id();
			}
			
			loops = CPQ.intersect(loops, path);
		}
		
		//TODO combine loop steps
//		private CPQ getLoops(){
//
//		}
	}
	
	private static class EdgeData{
		private final List<CPQ> paths;
		
		private EdgeData(CPQ path){
			paths = new ArrayList<CPQ>();
			paths.add(path);
		}
		
		private void addParallel(CPQ parallel){
			paths.add(parallel);
		}
		
		private CPQ getPath(){
			return paths.size() == 1 ? paths.getFirst() : CPQ.intersect(paths);
		}
		
		private CPQ getSinglePath(){
			return paths.getFirst();
		}
		
		private CPQ getBundledPath(){
			return paths.size() == 2 ? paths.getLast() : CPQ.intersect(paths.subList(1, paths.size()));
		}
	}
}
