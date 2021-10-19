package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.EdgeGraphData.IntersectionData;

public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	private Random random = ThreadLocalRandom.current();//TODO configurable
	private GraphNode<EdgeGraphData, Void> src;
	private GraphNode<EdgeGraphData, Void> trg;
	
	
	
	
	//TODO predicate are not unique, probably should use the node instead
	
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target){
		src = addUniqueNode(EdgeGraphData.of("source"));
		trg = addUniqueNode(EdgeGraphData.of("target"));

		for(GraphEdge<SelectivityType, Predicate> edge : gs.getEdges()){
			addUniqueNode(EdgeGraphData.of(edge.getData()));
		}
		
		Deque<Predicate> path = new ArrayDeque<Predicate>();
		computeAllPaths(path, gs, maxLen, gs.getNode(source), gs.getNode(target));
		
		//TODO more cycles
		for(IntersectionData parallel : findParallel()){
			GraphNode<EdgeGraphData, Void> n = addUniqueNode(parallel);
			//TODO could have just saved the source and target as real nodes
			n.addUniqueEdgeFrom(parallel.getSource());
			n.addUniqueEdgeTo(parallel.getTarget());
		}
	}
	
	private void computeAllPaths(Deque<Predicate> path, SchemaGraph gs, int maxLen, GraphNode<SelectivityType, Predicate> source, GraphNode<SelectivityType, Predicate> target){
		if(source.equals(target)){
			addPath(path);
		}else if(maxLen == 0 || source.getOutEdges().isEmpty()){
			return;
		}else{
			for(GraphEdge<SelectivityType, Predicate> edge : source.getOutEdges()){
				path.push(edge.getData());
				computeAllPaths(path, gs, maxLen - 1, edge.getTargetNode(), target);
				path.pop();
			}
		}
	}
	
	private void addPath(Deque<Predicate> path){
		src.addUniqueEdgeTo(EdgeGraphData.of(path.getFirst()));
		Iterator<Predicate> iter = path.iterator();
		EdgeGraphData last = EdgeGraphData.of(iter.next());
		while(iter.hasNext()){
			EdgeGraphData data = EdgeGraphData.of(iter.next());
			addUniqueEdge(last, data);
			last = data;
		}
		trg.addUniqueEdgeFrom(last);
	}
	
	private Set<IntersectionData> findParallel(){
		Set<IntersectionData> parallel = new HashSet<IntersectionData>();
		
		for(GraphNode<EdgeGraphData, Void> node : getNodes()){
			if(node.getInEdges().size() > 1){
				parallel.add(reverseParallel(node));
			}
		}
		
		return parallel;
	}
	
	private IntersectionData reverseParallel(GraphNode<EdgeGraphData, Void> target){
		Set<GraphEdge<EdgeGraphData, Void>> in = target.getInEdges();
		int[] indices = random.ints(0, in.size()).distinct().limit(2).toArray();
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
		
		System.out.println("paths: " + first + " / " + second);
		
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
			edge = Util.selectRandom(random, edge.getSourceNode().getInEdges());
			path.addFirst(edge.getSource());
		}
		
		return path;
	}
}
