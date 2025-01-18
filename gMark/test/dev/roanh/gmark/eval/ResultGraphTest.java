package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.data.CardStat;
import dev.roanh.gmark.data.SourceTargetPair;

public class ResultGraphTest{
	
	@Test
	public void empty(){
		ResultGraph empty = ResultGraph.empty(4);
		assertArrayEquals(new int[]{5, 5, 5, 5, 5}, empty.getData());
		assertEquals(4, empty.getVertexCount());
		assertEquals(0, empty.getEdgeCount());
	}
	
	@Test
	public void single0(){
		ResultGraph single = ResultGraph.single(4, 1, 2);
		assertArrayEquals(new int[]{5, 5, 6, 6, 6, 2}, single.getData());
		assertEquals(4, single.getVertexCount());
		assertEquals(1, single.getEdgeCount());
	}
	
	@Test
	public void single1(){
		ResultGraph single = ResultGraph.single(6, 1, true, 2, 4, 5);
		assertArrayEquals(new int[]{7, 7, 10, 10, 10, 10, 10, 2, 4, 5}, single.getData());
		assertEquals(6, single.getVertexCount());
		assertEquals(3, single.getEdgeCount());
	}
	
	@Test
	public void single2(){
		int[] data = new int[]{1, 1, 2, 4, 5, 1, 1};
		ResultGraph single = ResultGraph.single(6, 1, true, 2, 5, data);
		assertArrayEquals(new int[]{7, 7, 10, 10, 10, 10, 10, 2, 4, 5}, single.getData());
		assertEquals(6, single.getVertexCount());
		assertEquals(3, single.getEdgeCount());
	}

	@Test
	public void union(){
		ResultGraph left = new ResultGraph(5, 5, false);
		left.setActiveSource(0);
		left.addTarget(1);
		left.addTarget(2);
		left.addTarget(0);
		left.setActiveSource(1);
		left.setActiveSource(2);
		left.addTarget(1);
		left.addTarget(0);
		left.setActiveSource(3);
		left.addTarget(3);
		left.setActiveSource(4);
		left.addTarget(4);
		left.addTarget(2);
		left.endFinalSource();
		
		ResultGraph right = new ResultGraph(5, 5, false);
		right.setActiveSource(0);
		right.addTarget(1);
		right.addTarget(4);
		right.addTarget(0);
		right.setActiveSource(1);
		right.addTarget(1);
		right.addTarget(0);
		right.setActiveSource(2);
		right.setActiveSource(3);
		right.addTarget(2);
		right.setActiveSource(4);
		right.addTarget(4);
		right.addTarget(3);
		right.endFinalSource();
		
		ResultGraph result = left.union(right);
		assertEquals(new CardStat(5, 13, 5), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 2),
			new SourceTargetPair(0, 4),
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 2),
			new SourceTargetPair(4, 3),
			new SourceTargetPair(4, 4)
		));
	}
	
	@Test
	public void intersection(){
		ResultGraph left = new ResultGraph(5, 5, false);
		left.setActiveSource(0);
		left.addTarget(1);
		left.addTarget(2);
		left.addTarget(0);
		left.setActiveSource(1);
		left.setActiveSource(2);
		left.addTarget(1);
		left.addTarget(0);
		left.setActiveSource(3);
		left.addTarget(3);
		left.setActiveSource(4);
		left.addTarget(4);
		left.addTarget(2);
		left.endFinalSource();
		
		ResultGraph right = new ResultGraph(5, 5, false);
		right.setActiveSource(0);
		right.addTarget(1);
		right.addTarget(4);
		right.addTarget(0);
		right.setActiveSource(1);
		right.addTarget(1);
		right.addTarget(0);
		right.setActiveSource(2);
		right.setActiveSource(3);
		right.addTarget(2);
		right.setActiveSource(4);
		right.addTarget(4);
		right.addTarget(3);
		right.endFinalSource();

		ResultGraph result = left.intersection(right);
		assertEquals(new CardStat(2, 3, 3), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(4, 4)
		));
	}
	
	@Test
	public void join(){
		ResultGraph left = new ResultGraph(5, 5, false);
		left.setActiveSource(0);
		left.addTarget(1);
		left.addTarget(2);
		left.addTarget(0);
		left.setActiveSource(1);
		left.setActiveSource(2);
		left.addTarget(1);
		left.addTarget(0);
		left.setActiveSource(3);
		left.addTarget(3);
		left.setActiveSource(4);
		left.addTarget(4);
		left.addTarget(2);
		left.endFinalSource();
		
		ResultGraph right = new ResultGraph(5, 5, false);
		right.setActiveSource(0);
		right.addTarget(1);
		right.addTarget(4);
		right.addTarget(0);
		right.setActiveSource(1);
		right.addTarget(1);
		right.addTarget(0);
		right.setActiveSource(2);
		right.setActiveSource(3);
		right.addTarget(2);
		right.setActiveSource(4);
		right.addTarget(4);
		right.addTarget(3);
		right.endFinalSource();
		
		ResultGraph result = left.join(right);
		assertEquals(new CardStat(4, 9, 5), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsSorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 4),
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1),
			new SourceTargetPair(2, 4),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(4, 3),
			new SourceTargetPair(4, 4)
		));
	}
	
	@Test
	public void transitiveClosure(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosure();
		assertEquals(new CardStat(5, 15, 6), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsSorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 2),
			new SourceTargetPair(0, 5),
			new SourceTargetPair(1, 5),
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1),
			new SourceTargetPair(2, 2),
			new SourceTargetPair(2, 5),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 0),
			new SourceTargetPair(4, 1),
			new SourceTargetPair(4, 2),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(4, 5)
		));
	}
	
	@Test
	public void transitiveClosureFrom0(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosureFrom(0);
		assertEquals(new CardStat(1, 4, 4), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsSorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 2),
			new SourceTargetPair(0, 5)
		));
	}
	
	@Test
	public void transitiveClosureFrom1(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosureFrom(5);
		assertEquals(new CardStat(0, 0, 0), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsUnsorted(result, List.of());
	}
	
	@Test
	public void transitiveClosureTo(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosureTo(1);
		assertEquals(new CardStat(3, 3, 1), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsSorted(result, List.of(
			new SourceTargetPair(0, 1),
			new SourceTargetPair(2, 1),
			new SourceTargetPair(4, 1)
		));
	}
	
	@Test
	public void transitiveClosureExact0(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosure(4, 1);
		assertEquals(new CardStat(1, 1, 1), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(new SourceTargetPair(4, 1)));
	}
	
	@Test
	public void transitiveClosureExact1(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosure(2, 4);
		assertEquals(new CardStat(0, 0, 0), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of());
	}
	
	@Test
	public void transitiveClosureExact2(){
		ResultGraph graph = new ResultGraph(6, 6, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.addTarget(5);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.setActiveSource(5);
		graph.endFinalSource();
		
		ResultGraph result = graph.transitiveClosure(5, 4);
		assertEquals(new CardStat(0, 0, 0), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of());
	}
	
	@Test
	public void selectIdentityUnsorted(){
		ResultGraph graph = new ResultGraph(5, 5, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.endFinalSource();
		
		ResultGraph result = graph.selectIdentity();
		assertEquals(new CardStat(3, 3, 3), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4)
		));
	}
	
	@Test
	public void selectIdentitySorted(){
		ResultGraph graph = new ResultGraph(5, 5, true);
		graph.setActiveSource(0);
		graph.addTarget(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.setActiveSource(1);
		graph.setActiveSource(2);
		graph.addTarget(0);
		graph.addTarget(1);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(2);
		graph.addTarget(4);
		graph.endFinalSource();
		
		ResultGraph result = graph.selectIdentity();
		assertEquals(new CardStat(3, 3, 3), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4)
		));
	}
	
	@Test
	public void selectTarget(){
		ResultGraph graph = new ResultGraph(5, 5, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.endFinalSource();
		
		ResultGraph result = graph.selectTarget(0);
		assertEquals(new CardStat(2, 2, 1), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(2, 0)
		));
	}
	
	@Test
	public void selectSourceUnsorted(){
		ResultGraph graph = new ResultGraph(5, 5, false);
		graph.setActiveSource(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.addTarget(0);
		graph.setActiveSource(1);
		graph.setActiveSource(2);
		graph.addTarget(1);
		graph.addTarget(0);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(4);
		graph.addTarget(2);
		graph.endFinalSource();
		
		ResultGraph result = graph.selectSource(2);
		assertEquals(new CardStat(1, 2, 2), result.computeCardinality());
		assertFalse(result.isSorted());
		assertPathsSorted(result, List.of(
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1)
		));
	}
	
	@Test
	public void selectSourceSorted(){
		ResultGraph graph = new ResultGraph(5, 5, true);
		graph.setActiveSource(0);
		graph.addTarget(0);
		graph.addTarget(1);
		graph.addTarget(2);
		graph.setActiveSource(1);
		graph.setActiveSource(2);
		graph.addTarget(0);
		graph.addTarget(1);
		graph.setActiveSource(3);
		graph.addTarget(3);
		graph.setActiveSource(4);
		graph.addTarget(2);
		graph.addTarget(4);
		graph.endFinalSource();
		
		ResultGraph result = graph.selectSource(2);
		assertEquals(new CardStat(1, 2, 2), result.computeCardinality());
		assertTrue(result.isSorted());
		assertPathsUnsorted(result, List.of(
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1)
		));
	}
	
	private static void assertPathsUnsorted(ResultGraph result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().toList());
	}
	
	private static void assertPathsSorted(ResultGraph result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().sorted().toList());
	}
}
