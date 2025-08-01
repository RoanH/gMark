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
package dev.roanh.gmark.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.QueryGraphComponent;
import dev.roanh.gmark.lang.cq.AtomCQ;
import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.lang.cq.VarCQ;
import dev.roanh.gmark.type.IDable;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.SimpleGraph;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.graph.generic.Tree;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

public class UtilTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");
	private static final Predicate c = new Predicate(2, "c");
	private static final Predicate d = new Predicate(3, "d");
	private static final Predicate e = new Predicate(4, "e");
	private static final Predicate f = new Predicate(5, "f");
	private static final Predicate g = new Predicate(6, "g");
	private static final Predicate h = new Predicate(7, "h");

	@Test
	public void matching0(){
		SimpleGraph<Predicate, Void> graph = new SimpleGraph<Predicate, Void>(4);
		
		graph.addVertex(a);
		graph.addVertex(b);
		graph.addVertex(c);
		graph.addVertex(d);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, a);
		
		List<SimpleEdge<Predicate, Void>> matching = Util.findMaximalMatching(graph);
		assertEquals(2, matching.size());
	}
	
	@Test
	public void decomp0(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(5);
		
		List<Predicate> vertices = Arrays.asList(a, b, c, d, e);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(a, c);
		graph.addEdge(c, e);
		graph.addEdge(e, d);
		graph.addEdge(d, b);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp1(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(5);
		
		List<Predicate> vertices = Arrays.asList(a, b, c, d, e);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, e);
		graph.addEdge(e, d);
		graph.addEdge(d, b);
		graph.addEdge(b, a);
		graph.addEdge(c, b);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp2(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(8);
		
		List<Predicate> vertices = Arrays.asList(a, b, d, e, f, g, h, c);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, e);
		graph.addEdge(a, b);
		graph.addEdge(b, e);
		graph.addEdge(a, h);
		graph.addEdge(a, c);
		graph.addEdge(a, d);
		graph.addEdge(e, f);
		graph.addEdge(b, g);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp3(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(6);
		
		List<Predicate> vertices = Arrays.asList(a, b, c, d, e, f);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, e);
		graph.addEdge(e, f);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp4(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(6);
		
		List<Predicate> vertices = Arrays.asList(a, b, c, d, e, f);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(a, c);
		graph.addEdge(c, d);
		graph.addEdge(e, b);
		graph.addEdge(d, e);
		graph.addEdge(f, c);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp5(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(8);
		
		List<Predicate> vertices = Arrays.asList(a, b, c, d, e, f, g, h);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, e);
		graph.addEdge(e, f);
		graph.addEdge(f, g);
		graph.addEdge(g, h);
		graph.addEdge(h, a);
		graph.addEdge(f, a);
		graph.addEdge(a, e);
		graph.addEdge(e, b);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp6(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(6);
		
		Arrays.asList(a, b, c, d, e, f).forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(d, e);
		graph.addEdge(e, f);
		
		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(graph));
	}
	
	@Test
	public void decomp7(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(4);
		
		Arrays.asList(a, b, c, d).forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(a, c);
		graph.addEdge(a, d);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, d);

		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(graph));
	}
	
	@Test
	public void decomp8(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(5);
		
		Arrays.asList(a, b, c, d, e).forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(a, c);
		graph.addEdge(a, d);
		graph.addEdge(b, c);
		graph.addEdge(b, d);
		graph.addEdge(c, d);
		graph.addEdge(d, e);

		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(graph));
	}
	
	@Test
	public void decomp9(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(6);
		
		List<Predicate> vertices = Arrays.asList(a, c, b, d, e, f);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		graph.addEdge(b, c);
		graph.addEdge(c, d);
		graph.addEdge(d, e);
		graph.addEdge(e, f);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp10(){
		SimpleGraph<Predicate, List<Tree<List<Predicate>>>> graph = new SimpleGraph<Predicate, List<Tree<List<Predicate>>>>(2);
		
		List<Predicate> vertices = Arrays.asList(a, b);
		vertices.forEach(graph::addVertex);
		
		graph.addEdge(a, b);
		List<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>> edges = new ArrayList<SimpleEdge<Predicate, List<Tree<List<Predicate>>>>>(graph.getEdges());
		
		Tree<List<Predicate>> decomp = Util.computeTreeDecompositionWidth2(graph);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp11(){
		SimpleGraph<QueryGraphComponent, List<Tree<List<QueryGraphComponent>>>> graph = CPQ.parse("((0⁻ ∩ 0)◦(0⁻◦0))").toQueryGraph().toIncidenceGraph();
		List<QueryGraphComponent> vertices = graph.getVertices().stream().map(SimpleVertex::getData).toList();
		List<SimpleEdge<QueryGraphComponent, List<Tree<List<QueryGraphComponent>>>>> edges = new ArrayList<SimpleEdge<QueryGraphComponent, List<Tree<List<QueryGraphComponent>>>>>(graph.getEdges());
		assertValidTreeDecomposition(
			Util.computeTreeDecompositionWidth2(graph),
			vertices,
			edges
		);
	}
	
	@Test
	public void testCartesian0(){
		List<List<String>> prod = Util.cartesianProduct(Arrays.asList(
			Arrays.asList("a", "b"),
			Arrays.asList("c", "d"),
			Arrays.asList("e")
		));
		
		assertEquals(4, prod.size());
		
		assertIterableEquals(Arrays.asList("a", "c", "e"), prod.get(0));
		assertIterableEquals(Arrays.asList("a", "d", "e"), prod.get(1));
		assertIterableEquals(Arrays.asList("b", "c", "e"), prod.get(2));
		assertIterableEquals(Arrays.asList("b", "d", "e"), prod.get(3));
	}
	
	@Test
	public void testCartesian1(){
		List<List<String>> prod = Util.cartesianProduct(Arrays.asList(
			Arrays.asList("0"),
			Arrays.asList("a", "b", "c"),
			Arrays.asList("d", "e"),
			Arrays.asList("f", "g")
		));
		
		assertEquals(12, prod.size());
		
		assertIterableEquals(Arrays.asList("0", "a", "d", "f"), prod.get(0));
		assertIterableEquals(Arrays.asList("0", "a", "d", "g"), prod.get(1));
		assertIterableEquals(Arrays.asList("0", "a", "e", "f"), prod.get(2));
		assertIterableEquals(Arrays.asList("0", "a", "e", "g"), prod.get(3));
		assertIterableEquals(Arrays.asList("0", "b", "d", "f"), prod.get(4));
		assertIterableEquals(Arrays.asList("0", "b", "d", "g"), prod.get(5));
		assertIterableEquals(Arrays.asList("0", "b", "e", "f"), prod.get(6));
		assertIterableEquals(Arrays.asList("0", "b", "e", "g"), prod.get(7));
		assertIterableEquals(Arrays.asList("0", "c", "d", "f"), prod.get(8));
		assertIterableEquals(Arrays.asList("0", "c", "d", "g"), prod.get(9));
		assertIterableEquals(Arrays.asList("0", "c", "e", "f"), prod.get(10));
		assertIterableEquals(Arrays.asList("0", "c", "e", "g"), prod.get(11));
	}
	
	@Test
	public void testCartesian2(){
		List<List<String>> prod = Util.cartesianProduct(Arrays.asList(
			Arrays.asList(),
			Arrays.asList("a", "b", "c"),
			Arrays.asList("d", "e"),
			Arrays.asList("f", "g")
		));
		
		assertEquals(0, prod.size());
	}
	
	@Test
	public void testSubsets0(){
		List<String> items = Arrays.asList("a", "b", "c", "d");
		Set<Set<String>> found = new HashSet<Set<String>>();
		Util.computeAllSubsets(items, set->found.add(new HashSet<String>(set)));
		
		assertEquals(4 + 6 + 4 + 1, found.size());
		
		assertFalse(found.contains(new HashSet<String>()));
		for(Set<String> set : found){
			assertTrue(set.size() >= 1);
			assertTrue(set.size() <= 4);
			System.out.println(set);
		}
		
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d"))));
		
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "c"))));
		
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "b", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "b", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "c", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "c", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "d", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "d", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "a", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "a", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "c", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "c", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "d", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "d", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "a", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "a", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "b", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "b", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "d", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "d", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "a", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "a", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "b", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "b", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "c", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "c", "b"))));
		
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "b", "c", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "b", "d", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "c", "b", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "c", "d", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "d", "b", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("a", "d", "c", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "a", "c", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "a", "d", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "c", "a", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "c", "d", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "d", "a", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("b", "d", "c", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "a", "b", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "a", "d", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "b", "a", "d"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "b", "d", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "d", "a", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("c", "d", "b", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "a", "b", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "a", "c", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "b", "a", "c"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "b", "c", "a"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "c", "a", "b"))));
		assertTrue(found.contains(new HashSet<String>(Arrays.asList("d", "c", "b", "a"))));
	}
	
	@Test
	public void testSubsets1(){
		Util.computeAllSubsets(new ArrayList<String>(), s->fail());
	}
	
	@Test
	public void testSubsets2(){
		List<String> items = Arrays.asList("a");
		List<List<String>> found = new ArrayList<List<String>>();
		Util.computeAllSubsets(items, set->found.add(new ArrayList<String>(set)));
		
		assertEquals(1, found.size());
		assertEquals(1, found.get(0).size());
		assertEquals("a", found.get(0).get(0));
	}
	
	@Test
	public void isEmpty0() throws IOException{
		assertTrue(Util.isEmpty(Files.createTempDirectory(null)));
	}
	
	@Test
	public void isEmpty1() throws IOException{
		assertFalse(Util.isEmpty(Files.createTempFile(null, null).getParent()));
	}
	
	@Test
	public void splitOnNodes0(){
		CQ query = CQ.parse("(f1) ← b(f1, b2), a(b1, f1), a(b3, b2)");
		
		//b1 --a-> f1 --b-> b2 <-a-- b3
		//comp 1    | comp 2
		
		List<UniqueGraph<VarCQ, AtomCQ>> components = Util.splitOnNodes(query.toQueryGraph().toUniqueGraph(), query.getFreeVariables());
		assertEquals(2, components.size());
		
		components.sort(Comparator.comparingInt(UniqueGraph::getNodeCount));
		assertEquals("(f1) ← a(b1, f1)", CQ.of(components.get(0)).toFormalSyntax());
		assertEquals("(f1) ← a(b3, b2), b(f1, b2)", CQ.of(components.get(1)).toFormalSyntax());
	}
	
	@Test
	public void splitOnNodes1(){
		CQ query = CQ.empty();

		VarCQ b1 = query.addBoundVariable("b1");
		VarCQ f1 = query.addFreeVariable("f1");

		query.addAtom(b1, a, f1);

		//b1 --a-> f1
		//comp 1

		List<UniqueGraph<VarCQ, AtomCQ>> components = Util.splitOnNodes(query.toQueryGraph().toUniqueGraph(), query.getFreeVariables());
		assertEquals(1, components.size());
		assertEquals("(f1) ← a(b1, f1)", CQ.of(components.get(0)).toFormalSyntax());
	}

	@Test
	public void splitOnNodes2(){
		CQ query = CQ.empty();

		VarCQ f1 = query.addFreeVariable("f1");
		query.addAtom(f1, a, f1);

		//self loop on f1, one component

		List<UniqueGraph<VarCQ, AtomCQ>> components = Util.splitOnNodes(query.toQueryGraph().toUniqueGraph(), query.getFreeVariables());
		assertEquals(1, components.size());
		assertEquals("(f1) ← a(f1, f1)", CQ.of(components.get(0)).toFormalSyntax());
	}

	@Test
	public void splitOnNodes3(){
		CQ query = CQ.parse("(f1, f2) ← b(b2, b2), a(f1, b2), a(f2, b2), b(b1, f2), a(f1, b1), b(f1, f2), a(f1, f1)");

		//a         b
		//o         o
		//f1 --a-> b2
		//|  \      ^
		//a    b    a
		//v      \v |
		//b1 --b-> f2

		List<UniqueGraph<VarCQ, AtomCQ>> components = Util.splitOnNodes(query.toQueryGraph().toUniqueGraph(), query.getFreeVariables());
		assertEquals(4, components.size());

		components.sort(Comparator.<UniqueGraph<VarCQ, AtomCQ>>comparingInt(UniqueGraph::getEdgeCount).thenComparingInt(UniqueGraph::getNodeCount));
		assertEquals("(f1) ← a(f1, f1)", CQ.of(components.get(0)).toFormalSyntax());
		assertEquals("(f1, f2) ← b(f1, f2)", CQ.of(components.get(1)).toFormalSyntax());
		assertEquals("(f1, f2) ← a(f1, b1), b(b1, f2)", CQ.of(components.get(2)).toFormalSyntax());
		assertEquals("(f1, f2) ← a(f1, b2), a(f2, b2), b(b2, b2)", CQ.of(components.get(3)).toFormalSyntax());
	}
	
	public static <T extends IDable, M> void assertValidTreeDecomposition(Tree<List<T>> decomp, List<T> vertices, List<SimpleEdge<T, M>> edges){
		//1. All vertices are in the decomposition
		Set<T> found = new HashSet<T>();
		decomp.forEach(t->{
			found.addAll(t.getData());
			return false;
		});
		assertEquals(vertices.size(), found.size());
		found.addAll(vertices);
		assertEquals(vertices.size(), found.size());
		
		//2. All edges are in a bag
		outer: for(SimpleEdge<T, M> e : edges){
			for(List<T> bag : decomp.stream().map(Tree::getData).toList()){
				if(bag.contains(e.getFirstVertex().getData()) && bag.contains(e.getSecondVertex().getData())){
					continue outer;
				}
			}
			
			fail("Edge not in a bag");
		}
		
		//3. Check each vertex induces a connected subgraph
		for(T vertex : vertices){
			List<Tree<List<T>>> bags = new ArrayList<Tree<List<T>>>();
			decomp.forEach(t->{
				if(t.getData().contains(vertex)){
					bags.add(t);
				}
				return false;
			});

			Tree<List<T>> root = bags.get(0);
			while(!root.isRoot() && root.getParent().getData().contains(vertex)){
				root = root.getParent();
			}
			
			assertEquals(bags.size(), findVertex(root, vertex));
		}
	}
	
	private static <T> int findVertex(Tree<List<T>> root, T v){
		if(root.getData().contains(v)){
			return 1 + root.getChildren().stream().mapToInt(t->findVertex(t, v)).sum();
		}else{
			return 0;
		}
	}
}
