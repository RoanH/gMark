package dev.roanh.gmark.core.graph;

import java.util.List;

@Deprecated
public class GraphConfig{
	private List<Node> nodes;//nodes, types
	//relation between nodes with a certain distribution
	//all edges have a symbol
	private List<Edge> edges;//edges, predicates
	
	
	
	
	public List<Node> getNodes(){
		return nodes;
	}
	
	public List<Edge> getEdges(){
		return edges;
	}
	
	public int getNodeCount(){
		return nodes.size();
	}
}
