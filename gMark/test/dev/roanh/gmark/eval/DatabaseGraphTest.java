package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.util.graph.IntGraph;

public class DatabaseGraphTest{

	@Test
	public void construct(){
		IntGraph graph = createGraph();
		assertEquals(13, graph.getEdgeCount());
		
		DatabaseGraph db = new DatabaseGraph(graph);
		assertEquals(5, db.getEdgeCount(new Predicate(0, "0")));
		assertEquals(3, db.getEdgeCount(new Predicate(1, "1")));
		
		assertArrayEquals(
			new int[]{9, 9, 13, 17, 21, 25, 29, 33, 38, 12, 13, 13, 4, 16, 17, 17, 3, 20, 21, 21, 4, 24, 25, 25, 6, 28, 28, 29, 4, 32, 32, 33, 7, 36, 37, 38, 0, 5, 0, 0, 0, 0, 0},
			db.getData()
		);
		
		assertArrayEquals(
			new int[]{9, 13, 13, 13, 17, 23, 27, 31, 35, 12, 13, 13, 7, 16, 17, 17, 2, 20, 22, 23, 1, 3, 5, 26, 26, 27, 7, 30, 31, 31, 4, 34, 34, 35, 6, 0, 0, 0, 0, 0},
			db.getReverseData()
		);
	}
	
	@Test
	public void select0(){
		ResultGraph result = createDatabaseGraph().selectLabel(new Predicate(0, "0"));
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 4),
			new SourceTargetPair(2, 3),
			new SourceTargetPair(3, 4),
			new SourceTargetPair(4, 6),
			new SourceTargetPair(7, 0)
		));
	}
	
	@Test
	public void select1(){
		ResultGraph result = createDatabaseGraph().selectLabel(new Predicate(1, "1"));
		
		assertPaths(result, List.of(
			new SourceTargetPair(5, 4),
			new SourceTargetPair(6, 7),
			new SourceTargetPair(7, 5)
		));
	}
	
	@Test
	public void selectInv0(){
		ResultGraph result = createDatabaseGraph().selectLabel(new Predicate(0, "0").getInverse());
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 7),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(4, 1),
			new SourceTargetPair(4, 3),
			new SourceTargetPair(6, 4)
		));
	}
	
	@Test
	public void selectInv1(){
		ResultGraph result = createDatabaseGraph().selectLabel(new Predicate(1, "1").getInverse());
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 5),
			new SourceTargetPair(5, 7),
			new SourceTargetPair(7, 6)
		));
	}
	
	private void assertPaths(ResultGraph result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().sorted().toList());
	}
	
	private static DatabaseGraph createDatabaseGraph(){
		return new DatabaseGraph(createGraph());
	}
	
	private static IntGraph createGraph(){
		IntGraph graph = new IntGraph(8, 2);
		graph.addEdge(1, 4, 0);
		graph.addEdge(4, 6, 0);
		graph.addEdge(3, 4, 0);
		graph.addEdge(5, 4, 1);
		graph.addEdge(6, 7, 1);
		graph.addEdge(2, 3, 0);
		graph.addEdge(3, 4, 0);//duplicate
		graph.addEdge(5, 4, 1);//duplicate
		graph.addEdge(6, 7, 1);//duplicate
		graph.addEdge(5, 4, 1);//duplicate
		graph.addEdge(6, 7, 1);//duplicate
		graph.addEdge(7, 5, 1);
		graph.addEdge(7, 0, 0);
		return graph;
	}
}
