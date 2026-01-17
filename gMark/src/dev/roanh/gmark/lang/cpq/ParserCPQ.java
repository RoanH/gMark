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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
	
	
	
	
	public static <V> CPQ parse(UniqueGraph<V, Predicate> queryGraph, V sourceVertex, V targetVertex){
		UniqueGraph<Vertex<V>, Edge> graph = queryGraph.copy(Vertex::new, Edge::new);
		List<GraphNode<Vertex<V>, Edge>> cuts = Util.computeArticulationPoints(graph);
		Vertex<V> source = graph.getNodes().stream().filter(n->n.getData().data.equals(sourceVertex)).findAny().orElseThrow().getData();
		Vertex<V> target = graph.getNodes().stream().filter(n->n.getData().data.equals(targetVertex)).findAny().orElseThrow().getData();
		
		Set<Vertex<V>> splits = new HashSet<Vertex<V>>();
		cuts.forEach(v->splits.add(v.getData()));
		System.out.println("cuts: " + cuts);
		splits.add(source);
		splits.add(target);
//		splits.remove(source);//TODO probably optional?
//		splits.remove(target);
		List<GraphNode<Vertex<V>, Edge>> loops = graph.getNodes().stream().filter(GraphNode::hasSelfLoop).toList();
		System.out.println("loops: " + loops);
		loops.forEach(v->splits.add(v.getData()));
		
		List<UniqueGraph<Vertex<V>, Edge>> components = Util.splitOnNodes(graph, splits);
		for(UniqueGraph<Vertex<V>, Edge> component : components){
			System.out.println(component.getNodes().stream().map(GraphNode::getData).toList());
		}
		
		for(UniqueGraph<Vertex<V>, Edge> component : components){
			for(GraphNode<Vertex<V>, Edge> vertex : component.getNodes()){
				vertex.getData().components.add(new Component<V>(component));
			}
		}
		
		discoverEdges(source, target, graph, Edge::updateSourceDiscovery);
		discoverEdges(target, source, graph, Edge::updateTargetDiscovery);
		System.out.println("---");
		for(Vertex<V> split : splits){
			split.contractLoops();
		}
		
		CPQ paths = traversePaths(source, target, splits);
		if(source.equals(target)){
			//add the outermost intersection with ID we ignored
			paths = CPQ.intersect(paths, CPQ.id());
		}
		
		return paths;
	}
	
	private static <V> void discoverEdges(Vertex<V> source, Vertex<V> sink, UniqueGraph<Vertex<V>, Edge> graph, BiConsumer<Edge, Integer> discovery){
		HashSet<Vertex<V>> seen = new HashSet<Vertex<V>>();
		seen.add(source);
		seen.add(sink);
		
		Deque<GraphEdge<Vertex<V>, Edge>> edges = new ArrayDeque<GraphEdge<Vertex<V>, Edge>>();
		edges.addAll(graph.getNode(source).getOutEdges());
		edges.addAll(graph.getNode(source).getInEdges());
		
		int num = 0;
		while(!edges.isEmpty()){
			GraphEdge<Vertex<V>, Edge> edge = edges.removeFirst();
			discovery.accept(edge.getData(), num++);
			
			GraphNode<Vertex<V>, Edge> src = edge.getSourceNode();
			if(seen.add(src.getData())){
				edges.addAll(src.getInEdges());
				edges.addAll(src.getOutEdges());
			}
			
			GraphNode<Vertex<V>, Edge> trg = edge.getTargetNode();
			if(seen.add(trg.getData())){
				edges.addAll(trg.getInEdges());
				edges.addAll(trg.getOutEdges());
			}
		}
	}
	
	//(source loops) intersect (paths) intersect (target loops) intersect identity
	private static <V> CPQ traverseLoops(V sourceTarget, Map<V, List<UniqueGraph<V, Predicate>>> componentMap){
		List<UniqueGraph<V, Predicate>> subComponents = componentMap.get(sourceTarget);
		if(subComponents.size() <= 1){
			//wasn't an articulation point, or there was only one component
			subComponents = Util.splitOnNodes(subComponents.getFirst(), Set.of(sourceTarget));
		}
		
		CPQ cpq = CPQ.id();
		for(UniqueGraph<V, Predicate> component : subComponents){
			cpq = CPQ.intersect(cpq, )
		}
		
		
		
		
		return null;
	}
		
	//(source loops) concat (paths) concat (target loops)
	private static <V> CPQ traversePaths(Vertex<V> source, Vertex<V> target, Set<Vertex<V>> splits){
		for(Component<V> component : source.components){
			if(component.graph.containsNode(target)){
				
			}else{
				
			}
		}

		
		
		
		
		
		
		
		return null;
	}
	
//	private static CPQ addLoops(List<UniqueGraph<V, Predicate>>)
	
	
	
	
	
	
	
	
	private static class Vertex<V>{
		private final V data;
		private final List<Component<V>> components = new ArrayList<Component<V>>();
		private CPQ loops;
		
		private Vertex(V data){
			this.data = data;
		}
		
		public void contractLoops(){
			System.out.println(data + " is in components:");
			for(Component<V> component : components){
				System.out.println("- " + component.graph.getNodes().stream().map(GraphNode::getData).toList() + " with entry " + component.getInEdge().getSource() + "-" + component.getInEdge().getTarget() + " and exit " + component.getOutEdge().getSource() + "-" + component.getOutEdge().getTarget());
			}
			
			//TODO entry/exit is probably mostly relevant for vertices
		}

		@Override
		public boolean equals(Object obj){
			return obj instanceof Vertex<?> v && data.equals(v.data);
		}
		
		@Override
		public int hashCode(){
			return data.hashCode();
		}
		
		@Override
		public String toString(){
			return data.toString();
		}
	}
	
	private static class Component<V>{
		private final UniqueGraph<Vertex<V>, Edge> graph;
		
		private Component(UniqueGraph<Vertex<V>, Edge> graph){
			this.graph = graph;
		}
		
		public CPQ loopOn(Vertex<V> vertex){
			
		}
		
		public GraphEdge<Vertex<V>, Edge> getInEdge(){
			GraphEdge<Vertex<V>, Edge> min = null;
			for(GraphEdge<Vertex<V>, Edge> edge : graph.getEdges()){
				if(min == null || min.getData().srcDisc > edge.getData().srcDisc){
					min = edge;
				}
			}
			
			
			return min;
		}
		
		public GraphEdge<Vertex<V>, Edge> getOutEdge(){
			GraphEdge<Vertex<V>, Edge> min = null;
			for(GraphEdge<Vertex<V>, Edge> edge : graph.getEdges()){
				if(min == null || min.getData().trgDisc > edge.getData().trgDisc){
					min = edge;
				}
			}
			
			
			return min;
		}
	}
	
	private static class Edge{
		private final Predicate predicate;
		private int srcDisc = -1;
		private int trgDisc = -1;
		
		private Edge(Predicate predicate){
			this.predicate = predicate;
		}
		
		public void updateSourceDiscovery(int num){
			if(srcDisc == -1){
				srcDisc = num;
			}
		}
		
		public void updateTargetDiscovery(int num){
			if(trgDisc == -1){
				trgDisc = num;
			}
		}
		
		@Override
		public String toString(){
			// TODO Auto-generated method stub
			return super.toString();
		}
	}
}
