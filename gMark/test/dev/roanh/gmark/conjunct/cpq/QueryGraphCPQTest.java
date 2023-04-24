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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Edge;
import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.QueryGraphComponent;
import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SimpleGraph;
import dev.roanh.gmark.util.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.UniqueGraph;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;

public class QueryGraphCPQTest{
	private static Predicate l1 = new Predicate(1, "a");
	private static Predicate l2 = new Predicate(2, "b");
	private static Predicate l3 = new Predicate(3, "c");
	private static Predicate l4 = new Predicate(4, "d");
	
	@Test
	public void testConstruction0() throws Exception{
		CPQ q = CPQ.intersect(CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.label(l2), CPQ.id()), CPQ.label(l3)), CPQ.IDENTITY);
		assertEquals("((a◦(b ∩ id)◦c) ∩ id)", q.toString());

		QueryGraphCPQ queryGraph = q.toQueryGraph();
		UniqueGraph<Vertex, Predicate> graph = queryGraph.toUniqueGraph();
		
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
	public void testConstruction1(){
		CPQ q = CPQ.label(l1);
		QueryGraphCPQ qg = q.toQueryGraph();
		UniqueGraph<Vertex, Predicate> g = qg.toUniqueGraph();
		assertEquals(1, g.getEdgeCount());
		assertEquals(2, g.getNodeCount());
		assertEquals(1, g.getNode(qg.getSourceVertex()).getOutCount());
		assertEquals(0, g.getNode(qg.getSourceVertex()).getInCount());
		assertEquals(0, g.getNode(qg.getTargetVertex()).getOutCount());
		assertEquals(1, g.getNode(qg.getTargetVertex()).getInCount());
	}
	
	@Test
	public void testConstruction2(){
		CPQ q = CPQ.label(l1.getInverse());
		QueryGraphCPQ qg = q.toQueryGraph();
		UniqueGraph<Vertex, Predicate> g = qg.toUniqueGraph();
		assertEquals(1, g.getEdgeCount());
		assertEquals(2, g.getNodeCount());
		assertEquals(0, g.getNode(qg.getSourceVertex()).getOutCount());
		assertEquals(1, g.getNode(qg.getSourceVertex()).getInCount());
		assertEquals(1, g.getNode(qg.getTargetVertex()).getOutCount());
		assertEquals(0, g.getNode(qg.getTargetVertex()).getInCount());
	}
	
	@Test
	public void testRename(){
		CPQ cpq = CPQ.concat(
			CPQ.intersect(
				CPQ.concat(
					CPQ.label(l1),
					CPQ.intersect(
						CPQ.label(l2),
						CPQ.id()
					)
				),
				CPQ.id()
			),
			CPQ.intersect(
				CPQ.label(l1),
				CPQ.id()
			)
		);
		assertEquals("(((a◦(b ∩ id)) ∩ id)◦(a ∩ id))", cpq.toString());
		
		UniqueGraph<Vertex, Predicate> graph = cpq.toQueryGraph().toUniqueGraph();
		assertEquals(1, graph.getNodeCount());
		assertEquals(2, graph.getEdgeCount());
		
		GraphNode<Vertex, Predicate> node = graph.getNodes().get(0);
		for(GraphEdge<Vertex, Predicate> edge : graph.getEdges()){
			assertEquals(node, edge.getSourceNode());
			assertEquals(node, edge.getTargetNode());
		}
		assertIterableEquals(Arrays.asList("a", "b"), graph.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).sorted().collect(Collectors.toList()));
	}
	
	@Test
	public void withInverse(){
		Predicate a = new Predicate(1, "a");
		
		CPQ q = CPQ.intersect(CPQ.concat(CPQ.label(a), CPQ.label(a.getInverse())), CPQ.IDENTITY);
		assertEquals("((a◦a⁻) ∩ id)", q.toString());
		
		UniqueGraph<Vertex, Predicate> g = q.toQueryGraph().toUniqueGraph();
		assertEquals(2, g.getNodeCount());
		assertEquals(1, g.getEdgeCount());
		
		GraphEdge<Vertex, Predicate> edge = g.getEdges().get(0);
		assertEquals("a", edge.getData().getAlias());
		assertFalse(edge.getSourceNode().equals(edge.getTargetNode()));
	}
	
	@Test
	public void incidenceGraph(){
		Predicate l1 = new Predicate(1, "1");
		Predicate l2 = new Predicate(2, "2");
		Predicate l3 = new Predicate(3, "3");
		Predicate l4 = new Predicate(4, "4");
		
		SimpleGraph<QueryGraphComponent, Void> icGraph = CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.labels(l2, l4), CPQ.labels(l3, l2))).toQueryGraph().toIncidenceGraph();
		
		assertEquals(10, icGraph.getVertexCount());
		assertEquals(10, icGraph.getEdgeCount());
		
		int e = 0;
		int v = 0;
		for(SimpleVertex<QueryGraphComponent, Void> obj : icGraph.getVertices()){
			if(obj.getData() instanceof Edge){
				e++;
				assertEquals(2, obj.getDegree());
			}else if(obj.getData() instanceof Vertex){
				v++;
				assertTrue(obj.getDegree() <= 3);
			}else{
				assert false : "Invalid object type";
			}
		}
		assertEquals(5, e);
		assertEquals(5, v);
		
		for(SimpleEdge<QueryGraphComponent, Void> edge : icGraph.getEdges()){
			assertTrue(edge.getFirstVertex().getData() instanceof Edge);
			assertTrue(edge.getSecondVertex().getData() instanceof Vertex);
		}
	}
	
	@Test
	public void homomorphism0(){
		CPQ q = CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.labels(l2, l4), CPQ.labels(l3, l2)));
		assertTrue(isHomomorphic(q, q));
	}
	
	@Test
	public void homomorphism1(){
		CPQ q = CPQ.concat(CPQ.label(l2), CPQ.intersect(CPQ.labels(l2, l2), CPQ.labels(l2, l2)));
		assertTrue(isHomomorphic(q, q));
	}
	
	@Test
	public void homomorphism2(){
		assertTrue(isHomomorphic(
			CPQ.concat(CPQ.label(l2), CPQ.intersect(CPQ.labels(l2, l2), CPQ.labels(l2, l2))),
			CPQ.labels(l2, l2, l2)
		));
	}
	
	@Test
	public void homomorphism3(){
		assertFalse(isHomomorphic(
			CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.labels(l2, l4), CPQ.labels(l3, l2))),
			CPQ.labels(l1, l2, l4)
		));
	}
	
	@Test
	public void homomorphism4(){
		assertTrue(isHomomorphic(
			CPQ.labels(l1, l2, l4),
			CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.labels(l2, l4), CPQ.labels(l3, l2)))
		));
	}
	
	@Test
	public void homomorphism5(){
		assertFalse(isHomomorphic(
			CPQ.concat(
				CPQ.label(l1),
				CPQ.intersect(
					CPQ.concat(
						CPQ.label(l2),
						CPQ.intersect(CPQ.label(l2), CPQ.id()),
						CPQ.label(l2)
					),
					CPQ.id()
				)
			),
			CPQ.concat(
				CPQ.label(l1),
				CPQ.intersect(
					CPQ.labels(l2, l2, l2),
					CPQ.id()
				)
			)
		));
	}
	
	@Test
	public void homomorphism6(){
		assertTrue(isHomomorphic(
			CPQ.concat(
				CPQ.label(l1),
				CPQ.intersect(
					CPQ.labels(l2, l2, l2),
					CPQ.id()
				)
			),
			CPQ.concat(
				CPQ.label(l1),
				CPQ.intersect(
					CPQ.concat(
						CPQ.label(l2),
						CPQ.intersect(CPQ.label(l2), CPQ.id()),
						CPQ.label(l2)
					),
					CPQ.id()
				)
			)
		));
	}
	
	@Test
	public void homomorphism7(){
		assertFalse(isHomomorphic(
			CPQ.intersect(CPQ.labels(l1, l1, l1), CPQ.id()),
			CPQ.intersect(CPQ.labels(l1, l1, l1, l1, l1), CPQ.id())
		));
	}
	
	@Test
	public void homomorphism8(){
		assertFalse(isHomomorphic(
			CPQ.intersect(CPQ.labels(l1, l1, l1, l1, l1), CPQ.id()),
			CPQ.intersect(CPQ.labels(l1, l1, l1), CPQ.id())
		));
	}
	
	@Test
	public void homomorphism9(){
		//would be true if the source and target matched up
		assertFalse(isHomomorphic(
			CPQ.intersect(
				CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.label(l1.getInverse()), CPQ.label(l2))),
				CPQ.labels(l3, l3.getInverse())
			),
			CPQ.concat(CPQ.intersect(l1.getInverse(), l2), CPQ.label(l3))
		));
	}
	
	@Test
	public void homomorphism10(){
		//would be true if the source and target matched up
		assertFalse(isHomomorphic(
			CPQ.concat(CPQ.intersect(l1.getInverse(), l2), CPQ.label(l3)),
			CPQ.intersect(
				CPQ.concat(CPQ.label(l1), CPQ.intersect(CPQ.label(l1.getInverse()), CPQ.label(l2))),
				CPQ.labels(l3, l3.getInverse())
			)
		));
	}
	
	@Test
	public void homomorphism11(){
		assertFalse(isHomomorphic(
			CPQ.parse("(0◦(((1◦0) ∩ (1◦1))◦1⁻))"),
			CPQ.parse("(0◦((1◦0)◦1⁻))")
		));
	}
	
	public static void main(String[] args){
		new QueryGraphCPQTest().core0();
	}
	
	@Test
	public void core0(){
		QueryGraphCPQ g = CPQ.intersect(CPQ.labels(l1, l2), CPQ.labels(l1, l2)).toQueryGraph();
		
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(3, core.getNodeCount());
		assertEquals(2, core.getEdgeCount());
		assertEquals(3, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
		
		//src -a-> n -b-> trg
		GraphNode<Vertex, Predicate> node = core.getNode(g.getSourceVertex());
		assertEquals(g.getSourceVertex(), node.getData());
		assertNotEquals(g.getTargetVertex(), node.getData());
		assertEquals(0, node.getInCount());
		assertEquals(1, node.getOutCount());
		assertEquals(l1, node.getOutEdges().iterator().next().getData());
		
		node = node.getOutEdges().iterator().next().getTargetNode();
		assertNotEquals(g.getSourceVertex(), node.getData());
		assertNotEquals(g.getTargetVertex(), node.getData());
		assertEquals(1, node.getInCount());
		assertEquals(1, node.getOutCount());
		assertEquals(l1, node.getInEdges().iterator().next().getData());
		assertEquals(l2, node.getOutEdges().iterator().next().getData());
		
		node = node.getOutEdges().iterator().next().getTargetNode();
		assertNotEquals(g.getSourceVertex(), node.getData());
		assertEquals(g.getTargetVertex(), node.getData());
		assertEquals(1, node.getInCount());
		assertEquals(0, node.getOutCount());
		assertEquals(l2, node.getInEdges().iterator().next().getData());
	}
	
	@Test
	public void core1(){
		QueryGraphCPQ g = CPQ.parse("((0 ∩ 1) ◦ (0⁻ ∩ ((1 ∩ (0 ◦ 0⁻ ◦ 1)) ◦ (1 ∩ id) ◦ 0)))").toQueryGraph();
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(5, core.getNodeCount());
		assertEquals(7, core.getEdgeCount());
		assertEquals(4, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("0"::equals).count());
		assertEquals(3, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("1"::equals).count());
		assertEquals(5, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
	}
	
	@Test
	public void core2(){
		QueryGraphCPQ g = CPQ.parse("(((0 ∩ 1) ◦ 1) ∩ (1 ◦ 1⁻) ∩ 1 ∩ 0 ∩ id)").toQueryGraph();
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(2, core.getNodeCount());
		assertEquals(5, core.getEdgeCount());
		assertEquals(2, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("0"::equals).count());
		assertEquals(3, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("1"::equals).count());
		assertEquals(g.getSourceVertex(), core.getNode(g.getTargetVertex()).getData());
		assertEquals(g.getTargetVertex(), core.getNode(g.getSourceVertex()).getData());
		assertEquals(4, core.getNode(g.getSourceVertex()).getOutCount());
		assertEquals(3, core.getNode(g.getSourceVertex()).getInCount());
		assertEquals(2, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
	}
	
	@Test
	public void core3(){
		QueryGraphCPQ g = CPQ.parse("((0◦(((0◦0⁻) ∩ ((1◦1) ∩ (1⁻ ∩ id)))◦1⁻))◦(1⁻◦(0⁻◦1)))").toQueryGraph();
		
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(7, core.getNodeCount());
		assertEquals(7, core.getEdgeCount());
		assertEquals(3, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("0"::equals).count());
		assertEquals(4, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("1"::equals).count());
		assertEquals(7, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
	}
	
	@Test
	public void core4(){
		QueryGraphCPQ g = CPQ.parse("((0◦0◦0◦0◦0◦0◦0) ∩ (0◦0◦0◦0◦0◦0◦0))").toQueryGraph();
		
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(8, core.getNodeCount());
		assertEquals(7, core.getEdgeCount());
		assertEquals(7, core.getEdges().stream().map(GraphEdge::getData).map(Predicate::getAlias).filter("0"::equals).count());
		assertEquals(1, core.getNode(g.getSourceVertex()).getOutCount());
		assertEquals(0, core.getNode(g.getSourceVertex()).getInCount());
		assertEquals(0, core.getNode(g.getTargetVertex()).getOutCount());
		assertEquals(1, core.getNode(g.getTargetVertex()).getInCount());
		assertEquals(8, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
		for(GraphNode<Vertex, Predicate> node : core.getNodes()){
			if(node.getData() != g.getSourceVertex() && node.getData() != g.getTargetVertex()){
				assertEquals(1, node.getOutCount());
				assertEquals(1, node.getInCount());
			}
		}
	}
	
	@Test
	public void core5(){
		QueryGraphCPQ g = CPQ.parse("(((0 ◦ 0) ∩ id) ◦ 0⁻ ◦ 0)").toQueryGraph();
		
		UniqueGraph<Vertex, Predicate> core = g.computeCore();
		assertEquals(4, core.getNodeCount());
		assertEquals(4, core.getEdgeCount());
		assertEquals(1, core.getNode(g.getSourceVertex()).getOutCount());
		assertEquals(2, core.getNode(g.getSourceVertex()).getInCount());
		assertEquals(0, core.getNode(g.getTargetVertex()).getOutCount());
		assertEquals(1, core.getNode(g.getTargetVertex()).getInCount());
		assertEquals(4, countNodes(core.getNode(g.getSourceVertex()), new HashSet<Vertex>()));
	}
	
	private boolean isHomomorphic(CPQ cpq1, CPQ cpq2){
		Vertex s = new Vertex();
		Vertex t = new Vertex();
		return cpq1.toQueryGraph(s, t).isHomomorphicTo(cpq2.toQueryGraph(s, t).toUniqueGraph());
	}
	
	private int countNodes(GraphNode<Vertex, Predicate> n, Set<Vertex> found){
		found.add(n.getData());
		int count = 1;
		
		for(GraphEdge<Vertex, Predicate> o : n.getInEdges()){
			if(!found.contains(o.getSource())){
				count += countNodes(o.getSourceNode(), found);
			}
		}
		
		for(GraphEdge<Vertex, Predicate> o : n.getOutEdges()){
			if(!found.contains(o.getTarget())){
				count += countNodes(o.getTargetNode(), found);
			}
		}
		
		return count;
	}
}
