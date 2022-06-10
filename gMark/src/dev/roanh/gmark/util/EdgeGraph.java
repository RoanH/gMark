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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.util.EdgeGraphData.IntersectionData;

/**
 * The edge graph is a graph constructed from an input graph such that
 * all edges in the original input graph are nodes in the edge graph. In
 * addition, nodes are connected when there exists a node between them in
 * the original graph. The purpose of the edge graph is to encode all
 * possible paths between two preselected nodes in the input graph. For
 * this purpose two special nodes are added to the edge graph to represent
 * the source and target. All paths in the edge graph must originate from
 * the source node and end at the target node.
 * @author Roan
 * @see SchemaGraph
 * @see EdgeGraphData
 */
public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	/**
	 * The source node of all paths used to construct the edge graph.
	 */
	private GraphNode<EdgeGraphData, Void> src;
	/**
	 * The target node of all paths used to construct the edge graph.
	 */
	private GraphNode<EdgeGraphData, Void> trg;
	/**
	 * The maximum length of paths to use to construct the graph.
	 */
	private int maxLen;
	/**
	 * Length of the shortest path actually used to construct
	 * the initial edge graph.
	 */
	private int baseMin = Integer.MAX_VALUE;
	/**
	 * Length of the longest path actually used to construct
	 * the initial edge graph.
	 */
	private int baseMax;
	
	/**
	 * Constructs a new edge graph from the given schema graph. The
	 * edge graph is built up using all paths of at most the given
	 * maximum length between the given source and target node in
	 * the schema graph. Drawn paths will then be recursively
	 * intersected up to a maximum recursive depth of 5.
	 * @param gs The schema graph.
	 * @param maxLen The maximum length of paths to draw
	 *        from the schema graph.
	 * @param source The schema graph source node.
	 * @param target The schema graph target node.
	 * @throws GenerationException When there are no paths between
	 *         the given source and target node.
	 * @see #EdgeGraph(SchemaGraph, int, SelectivityType, SelectivityType, int)
	 */
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target) throws GenerationException{
		this(gs, maxLen, source, target, 5);
	}
	
	/**
	 * Constructs a new edge graph from the given schema graph. The
	 * edge graph is built up using all paths of at most the given
	 * maximum length between the given source and target node in
	 * the schema graph. Drawn paths will then be recursively
	 * intersected up to the given maximum recursive depth.
	 * @param gs The schema graph.
	 * @param maxLen The maximum length of paths to draw
	 *        from the schema graph.
	 * @param source The schema graph source node.
	 * @param target The schema graph target node.
	 * @param recursion The maximum recursive depth.
	 * @throws GenerationException When there are no paths between
	 *         the given source and target node.
	 * @see #EdgeGraph(SchemaGraph, int, SelectivityType, SelectivityType)
	 */
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target, int recursion) throws GenerationException{
		this.maxLen = maxLen;
		
		//add source and target
		src = addUniqueNode(EdgeGraphData.of("source", source));
		trg = addUniqueNode(EdgeGraphData.of("target", target));

		//add all schema graph edges at nodes
		for(GraphEdge<SelectivityType, Predicate> edge : gs.getEdges()){
			addUniqueNode(EdgeGraphData.of(edge));
		}
		
		//draw paths in the schema graph to connect the nodes
		Deque<GraphEdge<SelectivityType, Predicate>> path = new ArrayDeque<GraphEdge<SelectivityType, Predicate>>();
		computeAllPaths(path, gs, maxLen, gs.getNode(source), gs.getNode(target));
		
		if(getEdgeCount() == 0){
			throw new GenerationException("No paths between edge graph source and target.");
		}
		
		//iteratively build up the graph
		for(int i = 0; i < recursion; i++){
			for(IntersectionData parallel : findParallel()){
				if(parallel.size() <= maxLen){//TODO could factor in the distance to the source and target
					GraphNode<EdgeGraphData, Void> n = addUniqueNode(parallel);
					//TODO could have just saved the source and target as real nodes
					n.addUniqueEdgeFrom(parallel.getSource());
					n.addUniqueEdgeTo(parallel.getTarget());
				}
			}
		}
	}
	
	/**
	 * Gets the length of the shortest path used to construct
	 * the initial edge graph.
	 * @return The length of the shortest construction path.
	 */
	public int getBaseMinimum(){
		return baseMin;
	}
	
	/**
	 * Gets the length of the longest path used to construct
	 * the initial edge graph.
	 * @return The length of the longest construction path.
	 */
	public int getBaseMaximum(){
		return baseMax;
	}
	
	/**
	 * Gets the source node of the edge graph. Every node
	 * in the edge graph can be reached from the source node.
	 * @return The edge graph source node.
	 */
	public GraphNode<EdgeGraphData, Void> getSource(){
		return src;
	}
	
	/**
	 * Gets the target node of the edge graph. The target
	 * node is reachable by every node in the edge graph.
	 * @return The edge graph target node.
	 */
	public GraphNode<EdgeGraphData, Void> getTarget(){
		return trg;
	}
	
	/**
	 * Gets that maximum length of paths used to construct
	 * the edge graph. This value is also used to bound the
	 * length of paths drawn from the edge graph.
	 * @return The maximum path length.
	 * @see #drawPath()
	 * @see #drawPath(int)
	 */
	public int getMaxLength(){
		return maxLen;
	}
	
	/**
	 * Randomly draws a path from the edge graph using {@link #drawPath()}
	 * and prints it to standard output.
	 * @see #drawPath()
	 */
	public void printPath(){
		drawPath().stream().map(Object::toString).reduce((a, b)->a + "◦" + b).ifPresent(System.out::println);
	}
	
	/**
	 * Draws a random path from the edge graph that starts at the
	 * edge graph source node and ends at the edge graph target node.
	 * The length of the path will be at least 1 and at most configured
	 * edge graph maximum length.
	 * @return The randomly drawn path.
	 * @see #drawPath(int)
	 * @see #getMaxLength()
	 */
	public List<GraphNode<EdgeGraphData, Void>> drawPath(){
		return drawPath(1);
	}
	
	/**
	 * Draws a random path from the edge graph that starts at the
	 * edge graph source node and ends at the edge graph target node.
	 * The length of the path will be at least the given minimum length
	 * and at most configured edge graph maximum length. Care should be
	 * taken when passing large values for the minimum length as they
	 * might cause a valid to path to never be found.
	 * @param minLen The minimum length of the drawn path.
	 * @return The randomly drawn path.
	 * @see #drawPath()
	 * @see #getMaxLength()
	 */
	protected List<GraphNode<EdgeGraphData, Void>> drawPath(int minLen){
		find: while(true){
			List<GraphNode<EdgeGraphData, Void>> path = new ArrayList<GraphNode<EdgeGraphData, Void>>(maxLen);
			
			GraphNode<EdgeGraphData, Void> node = src;
			int len = 0;
			while(true){
				node = Util.selectRandom(node.getOutEdges()).getTargetNode();
				if(!node.equals(trg)){
					int size = node.getData().size();
					if(len + size <= maxLen){
						path.add(node);
						len += size;
						if(len == maxLen){
							break;
						}
					}else{
						continue find;
					}
				}else{
					if(len >= minLen){
						break;
					}else{
						continue find;
					}
				}
			}
			
			return path;
		}
	}
	
	/**
	 * Computes all paths from the given source node to the given
	 * target node in the schema graph that at most the given max
	 * length. These paths are then added to the edge graph with
	 * their edges converted to nodes.
	 * @param path The empty path deque to pass information
	 *        between recursive calls of this subroutine.
	 * @param gs The schema graph.
	 * @param maxLen The maximum path length from source to target.
	 * @param source The source node in the schema graph.
	 * @param target The target node in the schema graph.
	 * @see #addPath(Deque)
	 */
	private void computeAllPaths(Deque<GraphEdge<SelectivityType, Predicate>> path, SchemaGraph gs, int maxLen, GraphNode<SelectivityType, Predicate> source, GraphNode<SelectivityType, Predicate> target){
		if(source.equals(target) && !path.isEmpty()){
			addPath(path);
		}else if(maxLen == 0 || source.getOutEdges().isEmpty()){
			return;
		}else{
			for(GraphEdge<SelectivityType, Predicate> edge : source.getOutEdges()){
				path.push(edge);
				computeAllPaths(path, gs, maxLen - 1, edge.getTargetNode(), target);
				path.pop();
			}
		}
	}
	
	/**
	 * Adds the given path drawn from the schema graph to the edge
	 * graph where the edge in the drawn path are converted to nodes
	 * and the path is attached to the edge graph source and target nodes.
	 * @param path The path to add to the edge graph.
	 * @see #computeAllPaths
	 */
	private void addPath(Deque<GraphEdge<SelectivityType, Predicate>> path){
		baseMin = Math.min(baseMin, path.size());
		baseMax = Math.max(baseMax, path.size());
		
		trg.addUniqueEdgeFrom(EdgeGraphData.of(path.getFirst()));
		Iterator<GraphEdge<SelectivityType, Predicate>> iter = path.iterator();
		EdgeGraphData last = EdgeGraphData.of(iter.next());
		while(iter.hasNext()){
			EdgeGraphData data = EdgeGraphData.of(iter.next());
			addUniqueEdge(data, last);
			last = data;
		}
		src.addUniqueEdgeTo(last);
	}
	
	/**
	 * Attempts to find paths in the graph that can be used for an
	 * intersection with some other path in the graph or with identity.
	 * The intersections that are found are then returned.
	 * @return The found intersections that could be made.
	 * @see #reverseParallel
	 * @see #reverseIdentity
	 */
	private Set<IntersectionData> findParallel(){
		Set<IntersectionData> parallel = new HashSet<IntersectionData>();
		
		for(GraphNode<EdgeGraphData, Void> node : getNodes()){
			if(!node.getInEdges().isEmpty()){
				IntersectionData data = reverseIdentity(node);
				if(data != null){
					parallel.add(data);
				}
				
				data = reverseParallel(node);
				if(data != null){
					parallel.add(data);
				}
			}
		}
		
		return parallel;
	}
	
	/**
	 * Attempts to find a section of a path from the given target node
	 * to the source node that can be intersected with identity. This
	 * is done by reversing a path from the given target node to the
	 * source node of the graph and then checking for each path node
	 * if it is of the same type as the target node. If such a path
	 * node is found then data for an intersection is returned if the
	 * final selectivity of the path is at most linear.
	 * @param target The target node to find identity intersections for.
	 * @return The data for an identity intersection if it was found or
	 *         <code>null</code> otherwise.
	 * @see #reverseToSource
	 */
	private IntersectionData reverseIdentity(GraphNode<EdgeGraphData, Void> target){
		if(target.getData().getSourceSelectivity() == Selectivity.QUADRATIC){
			//any path intersected with identity has at most linear selectivity
			return null;
		}
		
		Deque<EdgeGraphData> path = reverseToSource(Util.selectRandom(target.getInEdges()));
		Type type = target.getData().getSourceType();
		
		while(path.size() > 1){
			//TODO, there might be more, this always finds the largest cycle
			if(path.peek().getTargetType().equals(type)){
				return EdgeGraphData.of(path.pop(), target.getData(), path);
			}
			path.pop();
		}
		
		return null;
	}
	
	/**
	 * Attempts to find two distinct paths from the given node back to the
	 * source node of the graph. If two paths are found then the identical
	 * prefix path is removed from both paths. This leaves two paths that
	 * can start from the same node and end at the given target node. These
	 * two paths are returned as the intersection data together with these
	 * shared source and target nodes.
	 * @param target The target node to find parallel paths to.
	 * @return Data about two parallel paths or <code>null</code> if two
	 *         paths like that were not found.
	 */
	private IntersectionData reverseParallel(GraphNode<EdgeGraphData, Void> target){
		if(target.getInEdges().size() <= 1){
			return null;
		}
		
		Set<GraphEdge<EdgeGraphData, Void>> in = target.getInEdges();
		int[] indices = Util.getRandom().ints(0, in.size()).distinct().limit(2).toArray();
		int index = 0;
		
		Deque<EdgeGraphData> first = null;
		Deque<EdgeGraphData> second = null;
		for(GraphEdge<EdgeGraphData, Void> edge : in){
			if(index == indices[0]){
				first = reverseToSource(edge);
			}else if(index == indices[1]){
				second = reverseToSource(edge);
			}
			index++;
		}
		
		if(first.size() == 1 || second.size() == 1){
			//this can happen when the target has an edge to the source
			return null;
		}
		
		//remove the shared prefix (always exists, at least src)
		EdgeGraphData source = null;
		while(first.size() > 1 && second.size() > 1 && first.getFirst().equals(second.getFirst())){
			source = first.removeFirst();
			second.removeFirst();
		}
		
		return EdgeGraphData.of(source, target.getData(), first, second);
	}
	
	/**
	 * Reverses a path from the given edge all the way to source node of the graph.
	 * The returned deque will contain from front to back all the nodes encountered
	 * from the source node to a node connected to (out edge) the given edge. The
	 * source node itself is not included in the result.
	 * @param edge The edge to reverse.
	 * @return A path of nodes leading to the given edge from the source.
	 */
	private Deque<EdgeGraphData> reverseToSource(GraphEdge<EdgeGraphData, Void> edge){
		Deque<EdgeGraphData> path = new ArrayDeque<EdgeGraphData>();
		path.addLast(edge.getSource());
		
		while(!edge.getSourceNode().equals(src)){
			edge = Util.selectRandom(edge.getSourceNode().getInEdges());
			path.addFirst(edge.getSource());
		}
		
		return path;
	}
}
