/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
package dev.roanh.gmark.util.graph.specific;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import dev.roanh.gmark.type.IDable;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.graph.generic.SimpleGraph;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleVertex;

/**
 * Depth-First Search Spanning Tree implementation with low link information
 * to determine articulation points in the input graph. The implementation is
 * iterative instead of recursive to avoid stack overflows.
 * @author Roan
 * @param <V> The vertex data type.
 * @param <M> The metadata type.
 * @see SimpleGraph
 */
public class SpanningTreeDFS<V extends IDable, M>{
	private final List<Vertex> vertices;
	private final Vertex root;
	
	public SpanningTreeDFS(SimpleGraph<V, M> graph) throws IllegalArgumentException{
		vertices = new ArrayList<Vertex>(graph.getVertexCount());
		if(graph.getVertexCount() == 0){
			root = null;
			return;
		}
		
		int depth = 1;
		int rootChildren = 0;
		
		SimpleVertex<V, M> first = graph.getVertices().getFirst();
		root = new Vertex(first, null, depth++);
		vertices.add(root);
		
		RangeList<Vertex> vertexMap = new RangeList<Vertex>(graph.getVertexCapacity());
		vertexMap.set(first, root);
		
		Deque<Vertex> stack = new ArrayDeque<Vertex>();
		stack.push(root);
		
		while(!stack.isEmpty()){
			Vertex source = stack.peek();
			
			if(source.hasNext()){
				SimpleVertex<V, M> child = source.nextChild();
				Vertex target = vertexMap.get(child);
				
				if(target == null){
					//new vertex
					target = new Vertex(child, source, depth++);
					if(source == root){
						rootChildren++;
					}
					
					vertexMap.set(child, target);
					vertices.add(target);
					stack.push(target);
				}else if(target != source.parent){
					//back edge
					source.updateLowLink(target.discovery);
				}
			}else{
				//processed all children
				stack.pop();
				
				Vertex parent = source.parent;
				if(parent != null){
					parent.updateLowLink(source.lowlink);
					if(source.lowlink >= parent.discovery){
						parent.isArticulationPoint = true;
					}
				}
			}
		}
		
		//note that the root might have been marked as an AP before when popping its only direct child
		//so this cannot be an if as it has to clear the AP flag if it was incorrectly set
		root.isArticulationPoint = rootChildren >= 2;
		
		if(vertices.size() != graph.getVertexCount()){
			throw new IllegalArgumentException("Input was not a connected graph.");
		}
	}
	
	public List<SimpleVertex<V, M>> getArticulationPoints(){
		return vertices.stream().filter(v->v.isArticulationPoint).map(v->v.original).toList();
	}
	
	private class Vertex{
		private final SimpleVertex<V, M> original;
		private int discovery;
		private int lowlink;
		private Vertex parent;
		private Iterator<SimpleEdge<V, M>> childIterator;
		private boolean isArticulationPoint = false;
		
		private Vertex(SimpleVertex<V, M> vertex, Vertex parent, int discovery){
			this.original = vertex;
			this.discovery = discovery;
			this.parent = parent;
			lowlink = discovery;
			childIterator = vertex.getEdges().iterator();
		}
		
		public boolean hasNext(){
			return childIterator.hasNext();
		}
		
		public SimpleVertex<V, M> nextChild(){
			return childIterator.next().getTarget(original);
		}
		
		public void updateLowLink(int lowlink){
			this.lowlink = Math.min(this.lowlink, lowlink);
		}
	}
}
