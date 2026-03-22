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
package dev.roanh.gmark.lang.cpq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.cq.AtomCQ;
import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.lang.cq.QueryGraphCQ;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

public class ParserCPQTest{
	private static final Predicate l1 = new Predicate(1, "1");
	private static final Predicate l2 = new Predicate(2, "2");
	private static final Predicate l3 = new Predicate(3, "3");

	@Test
	public void parse0(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", CPQ.parse("(0◦(((1◦0) ∩ (1◦1))◦1⁻))").toString());
	}
	
	@Test
	public void parse1(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", CPQ.parse("0◦(((1◦0) ∩ (1◦1))◦1⁻)").toString());
	}
	
	@Test
	public void parse2(){
		assertEquals("(a ∩ b ∩ c)", CPQ.parse("a∩b∩c").toString());
	}
	
	@Test
	public void parse3(){
		assertThrows(IllegalArgumentException.class, ()->CPQ.parse("(a ∩ b ∩ c"));
	}
	
	@Test
	public void parse4(){
		assertThrows(IllegalArgumentException.class, ()->CPQ.parse("a ∩ b ∩ c)"));
	}
	
	@Test
	public void parse5(){
		assertEquals(CPQ.IDENTITY, CPQ.parse("id"));
	}
	
	@Test
	public void parse6(){
		assertEquals("(b◦(a ∩ id))", CPQ.parse("b ◦ (a ∩ id)").toString());
	}
	
	@Test
	public void predicates(){
		assertArrayEquals(
			new int[]{0, 1, 2},
			CPQ.parse("a ∩ b ∩ c").toAbstractSyntaxTree().stream().filter(QueryTree::isLeaf).map(QueryTree::getEdgeAtom).map(EdgeQueryAtom::getLabel).mapToInt(Predicate::getID).distinct().sorted().toArray()
		);
	}
	
	@Test
	public void parseGraphSimpleChain(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		graph.addUniqueNode("4t");
		
		graph.addUniqueEdge("1s", "2", l1);
		graph.addUniqueEdge("2", "3", l2);
		graph.addUniqueEdge("3", "4t", l1);
		
		assertEquivalentCPQ("1 ◦ 2 ◦ 1", CPQ.parse(graph, "1s", "4t"));
	}
	
	@Test
	public void parseGraphParallelPaths(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		graph.addUniqueNode("4");
		graph.addUniqueNode("5");
		graph.addUniqueNode("6t");
		
		graph.addUniqueEdge("1s", "2", l1);
		graph.addUniqueEdge("2", "3", l2);
		graph.addUniqueEdge("2", "5", l1);
		graph.addUniqueEdge("3", "4", l1);
		graph.addUniqueEdge("3", "4", l2);
		graph.addUniqueEdge("3", "4", l3);
		graph.addUniqueEdge("4", "5", l2);
		graph.addUniqueEdge("5", "6t", l1);
		
		assertEquivalentCPQ("1 ◦ (1 ∩ (2 ◦ (1 ∩ 2 ∩ 3) ◦ 2)) ◦ 1", CPQ.parse(graph, "1s", "6t"));
	}
	
	@Test
	public void parseGraphParallelPathsAndInverse(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		graph.addUniqueNode("4");
		graph.addUniqueNode("5");
		graph.addUniqueNode("6");
		graph.addUniqueNode("7t");
		
		graph.addUniqueEdge("1s", "2", l1);
		graph.addUniqueEdge("3", "2", l2);
		graph.addUniqueEdge("5", "2", l1);
		graph.addUniqueEdge("3", "4", l1);
		graph.addUniqueEdge("4", "3", l2);
		graph.addUniqueEdge("3", "4", l3);
		graph.addUniqueEdge("4", "6", l2);
		graph.addUniqueEdge("6", "5", l3);
		graph.addUniqueEdge("7t", "5", l1);
		
		assertEquivalentCPQ("1 ◦ (1⁻ ∩ (2⁻ ◦ (1 ∩ 2⁻ ∩ 3) ◦ 2 ◦ 3)) ◦ 1⁻", CPQ.parse(graph, "1s", "7t"));
	}
	
	@Test
	public void parseGraphSimpleSelfLoop(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1st");
		graph.addUniqueEdge("1st", "1st", l1);
		
		assertEquivalentCPQ("1 ∩ id", CPQ.parse(graph, "1st", "1st"));
	}
	
	@Test
	public void parseGraphInnerLoop(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3t");
		
		graph.addUniqueEdge("1s", "2", l2);
		graph.addUniqueEdge("2", "2", l1);
		graph.addUniqueEdge("2", "3t", l3);
		
		assertEquivalentCPQ("2 ◦ (1 ∩ id) ◦ 3", CPQ.parse(graph, "1s", "3t"));
	}
	
	@Test
	public void parseGraphNestedLoops(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3t");
		graph.addUniqueNode("4");
		graph.addUniqueNode("5");
		
		graph.addUniqueEdge("1s", "2", l1);
		graph.addUniqueEdge("2", "2", l1);
		graph.addUniqueEdge("3t", "2", l1);
		graph.addUniqueEdge("4", "2", l2);
		graph.addUniqueEdge("5", "2", l3);
		graph.addUniqueEdge("4", "5", l2);
		graph.addUniqueEdge("4", "4", l1);
		graph.addUniqueEdge("4", "4", l2);
		graph.addUniqueEdge("4", "4", l3);
		
		assertEquivalentCPQ("1 ◦ (1⁻ ∩ (2⁻ ◦ (1 ∩ 2⁻ ∩ 3 ∩ id) ◦ 2 ◦ 3) ∩ id) ◦ 1⁻", CPQ.parse(graph, "1s", "3t"));
	}
	
	@Test
	public void parseGraphReturnLoop(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1st");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		
		graph.addUniqueEdge("1st", "2", l1);
		graph.addUniqueEdge("2", "3", l2);
		graph.addUniqueEdge("3", "1st", l3);
		
		assertEquivalentCPQ("(1 ◦ 2 ◦ 3) ∩ id", CPQ.parse(graph, "1st", "1st"));
	}
	
	@Test
	public void parseGraphReturnLoopEdge(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1st");
		graph.addUniqueNode("2");
		
		graph.addUniqueEdge("1st", "2", l1);
		graph.addUniqueEdge("2", "1st", l3);
		
		assertEquivalentCPQ("(1 ◦ 3) ∩ id", CPQ.parse(graph, "1st", "1st"));
	}
	
	@Test
	public void parseGraphSelfReturnLoop1(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1st");
		graph.addUniqueNode("2");
		
		graph.addUniqueEdge("1st", "2", l3);
		
		assertEquivalentCPQ("(id ∩ (3◦3⁻))", CPQ.parse(graph, "1st", "1st"));
	}
	
	@Test
	public void parseGraphSelfReturnLoop2(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1st");
		graph.addUniqueNode("2");
		
		graph.addUniqueEdge("2", "1st", l3);
		
		assertEquivalentCPQ("(id ∩ (3⁻◦3))", CPQ.parse(graph, "1st", "1st"));
	}
	
	@Test
	public void parseGraphSelfReturnLoop3(){
		CPQ q = CPQ.parse("(id ∩ (3◦3⁻))", List.of(l1, l2, l3));
		assertEquivalentCPQ(q, q.toQueryGraph().toCPQ());
	}
	
	@Test
	public void parseGraphNotCPQ(){
		QueryGraphCQ q = CQ.parse("(src, trg) ← 1(src, b1), 1(b1, trg), 2(src, b2), 2(b2, trg), 3(b1, b2)").toQueryGraph();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()->CPQ.parse(q.toUniqueGraph().copy(Function.identity(), AtomCQ::getLabel), q.getVariable("src"), q.getVariable("trg")));
		assertEquals("The given input graph does not represent a valid CPQ.", e.getMessage());
	}
	
	@Test
	public void parseGraphCQ(){
		QueryGraphCQ q = CQ.parse("(src, trg) ← 1(src, b1), 1(b1, trg), 2(src, b2), 2(b2, trg)").toQueryGraph();
		assertEquivalentCPQ("((2◦2) ∩ (1◦1))", CPQ.parse(q.toUniqueGraph().copy(Function.identity(), AtomCQ::getLabel), q.getVariable("src"), q.getVariable("trg")));
	}
	
	
	@Test
	public void parseGraph10(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();

		graph.addUniqueNode("1s");
		graph.addUniqueNode("2");
		graph.addUniqueNode("3");
		graph.addUniqueNode("4");
		graph.addUniqueNode("5");
		graph.addUniqueNode("6");
		graph.addUniqueNode("7");
		graph.addUniqueNode("8");
		graph.addUniqueNode("9");
		graph.addUniqueNode("10");
		graph.addUniqueNode("11");
		graph.addUniqueNode("12");
		graph.addUniqueNode("13");
		graph.addUniqueNode("14");
		graph.addUniqueNode("15t");

		graph.addUniqueEdge("1s", "1s", l1);
		graph.addUniqueEdge("1s", "2", l1);
		graph.addUniqueEdge("1s", "3", l1);
		graph.addUniqueEdge("1s", "4", l1);
		graph.addUniqueEdge("2", "8", l1);
		graph.addUniqueEdge("2", "9", l1);
		graph.addUniqueEdge("3", "11", l1);
		graph.addUniqueEdge("4", "5", l1);
		graph.addUniqueEdge("4", "5", l2);
		graph.addUniqueEdge("5", "15t", l1);
		graph.addUniqueEdge("6", "7", l1);
		graph.addUniqueEdge("6", "8", l1);
		graph.addUniqueEdge("7", "8", l1);
		graph.addUniqueEdge("8", "10", l1);
		graph.addUniqueEdge("8", "10", l2);
		graph.addUniqueEdge("9", "10", l1);
		graph.addUniqueEdge("10", "15t", l1);
		graph.addUniqueEdge("11", "14", l1);
		graph.addUniqueEdge("11", "15t", l1);
		graph.addUniqueEdge("12", "13", l1);
		graph.addUniqueEdge("12", "14", l1);
		graph.addUniqueEdge("13", "13", l1);
		graph.addUniqueEdge("13", "14", l1);
		graph.addUniqueEdge("13", "14", l2);
		graph.addUniqueEdge("14", "14", l1);
		graph.addUniqueEdge("15t", "15t", l1);

		//       6 --- 7
		//        \   /
		//         \ / /---\
		//      --- 8 /--  |
		//     /         \ |
		//    2 --- 9 --- 10
		//   /              \
		//  /                \
		// 1s (loop)  (loop) /15t--\
		// |\               /      |
		// | \3 --------- 11       |
		// |               \       |
		// 4                \ (lp) |
		// |\          12 -- 14    |
		// | |           \  //     |
		// |/             \//      |
		// 5       (loop) 13       |
		// |                       |
		// \-----------------------/


		
		
		
		ParserCPQ.parse(graph, "1s", "15t");
		
		
		
		//TODO

	}
	
	@RepeatedTest(1000)
	public void parseGraphRandom(){
		CPQ q = GeneratorCPQ.generatePlainCPQ(50, List.of(l1, l2, l3));
		assertEquivalentCPQ(q, q.toQueryGraph().toCPQ());
	}
	
	//TODO recog test with top level loop

	//TODO random -> graph -> parse -> equals
	
	private static void assertEquivalentCPQ(String expected, CPQ actual){
		assertEquivalentCPQ(CPQ.parse(expected, List.of(l1, l2, l3)), actual);
	}
		
	private static void assertEquivalentCPQ(CPQ expected, CPQ actual){
		assertTrue(expected.isHomomorphicTo(actual), expected + " vs " + actual);
		assertTrue(actual.isHomomorphicTo(expected), expected + " vs " + actual);
	}
}
