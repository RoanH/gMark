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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		
		List<String> vertices = Arrays.asList("a", "b", "c", "d", "e");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("a", "c");
		g.addEdge("c", "e");
		g.addEdge("e", "d");
		g.addEdge("d", "b");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		
		decomp.forEach(t->System.out.println(t.getDepth() + ": " + t.getData()));
		
		assertValidTreeDecomposition(decomp, vertices, edges);
		
		
	}
	
	
	private void assertValidTreeDecomposition(Tree<List<String>> decomp, List<String> vertices, List<SimpleEdge<String>> edges){
		//1. All vertices are in the decomposition
		Set<String> found = new HashSet<String>();
		decomp.forEach(t->found.addAll(t.getData()));
		assertEquals(vertices.size(), found.size());
		found.addAll(vertices);
		assertEquals(vertices.size(), found.size());
		
		//2. All edges are in a bag
		outer: for(SimpleEdge<String> e : edges){
			for(List<String> bag : decomp.stream().map(Tree::getData).collect(Collectors.toList())){
				if(bag.contains(e.getFirstVertex().getData()) && bag.contains(e.getSecondVertex().getData())){
					continue outer;
				}
			}
			
			fail("Edge not in a bag");
		}
		
		//3. Check each vertex induces a connected subgraph
		for(String vertex : vertices){
			List<Tree<List<String>>> bags = new ArrayList<Tree<List<String>>>();
			decomp.forEach(t->{
				if(t.getData().contains(vertex)){
					bags.add(t);
				}
			});

			Tree<List<String>> root = bags.get(0);
			while(!root.isRoot() && root.getParent().getData().contains(vertex)){
				root = root.getParent();
			}
			
			assertEquals(bags.size(), findVertex(root, vertex));
		}
	}
	
	private int findVertex(Tree<List<String>> root, String v){
		if(root.getData().contains(v)){
			return 1 + root.getChildren().stream().mapToInt(t->findVertex(t, v)).sum();
		}else{
			return 0;
		}
	}
}
