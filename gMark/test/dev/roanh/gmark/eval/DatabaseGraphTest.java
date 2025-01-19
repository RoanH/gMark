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
package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.IntGraph;

public class DatabaseGraphTest{
	private static final Predicate l0 = new Predicate(0, "0");
	private static final Predicate l1 = new Predicate(1, "1");

	@Test
	public void construct(){
		IntGraph graph = createGraph();
		assertEquals(13, graph.getEdgeCount());
		
		DatabaseGraph db = new DatabaseGraph(graph);
		assertEquals(5, db.getEdgeCount(l0));
		assertEquals(3, db.getEdgeCount(l1));
		assertEquals(8, db.getVertexCount());
		assertEquals(8, db.getEdgeCount());
		assertEquals(2, db.getLabelCount());
		
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
		ResultGraph result = createDatabaseGraph().selectLabel(l0);
		
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
		ResultGraph result = createDatabaseGraph().selectLabel(l1);
		
		assertPaths(result, List.of(
			new SourceTargetPair(5, 4),
			new SourceTargetPair(6, 7),
			new SourceTargetPair(7, 5)
		));
	}
	
	@Test
	public void selectTarget0(){
		ResultGraph result = createDatabaseGraph().selectLabel(l1, 4);
		
		assertPaths(result, List.of(
			new SourceTargetPair(5, 4)
		));
	}
	
	@Test
	public void selectTarget1(){
		ResultGraph result = createDatabaseGraph().selectLabel(l0, 4);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 4),
			new SourceTargetPair(3, 4)
		));
	}
	
	@Test
	public void selectTarget2(){
		assertPaths(createDatabaseGraph().selectLabel(l1, 6), List.of());
	}
	
	@Test
	public void selectTarget4(){
		ResultGraph result = createDatabaseGraph().selectLabel(l0.getInverse(), 4);
		
		assertPaths(result, List.of(
			new SourceTargetPair(6, 4)
		));
	}
	
	@Test
	public void selectSource0(){
		ResultGraph result = createDatabaseGraph().selectLabel(4, l0);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 6)
		));
	}
	
	@Test
	public void selectSource1(){
		ResultGraph result = createDatabaseGraph().selectLabel(7, l0);
		
		assertPaths(result, List.of(
			new SourceTargetPair(7, 0)
		));
	}
	
	@Test
	public void selectSource2(){
		ResultGraph result = createDatabaseGraph().selectLabel(4, l0.getInverse());
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 1),
			new SourceTargetPair(4, 3)
		));
	}
	
	@Test
	public void selectSource3(){
		ResultGraph result = createDatabaseGraph().selectLabel(4, l1.getInverse());
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 5)
		));
	}
	
	@Test
	public void selectExact0(){
		ResultGraph result = createDatabaseGraph().selectLabel(4, l0, 6);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 6)
		));
	}
	
	@Test
	public void selectExact1(){
		ResultGraph result = createDatabaseGraph().selectLabel(4, l1, 6);
		
		assertPaths(result, List.of());
	}
	
	@Test
	public void selectExact2(){
		ResultGraph result = createDatabaseGraph().selectLabel(7, l1.getInverse(), 6);
		
		assertPaths(result, List.of(
			new SourceTargetPair(7, 6)
		));
	}
	
	@Test
	public void selectInv0(){
		ResultGraph result = createDatabaseGraph().selectLabel(l0.getInverse());
		
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
		ResultGraph result = createDatabaseGraph().selectLabel(l1.getInverse());
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 5),
			new SourceTargetPair(5, 7),
			new SourceTargetPair(7, 6)
		));
	}
	
	@Test
	public void id0(){
		ResultGraph result = createDatabaseGraph().selectIdentity();
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(2, 2),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(7, 7)
		));
	}
	
	@Test
	public void idBound0(){
		ResultGraph result = createDatabaseGraph().selectIdentity(4);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 4)
		));
	}
	
	private void assertPaths(ResultGraph result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().toList());
	}
	
	private static DatabaseGraph createDatabaseGraph(){
		return new DatabaseGraph(createGraph());
	}
	
	private static IntGraph createGraph(){
		/*
		 *         |---0-- 3 <-0-- 2
		 *         v
		 * 1 --0-> 4 --0-> 6 --1-> 7 --0-> 0
		 *         ^               |
		 *         |---1-- 5 <-1---|
		 */
		
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
