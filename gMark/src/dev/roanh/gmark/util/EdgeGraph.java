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

public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	private GraphNode<EdgeGraphData, Void> src;
	private GraphNode<EdgeGraphData, Void> trg;
	private int minLen;
	private int maxLen;
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target){
		this(gs, maxLen, source, target, 5);
	}
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target, int recursion){
		minLen = 1;//TODO make configurable
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
			
//			assert path.size() >= minLen : "Path too short";
//			assert path.size() <= maxLen : "Path too long";
//			assert path.get(0).getInEdges().stream().map(GraphEdge::getSourceNode).anyMatch(src::equals) : "Start not connected to source";
//			assert path.get(path.size() - 1).getOutEdges().stream().map(GraphEdge::getTargetNode).anyMatch(trg::equals) : "End not connected to target";
			
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
	
	//TODO can be an empty dequeue I guess
	private void addPath(Deque<GraphEdge<SelectivityType, Predicate>> path){
//		System.out.println("add path: " + path);
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
	
	private Set<IntersectionData> findParallel(){
		Set<IntersectionData> parallel = new HashSet<IntersectionData>();
		
		//steps to consider ID
		//1. consider all nodes even those with 1 in edge (not those with 0 though, aka src)
		//2. reverse a path
		//3. for each node of the reverse path check if an intersection with ID can be made
		//4. if yes, do it with (probability ???, probably more chance if only 1 in edge) if not continue reversing
		//5. if no ID cycles were found find a second reverse path if possible and make an intersection as normal
		
		for(GraphNode<EdgeGraphData, Void> node : getNodes()){
			if(!node.getInEdges().isEmpty()){
				if(node.getData().getSourceSelectivity() != Selectivity.QUADRATIC){
					IntersectionData data = reverseIdentity(node);
					if(data != null){
//						System.out.println("id add: " + data);
						parallel.add(data);
					}
				}
				
				IntersectionData intersect = reverseParallel(node);
				if(intersect != null){
					parallel.add(intersect);
				}
			}
		}
		
		return parallel;
	}
	
	private IntersectionData reverseIdentity(GraphNode<EdgeGraphData, Void> target){
		Deque<EdgeGraphData> path = reverseToSource(Util.selectRandom(target.getInEdges()));
		Type type = target.getData().getSourceType();
		
		while(path.size() > 1){
			if(path.peek().getTargetType().equals(type)){//TODO, there might be more, this always finds the largest cycle
				return EdgeGraphData.of(path.pop(), target.getData(), path);
			}
			path.pop();
		}
		
		return null;
	}
	
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
		
//		System.out.println("paths: " + first + " / " + second);
		
		//remove the shared prefix (always exists, at least src)
		EdgeGraphData source = null;
		while(first.size() > 1 && second.size() > 1 && first.getFirst().equals(second.getFirst())){
			source = first.removeFirst();
			second.removeFirst();
		}
		
		return EdgeGraphData.of(source, target.getData(), first, second);
	}
	
	private Deque<EdgeGraphData> reverseToSource(GraphEdge<EdgeGraphData, Void> edge){
		Deque<EdgeGraphData> path = new ArrayDeque<EdgeGraphData>();
		path.addLast(edge.getSource());
		
//		do{
//			path.addFirst(edge.getSource());
//			edge = Util.selectRandom(random, edge.getSourceNode().getInEdges());
//		}while(edge != null);//only the source node has no incoming edges
		
		while(!edge.getSourceNode().equals(src)){
			edge = Util.selectRandom(edge.getSourceNode().getInEdges());
			path.addFirst(edge.getSource());
		}
		
		return path;
	}
}
