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

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.generic.GenericParser;
import dev.roanh.gmark.type.schema.Predicate;
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
	
	public static <V> CPQ parse(UniqueGraph<V, Predicate> queryGraph, V sourceVertex, V targetVertex){
		if(!queryGraph.containsNode(sourceVertex) || !queryGraph.containsNode(targetVertex)){
			throw new IllegalArgumentException("The given source and target vertex do not belong to the query graph.");
		}
		
		ReductionGraph<V> graph = new ReductionGraph<V>(queryGraph, sourceVertex, targetVertex);
		boolean changed;
		do{
			changed = false;
			
			for(GraphNode<VertexData<V>, EdgeData> v : graph.getNodes()){
				if(v.getData().isTerminal() || v.getDegree() != 1){
					continue;
				}

				println("v is " + v.getData().vertex);

				//reduce degree 1 vertices
				if(v.getInCount() == 1){
					//base --path-> v loops --path inv-> base
					GraphEdge<VertexData<V>, EdgeData> edge = v.getInEdges().iterator().next();
					if(edge.getData().paths.size() == 1){
						edge.getSourceNode().getData().addLoop(concat(
							edge.getData().getPath(),
							v.getData().loops,
							edge.getData().getPath().inverse()
						));
					}else{
						edge.getSourceNode().getData().addLoop(concat(
							edge.getData().getBundledPath(),
							v.getData().loops,
							edge.getData().getSinglePath().inverse()
						));
					}
				}else{
					//base --path inv-> v loops --path-> base
					GraphEdge<VertexData<V>, EdgeData> edge = v.getOutEdges().iterator().next();
					if(edge.getData().paths.size() == 1){
						edge.getTargetNode().getData().addLoop(concat(
							edge.getData().getPath().inverse(),
							v.getData().loops,
							edge.getData().getPath()
						));
					}else{
						edge.getTargetNode().getData().addLoop(concat(
							edge.getData().getSinglePath().inverse(),
							v.getData().loops,
							edge.getData().getBundledPath()
						));
					}
				}

				println("reduce degree 1");
				v.remove();
				changed = true;
			}
			
			//handle fully reduced loops
			for(GraphEdge<VertexData<V>, EdgeData> edge : graph.getEdges()){
				if(edge.getSourceNode().equals(edge.getTargetNode())){
					edge.getSource().addLoop(edge.getData());
					edge.remove();
					changed = true;
					println("reduce loop edge");
				}
			}
			
			for(GraphNode<VertexData<V>, EdgeData> v : graph.getNodes()){
				if(v.getData().isTerminal() || v.getDegree() != 2){
					continue;
				}

				println("v is " + v.getData().vertex);

				//reduce degree 2 vertices
				if(v.getInCount() == 2){
					//from -> v loops -> to inverse
					Iterator<GraphEdge<VertexData<V>, EdgeData>> iter = v.getInEdges().iterator();
					GraphEdge<VertexData<V>, EdgeData> from = iter.next();
					GraphEdge<VertexData<V>, EdgeData> to = iter.next();
					println("reduce by concat in 2");
					graph.addEdge(
						from.getSourceNode(),
						to.getSourceNode(),
						concat(
							from.getData().getPath(),
							v.getData().loops,
							to.getData().getPath().inverse()
							)
						);
				}else if(v.getOutCount() == 2){
					//from inverse -> v loops -> to
					Iterator<GraphEdge<VertexData<V>, EdgeData>> iter = v.getOutEdges().iterator();
					GraphEdge<VertexData<V>, EdgeData> from = iter.next();
					GraphEdge<VertexData<V>, EdgeData> to = iter.next();
					println("reduce by concat out 2");
					graph.addEdge(
						from.getTargetNode(),
						to.getTargetNode(),
						concat(
							from.getData().getPath().inverse(),
							v.getData().loops,
							to.getData().getPath()
							)
						);
				}else{
					//from -> v loops -> to
					GraphEdge<VertexData<V>, EdgeData> from = v.getInEdges().iterator().next();
					GraphEdge<VertexData<V>, EdgeData> to = v.getOutEdges().iterator().next();
					println("reduce by concat in-out / " + v.getData().vertex.toString() + " / " + from.getData().getPath() + " | " + to.getData().getPath());
					graph.addEdge(
						from.getSourceNode(),
						to.getTargetNode(),
						concat(
							from.getData().getPath(),
							v.getData().loops,
							to.getData().getPath()
							)
						);
				}

				v.remove();
				changed = true;
				break;
			}
			
			//collapse (intersect) edges between the same source and target vertex
			collapse: for(GraphNode<VertexData<V>, EdgeData> v : graph.getNodes()){
				if(v.getOutEdges().size() >= 2){
					Iterator<GraphEdge<VertexData<V>, EdgeData>> edges = v.getOutEdges().stream().sorted(Comparator.comparingInt(e->e.getTargetNode().getID())).iterator();

					GraphEdge<VertexData<V>, EdgeData> last = edges.next();
					while(edges.hasNext()){
						GraphEdge<VertexData<V>, EdgeData> next = edges.next();
						if(next.getTargetNode().getID() == last.getTargetNode().getID()){
							last.getData().addParallel(next.getData().getPath());
							next.remove();
							changed = true;
							println("collapse parallel (full): " + last.getData().getPath() + " | " + next.getData().getPath());
							break collapse;
						}else{
							last = next;
						}
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
	
	private static void println(String arg){
//		System.out.println(arg);
	}
	
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
	
	private static class ReductionGraph<V>{
		private final UniqueGraph<VertexData<V>, EdgeData> graph = new UniqueGraph<ParserCPQ.VertexData<V>, ParserCPQ.EdgeData>();
		private final GraphNode<VertexData<V>, EdgeData> source;
		private final GraphNode<VertexData<V>, EdgeData> target;
		
		private ReductionGraph(UniqueGraph<V, Predicate> queryGraph, V source, V target){
			Map<V, VertexData<V>> transform = new HashMap<V, VertexData<V>>();
			
			for(GraphNode<V, Predicate> node : queryGraph.getNodes()){
				V v = node.getData();
				VertexData<V> data = new VertexData<V>(v, v.equals(source) || v.equals(target));
				graph.addUniqueNode(data);
				transform.put(data.vertex, data);
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
		}
		
		private List<GraphNode<VertexData<V>, EdgeData>> getNodes(){
			//this has to be co-mod safe
			return List.copyOf(graph.getNodes());
		}
		
		private List<GraphEdge<VertexData<V>, EdgeData>> getEdges(){
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
		private void addEdge(GraphNode<VertexData<V>, EdgeData> from, GraphNode<VertexData<V>, EdgeData> to, CPQ path){
			if(from.getID() <= to.getID()){
				from.addUniqueEdgeTo(to, new EdgeData(path));
			}else{
				to.addUniqueEdgeTo(from, new EdgeData(path.inverse()));
			}
		}
	}
	
	private static class VertexData<V>{
		private final V vertex;
		private final boolean terminal;
		private CPQ loops;
		
		private VertexData(V vertex, boolean terminal){
			this.vertex = vertex;
			this.terminal = terminal;
		}
		
		private boolean isTerminal(){
			return terminal;
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
