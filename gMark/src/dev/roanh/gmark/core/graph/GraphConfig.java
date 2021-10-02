package dev.roanh.gmark.core.graph;

import java.util.List;

public class GraphConfig{
	private List<NodeConfig> nodes;//nodes, types
	//relation between nodes with a certain distribution
	//all edges have a symbol
	private List<EdgeConfig> edges;//edges, predicates
	
	
	
	
	public List<NodeConfig> getNodes(){
		return nodes;
	}
	
	public List<EdgeConfig> getEdges(){
		return edges;
	}
	
	public int getNodeCount(){
		return nodes.size();
	}
}
