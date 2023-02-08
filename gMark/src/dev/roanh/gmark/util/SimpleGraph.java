package dev.roanh.gmark.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//undirected no edge labels
public class SimpleGraph<T>{
	private Map<T, SimpleVertex<T>> vertexMap = new HashMap<T, SimpleVertex<T>>();
	
	
	
	public SimpleVertex<T> addVertex(T data){
		SimpleVertex<T> v = vertexMap.get(data);
		if(v != null){
			return v;
		}else{
			v = new SimpleVertex<T>(data);
			vertexMap.put(data, v);
			return v;
		}
	}
	
	public void addEdge(T a, T b){
		addEdge(vertexMap.get(a), b);
	}
	
	public void addEdge(SimpleVertex<T> a, T b){
		a.addEdge(vertexMap.get(b));
	}
	
	public int getVertexCount(){
		return vertexMap.size();
	}
	
	public int getEdgeCount(){
		return vertexMap.values().stream().mapToInt(SimpleVertex::getDegree).sum() / 2;
	}
	
	public Collection<SimpleVertex<T>> getVertices(){
		return vertexMap.values();
	}
	
	public UniqueGraph<T, Void> toUniqueGraph(){
		UniqueGraph<T, Void> g = new UniqueGraph<T, Void>();
		vertexMap.keySet().forEach(g::addUniqueNode);
		for(SimpleVertex<T> a : vertexMap.values()){
			for(SimpleVertex<T> b : a.getEdges()){
				g.addUniqueEdge(a.getData(), b.getData());
				g.addUniqueEdge(b.getData(), a.getData());
			}
		}
		return g;
	}
	
	public static final class SimpleVertex<T>{
		private T data;
		private Set<SimpleVertex<T>> edges = new HashSet<SimpleVertex<T>>();
		
		private SimpleVertex(T data){
			this.data = data;
		}
		
		public void addEdge(SimpleVertex<T> target){
			target.edges.add(this);
			edges.add(target);
		}
		
		public Set<SimpleVertex<T>> getEdges(){
			return edges;
		}
		
		public T getData(){
			return data;
		}
		
		public int getDegree(){
			return edges.size();
		}
	}
}
