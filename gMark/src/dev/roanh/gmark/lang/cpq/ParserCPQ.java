/*roma
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

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.generic.GenericParser;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.GraphPanel;
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
				if(v.getData().vertex == sourceVertex || v.getData().vertex == targetVertex){
					continue;
				}
				
				if(v.getDegree() == 2){
					//reduce degree 2 vertices
					if(v.getInCount() == 2){
						Iterator<GraphEdge<VertexData<V>, EdgeData>> iter = v.getInEdges().iterator();
						GraphEdge<VertexData<V>, EdgeData> from = iter.next();
						GraphEdge<VertexData<V>, EdgeData> to = iter.next();
						graph.addParallel(
							from.getSourceNode(),
							to.getSourceNode(),
							v.getData().concatAfter(from.getData().path).concat(to.getData().path.inverse())//from -> v loops -> to inverse
						);
					}else if(v.getOutCount() == 2){
						Iterator<GraphEdge<VertexData<V>, EdgeData>> iter = v.getOutEdges().iterator();
						GraphEdge<VertexData<V>, EdgeData> from = iter.next();
						GraphEdge<VertexData<V>, EdgeData> to = iter.next();
						graph.addParallel(
							from.getTargetNode(),
							to.getTargetNode(),
							v.getData().concatAfter(from.getData().path.inverse()).concat(to.getData().path)//from inverse -> v loops -> to
						);
					}else{
						GraphEdge<VertexData<V>, EdgeData> from = v.getInEdges().iterator().next();
						GraphEdge<VertexData<V>, EdgeData> to = v.getOutEdges().iterator().next();
						graph.addParallel(
							from.getSourceNode(),
							to.getTargetNode(),
							v.getData().concatAfter(from.getData().path).concat(to.getData().path)//from -> v loops -> to
						);
					}
					
					v.remove();
					changed = true;
				}else if(v.getDegree() == 1){
					//reduce degree 1 vertices
					GraphEdge<VertexData<V>, EdgeData> edge;
					GraphNode<VertexData<V>, EdgeData> base;
					if(v.getInCount() == 1){
						edge = v.getInEdges().iterator().next();
						base = edge.getSourceNode();
					}else{
						edge = v.getOutEdges().iterator().next();
						base = edge.getTargetNode();
					}
					
					//base --path-> v loops --path inv-> base
					base.getData().addLoop(v.getData().concatAfter(edge.getData().path).concat(edge.getData().path.inverse()));
					v.remove();
					changed = true;
				}
			}
			
			//handle fully reduced loops
			for(GraphEdge<VertexData<V>, EdgeData> edge : graph.getEdges()){
				if(edge.getSourceNode().equals(edge.getTargetNode())){
					edge.getSource().addLoop(edge.getData());
					edge.remove();
					changed = true;
				}
			}
			
			
//			GraphPanel.show(graph.graph.copy());
		}while(changed);
		
//		try{
//			Thread.sleep(Duration.ofMinutes(38279));
//		}catch(InterruptedException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		if(sourceVertex.equals(targetVertex) && graph.graph.getNodeCount() == 1 && graph.graph.getEdgeCount() == 0){
			return graph.graph.getNodes().getFirst().getData().loops;
		}else if(!sourceVertex.equals(targetVertex) && graph.graph.getNodeCount() == 2 && graph.graph.getEdgeCount() == 1){
			CPQ path = graph.graph.getEdges().getFirst().getData().path;
			if(graph.source.getInCount() == 1){
				//flip the edges as we walked it from target to source
				path = path.inverse();
			}
			
			return graph.target.getData().concatAfter(graph.source.getData().concatBefore(path));//src loops -> path -> trg loops
		}
		
		throw new IllegalArgumentException("The given input graph does not represent a valid CPQ.");
	}
	
	private static class ReductionGraph<V>{
		private final UniqueGraph<VertexData<V>, EdgeData> graph = new UniqueGraph<ParserCPQ.VertexData<V>, ParserCPQ.EdgeData>();
		private final GraphNode<VertexData<V>, EdgeData> source;
		private final GraphNode<VertexData<V>, EdgeData> target;
		
		private ReductionGraph(UniqueGraph<V, Predicate> queryGraph, V source, V target){
			Map<V, VertexData<V>> transform = new HashMap<V, VertexData<V>>();
			
			for(GraphNode<V, Predicate> node : queryGraph.getNodes()){
				VertexData<V> data = new VertexData<V>(node.getData());
				graph.addUniqueNode(data);
				transform.put(data.vertex, data);
			}
			
			this.source = graph.getNode(transform.get(source));
			this.target = graph.getNode(transform.get(target));
			
			//collapse parallel edges
			for(GraphEdge<V, Predicate> edge : queryGraph.getEdges()){
				addParallel(
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
		
		private void addParallel(GraphNode<VertexData<V>, EdgeData> from, GraphNode<VertexData<V>, EdgeData> to, CPQ path){
			for(GraphEdge<VertexData<V>, EdgeData> edge : from.getOutEdges()){
				if(edge.getTargetNode().equals(to)){
					edge.getData().addParallel(path);
					return;
				}
			}
			
			for(GraphEdge<VertexData<V>, EdgeData> edge : from.getInEdges()){
				if(edge.getSourceNode().equals(to)){
					edge.getData().addParallel(path.inverse());
					return;
				}
			}
			
			from.addUniqueEdgeTo(to, new EdgeData(path));
		}
	}
	
	private static class VertexData<V>{
		private final V vertex;
		private CPQ loops;
		
		private VertexData(V vertex){
			this.vertex = vertex;
		}
		
		private void addLoop(EdgeData edge){
			addLoop(edge.path);
		}
		
		private void addLoop(CPQ path){
			if(loops == null){
				loops = CPQ.id();
			}
			
			loops = loops.intersect(path);
		}
		
		private CPQ concatAfter(CPQ first){
			return loops == null ? first : first.concat(loops);
		}
		
		private CPQ concatBefore(CPQ second){
			return loops == null ? second : loops.concat(second);
		}
	}
	
	private static class EdgeData{
		private CPQ path;
		
		private EdgeData(CPQ path){
			this.path = path;
		}
		
		private void addParallel(CPQ parallel){
			path = path.intersect(parallel);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	public static <V> CPQ parseOld(UniqueGraph<V, Predicate> queryGraph, V sourceVertex, V targetVertex){
		UniqueGraph<Vertex<V>, Edge> graph = queryGraph.copy(Vertex::new, Edge::new);
		graph.getNodes().forEach(n->n.getData().node = n);
		
		List<GraphNode<Vertex<V>, Edge>> cuts = Util.computeArticulationPoints(graph);
		Vertex<V> source = graph.getNodes().stream().filter(n->n.getData().data.equals(sourceVertex)).findAny().orElseThrow().getData();
		Vertex<V> target = graph.getNodes().stream().filter(n->n.getData().data.equals(targetVertex)).findAny().orElseThrow().getData();
		
		Set<Vertex<V>> splits = new HashSet<Vertex<V>>();
		cuts.forEach(v->splits.add(v.getData()));
		System.out.println("cuts: " + cuts);
//		splits.add(source);
//		splits.add(target);
//		splits.remove(source);//TODO probably optional?
//		splits.remove(target);
		List<GraphNode<Vertex<V>, Edge>> loops = graph.getNodes().stream().filter(GraphNode::hasSelfLoop).toList();
		System.out.println("loops: " + loops);
//		loops.forEach(v->splits.add(v.getData()));
		
		List<UniqueGraph<Vertex<V>, Edge>> components = Util.splitOnNodes(graph, splits);
		for(UniqueGraph<Vertex<V>, Edge> component : components){
			System.out.println(component.getNodes().stream().map(GraphNode::getData).toList());
		}
		
		for(UniqueGraph<Vertex<V>, Edge> component : components){
			for(GraphNode<Vertex<V>, Edge> vertex : component.getNodes()){
				vertex.getData().components.add(new Component<V>(component));
			}
		}
		
		discoverEdges(source, target, graph, (v, e)->v.canReachSrc.add(e), (v, e)->v.canReachSrc.contains(e));
		discoverEdges(target, source, graph, (v, e)->v.canReachTrg.add(e), (v, e)->v.canReachTrg.contains(e));
		System.out.println("---");
		for(GraphNode<Vertex<V>, Edge> v : graph.getNodes()){
			System.out.println(
				v.getData() +
				" has entries " + v.getData().canReachSrc.stream().map(ParserCPQ::edgeToString).toList() +
				" and exits " + v.getData().canReachTrg.stream().map(ParserCPQ::edgeToString).toList());
		}
		
		System.out.println("---");
		for(Vertex<V> split : splits){
			split.contractLoops();
		}
		
		CPQ paths = traversePaths(source, target);
		System.out.println("main CPQ: " + paths);
		GraphPanel.show(paths);
		try{
			Thread.sleep(Duration.ofHours(1));
		}catch(InterruptedException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(source.equals(target)){//TODO have a loop variant anyway maybe? -- should current path version does not handle loops at all
			//add the outermost intersection with ID we ignored
			paths = CPQ.intersect(paths, CPQ.id());
		}
		
		return paths;
	}
	
	private static <V> String edgeToString(GraphEdge<Vertex<V>, Edge> edge){
		return edge.getSource() + "-" + edge.getTarget() + " (" + edge.getData().predicate.getAlias() + ")";
	}
	
	private static <V> void discoverEdges(Vertex<V> source, Vertex<V> sink, UniqueGraph<Vertex<V>, Edge> graph, BiConsumer<Vertex<V>, GraphEdge<Vertex<V>, Edge>> discovery, BiPredicate<Vertex<V>, GraphEdge<Vertex<V>, Edge>> discovered){
		HashSet<GraphNode<Vertex<V>, Edge>> seen = new HashSet<GraphNode<Vertex<V>, Edge>>();
		seen.add(graph.getNode(source));
		seen.add(graph.getNode(sink));
		
		Deque<GraphNode<Vertex<V>, Edge>> nodes = new ArrayDeque<GraphNode<Vertex<V>, Edge>>();
		nodes.add(graph.getNode(source));

		while(!nodes.isEmpty()){
			GraphNode<Vertex<V>, Edge> node = nodes.removeFirst();
			
			for(GraphEdge<Vertex<V>, Edge> edge : node.getOutEdges()){
				GraphNode<Vertex<V>, Edge> trg = edge.getTargetNode();
				if(!discovered.test(node.getData(), edge) && !trg.equals(node)){
					discovery.accept(trg.getData(), edge);
					if(seen.add(trg)){
						nodes.add(trg);
					}
				}
			}
			
			for(GraphEdge<Vertex<V>, Edge> edge : node.getInEdges()){
				GraphNode<Vertex<V>, Edge> src = edge.getSourceNode();
				if(!discovered.test(node.getData(), edge) && !src.equals(node)){
					discovery.accept(src.getData(), edge);
					if(seen.add(src)){
						nodes.add(src);
					}
				}
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
//			cpq = CPQ.intersect(cpq, )
		}
		
		
		
		
		return null;
	}
	
	
		
	//TODO probably ignore src/trg loops here they'll be handled by the caller so not all paralel
	//(source loops) concat (paths) concat (target loops)
	private static <V> CPQ traversePaths(Vertex<V> source, Vertex<V> target){
		CPQ cpq = null;
		
		while(!source.equals(target)){
			assert !source.canReachTrg.isEmpty();
			if(source.canReachTrg.size() == 1){
				GraphEdge<Vertex<V>, Edge> edge = source.canReachTrg.iterator().next();
				cpq = cpq == null ? edge.getData().toCPQ() : CPQ.concat(cpq, edge.getData().toCPQ());
				source = edge.getTarget();
			}else{
				MergePath<V> path = traverseToMerge(source);
				cpq = cpq == null ? path.paths() : CPQ.concat(cpq, path.paths());
				source = path.mergeNode();
			}
		}
		
		return cpq;
	}
	
	
	//basically need to cover the entire graph with CPQ semantics and if we manage a full cover we reverse
	//if we cannot cover its not a CPQ. So basically just apply my original flood mechanism of extending to
	//all nodes and only advancing when a single exit remains, or if no exists remaing then reverse the complete
	//graph from that node
	//actually no, the last path at the final node returns via the intersection of the others
	//this should be SPII recognition
	private static <V> CPQ floodGraph(UniqueGraph<V, Predicate> subgraph, Vertex<V> source){
		CPQ cpq = null;
		
//		while(!source.equals(target)){
//			assert !source.canReachTrg.isEmpty();
//			if(source.canReachTrg.size() == 1){
//				GraphEdge<Vertex<V>, Edge> edge = source.canReachTrg.iterator().next();
//				cpq = cpq == null ? edge.getData().toCPQ() : CPQ.concat(cpq, edge.getData().toCPQ());
//				source = edge.getTarget();
//			}else{
//				MergePath<V> path = traverseToMerge(source);
//				cpq = cpq == null ? path.paths() : CPQ.concat(cpq, path.paths());
//				source = path.mergeNode();
//			}
//		}
//
		return cpq;
	}
	
	private static <V> MergePath<V> traverseToMerge(Vertex<V> source){
		System.out.println("reversing iterators: " + source.canReachTrg.size() + " from " + source);
		assert !source.canReachTrg.isEmpty();
		List<PathIterator<V>> iterators = new ArrayList<PathIterator<V>>(source.canReachTrg.size());
		source.canReachTrg.forEach(e->iterators.add(new PathIterator<V>(source, e)));
		
		while(iterators.size() > 1){
			try{
				Thread.sleep(100);
			}catch(InterruptedException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Iterator<PathIterator<V>> iters = iterators.iterator();
			while(iters.hasNext()){
				PathIterator<V> iter = iters.next();
				if(iter.removed){
					iters.remove();
					System.out.println("drop iterator at head " + iter.head + " from " + iter.path);
					continue;
				}
				
				iter.advance();
				System.out.println("iter at: " + iter.head + " (" + iter.merged + ")");

				if(iter.isMergeNode()){
					for(PathIterator<V> other : iterators){
						if(other != iter && other.head.equals(iter.head)){
							System.out.println("merge " + iter.path + " | " + other.path);
							iter.path = CPQ.intersect(iter.path, other.path);
							other.removed = true;
						}
					}
				}else{
					if(iter.isSplitNode()){
						MergePath<V> path = traverseToMerge(iter.head);
						iter.path = CPQ.concat(iter.path, path.paths());
						iter.head = path.mergeNode;
						iter.head.contractLoops();//TODO
						iter.merged = true;
					}
				}
			}
			
			
		}
		
		return new MergePath<V>(iterators.getFirst());
	}
	
	private static class PathIterator<V>{
		private CPQ path;
		private Vertex<V> head;
		private boolean removed = false;
		private boolean merged = false;
		
		private PathIterator(Vertex<V> source, GraphEdge<Vertex<V>, Edge> edge){
			path = CPQ.label(edge.getData().predicate);
			head = source.equals(edge.getSource()) ? edge.getTarget() : edge.getSource();
			head.contractLoops();//TODO
		}
		
		private void advance(){
			while(!isMergeNode() && !isSplitNode() && !isTargetNode()){
				GraphEdge<Vertex<V>, Edge> edge = head.canReachTrg.iterator().next();
				path = CPQ.concat(path, CPQ.label(edge.getData().predicate));//TODO incorporate loops
				head = getTo(edge);
				head.contractLoops();//TODO
			}
		}
		
		private boolean isMergeNode(){
			return !merged && head.canReachSrc.size() > 1;
		}
		
		private boolean isSplitNode(){
			return head.canReachTrg.size() > 1;
		}
		
		private boolean isTargetNode(){
			return head.canReachTrg.isEmpty();
		}
		
		private Vertex<V> getTo(GraphEdge<Vertex<V>, Edge> edge){
			return edge.getSource().equals(head) ? edge.getTarget() : edge.getSource();
		}
	}
	
	private static record MergePath<V>(CPQ paths, Vertex<V> mergeNode){
		
		private MergePath(PathIterator<V> iter){
			this(iter.path, iter.head);
		}
	}
	
//	private static CPQ addLoops(List<UniqueGraph<V, Predicate>>)
	
	
	
	
	
	
	
	
	private static class Vertex<V>{
		private final V data;
		@Deprecated
		private final List<Component<V>> components = new ArrayList<Component<V>>();
		private final Set<GraphEdge<Vertex<V>, Edge>> canReachSrc = new HashSet<GraphEdge<Vertex<V>, Edge>>();
		private final Set<GraphEdge<Vertex<V>, Edge>> canReachTrg = new HashSet<GraphEdge<Vertex<V>, Edge>>();
		private GraphNode<Vertex<V>, Edge> node;
		
		private CPQ loops;
		@Deprecated
		private int arrivals = 0;
		
		private Vertex(V data){
			this.data = data;
		}
		
		public CPQ contractLoops(){
			System.out.println("ctrl: " + data);
			for(GraphEdge<Vertex<V>, Edge> edge : node.getInEdges()){
				if(!canReachSrc.contains(edge) && !canReachTrg.contains(edge)){
					loops = traversePaths(node.getData(), node.getData());
					System.out.println("find loop on: " + data + " as " + loops);
				}
			}
			
			for(GraphEdge<Vertex<V>, Edge> edge : node.getOutEdges()){//TODO maybe just add a getEdges... (also would eliminate duplicates, i.e., self loop)
				if(!canReachSrc.contains(edge) && !canReachTrg.contains(edge)){
					loops = traversePaths(node.getData(), node.getData());
					System.out.println("find loop on: " + data + " as " + loops);
				}
			}
			
			
			return null;
			
//			System.out.println(
//				data +
//				" has entry " + getInEdge().getSource() + "-" + getInEdge().getTarget() + " (" + getInEdge().getData().predicate.getAlias() + ")" +
//				" and exit " + getOutEdge().getSource() + "-" + getOutEdge().getTarget() + " (" + getOutEdge().getData().predicate.getAlias() + ")"
//			);
			
			
			
		}
		
//		public GraphEdge<Vertex<V>, Edge> getInEdge(){
//			GraphEdge<Vertex<V>, Edge> min = null;
//
//			for(GraphEdge<Vertex<V>, Edge> edge : node.getInEdges()){
//				if(min == null || min.getData().srcDisc > edge.getData().srcDisc){
//					min = edge;
//				}
//			}
//
//			for(GraphEdge<Vertex<V>, Edge> edge : node.getOutEdges()){
//				if(min == null || min.getData().srcDisc > edge.getData().srcDisc){
//					min = edge;
//				}
//			}
//
//			return min;
//		}
//
//		public GraphEdge<Vertex<V>, Edge> getOutEdge(){
//			GraphEdge<Vertex<V>, Edge> min = null;
//
//			for(GraphEdge<Vertex<V>, Edge> edge : node.getInEdges()){
//				if(min == null || min.getData().trgDisc > edge.getData().trgDisc){
//					min = edge;
//				}
//			}
//
//			for(GraphEdge<Vertex<V>, Edge> edge : node.getOutEdges()){
//				if(min == null || min.getData().trgDisc > edge.getData().trgDisc){
//					min = edge;
//				}
//			}
//
//			return min;
//		}

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
		
//		public CPQ loopOn(Vertex<V> vertex){
//
//		}
		
		
	}
	
	private static class Edge{
		private final Predicate predicate;
		@Deprecated
		private boolean traversed = false;
//		private int srcDisc = -1;
//		private int trgDisc = -1;
		
		private Edge(Predicate predicate){
			this.predicate = predicate;
		}
		
		public CPQ toCPQ(){
			return CPQ.label(predicate);
		}
		
//		public void updateSourceDiscovery(int num){
//			if(srcDisc == -1){
//				srcDisc = num;
//			}
//		}
//
//		public void updateTargetDiscovery(int num){
//			if(trgDisc == -1){
//				trgDisc = num;
//			}
//		}
		
		@Override
		public String toString(){
			// TODO Auto-generated method stub
			return super.toString();
		}
	}
}
