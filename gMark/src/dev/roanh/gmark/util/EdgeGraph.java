package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import dev.roanh.gmark.core.graph.Predicate;

public class EdgeGraph extends Graph<EdgeGraphData, Void>{
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
	
	
}
