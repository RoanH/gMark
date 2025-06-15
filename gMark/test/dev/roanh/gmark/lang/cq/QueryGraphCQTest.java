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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

public class QueryGraphCQTest{
	private static final Predicate l0 = new Predicate(0, "zero");
	private static final Predicate l1 = new Predicate(1, "one");

	@Test
	public void cqToQueryGraph(){
		QueryGraphCQ queryGraph = CQ.parse("(f1, f2) ← zero(b2, b2), one(f1, b2), one(f2, b1)", List.of(l0, l1)).toQueryGraph();
		UniqueGraph<VarCQ, AtomCQ> graph = queryGraph.toUniqueGraph();
		
		assertEquals("(f1, f2) ← one(f1, b2), one(f2, b1), zero(b2, b2)", queryGraph.toString());
		assertEquals(3, queryGraph.getEdgeCount());
		assertEquals(4, queryGraph.getVertexCount());
		assertEquals(3, graph.getEdgeCount());
		assertEquals(4, graph.getNodeCount());
		
		List<VarCQ> variables = graph.getNodes().stream().map(GraphNode::getData).sorted(Comparator.comparing(VarCQ::getName)).toList();
		assertEquals("b1", variables.get(0).getName());
		assertEquals("b2", variables.get(1).getName());
		assertEquals("f1", variables.get(2).getName());
		assertEquals("f2", variables.get(3).getName());

		List<AtomCQ> atoms = graph.getEdges().stream().map(GraphEdge::getData).sorted(Comparator.comparing(e->e.getSource().getName())).toList();
		
		assertEquals(variables.get(1), atoms.get(0).getSource());
		assertEquals(l0, atoms.get(0).getLabel());
		assertEquals(variables.get(1), atoms.get(0).getTarget());
		
		assertEquals(variables.get(2), atoms.get(1).getSource());
		assertEquals(l1, atoms.get(1).getLabel());
		assertEquals(variables.get(1), atoms.get(1).getTarget());
		
		assertEquals(variables.get(3), atoms.get(2).getSource());
		assertEquals(l1, atoms.get(2).getLabel());
		assertEquals(variables.get(0), atoms.get(2).getTarget());
	}
	
	@Test
	public void queryGraphToCQ(){
		String query = "(f1, f2) ← one(f1, b2), one(f2, b2), zero(b2, b2)";
		assertEquals(query, CQ.parse(query).toQueryGraph().toCQ().toFormalSyntax());
	}
}
