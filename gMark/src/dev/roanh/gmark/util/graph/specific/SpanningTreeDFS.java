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
 * iterative instead of recursive to avoid stack overflows but based on the
 * DFS algorithm by John Hopcroft and Robert Tarjan and still runs in linear time.
 * @author Roan
 * @param <V> The vertex data type.
 * @param <M> The metadata type.
 * @see SimpleGraph
 * @see <a href="https://en.wikipedia.org/wiki/Biconnected_component">Wikipedia article on computing Articulation Points</a>
 */
public class SpanningTreeDFS<V extends IDable, M>{
	/**
	 * The vertices in the spanning tree.
	 */
	private final List<Vertex> vertices;
	
	/**
	 * Computes a DFS spanning tree for the given graph.
	 * @param graph The graph to compute a spanning tree for.
	 * @throws IllegalArgumentException When the given graph is not connected.
	 */
	public SpanningTreeDFS(SimpleGraph<V, M> graph) throws IllegalArgumentException{
		vertices = new ArrayList<Vertex>(graph.getVertexCount());
		if(graph.getVertexCount() == 0){
			return;
		}
		
		int depth = 1;
		int rootChildren = 0;
		
		SimpleVertex<V, M> first = graph.getVertices().getFirst();
		Vertex root = new Vertex(first, null, depth++);
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
	
	/**
	 * Gets a list of all the articulation points (cut-vertices) found in the graph.
	 * @return A list of articulation points.
	 */
	public List<SimpleVertex<V, M>> getArticulationPoints(){
		return vertices.stream().filter(v->v.isArticulationPoint).map(v->v.original).toList();
	}
	
	/**
	 * DFS vertex state information.
	 * @author Roan
	 */
	private class Vertex{
		/**
		 * The original input graph vertex.
		 */
		private final SimpleVertex<V, M> original;
		/**
		 * DFS discovery time for this vertex.
		 */
		private int discovery;
		/**
		 * DFS back link information, used to detect alternative paths to a node.
		 */
		private int lowlink;
		/**
		 * The DFS spanning tree parent of this vertex.
		 */
		private Vertex parent;
		/**
		 * DFS state and iterator over the yet to be visited children of this vertex in DFS.
		 */
		private Iterator<SimpleEdge<V, M>> childIterator;
		/**
		 * Flag set when this vertex is an articulation point, this is set when:
		 * <ol>
		 *   <li>The vertex is the DFS root and has at least 2 child nodes in the DFS spanning tree.</li>
		 *   <li>The vertex is not the DFS root and one of its child nodes has a lowlink value lower than
		 *   the discovery time of the vertex itself.</li>
		 * </ol>
		 */
		private boolean isArticulationPoint = false;
		
		/**
		 * Constructs a new DFS spanning tree vertex.
		 * @param vertex The original graph vertex.
		 * @param parent The DFS spanning tree parent vertex, or null if the root.
		 * @param discovery The DFS discovery time of this vertex.
		 */
		private Vertex(SimpleVertex<V, M> vertex, Vertex parent, int discovery){
			this.original = vertex;
			this.discovery = discovery;
			this.parent = parent;
			lowlink = discovery;
			childIterator = vertex.getEdges().iterator();
		}
		
		/**
		 * Checks if there are any children remaining in the iterator for this vertex.
		 * @return True if unvisited child vertices remain.
		 */
		public boolean hasNext(){
			return childIterator.hasNext();
		}
		
		/**
		 * Gets the next unvisited child node of this vertex.
		 * @return The next child vertex.
		 */
		public SimpleVertex<V, M> nextChild(){
			return childIterator.next().getTarget(original);
		}
		
		/**
		 * Updates the lowlink value for this vertex if it is higher than the given value.
		 * @param lowlink The new lowlink value, if lower.
		 */
		public void updateLowLink(int lowlink){
			this.lowlink = Math.min(this.lowlink, lowlink);
		}
	}
}
