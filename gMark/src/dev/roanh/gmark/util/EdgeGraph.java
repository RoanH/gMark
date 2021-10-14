package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.roanh.gmark.core.graph.Predicate;

public class EdgeGraph extends Graph<EdgeGraphData, Void>{
	private GraphNode<EdgeGraphData, Void> src;
	private GraphNode<EdgeGraphData, Void> trg;

	
	
	
	
	
	
	
	
	public EdgeGraph(SchemaGraph gs, int maxLen, SelectivityType source, SelectivityType target){
		//TODO properly label these two
		src = addUniqueNode(new EdgeGraphData());
		trg = addUniqueNode(new EdgeGraphData());

		for(GraphEdge<SelectivityType, Predicate> edge : gs.getEdges()){
			//TODO these need proper metadata
			addUniqueNode(new EdgeGraphData());
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
		//TODO also do not modify path
		System.out.println("path: " + path);
	}
	
	
}
