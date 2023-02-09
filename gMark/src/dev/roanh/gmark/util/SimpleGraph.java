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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//undirected no edge labels
public class SimpleGraph<T>{
	private Map<T, SimpleVertex<T>> vertexMap = new HashMap<T, SimpleVertex<T>>();
	private List<SimpleEdge<T>> edges = new ArrayList<SimpleEdge<T>>();
	
	public List<SimpleEdge<T>> getEdges(){
		return edges;
	}
	
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
		SimpleVertex<T> target = vertexMap.get(b);
		target.edges.add(a);
		a.edges.add(target);
		edges.add(new SimpleEdge<T>(a, target));
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
		for(SimpleVertex<T> a : vertexMap.values()){
			for(SimpleVertex<T> b : a.getEdges()){
				g.addUniqueEdge(a.getData(), b.getData());
				g.addUniqueEdge(b.getData(), a.getData());
			}
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
	}
	
	public static final class SimpleVertex<T>{
		private T data;
		private Set<SimpleVertex<T>> edges = new HashSet<SimpleVertex<T>>();
		
		private SimpleVertex(T data){
			this.data = data;
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
