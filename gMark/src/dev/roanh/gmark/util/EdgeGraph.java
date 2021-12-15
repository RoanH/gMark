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
 */
public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	/**
	 * The source node of all paths used to construct the edge graph.
	 */
	private GraphNode<EdgeGraphData, Void> src;
	/**
	 * The target node of all paths used to constrct teh edge graph.
	 */
	private GraphNode<EdgeGraphData, Void> trg;
	private int maxLen;
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target){
		this(gs, maxLen, source, target, 5);
	}
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target, int recursion){
		this.maxLen = maxLen;
		
		src = addUniqueNode(EdgeGraphData.of("source", source));
		trg = addUniqueNode(EdgeGraphData.of("target", target));

		for(GraphEdge<SelectivityType, Predicate> edge : gs.getEdges()){
			addUniqueNode(EdgeGraphData.of(edge));
		}
		
		Deque<GraphEdge<SelectivityType, Predicate>> path = new ArrayDeque<GraphEdge<SelectivityType, Predicate>>();
		computeAllPaths(path, gs, maxLen, gs.getNode(source), gs.getNode(target));
		
		//TODO add identity - note that we need to see how findParallel deals with that, probably it can choose either pick a path or a SINGLE id edge if it exists.
		//just pick one path and then check everytime if we can link back
		//edge case: we could id a path that doesn't split
		
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
	
	public void printPath(){
		drawPath().stream().map(Object::toString).reduce((a, b)->a + "◦" + b).ifPresent(System.out::println);
	}
	
	public List<GraphNode<EdgeGraphData, Void>> drawPath(){
		return drawPath(1);
	}
	
	public List<GraphNode<EdgeGraphData, Void>> drawPath(int minLen){
		//TODO return a valid path, should probably respect min/max length, should make sure to never follow identity edges
		//TODO investigate performance
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
			
			assert path.size() >= minLen : "Path too short";
			assert path.size() <= maxLen : "Path too long";
			assert path.get(0).getInEdges().stream().map(GraphEdge::getSourceNode).anyMatch(src::equals) : "Start not connected to source";
			assert path.get(path.size() - 1).getOutEdges().stream().map(GraphEdge::getTargetNode).anyMatch(trg::equals) : "End not connected to target";
			
			return path;
		}
	}
	
	//path can be empty if source connected to target
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
	
	private void addPath(Deque<GraphEdge<SelectivityType, Predicate>> path){
		assert !path.isEmpty() : "Path not allowed to be empty";
		
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
	 * @see #reverseParallel(GraphNode)
	 * @see #reverseIdentity(GraphNode)
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
	 * @see #reverseToSource(GraphEdge)
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
