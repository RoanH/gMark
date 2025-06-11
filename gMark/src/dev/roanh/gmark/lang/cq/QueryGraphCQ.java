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

public class QueryGraphCQ{
	private final Set<VarCQ> variables;
	private final List<AtomCQ> edges;
	
	public QueryGraphCQ(Set<VarCQ> variables, List<AtomCQ> edges){
		this.variables = variables;
		this.edges = edges;
	}
	
	public QueryGraphCQ(UniqueGraph<VarCQ, AtomCQ> graph){
		this(
			graph.getNodes().stream().map(GraphNode::getData).collect(Collectors.toSet()),
			graph.getEdges().stream().map(GraphEdge::getData).toList()
		);
	}
	
	public CQ toCQ(){
		return new CQ(variables, edges);
	}
	
	public int getVertexCount(){
		return variables.size();
	}
	
	public int getEdgeCount(){
		return edges.size();
	}
	
	public UniqueGraph<VarCQ, AtomCQ> toUniqueGraph(){
		UniqueGraph<VarCQ, AtomCQ> graph = new UniqueGraph<VarCQ, AtomCQ>();
		
		for(VarCQ v : variables){
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
