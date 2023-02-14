/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//undirected no edge labels
public class SimpleGraph<T>{
	private Map<T, SimpleVertex<T>> vertexMap = new LinkedHashMap<T, SimpleVertex<T>>();
	private Set<SimpleEdge<T>> edges = new HashSet<SimpleEdge<T>>();
	/**
	 * The ID to assign to the next node added to the graph.
	 */
	private int nextNodeID = 0;
	
	public void deleteVertex(SimpleVertex<T> vertex){
		vertexMap.remove(vertex.getData());
		for(SimpleEdge<T> edge : vertex.getEdges()){
			edges.remove(edge);
			edge.getTarget(vertex).edges.remove(edge);
		}
	}
	
	public SimpleVertex<T> getVertex(T data){
		return vertexMap.get(data);
	}
	
	public Set<SimpleEdge<T>> getEdges(){
		return edges;
	}
	
	public SimpleVertex<T> addVertex(T data){
		SimpleVertex<T> v = vertexMap.get(data);
		if(v != null){
			return v;
		}else{
			v = new SimpleVertex<T>(nextNodeID++, data);
			vertexMap.put(data, v);
			return v;
		}
	}
	
	public SimpleEdge<T> addEdge(T a, T b){
		return addEdge(vertexMap.get(a), b);
	}
	
	public SimpleEdge<T> addEdge(SimpleVertex<T> a, T b){
		return addEdge(a, vertexMap.get(b));
	}
	
	public SimpleEdge<T> addEdge(SimpleVertex<T> a, SimpleVertex<T> b){
		SimpleEdge<T> edge = new SimpleEdge<T>(a, b);
		b.edges.add(edge);
		a.edges.add(edge);
		edges.add(edge);
		return edge;
	}
	
	public int getVertexCount(){
		return vertexMap.size();
	}
	
	public int getEdgeCount(){
		return edges.size();
	}
	
	public Collection<SimpleVertex<T>> getVertices(){
		return vertexMap.values();
	}
	
	public UniqueGraph<T, Void> toUniqueGraph(){
		UniqueGraph<T, Void> g = new UniqueGraph<T, Void>();
		vertexMap.keySet().forEach(g::addUniqueNode);
		for(SimpleEdge<T> edge : edges){
			g.addUniqueEdge(edge.getFirstVertex().getData(), edge.getSecondVertex().getData());
			g.addUniqueEdge(edge.getSecondVertex().getData(), edge.getFirstVertex().getData());
		}
		return g;
	}
	
	public void contractEdge(SimpleEdge<T> edge, SimpleVertex<T> vertex){
		SimpleVertex<T> v1 = edge.getFirstVertex();
		SimpleVertex<T> v2 = edge.getSecondVertex();
		
		edges.remove(edge);
		vertexMap.remove(v1.getData());
		vertexMap.remove(v2.getData());
		
		for(SimpleEdge<T> e : edges){
			if(e.getFirstVertex() == v1 || e.getFirstVertex() == v2){
				e.a = vertex;
			}else if(e.getSecondVertex() == v1 || e.getSecondVertex() == v2){
				e.b = vertex;
			}
		}
	}
	
	public void dropAllEdges(){
		vertexMap.values().forEach(v->v.edges.clear());
		edges.clear();
	}
	
	public static final class SimpleEdge<T>{
		private SimpleVertex<T> a;
		private SimpleVertex<T> b;
		
		private SimpleEdge(SimpleVertex<T> a, SimpleVertex<T> b){
			this.a = a;
			this.b = b;
		}
		
		public SimpleVertex<T> getFirstVertex(){
			return a;
		}
		
		public SimpleVertex<T> getSecondVertex(){
			return b;
		}
		
		public SimpleVertex<T> getTarget(SimpleVertex<T> source){
			assert a == source || b == source;
			return a == source ? b : a;
		}
	}
	
	public static final class SimpleVertex<T> implements IDable{
		private T data;
		private Set<SimpleEdge<T>> edges = new HashSet<SimpleEdge<T>>();
		private final int id;
		
		private SimpleVertex(int id, T data){
			this.id = id;
			this.data = data;
		}
		
		public Set<SimpleEdge<T>> getEdges(){
			return edges;
		}
		
		public T getData(){
			return data;
		}
		
		public int getDegree(){
			return edges.size();
		}
		
		public boolean hasEdge(SimpleVertex<T> target){
			return getEdge(target) != null;
		}
		
		public SimpleEdge<T> getEdge(SimpleVertex<T> target){
			for(SimpleEdge<T> edge : edges){
				if(edge.b == target || edge.a == target){
					return edge;
				}
			}
			return null;
		}
		
		public List<SimpleVertex<T>> getNeighbours(){
			return edges.stream().map(e->e.getTarget(this)).collect(Collectors.toList());
		}
		
		@Override
		public int getID(){
			return id;
		}
		
		@Override
		public boolean equals(Object obj){
			return obj instanceof SimpleVertex<?> ? ((SimpleVertex<?>)obj).data.equals(data) : false;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(data);
		}
	}
}
