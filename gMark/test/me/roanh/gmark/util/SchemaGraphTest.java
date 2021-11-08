package me.roanh.gmark.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.util.Graph.GraphEdge;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityType;

import static dev.roanh.gmark.util.SelectivityType.of;
import static dev.roanh.gmark.core.SelectivityClass.*;

public class SchemaGraphTest{
	private Schema schema = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml")).getSchema();
	private SchemaGraph gs = new SchemaGraph(schema);

	@Test
	public void fullCheck(){
		//types
		Type t0 = schema.getTypes().get(0);
		Type t1 = schema.getTypes().get(1);
		Type t2 = schema.getTypes().get(2);
		Type t3 = schema.getTypes().get(3);
		Type t4 = schema.getTypes().get(4);
		
		//predicates
		Predicate p0 = schema.getPredicates().get(0);
		Predicate p1 = schema.getPredicates().get(1);
		Predicate p2 = schema.getPredicates().get(2);
		Predicate p3 = schema.getPredicates().get(3);
		
		//inverse predicates
		Predicate p0i = schema.getPredicates().get(0).getInverse();
		Predicate p1i = schema.getPredicates().get(1).getInverse();
		Predicate p2i = schema.getPredicates().get(2).getInverse();
		Predicate p3i = schema.getPredicates().get(3).getInverse();
		
		
		//edges that should exists (note that there are more than strictly need to exist)
		assertTrue(find(of(t0, ONE_ONE), p0, of(t1, ONE_N)));
//			1,1=1 via -1 to 0,1<N
//			0,N>1 via 0 to 1,N>1
//			1,N>1 via -1 to 0,N>1
//			0,1<N via 0 to 1,1<N
//			1,1<N via -1 to 0,1<N
//			0,N=N via 0 to 1,N<N
//			1,N=N via -1 to 0,N>N
//			0,N<N via 0 to 1,N<N
//			1,N<N via -1 to 0,N<>N
//			0,N>N via 0 to 1,NXN7
//			1,N>N via -1 to 0,N>N
//			0,N<>N via 0 to 1,NXN7
//			1,N<>N via -1 to 0,N<>N
//			0,NXN7 via 0 to 1,NXN7
//			1,NXN7 via -1 to 0,NXN7
//			1,1=1 via 1 to 3,1<N
//			3,1=1 via -2 to 1,1<N
//			1,N>1 via 1 to 3,N>1
//			3,N>1 via -2 to 1,N>1
//			1,1<N via 1 to 3,1<N
//			3,1<N via -2 to 1,1<N
//			1,N=N via 1 to 3,N=N
//			3,N=N via -2 to 1,N=N
//			1,N<N via 1 to 3,N<N
//			3,N<N via -2 to 1,N<N
//			1,N>N via 1 to 3,N>N
//			3,N>N via -2 to 1,N>N
//			1,N<>N via 1 to 3,N<>N
//			3,N<>N via -2 to 1,N<>N
//			1,NXN7 via 1 to 3,NXN7
//			3,NXN7 via -2 to 1,NXN7
//			1,1=1 via 3 to 2,1<N
//			2,1=1 via -4 to 1,1<N
//			1,N>1 via 3 to 2,N>1
//			2,N>1 via -4 to 1,N>1
//			1,1<N via 3 to 2,1<N
//			2,1<N via -4 to 1,1<N
//			1,N=N via 3 to 2,N>N
//			2,N=N via -4 to 1,N<N
//			1,N<N via 3 to 2,N<>N
//			2,N<N via -4 to 1,N<N
//			1,N>N via 3 to 2,N>N
//			2,N>N via -4 to 1,NXN7
//			1,N<>N via 3 to 2,N<>N
//			2,N<>N via -4 to 1,NXN7
//			1,NXN7 via 3 to 2,NXN7
//			2,NXN7 via -4 to 1,NXN7
//			3,1=1 via 2 to 4,1=1
//			4,1=1 via -3 to 3,1<N
//			3,N>1 via 2 to 4,N>1
//			4,N>1 via -3 to 3,NXN7
//			3,1<N via 2 to 4,1=1
//			4,1<N via -3 to 3,1<N
//			3,N=N via 2 to 4,N>1
//			4,N=N via -3 to 3,1<N
//			3,N<N via 2 to 4,N>1
//			4,N<N via -3 to 3,1<N
//			3,N>N via 2 to 4,N>1
//			4,N>N via -3 to 3,1<N
//			3,N<>N via 2 to 4,N>1
//			4,N<>N via -3 to 3,1<N
//			3,NXN7 via 2 to 4,N>1
//			4,NXN7 via -3 to 3,1<N

		assertTrue(find(of(t1, ONE_ONE), p0i, of(t0, ONE_N)));
		assertTrue(find(of(t0, N_ONE), p0, of(t1, N_ONE)));
		assertTrue(find(of(t1, N_ONE), p0i, of(t0, N_ONE)));
		assertTrue(find(of(t0, ONE_N), p0, of(t1, ONE_N)));
		assertTrue(find(of(t1, ONE_N), p0i, of(t0, ONE_N)));
		assertTrue(find(of(t0, EQUALS), p0, of(t1, LESS)));
		assertTrue(find(of(t1, EQUALS), p0i, of(t0, GREATER)));
		assertTrue(find(of(t0, LESS), p0, of(t1, LESS)));
		assertTrue(find(of(t1, LESS), p0i, of(t0, LESS_GREATER)));
		assertTrue(find(of(t0, GREATER), p0, of(t1, CROSS)));
		assertTrue(find(of(t1, GREATER), p0i, of(t0, GREATER)));
		assertTrue(find(of(t0, LESS_GREATER), p0, of(t1, CROSS)));
		assertTrue(find(of(t1, LESS_GREATER), p0i, of(t0, LESS_GREATER)));
		assertTrue(find(of(t0, CROSS), p0, of(t1, CROSS)));
		assertTrue(find(of(t1, CROSS), p0i, of(t0, CROSS)));
		assertTrue(find(of(t1, ONE_ONE), p1, of(t3, ONE_N)));
		assertTrue(find(of(t3, ONE_ONE), p1i, of(t1, ONE_N)));
		assertTrue(find(of(t1, N_ONE), p1, of(t3, N_ONE)));
		assertTrue(find(of(t3, N_ONE), p1i, of(t1, N_ONE)));
		assertTrue(find(of(t1, ONE_N), p1, of(t3, ONE_N)));
		assertTrue(find(of(t3, ONE_N), p1i, of(t1, ONE_N)));
		assertTrue(find(of(t1, EQUALS), p1, of(t3, EQUALS)));
		assertTrue(find(of(t3, EQUALS), p1i, of(t1, EQUALS)));
		assertTrue(find(of(t1, LESS), p1, of(t3, LESS)));
		assertTrue(find(of(t3, LESS), p1i, of(t1, LESS)));
		assertTrue(find(of(t1, GREATER), p1, of(t3, GREATER)));
		assertTrue(find(of(t3, GREATER), p1i, of(t1, GREATER)));
		assertTrue(find(of(t1, LESS_GREATER), p1, of(t3, LESS_GREATER)));
		assertTrue(find(of(t3, LESS_GREATER), p1i, of(t1, LESS_GREATER)));
		assertTrue(find(of(t1, CROSS), p1, of(t3, CROSS)));
		assertTrue(find(of(t3, CROSS), p1i, of(t1, CROSS)));
		assertTrue(find(of(t1, ONE_ONE), p3, of(t2, ONE_N)));
		assertTrue(find(of(t2, ONE_ONE), p3i, of(t1, ONE_N)));
		assertTrue(find(of(t1, N_ONE), p3, of(t2, N_ONE)));
		assertTrue(find(of(t2, N_ONE), p3i, of(t1, N_ONE)));
		assertTrue(find(of(t1, ONE_N), p3, of(t2, ONE_N)));
		assertTrue(find(of(t2, ONE_N), p3i, of(t1, ONE_N)));
		assertTrue(find(of(t1, EQUALS), p3, of(t2, GREATER)));
		assertTrue(find(of(t2, EQUALS), p3i, of(t1, LESS)));
		assertTrue(find(of(t1, LESS), p3, of(t2, LESS_GREATER)));
		assertTrue(find(of(t2, LESS), p3i, of(t1, LESS)));
		assertTrue(find(of(t1, GREATER), p3, of(t2, GREATER)));
		assertTrue(find(of(t2, GREATER), p3i, of(t1, CROSS)));
		assertTrue(find(of(t1, LESS_GREATER), p3, of(t2, LESS_GREATER)));
		assertTrue(find(of(t2, LESS_GREATER), p3i, of(t1, CROSS)));
		assertTrue(find(of(t1, CROSS), p3, of(t2, CROSS)));
		assertTrue(find(of(t2, CROSS), p3i, of(t1, CROSS)));
		assertTrue(find(of(t3, ONE_ONE), p2, of(t4, ONE_ONE)));
		assertTrue(find(of(t4, ONE_ONE), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, N_ONE), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, N_ONE), p2i, of(t3, CROSS)));
		assertTrue(find(of(t3, ONE_N), p2, of(t4, ONE_ONE)));
		assertTrue(find(of(t4, ONE_N), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, EQUALS), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, EQUALS), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, LESS), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, LESS), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, GREATER), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, GREATER), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, LESS_GREATER), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, LESS_GREATER), p2i, of(t3, ONE_N)));
		assertTrue(find(of(t3, CROSS), p2, of(t4, N_ONE)));
		assertTrue(find(of(t4, CROSS), p2i, of(t3, ONE_N)));
	}
	
	private boolean find(SelectivityType source, Predicate sym, SelectivityType target){
		GraphEdge<SelectivityType, Predicate> edge = gs.getEdge(source, target, sym);
		if(edge != null){
			edge.remove();
			return true;
		}else{
			return false;
		}
	}
}
