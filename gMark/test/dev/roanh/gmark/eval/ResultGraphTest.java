package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.data.SourceTargetPair;

public class ResultGraphTest{

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
		
		assertPaths(left.union(right), List.of(
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
		
		assertPaths(left.intersection(right), List.of(
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
		
		assertPaths(left.join(right), List.of(
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
		
		assertPaths(graph.transitiveClosure(), List.of(
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
	public void selectIdentity(){
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
		
		assertPaths(graph.selectIdentity(), List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4)
		));
	}
	
	private void assertPaths(ResultGraph result, List<SourceTargetPair> expected){
		System.out.println(result.getSourceTargetPairs().stream().sorted().toList());
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().sorted().toList());
	}
}
