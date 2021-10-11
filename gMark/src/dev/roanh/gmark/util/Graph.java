package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.List;

public class Graph<V, E>{
	private List<GraphNode<V, E>> nodes = new ArrayList<GraphNode<V, E>>();
	private List<GraphEdge<V, E>> edges = new ArrayList<GraphEdge<V, E>>();
	
	public List<GraphNode<V, E>> getNodes(){
		return nodes;
	}
	
	public List<GraphEdge<V, E>> getEdges(){
		return edges;
	}
	
	
	
	public static class GraphNode<V, E>{
		private List<GraphEdge<V, E>> out;
		private List<GraphEdge<V, E>> in;
	}
	
	public static class GraphEdge<V, E>{
		private GraphNode<V, E> source;
		private GraphNode<V, E> target;
		
		
		public GraphNode<V, E> getSource(){
			return source;
		}
	}
}
