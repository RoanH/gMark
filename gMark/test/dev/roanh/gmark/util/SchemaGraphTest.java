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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;

import static dev.roanh.gmark.util.SelectivityType.of;
import static dev.roanh.gmark.core.SelectivityClass.*;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.util.Graph.GraphEdge;

public class SchemaGraphTest{
	private static Schema schema;
	private SchemaGraph gs = new SchemaGraph(schema);

	@BeforeAll
	public static void parseConfig(){
		try{
			schema = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml")).getSchema();
		}catch(ConfigException e){
			e.printStackTrace();
		}
	}
	
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
		
		//no extra edges
		assertEquals(0, gs.getEdgeCount());
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
