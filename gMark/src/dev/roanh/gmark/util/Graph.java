package dev.roanh.gmark.util;

public class Graph<V, E>{

	
	
	
	public static class GraphNode<V, E>{
		
	}
	
	public static class GraphEdge<V, E>{
		private GraphNode<V, E> source;
		private GraphNode<V, E> target;
		
		
		public GraphNode<V, E> getSource(){
			return source;
		}
	}
}
