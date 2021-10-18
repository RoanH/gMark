package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import dev.roanh.gmark.core.graph.Predicate;

public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	private Random random = ThreadLocalRandom.current();//TODO configurable
	private GraphNode<EdgeGraphData, Void> src;
	private GraphNode<EdgeGraphData, Void> trg;
	
	
	
	
	
	
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target){
		src = addUniqueNode(EdgeGraphData.of("source"));
		trg = addUniqueNode(EdgeGraphData.of("target"));

		for(GraphEdge<SelectivityType, Predicate> edge : gs.getEdges()){
			addUniqueNode(EdgeGraphData.of(edge.getData()));
		}
		
		Deque<Predicate> path = new ArrayDeque<Predicate>();
		computeAllPaths(path, gs, maxLen, gs.getNode(source), gs.getNode(target));
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
	
	private Set<EdgeGraphData> findParallel(){
		Set<EdgeGraphData> parallel = new HashSet<EdgeGraphData>();
		
		for(GraphNode<EdgeGraphData, Void> node : getNodes()){
			if(node.getInEdges().size() > 1){
				parallel.add(reverseParallel(node));
			}
		}
		
		return parallel;
	}
	
	private EdgeGraphData reverseParallel(GraphNode<EdgeGraphData, Void> target){
		Set<GraphEdge<EdgeGraphData, Void>> in = target.getInEdges();
		int[] indices = random.ints(0, in.size()).distinct().limit(2).sorted().toArray();
		int index = 0;
		
		Deque<EdgeGraphData> first;
		Deque<EdgeGraphData> second;
		for(GraphEdge<EdgeGraphData, Void> edge : in){
			if(index == indices[0]){
				first = reverseToSource(edge);
			}else if(index == indices[1]){
				second = reverseToSource(edge);
			}
			index++;
		}
		
		//TODO remove the shared path and make an intesrection edge graph data
		
	
		return null;//TODO
	}
	
	private Deque<EdgeGraphData> reverseToSource(GraphEdge<EdgeGraphData, Void> edge){
		Deque<EdgeGraphData> path = new ArrayDeque<EdgeGraphData>();
		path.addLast(edge.getSource());
		
		while(!edge.getSourceNode().equals(src)){
			edge = Util.selectRandom(random, edge.getSourceNode().getInEdges());
			path.addFirst(edge.getSource());
		}
		
		return path;
	}
}
