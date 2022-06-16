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
package dev.roanh.gmark.conjunct.cpq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Graph;
import dev.roanh.gmark.util.Graph.GraphEdge;
import dev.roanh.gmark.util.Graph.GraphNode;

public class QueryGraphCPQTest{
	
	@Test
	public void testConstruction() throws Exception{
		Predicate a = new Predicate(1, "a");
		Predicate b = new Predicate(2, "b");
		Predicate c = new Predicate(3, "c");
		
		CPQ q = CPQ.intersect(CPQ.concat(CPQ.label(a), CPQ.intersect(CPQ.label(b), CPQ.id()), CPQ.label(c)), CPQ.IDENTITY);
		assertEquals("((a◦(b ∩ id)◦c) ∩ id)", q.toString());

		QueryGraphCPQ queryGraph = q.toQueryGraph();
		Graph<Vertex, Predicate> graph = queryGraph.toGraph();
		
		assertEquals(3, graph.getEdgeCount());
		assertEquals(2, graph.getNodeCount());
				
		assertEquals(Arrays.asList("a", "b", "c"), graph.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).sorted().collect(Collectors.toList()));
		assertEquals(Arrays.asList("", "src,trg"), graph.getNodes().stream().map(GraphNode::getData).map(queryGraph::getVertexLabel).sorted().collect(Collectors.toList()));

		List<GraphNode<Vertex, Predicate>> nodes = graph.getNodes();
		for(int i = 0; i < 2; i++){
			GraphNode<Vertex, Predicate> node = nodes.get(i);
			if(node.getInCount() == 1){
				assertEquals(1, node.getOutCount());
				assertEquals("a", node.getOutEdges().iterator().next().getData().getAlias());
				assertEquals("c", node.getInEdges().iterator().next().getData().getAlias());
				assertEquals(nodes.get((i + 1) % 2), node.getOutEdges().iterator().next().getTargetNode());
				assertEquals(nodes.get((i + 1) % 2), node.getInEdges().iterator().next().getSourceNode());
			}else{
				assertEquals(2, node.getInCount());
				assertEquals(2, node.getOutCount());
				for(GraphEdge<Vertex, Predicate> edge : node.getOutEdges()){
					if(edge.getData().getAlias().equals("b")){
						assertEquals(node, edge.getTargetNode());
					}else{
						assertEquals(nodes.get((i + 1) % 2), edge.getTargetNode());
					}
				}
				for(GraphEdge<Vertex, Predicate> edge : node.getInEdges()){
					if(edge.getData().getAlias().equals("b")){
						assertEquals(node, edge.getTargetNode());
					}else{
						assertEquals(nodes.get((i + 1) % 2), edge.getSourceNode());
					}
				}
			}
		}
	}
	
	@Test
	public void testRename(){
		Predicate a = new Predicate(1, "a");
		Predicate b = new Predicate(2, "b");
		
		CPQ cpq = CPQ.concat(
			CPQ.intersect(
				CPQ.concat(
					CPQ.label(a),
					CPQ.intersect(
						CPQ.label(b),
						CPQ.id()
					)
				),
				CPQ.id()
			),
			CPQ.intersect(
				CPQ.label(a),
				CPQ.id()
			)
		);
		assertEquals("(((a◦(b ∩ id)) ∩ id)◦(a ∩ id))", cpq.toString());
		
		Graph<Vertex, Predicate> graph = cpq.toQueryGraph().toGraph();
		assertEquals(1, graph.getNodeCount());
		assertEquals(2, graph.getEdgeCount());
		
		GraphNode<Vertex, Predicate> node = graph.getNodes().get(0);
		for(GraphEdge<Vertex, Predicate> edge : graph.getEdges()){
			assertEquals(node, edge.getSourceNode());
			assertEquals(node, edge.getTargetNode());
		}
		assertIterableEquals(Arrays.asList("a", "b"), graph.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).sorted().collect(Collectors.toList()));
	}
}
