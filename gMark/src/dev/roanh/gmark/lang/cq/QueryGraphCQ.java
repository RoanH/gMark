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
package dev.roanh.gmark.lang.cq;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

/**
 * Representation of the query graph of a CQ. This is effectively a visual representation
 * of the CQ as a graph with the variables of the CQ corresponding to vertices and the formulae
 * corresponding to directed edges in the graph.
 * @author Roan
 */
public class QueryGraphCQ{
	/**
	 * The set of vertices (variables) for the graph.
	 */
	private final Set<VarCQ> vertices;
	/**
	 * The set of edges (formulae) for the graph.
	 */
	private final List<AtomCQ> edges;
	
	/**
	 * Constructs a new CQ query graph with the given vertices and edges.
	 * @param vertices The graph vertices (variables).
	 * @param edges The graph edges (formulae).
	 */
	public QueryGraphCQ(Set<VarCQ> vertices, List<AtomCQ> edges){
		this.vertices = vertices;
		this.edges = edges;
	}
	
	/**
	 * Constructs a new CQ query graph from the given generic graph.
	 * @param graph The graph structure to parse into a CQ query graph.
	 */
	public QueryGraphCQ(UniqueGraph<VarCQ, AtomCQ> graph){
		this(
			graph.getNodes().stream().map(GraphNode::getData).collect(Collectors.toSet()),
			graph.getEdges().stream().map(GraphEdge::getData).toList()
		);
	}
	
	/**
	 * Converts this CQ query graph to a CQ.
	 * @return The constructed equivalent CQ.
	 */
	public CQ toCQ(){
		return new CQ(vertices, edges);
	}
	
	/**
	 * Gets the number of vertices (variables) in this query graph.
	 * @return The number of vertices in this query graph.
	 */
	public int getVertexCount(){
		return vertices.size();
	}
	
	/**
	 * Gets the number of edges (formulae) in this query graph.
	 * @return The number of edges in this query graph.
	 */
	public int getEdgeCount(){
		return edges.size();
	}
	
	/**
	 * Converts this query graph to a generic graph instance with relevant metadata.
	 * @return The constructed generic graph.
	 */
	public UniqueGraph<VarCQ, AtomCQ> toUniqueGraph(){
		UniqueGraph<VarCQ, AtomCQ> graph = new UniqueGraph<VarCQ, AtomCQ>();
		
		for(VarCQ v : vertices){
			graph.addUniqueNode(v);
		}
		
		for(AtomCQ atom : edges){
			graph.addUniqueEdge(atom.getSource(), atom.getTarget(), atom);
		}
		
		return graph;
	}
	
	@Override
	public String toString(){
		return toCQ().toString();
	}
}
