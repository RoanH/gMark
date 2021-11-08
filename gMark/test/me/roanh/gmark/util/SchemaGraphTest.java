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
