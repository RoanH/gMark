/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dev.roanh.cpqindex.CanonForm.CoreHash;
import dev.roanh.cpqindex.Index.Block;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

public class IndexTest{
	private static UniqueGraph<Integer, Predicate> testGraph;
	private static Index testIndex;
	private static List<Predicate> symbols = List.of(
		new Predicate(0, "0"),
		new Predicate(1, "1"),
		new Predicate(2, "2"),
		new Predicate(3, "3")
	);
	
	static{
		testGraph = new UniqueGraph<Integer, Predicate>();
		testGraph.addUniqueNode(0);
		testGraph.addUniqueNode(1);
		testGraph.addUniqueNode(2);
		
		testGraph.addUniqueEdge(0, 1, symbols.get(0));
		testGraph.addUniqueEdge(0, 2, symbols.get(0));
		testGraph.addUniqueEdge(1, 2, symbols.get(1));
		
		try{
			testIndex = new Index(testGraph, 2, true, false, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		}catch(IllegalArgumentException | InterruptedException e){
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}
	
	@Test
	public void resumeTest() throws InterruptedException, IOException{
		Index index = new Index(testGraph, 3, false, false, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out, true);
		
		Index read = new Index(new ByteArrayInputStream(out.toByteArray()));
		read.computeCores(1);
		
		Index other = new Index(testGraph, 3, true, false, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		
		List<Block> a = read.getBlocks();
		List<Block> b = other.getBlocks();
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			assertIterableEquals(a.get(i).getPaths(), b.get(i).getPaths());
			assertIterableEquals(a.get(i).getCanonCores(), b.get(i).getCanonCores());
		}
		
		CPQ q = CPQ.labels(symbols.get(0), symbols.get(0));
		assertIterableEquals(read.query(q), other.query(q));
	}
	
	@Test
	public void writeReadTestFullResumeCores() throws IllegalArgumentException, InterruptedException, IOException{
		Index index = new Index(testGraph, 3, false, true, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out, true);
		index.computeCores(1);
		
		Index read = new Index(new ByteArrayInputStream(out.toByteArray()));
		read.computeCores(1);
		
		List<Block> a = index.getBlocks();
		List<Block> b = read.getBlocks();
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			assertIterableEquals(a.get(i).getPaths(), b.get(i).getPaths());
			assertIterableEquals(a.get(i).getCanonCores(), b.get(i).getCanonCores());
			assertIterableEquals(a.get(i).getLabels(), b.get(i).getLabels());
		}
		
		CPQ q = CPQ.labels(symbols.get(0), symbols.get(0));
		assertIterableEquals(index.query(q), read.query(q));
	}
	
	@Test
	public void writeReadTestFull() throws IllegalArgumentException, InterruptedException, IOException{
		Index index = new Index(testGraph, 3, true, true, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out, true);
		
		Index read = new Index(new ByteArrayInputStream(out.toByteArray()));
		
		List<Block> a = index.getBlocks();
		List<Block> b = read.getBlocks();
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			assertIterableEquals(a.get(i).getPaths(), b.get(i).getPaths());
			assertIterableEquals(a.get(i).getCanonCores(), b.get(i).getCanonCores());
			assertIterableEquals(a.get(i).getLabels(), b.get(i).getLabels());
		}
		
		CPQ q = CPQ.labels(symbols.get(0), symbols.get(0));
		assertIterableEquals(index.query(q), read.query(q));
	}
	
	@Test
	public void writeReadTestPartial() throws IllegalArgumentException, InterruptedException, IOException{
		Index index = new Index(testGraph, 3, true, true, 1, Integer.MAX_VALUE, ProgressListener.NONE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out, false);
		
		Index read = new Index(new ByteArrayInputStream(out.toByteArray()));
		
		List<Block> a = index.getBlocks();
		List<Block> b = read.getBlocks();
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			assertIterableEquals(a.get(i).getPaths(), b.get(i).getPaths());
		}
		
		CPQ q = CPQ.labels(symbols.get(0), symbols.get(0));
		assertIterableEquals(index.query(q), read.query(q));
	}
	
	@Test
	public void evaluateQuery() throws IllegalArgumentException{
		assertIterableEquals(List.of(new Pair(1, 2)), testIndex.query(CPQ.label(symbols.get(1))));
	}
	
	@Test
	public void evaluateQueryDiameterTooLarge() throws IllegalArgumentException{
		assertThrows(IllegalArgumentException.class, ()->testIndex.query(CPQ.parse("0◦1◦2", symbols)));
	}
	
	@Test
	public void evaluateQueryDiameterZero() throws IllegalArgumentException{
		assertThrows(IllegalArgumentException.class, ()->testIndex.query(CPQ.id()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"0", "1", "0◦1"})
	public void computeResultCardinality(String query) throws IllegalArgumentException{
		CPQ cpq = CPQ.parse(query, symbols);
		assertEquals(testIndex.query(cpq).size(), testIndex.computeResultCardinality(cpq), cpq.toString());
	}
	
	@Test
	public void coresTest() throws IllegalArgumentException, InterruptedException{
		Predicate l0 = new Predicate(0, "0");
		Predicate l1 = new Predicate(1, "1");
		Predicate l2 = new Predicate(2, "2");
		Predicate l3 = new Predicate(3, "3");
		
		UniqueGraph<Integer, Predicate> g = new UniqueGraph<Integer, Predicate>();
		g.addUniqueNode(0);
		g.addUniqueNode(1);
		g.addUniqueNode(2);
		g.addUniqueNode(3);
		g.addUniqueNode(4);
		g.addUniqueNode(5);
		g.addUniqueNode(6);

		g.addUniqueEdge(0, 1, l0);
		g.addUniqueEdge(0, 2, l1);
		g.addUniqueEdge(1, 3, l0);
		g.addUniqueEdge(2, 3, l1);
		g.addUniqueEdge(3, 4, l2);
		g.addUniqueEdge(3, 5, l3);
		g.addUniqueEdge(4, 6, l2);
		g.addUniqueEdge(5, 6, l3);
		
		Index index = new Index(g, 2, true, true, 1);
		index.sort();
		
		List<Block> blocks = index.getBlocks();
		assertEquals(31, blocks.size());
		Iterator<Block> iter = blocks.iterator();
		
		Block block = iter.next();
		assertEquals("[(0,0)]", block.getPaths().toString());
		assertEquals("[00⁻, 11⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0)◦(0⁻))", "((1)◦(1⁻))", "(((0)◦(0⁻)) ∩ ((1)◦(1⁻)))", "(((0)◦(0⁻)) ∩ id)", "(((1)◦(1⁻)) ∩ id)", "((((0)◦(0⁻)) ∩ ((1)◦(1⁻))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(0,1), (1,3)]", block.getPaths().toString());
		assertEquals("[0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(0)");
		
		block = iter.next();
		assertEquals("[(0,2), (2,3)]", block.getPaths().toString());
		assertEquals("[1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(1)");
		
		block = iter.next();
		assertEquals("[(0,3)]", block.getPaths().toString());
		assertEquals("[00, 11]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0)◦(0))", "((1)◦(1))", "(((0)◦(0)) ∩ ((1)◦(1)))");
		
		block = iter.next();
		assertEquals("[(1,0), (3,1)]", block.getPaths().toString());
		assertEquals("[0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(0⁻)");
		
		block = iter.next();
		assertEquals("[(1,1)]", block.getPaths().toString());
		assertEquals("[00⁻, 0⁻0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0)◦(0⁻))", "((0⁻)◦(0))", "(((0)◦(0⁻)) ∩ ((0⁻)◦(0)))", "(((0)◦(0⁻)) ∩ id)", "(((0⁻)◦(0)) ∩ id)", "((((0)◦(0⁻)) ∩ ((0⁻)◦(0))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(1,2)]", block.getPaths().toString());
		assertEquals("[01⁻, 0⁻1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0)◦(1⁻))", "((0⁻)◦(1))", "(((0)◦(1⁻)) ∩ ((0⁻)◦(1)))");
		
		block = iter.next();
		assertEquals("[(1,4)]", block.getPaths().toString());
		assertEquals("[02]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "0◦2");
		
		block = iter.next();
		assertEquals("[(1,5)]", block.getPaths().toString());
		assertEquals("[03]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "0◦3");
		
		block = iter.next();
		assertEquals("[(2,0), (3,2)]", block.getPaths().toString());
		assertEquals("[1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(1⁻)");
		
		block = iter.next();
		assertEquals("[(2,1)]", block.getPaths().toString());
		assertEquals("[10⁻, 1⁻0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((1)◦(0⁻))", "((1⁻)◦(0))", "(((1)◦(0⁻)) ∩ ((1⁻)◦(0)))");
		
		block = iter.next();
		assertEquals("[(2,2)]", block.getPaths().toString());
		assertEquals("[11⁻, 1⁻1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((1)◦(1⁻))", "((1⁻)◦(1))", "(((1)◦(1⁻)) ∩ ((1⁻)◦(1)))", "(((1)◦(1⁻)) ∩ id)", "(((1⁻)◦(1)) ∩ id)", "((((1)◦(1⁻)) ∩ ((1⁻)◦(1))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(2,4)]", block.getPaths().toString());
		assertEquals("[12]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(1◦2)");
		
		block = iter.next();
		assertEquals("[(2,5)]", block.getPaths().toString());
		assertEquals("[13]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "(1◦3)");
		
		block = iter.next();
		assertEquals("[(3,0)]", block.getPaths().toString());
		assertEquals("[0⁻0⁻, 1⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0⁻)◦(0⁻))", "((1⁻)◦(1⁻))", "(((0⁻)◦(0⁻)) ∩ ((1⁻)◦(1⁻)))");
		
		block = iter.next();
		assertEquals("[(3,3)]", block.getPaths().toString());
		assertEquals("[0⁻0, 1⁻1, 22⁻, 33⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((0⁻)◦(0))", "((1⁻)◦(1))", "((2)◦(2⁻))", "((3)◦(3⁻))", "(((2)◦(2⁻)) ∩ ((3)◦(3⁻)))", "(((1⁻)◦(1)) ∩ ((3)◦(3⁻)))", "(((1⁻)◦(1)) ∩ ((2)◦(2⁻)))", "((((1⁻)◦(1)) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻)))", "(((0⁻)◦(0)) ∩ ((3)◦(3⁻)))", "(((0⁻)◦(0)) ∩ ((2)◦(2⁻)))", "((((0⁻)◦(0)) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻)))", "(((0⁻)◦(0)) ∩ ((1⁻)◦(1)))", "((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((3)◦(3⁻)))", "((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((2)◦(2⁻)))", "(((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻)))", "(((0⁻)◦(0)) ∩ id)", "(((1⁻)◦(1)) ∩ id)", "(((2)◦(2⁻)) ∩ id)", "(((3)◦(3⁻)) ∩ id)", "((((2)◦(2⁻)) ∩ ((3)◦(3⁻))) ∩ id)", "((((1⁻)◦(1)) ∩ ((3)◦(3⁻))) ∩ id)", "((((1⁻)◦(1)) ∩ ((2)◦(2⁻))) ∩ id)", "(((((1⁻)◦(1)) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻))) ∩ id)", "((((0⁻)◦(0)) ∩ ((3)◦(3⁻))) ∩ id)", "((((0⁻)◦(0)) ∩ ((2)◦(2⁻))) ∩ id)", "(((((0⁻)◦(0)) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻))) ∩ id)", "((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ id)", "(((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((3)◦(3⁻))) ∩ id)", "(((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((2)◦(2⁻))) ∩ id)", "((((((0⁻)◦(0)) ∩ ((1⁻)◦(1))) ∩ ((2)◦(2⁻))) ∩ ((3)◦(3⁻))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(3,4), (4,6)]", block.getPaths().toString());
		assertEquals("[2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "2");
		
		block = iter.next();
		assertEquals("[(3,5), (5,6)]", block.getPaths().toString());
		assertEquals("[3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "3");
		
		block = iter.next();
		assertEquals("[(3,6)]", block.getPaths().toString());
		assertEquals("[22, 33]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((2)◦(2))", "((3)◦(3))", "(((2)◦(2)) ∩ ((3)◦(3)))");
		
		block = iter.next();
		assertEquals("[(4,1)]", block.getPaths().toString());
		assertEquals("[2⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "2⁻◦0⁻");
		
		block = iter.next();
		assertEquals("[(4,2)]", block.getPaths().toString());
		assertEquals("[2⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "2⁻◦1⁻");
		
		block = iter.next();
		assertEquals("[(4,3), (6,4)]", block.getPaths().toString());
		assertEquals("[2⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "2⁻");
		
		block = iter.next();
		assertEquals("[(4,4)]", block.getPaths().toString());
		assertEquals("[22⁻, 2⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((2)◦(2⁻))", "((2⁻)◦(2))", "(((2)◦(2⁻)) ∩ ((2⁻)◦(2)))", "(((2)◦(2⁻)) ∩ id)", "(((2⁻)◦(2)) ∩ id)", "((((2)◦(2⁻)) ∩ ((2⁻)◦(2))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(4,5)]", block.getPaths().toString());
		assertEquals("[23⁻, 2⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((2)◦(3⁻))", "((2⁻)◦(3))", "(((2)◦(3⁻)) ∩ ((2⁻)◦(3)))");
		
		block = iter.next();
		assertEquals("[(5,1)]", block.getPaths().toString());
		assertEquals("[3⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "3⁻◦0⁻");
		
		block = iter.next();
		assertEquals("[(5,2)]", block.getPaths().toString());
		assertEquals("[3⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "3⁻◦1⁻");
		
		block = iter.next();
		assertEquals("[(5,3), (6,5)]", block.getPaths().toString());
		assertEquals("[3⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "3⁻");
		
		block = iter.next();
		assertEquals("[(5,4)]", block.getPaths().toString());
		assertEquals("[32⁻, 3⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((3)◦(2⁻))", "((3⁻)◦(2))", "(((3)◦(2⁻)) ∩ ((3⁻)◦(2)))");
		
		block = iter.next();
		assertEquals("[(5,5)]", block.getPaths().toString());
		assertEquals("[33⁻, 3⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((3)◦(3⁻))", "((3⁻)◦(3))", "(((3)◦(3⁻)) ∩ ((3⁻)◦(3)))", "(((3)◦(3⁻)) ∩ id)", "(((3⁻)◦(3)) ∩ id)", "((((3)◦(3⁻)) ∩ ((3⁻)◦(3))) ∩ id)");
		
		block = iter.next();
		assertEquals("[(6,3)]", block.getPaths().toString());
		assertEquals("[2⁻2⁻, 3⁻3⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((2⁻)◦(2⁻))", "((3⁻)◦(3⁻))", "(((2⁻)◦(2⁻)) ∩ ((3⁻)◦(3⁻)))");
		
		block = iter.next();
		assertEquals("[(6,6)]", block.getPaths().toString());
		assertEquals("[2⁻2, 3⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		checkCores(block, "((2⁻)◦(2))", "((3⁻)◦(3))", "(((2⁻)◦(2)) ∩ ((3⁻)◦(3)))", "(((2⁻)◦(2)) ∩ id)", "(((3⁻)◦(3)) ∩ id)", "((((2⁻)◦(2)) ∩ ((3⁻)◦(3))) ∩ id)");
	}
	
	@Test
	public void robotsK2NoLabels() throws IOException, ClassNotFoundException, IllegalArgumentException, InterruptedException{
		UniqueGraph<Integer, Predicate> graph = IndexUtil.readGraph(ClassLoader.getSystemResourceAsStream("robots.edge"));
		List<Entry<List<String>, List<String>>> bin = readGraph("robots2.bin");
		
		Index index = new Index(graph, 2, false, false, 1);
		index.sort();
		
		List<Index.Block> blocks = index.getBlocks();
		assertEquals(7713, bin.size());
		assertEquals(7713, blocks.size());
		
		Iterator<Index.Block> iter = blocks.iterator();
		Iterator<Entry<List<String>, List<String>>> real = bin.iterator();
		while(iter.hasNext()){
			Entry<List<String>, List<String>> test = real.next();
			Index.Block block = iter.next();
			
			assertEquals(test.getKey().toString(), block.getPaths().toString());
			if(block.getLabels() != null){
				block.getLabels().forEach(s->{
					assertEquals(1, s.getLabels().length);
				});
			}
		}
	}
	
	@Test
	public void robotsK2() throws IOException, ClassNotFoundException, IllegalArgumentException, InterruptedException{
		UniqueGraph<Integer, Predicate> graph = IndexUtil.readGraph(ClassLoader.getSystemResourceAsStream("robots.edge"));
		List<Entry<List<String>, List<String>>> bin = readGraph("robots2.bin");
		
		Index index = new Index(graph, 2, false, true, 1);
		index.sort();
		
		List<Index.Block> blocks = index.getBlocks();
		assertEquals(7713, bin.size());
		assertEquals(7713, blocks.size());
		
		Iterator<Index.Block> iter = blocks.iterator();
		Iterator<Entry<List<String>, List<String>>> real = bin.iterator();
		while(iter.hasNext()){
			Entry<List<String>, List<String>> test = real.next();
			Index.Block block = iter.next();
			
			assertEquals(test.getKey().toString(), block.getPaths().toString());
			assertEquals(test.getValue().toString(), block.getLabels().stream().map(LabelSequence::toString).sorted().toList().toString());
		}
	}
	
	@Test
	public void robotsK1() throws IOException, ClassNotFoundException, IllegalArgumentException, InterruptedException{
		UniqueGraph<Integer, Predicate> graph = IndexUtil.readGraph(ClassLoader.getSystemResourceAsStream("robots.edge"));
		List<Entry<List<String>, List<String>>> bin = readGraph("robots1.bin");
		
		Index index = new Index(graph, 1, false, true, 1);
		index.sort();
		
		List<Index.Block> blocks = index.getBlocks();
		assertEquals(24, bin.size());
		assertEquals(24, blocks.size());
		
		Iterator<Index.Block> iter = blocks.iterator();
		Iterator<Entry<List<String>, List<String>>> real = bin.iterator();
		while(iter.hasNext()){
			Entry<List<String>, List<String>> test = real.next();
			Index.Block block = iter.next();
			
			assertEquals(test.getKey().toString(), block.getPaths().toString());
			assertEquals(test.getValue().toString(), block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		}
	}

	@Test
	public void constructionTest() throws IllegalArgumentException, InterruptedException{
		Predicate l0 = new Predicate(0, "0");
		Predicate l1 = new Predicate(1, "1");
		Predicate l2 = new Predicate(2, "2");
		Predicate l3 = new Predicate(3, "3");
		
		UniqueGraph<Integer, Predicate> g = new UniqueGraph<Integer, Predicate>();
		g.addUniqueNode(0);
		g.addUniqueNode(1);
		g.addUniqueNode(2);
		g.addUniqueNode(3);
		g.addUniqueNode(4);
		g.addUniqueNode(5);
		g.addUniqueNode(6);

		g.addUniqueEdge(0, 1, l0);
		g.addUniqueEdge(0, 2, l1);
		g.addUniqueEdge(1, 3, l0);
		g.addUniqueEdge(2, 3, l1);
		g.addUniqueEdge(3, 4, l2);
		g.addUniqueEdge(3, 5, l3);
		g.addUniqueEdge(4, 6, l2);
		g.addUniqueEdge(5, 6, l3);
		
		Index index = new Index(g, 4, false, true, 1);
		index.sort();
		
		List<Block> blocks = index.getBlocks();
		assertEquals(49, blocks.size());
		Iterator<Block> iter = blocks.iterator();
		
		Block block = iter.next();
		assertEquals("[(0,0)]", block.getPaths().toString());
		assertEquals("[00⁻, 11⁻, 000⁻0⁻, 001⁻1⁻, 00⁻00⁻, 00⁻11⁻, 110⁻0⁻, 111⁻1⁻, 11⁻00⁻, 11⁻11⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,1)]", block.getPaths().toString());
		assertEquals("[0, 000⁻, 00⁻0, 110⁻, 11⁻0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,2)]", block.getPaths().toString());
		assertEquals("[1, 001⁻, 00⁻1, 111⁻, 11⁻1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,3)]", block.getPaths().toString());
		assertEquals("[00, 11, 000⁻0, 001⁻1, 0022⁻, 0033⁻, 00⁻00, 00⁻11, 110⁻0, 111⁻1, 1122⁻, 1133⁻, 11⁻00, 11⁻11]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,4)]", block.getPaths().toString());
		assertEquals("[002, 112]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,5)]", block.getPaths().toString());
		assertEquals("[003, 113]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(0,6)]", block.getPaths().toString());
		assertEquals("[0022, 0033, 1122, 1133]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,0)]", block.getPaths().toString());
		assertEquals("[0⁻, 00⁻0⁻, 01⁻1⁻, 0⁻00⁻, 0⁻11⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,1)]", block.getPaths().toString());
		assertEquals("[00⁻, 0⁻0, 00⁻00⁻, 00⁻0⁻0, 01⁻10⁻, 01⁻1⁻0, 022⁻0⁻, 033⁻0⁻, 0⁻000⁻, 0⁻00⁻0, 0⁻110⁻, 0⁻11⁻0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,2)]", block.getPaths().toString());
		assertEquals("[01⁻, 0⁻1, 00⁻01⁻, 00⁻0⁻1, 01⁻11⁻, 01⁻1⁻1, 022⁻1⁻, 033⁻1⁻, 0⁻001⁻, 0⁻00⁻1, 0⁻111⁻, 0⁻11⁻1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,3)]", block.getPaths().toString());
		assertEquals("[0, 00⁻0, 01⁻1, 022⁻, 033⁻, 0⁻00, 0⁻11]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,4)]", block.getPaths().toString());
		assertEquals("[02, 00⁻02, 01⁻12, 0222⁻, 022⁻2, 0332⁻, 033⁻2, 0⁻002, 0⁻112]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,5)]", block.getPaths().toString());
		assertEquals("[03, 00⁻03, 01⁻13, 0223⁻, 022⁻3, 0333⁻, 033⁻3, 0⁻003, 0⁻113]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(1,6)]", block.getPaths().toString());
		assertEquals("[022, 033]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,0)]", block.getPaths().toString());
		assertEquals("[1⁻, 10⁻0⁻, 11⁻1⁻, 1⁻00⁻, 1⁻11⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,1)]", block.getPaths().toString());
		assertEquals("[10⁻, 1⁻0, 10⁻00⁻, 10⁻0⁻0, 11⁻10⁻, 11⁻1⁻0, 122⁻0⁻, 133⁻0⁻, 1⁻000⁻, 1⁻00⁻0, 1⁻110⁻, 1⁻11⁻0]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,2)]", block.getPaths().toString());
		assertEquals("[11⁻, 1⁻1, 10⁻01⁻, 10⁻0⁻1, 11⁻11⁻, 11⁻1⁻1, 122⁻1⁻, 133⁻1⁻, 1⁻001⁻, 1⁻00⁻1, 1⁻111⁻, 1⁻11⁻1]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,3)]", block.getPaths().toString());
		assertEquals("[1, 10⁻0, 11⁻1, 122⁻, 133⁻, 1⁻00, 1⁻11]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,4)]", block.getPaths().toString());
		assertEquals("[12, 10⁻02, 11⁻12, 1222⁻, 122⁻2, 1332⁻, 133⁻2, 1⁻002, 1⁻112]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,5)]", block.getPaths().toString());
		assertEquals("[13, 10⁻03, 11⁻13, 1223⁻, 122⁻3, 1333⁻, 133⁻3, 1⁻003, 1⁻113]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(2,6)]", block.getPaths().toString());
		assertEquals("[122, 133]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,0)]", block.getPaths().toString());
		assertEquals("[0⁻0⁻, 1⁻1⁻, 0⁻00⁻0⁻, 0⁻01⁻1⁻, 0⁻0⁻00⁻, 0⁻0⁻11⁻, 1⁻10⁻0⁻, 1⁻11⁻1⁻, 1⁻1⁻00⁻, 1⁻1⁻11⁻, 22⁻0⁻0⁻, 22⁻1⁻1⁻, 33⁻0⁻0⁻, 33⁻1⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,1)]", block.getPaths().toString());
		assertEquals("[0⁻, 0⁻00⁻, 0⁻0⁻0, 1⁻10⁻, 1⁻1⁻0, 22⁻0⁻, 33⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,2)]", block.getPaths().toString());
		assertEquals("[1⁻, 0⁻01⁻, 0⁻0⁻1, 1⁻11⁻, 1⁻1⁻1, 22⁻1⁻, 33⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,3)]", block.getPaths().toString());
		assertEquals("[0⁻0, 1⁻1, 22⁻, 33⁻, 0⁻00⁻0, 0⁻01⁻1, 0⁻022⁻, 0⁻033⁻, 0⁻0⁻00, 0⁻0⁻11, 1⁻10⁻0, 1⁻11⁻1, 1⁻122⁻, 1⁻133⁻, 1⁻1⁻00, 1⁻1⁻11, 222⁻2⁻, 223⁻3⁻, 22⁻0⁻0, 22⁻1⁻1, 22⁻22⁻, 22⁻33⁻, 332⁻2⁻, 333⁻3⁻, 33⁻0⁻0, 33⁻1⁻1, 33⁻22⁻, 33⁻33⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,4)]", block.getPaths().toString());
		assertEquals("[2, 0⁻02, 1⁻12, 222⁻, 22⁻2, 332⁻, 33⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,5)]", block.getPaths().toString());
		assertEquals("[3, 0⁻03, 1⁻13, 223⁻, 22⁻3, 333⁻, 33⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(3,6)]", block.getPaths().toString());
		assertEquals("[22, 33, 0⁻022, 0⁻033, 1⁻122, 1⁻133, 222⁻2, 223⁻3, 22⁻22, 22⁻33, 332⁻2, 333⁻3, 33⁻22, 33⁻33]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,0)]", block.getPaths().toString());
		assertEquals("[2⁻0⁻0⁻, 2⁻1⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,1)]", block.getPaths().toString());
		assertEquals("[2⁻0⁻, 22⁻2⁻0⁻, 23⁻3⁻0⁻, 2⁻0⁻00⁻, 2⁻0⁻0⁻0, 2⁻1⁻10⁻, 2⁻1⁻1⁻0, 2⁻22⁻0⁻, 2⁻33⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,2)]", block.getPaths().toString());
		assertEquals("[2⁻1⁻, 22⁻2⁻1⁻, 23⁻3⁻1⁻, 2⁻0⁻01⁻, 2⁻0⁻0⁻1, 2⁻1⁻11⁻, 2⁻1⁻1⁻1, 2⁻22⁻1⁻, 2⁻33⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,3)]", block.getPaths().toString());
		assertEquals("[2⁻, 22⁻2⁻, 23⁻3⁻, 2⁻0⁻0, 2⁻1⁻1, 2⁻22⁻, 2⁻33⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,4)]", block.getPaths().toString());
		assertEquals("[22⁻, 2⁻2, 22⁻22⁻, 22⁻2⁻2, 23⁻32⁻, 23⁻3⁻2, 2⁻0⁻02, 2⁻1⁻12, 2⁻222⁻, 2⁻22⁻2, 2⁻332⁻, 2⁻33⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,5)]", block.getPaths().toString());
		assertEquals("[23⁻, 2⁻3, 22⁻23⁻, 22⁻2⁻3, 23⁻33⁻, 23⁻3⁻3, 2⁻0⁻03, 2⁻1⁻13, 2⁻223⁻, 2⁻22⁻3, 2⁻333⁻, 2⁻33⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(4,6)]", block.getPaths().toString());
		assertEquals("[2, 22⁻2, 23⁻3, 2⁻22, 2⁻33]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,0)]", block.getPaths().toString());
		assertEquals("[3⁻0⁻0⁻, 3⁻1⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,1)]", block.getPaths().toString());
		assertEquals("[3⁻0⁻, 32⁻2⁻0⁻, 33⁻3⁻0⁻, 3⁻0⁻00⁻, 3⁻0⁻0⁻0, 3⁻1⁻10⁻, 3⁻1⁻1⁻0, 3⁻22⁻0⁻, 3⁻33⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,2)]", block.getPaths().toString());
		assertEquals("[3⁻1⁻, 32⁻2⁻1⁻, 33⁻3⁻1⁻, 3⁻0⁻01⁻, 3⁻0⁻0⁻1, 3⁻1⁻11⁻, 3⁻1⁻1⁻1, 3⁻22⁻1⁻, 3⁻33⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,3)]", block.getPaths().toString());
		assertEquals("[3⁻, 32⁻2⁻, 33⁻3⁻, 3⁻0⁻0, 3⁻1⁻1, 3⁻22⁻, 3⁻33⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,4)]", block.getPaths().toString());
		assertEquals("[32⁻, 3⁻2, 32⁻22⁻, 32⁻2⁻2, 33⁻32⁻, 33⁻3⁻2, 3⁻0⁻02, 3⁻1⁻12, 3⁻222⁻, 3⁻22⁻2, 3⁻332⁻, 3⁻33⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,5)]", block.getPaths().toString());
		assertEquals("[33⁻, 3⁻3, 32⁻23⁻, 32⁻2⁻3, 33⁻33⁻, 33⁻3⁻3, 3⁻0⁻03, 3⁻1⁻13, 3⁻223⁻, 3⁻22⁻3, 3⁻333⁻, 3⁻33⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(5,6)]", block.getPaths().toString());
		assertEquals("[3, 32⁻2, 33⁻3, 3⁻22, 3⁻33]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,0)]", block.getPaths().toString());
		assertEquals("[2⁻2⁻0⁻0⁻, 2⁻2⁻1⁻1⁻, 3⁻3⁻0⁻0⁻, 3⁻3⁻1⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,1)]", block.getPaths().toString());
		assertEquals("[2⁻2⁻0⁻, 3⁻3⁻0⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,2)]", block.getPaths().toString());
		assertEquals("[2⁻2⁻1⁻, 3⁻3⁻1⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,3)]", block.getPaths().toString());
		assertEquals("[2⁻2⁻, 3⁻3⁻, 2⁻22⁻2⁻, 2⁻23⁻3⁻, 2⁻2⁻0⁻0, 2⁻2⁻1⁻1, 2⁻2⁻22⁻, 2⁻2⁻33⁻, 3⁻32⁻2⁻, 3⁻33⁻3⁻, 3⁻3⁻0⁻0, 3⁻3⁻1⁻1, 3⁻3⁻22⁻, 3⁻3⁻33⁻]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,4)]", block.getPaths().toString());
		assertEquals("[2⁻, 2⁻22⁻, 2⁻2⁻2, 3⁻32⁻, 3⁻3⁻2]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,5)]", block.getPaths().toString());
		assertEquals("[3⁻, 2⁻23⁻, 2⁻2⁻3, 3⁻33⁻, 3⁻3⁻3]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
		
		block = iter.next();
		assertEquals("[(6,6)]", block.getPaths().toString());
		assertEquals("[2⁻2, 3⁻3, 2⁻22⁻2, 2⁻23⁻3, 2⁻2⁻22, 2⁻2⁻33, 3⁻32⁻2, 3⁻33⁻3, 3⁻3⁻22, 3⁻3⁻33]", block.getLabels().stream().map(LabelSequence::toString).toList().toString());
	}
	
	@SuppressWarnings("unchecked")
	private static List<Entry<List<String>, List<String>>> readGraph(String name) throws IOException, ClassNotFoundException{
		try(ObjectInputStream obsout = new ObjectInputStream(ClassLoader.getSystemResourceAsStream(name))){
			return (List<Entry<List<String>, List<String>>>)obsout.readObject();
		}
	}
	
	private void checkCores(Block block, String... expected){
		assertEquals(expected.length, block.getCanonCores().size(), "found: " + block.getCores());
		for(String cpq : expected){
			CoreHash canon = CanonForm.computeCanon(CPQ.parse(cpq, symbols), false).toHashCanon();
			assertTrue(block.getCanonCores().contains(canon), "real: " + block.getCores() + " / " + canon + " | " + block.getCanonCores() + " | " + cpq);
		}
	}
}
