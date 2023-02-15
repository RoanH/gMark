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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp1(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "b", "c", "d", "e");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "e");
		g.addEdge("e", "d");
		g.addEdge("d", "b");
		g.addEdge("b", "a");
		g.addEdge("c", "b");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp2(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "b", "d", "e", "f", "g", "h", "i");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "e");
		g.addEdge("a", "b");
		g.addEdge("b", "e");
		g.addEdge("a", "h");
		g.addEdge("a", "i");
		g.addEdge("a", "d");
		g.addEdge("e", "f");
		g.addEdge("b", "g");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp3(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "b", "c", "d", "e", "f");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("b", "c");
		g.addEdge("c", "d");
		g.addEdge("d", "e");
		g.addEdge("e", "f");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp4(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "b", "c", "d", "e", "f");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("a", "c");
		g.addEdge("c", "d");
		g.addEdge("e", "b");
		g.addEdge("d", "e");
		g.addEdge("f", "c");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp5(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
		vertices.forEach(g::addVertex);
		
		g.addEdge("1", "2");
		g.addEdge("2", "3");
		g.addEdge("3", "4");
		g.addEdge("4", "5");
		g.addEdge("5", "6");
		g.addEdge("6", "7");
		g.addEdge("7", "8");
		g.addEdge("8", "1");
		g.addEdge("6", "1");
		g.addEdge("1", "5");
		g.addEdge("5", "2");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp6(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		Arrays.asList("a", "b", "c", "d", "e", "f").forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("b", "c");
		g.addEdge("d", "e");
		g.addEdge("e", "f");
		
		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(g));
	}
	
	@Test
	public void decomp7(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		Arrays.asList("a", "b", "c", "d").forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("a", "c");
		g.addEdge("a", "d");
		g.addEdge("b", "c");
		g.addEdge("b", "d");
		g.addEdge("c", "d");

		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(g));
	}
	
	@Test
	public void decomp8(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		Arrays.asList("a", "b", "c", "d", "e").forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("a", "c");
		g.addEdge("a", "d");
		g.addEdge("b", "c");
		g.addEdge("b", "d");
		g.addEdge("c", "d");
		g.addEdge("d", "e");

		assertThrows(IllegalArgumentException.class, ()->Util.computeTreeDecompositionWidth2(g));
	}
	
	@Test
	public void decomp9(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "c", "b", "d", "e", "f");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("b", "c");
		g.addEdge("c", "d");
		g.addEdge("d", "e");
		g.addEdge("e", "f");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp10(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "b");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
	}
	
	@Test
	public void decomp11(){
		SimpleGraph<String> g = new SimpleGraph<String>();
		
		List<String> vertices = Arrays.asList("a", "c", "b", "d", "e", "f");
		vertices.forEach(g::addVertex);
		
		g.addEdge("a", "b");
		g.addEdge("b", "c");
		g.addEdge("c", "d");
		g.addEdge("d", "e");
		g.addEdge("e", "f");
		List<SimpleEdge<String>> edges = new ArrayList<SimpleEdge<String>>(g.getEdges());
		
		Tree<List<String>> decomp = Util.computeTreeDecompositionWidth2(g);
		assertValidTreeDecomposition(decomp, vertices, edges);
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
