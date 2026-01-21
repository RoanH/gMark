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

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

public class ParserCPQTest{

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
	public void parseGraph0(){
		UniqueGraph<String, Predicate> graph = new UniqueGraph<String, Predicate>();
		Predicate l1 = new Predicate(1, "1");
		Predicate l2 = new Predicate(2, "2");

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
	
	//TODO recog test with top level loop

	//TODO random -> graph -> parse -> equals
}
