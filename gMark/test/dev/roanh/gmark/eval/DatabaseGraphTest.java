package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;

import nl.group9.quicksilver.impl.IntGraph;

public class DatabaseGraphTest{

	@Test
	public void construct(){
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
		
		assertEquals(13, graph.getEdgeCount());
		
		DatabaseGraph db = new DatabaseGraph(graph);
		assertEquals(5, db.getEdgeCount(new Predicate(0, "0")));
		assertEquals(3, db.getEdgeCount(new Predicate(1, "1")));
		assertArrayEquals(
			new int[]{9, 9, 13, 17, 21, 25, 29, 33, 38, 12, 13, 13, 4, 16, 17, 17, 3, 20, 21, 21, 4, 24, 25, 25, 6, 28, 28, 29, 4, 32, 32, 33, 7, 36, 37, 38, 0, 5, 0, 0, 0, 0, 0},
			db.slt
		);
	}
}
