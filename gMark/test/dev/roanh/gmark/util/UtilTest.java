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
package dev.roanh.gmark.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.util.SimpleGraph.SimpleEdge;

public class UtilTest{

	@Test
	public void matching0(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		g.addVertex("a");
		g.addVertex("b");
		g.addVertex("c");
		g.addVertex("d");
		
		g.addEdge("a", "b");
		g.addEdge("b", "c");
		g.addEdge("c", "d");
		g.addEdge("d", "a");
		
		List<SimpleEdge<String>> matching = Util.findMaximalMatching(g);
		assertEquals(2, matching.size());
	}
	
	public static void main(String[] args){
		new UtilTest().decomp0();
	}
	
	@Test
	public void decomp0(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		g.addVertex("a");
		g.addVertex("b");
		g.addVertex("c");
		g.addVertex("d");
		g.addVertex("e");
		
		g.addEdge("a", "b");
		g.addEdge("a", "c");
		g.addEdge("c", "e");
		g.addEdge("e", "d");
		g.addEdge("d", "b");
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		
		decomp.forEach(t->System.out.println(t.getDepth() + ": " + t.getData()));
		
		
		
		
	}
}
