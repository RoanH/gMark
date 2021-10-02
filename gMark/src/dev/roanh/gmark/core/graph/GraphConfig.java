package dev.roanh.gmark.core.graph;

import java.util.List;

public class GraphConfig{
	private List<NodeConfig> nodes;//nodes, types
	private List<EdgeConfig> edges;//edges, predicates
	
	
	
	
	public List<NodeConfig> getNodes(){
		return nodes;
	}
	
	public List<EdgeConfig> getEdges(){
		return edges;
	}
}
