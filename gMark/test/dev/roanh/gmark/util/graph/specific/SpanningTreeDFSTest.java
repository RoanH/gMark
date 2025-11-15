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

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

public class SpanningTreeDFSTest{
	
	@Test
	public void articulationPointsSimple(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();
		
		//     a
		//    / \
		//  (1) (2)
		//  /     \
		// b       c
		//  \     /
		//  (3) (4)
		//    \ /
		//     d
		//    / \
		//  (5) (6)
		//  /     \
		// e ----- f
		//    (7)
		
		graph.addUniqueNode("a");
		graph.addUniqueNode("b");
		graph.addUniqueNode("c");
		graph.addUniqueNode("d");
		graph.addUniqueNode("e");
		graph.addUniqueNode("f");
		
		graph.addUniqueEdge("a", "b", 1);
		graph.addUniqueEdge("a", "c", 2);
		graph.addUniqueEdge("b", "d", 3);
		graph.addUniqueEdge("c", "d", 4);
		graph.addUniqueEdge("d", "e", 5);
		graph.addUniqueEdge("d", "f", 6);
		graph.addUniqueEdge("e", "f", 7);
		
		List<String> points = Util.computeArticulationPoints(graph).stream().map(GraphNode::getData).sorted().toList();
		assertIterableEquals(List.of("d"), points);
	}
	
	@Test
	public void articulationPointsComplex(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();
		
		graph.addUniqueNode("1");
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
		graph.addUniqueNode("15");

		//note: the graph will be treated as undirected anyway so edge direction isn't relevant
		graph.addUniqueEdge("1", "2", 1);
		graph.addUniqueEdge("1", "3", 1);
		graph.addUniqueEdge("1", "4", 1);
		graph.addUniqueEdge("2", "8", 1);
		graph.addUniqueEdge("2", "9", 1);
		graph.addUniqueEdge("3", "11", 1);
		graph.addUniqueEdge("4", "5", 1);
		graph.addUniqueEdge("4", "5", 2);
		graph.addUniqueEdge("5", "15", 1);
		graph.addUniqueEdge("6", "7", 1);
		graph.addUniqueEdge("6", "8", 1);
		graph.addUniqueEdge("7", "8", 1);
		graph.addUniqueEdge("8", "10", 1);
		graph.addUniqueEdge("9", "10", 1);
		graph.addUniqueEdge("10", "15", 1);
		graph.addUniqueEdge("11", "12", 1);
		graph.addUniqueEdge("11", "14", 1);
		graph.addUniqueEdge("11", "15", 1);
		graph.addUniqueEdge("12", "13", 1);
		graph.addUniqueEdge("12", "14", 1);
		graph.addUniqueEdge("13", "13", 1);
		graph.addUniqueEdge("13", "14", 1);
		
		//       6 --- 7
		//        \   /
		//         \ /
		//      --- 8 ---
		//     /         \
		//    2 --- 9 --- 10
		//   /|             \
		//  / |              \
		// 1  |              /15 --\
		// |\ |             /      |
		// | \3 --------- 11       |
		// |              /\       |
		// 4             /  \      |
		// |\          12 -- 14    |
		// | |           \  /      |
		// |/             \/       |
		// 5       (loop) 13       |
		// |                       |
		// \-----------------------/
		
		List<String> points = Util.computeArticulationPoints(graph).stream().map(GraphNode::getData).sorted().toList();
		assertIterableEquals(List.of("11", "8"), points);
	}
	
	@Test
	public void articulationPointsRoot(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();
		
		// b --- c
		//  \   /
		//   \ /
		//    a
		//   / \
		//  /   \
		// e --- d
		
		graph.addUniqueNode("a");//root
		graph.addUniqueNode("b");
		graph.addUniqueNode("c");
		graph.addUniqueNode("d");
		graph.addUniqueNode("e");
		
		graph.addUniqueEdge("a", "b", 1);
		graph.addUniqueEdge("a", "c", 1);
		graph.addUniqueEdge("a", "e", 1);
		graph.addUniqueEdge("a", "d", 1);
		graph.addUniqueEdge("b", "c", 1);
		graph.addUniqueEdge("e", "d", 1);
		
		List<String> points = Util.computeArticulationPoints(graph).stream().map(GraphNode::getData).sorted().toList();
		assertIterableEquals(List.of("a"), points);
	}
	
	@Test
	public void articulationPointsNotConnected(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();
		
		// b --- c
		//
		// e --- d
		
		graph.addUniqueNode("b");
		graph.addUniqueNode("c");
		graph.addUniqueNode("d");
		graph.addUniqueNode("e");
		
		graph.addUniqueEdge("b", "c", 1);
		graph.addUniqueEdge("e", "d", 1);
		
		assertThrows(IllegalArgumentException.class, ()->Util.computeArticulationPoints(graph));
	}
	
	@Test
	public void articulationPointsEmptyGraph(){
		UniqueGraph<String, Integer> graph = new UniqueGraph<String, Integer>();
		assertTrue(Util.computeArticulationPoints(graph).isEmpty());
	}
}
