package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.data.SourceTargetPair;

public class ResultGraphTest{

	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
