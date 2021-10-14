package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Graph<V, E>{
	private Map<V, GraphNode<V, E>> nodeMap = new HashMap<V, GraphNode<V, E>>();
	private List<GraphNode<V, E>> nodes = new ArrayList<GraphNode<V, E>>();
	private List<GraphEdge<V, E>> edges = new ArrayList<GraphEdge<V, E>>();
	
	public List<GraphNode<V, E>> getNodes(){
		return nodes;
	}
	
	public List<GraphEdge<V, E>> getEdges(){
		return edges;
	}
	
	//TODO assumed to be unique but not checked
	public GraphNode<V, E> addUniqueNode(V data){
		GraphNode<V, E> node = new GraphNode<V, E>(this, data);
		if(nodeMap.put(data, node) != null){
			throw new IllegalStateException("TODO");
		}
		nodes.add(node);
		return node;
	}
	
	public void addUniqueEdge(V source, V target, E data){
		addUniqueEdge(nodeMap.get(source), nodeMap.get(target), data);
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, V target, E data){
		addUniqueEdge(source, nodeMap.get(target), data);
	}
	
	public void addUniqueEdge(V source, GraphNode<V, E> target, E data){
		addUniqueEdge(nodeMap.get(source), target, data);
	}
	
	public void addUniqueEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
		//TODO this duplicate check is fairly expensive as is
		for(GraphEdge<V, E> edge : source.out){
			if(edge.target.equals(target)){
				return;
			}
		}
		edges.add(new GraphEdge<V, E>(source, target, data));
	}
	
	public static class GraphNode<V, E>{
		private Graph<V, E> graph;
		private Set<GraphEdge<V, E>> out = new HashSet<GraphEdge<V, E>>();
		private Set<GraphEdge<V, E>> in = new HashSet<GraphEdge<V, E>>();
		private V data;
		
		private GraphNode(Graph<V, E> graph, V data){
			this.graph = graph;
			this.data = data;
		}
		
		public void addUniqueEdgeTo(GraphNode<V, E> target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		public void addUniqueEdgeTo(V target, E data){
			graph.addUniqueEdge(this, target, data);
		}
		
		@Override
		public String toString(){
			return data.toString();
		}
		
		@Override
		public boolean equals(Object other){
			return other instanceof GraphNode ? ((GraphNode<?, ?>)other).data.equals(data) : false;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(data);
		}
	}
	
	public static class GraphEdge<V, E>{
		private GraphNode<V, E> source;
		private GraphNode<V, E> target;
		private E data;
		
		private GraphEdge(GraphNode<V, E> source, GraphNode<V, E> target, E data){
			this.source = source;
			this.target = target;
			this.data = data;
			source.out.add(this);
			target.in.add(this);
		}
		
		public GraphNode<V, E> getSource(){
			return source;
		}
		
		public GraphNode<V, E> getTarget(){
			return target;
		}
		
		public E getData(){
			return data;
		}
		
		@Override
		public String toString(){
			return data.toString();
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof GraphEdge){
				GraphEdge<?, ?> edge = (GraphEdge<?, ?>)other;
				return edge.source.equals(source) && edge.target.equals(target);
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(source, target);
		}
	}
}
