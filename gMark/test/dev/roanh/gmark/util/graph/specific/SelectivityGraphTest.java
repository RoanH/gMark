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
package dev.roanh.gmark.util.graph.specific;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;

import static dev.roanh.gmark.data.SelectivityType.of;
import static dev.roanh.gmark.type.SelectivityClass.*;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.data.SelectivityType;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.gen.workload.ConfigParser;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.gmark.type.SelectivityClass;
import dev.roanh.gmark.type.schema.Schema;
import dev.roanh.gmark.type.schema.Type;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.specific.SelectivityGraph.DistanceMatrix;

public class SelectivityGraphTest{
	private static Schema schema;
	
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
		SelectivityGraph gs = new SelectivityGraph(schema, 4);
		
		//types
		Type t0 = schema.getTypes().get(0);
		Type t1 = schema.getTypes().get(1);
		Type t2 = schema.getTypes().get(2);
		Type t3 = schema.getTypes().get(3);
		Type t4 = schema.getTypes().get(4);
		
		//edges that should exists
		assertTrue(find(gs, of(t0, ONE_ONE), LESS_GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_ONE), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t0, N_ONE), LESS_GREATER, of(t0, N_ONE)));
		assertTrue(find(gs, of(t0, N_ONE), CROSS, of(t0, N_ONE)));
		assertTrue(find(gs, of(t0, ONE_N), LESS_GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_N), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t0, EQUALS), LESS_GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t0, EQUALS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, LESS), LESS_GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t0, LESS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t0, ONE_ONE), LESS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_ONE), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t0, N_ONE), LESS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t0, N_ONE), CROSS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t0, ONE_N), LESS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_N), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t0, EQUALS), LESS, of(t1, LESS)));
		assertTrue(find(gs, of(t0, EQUALS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, LESS), LESS, of(t1, LESS)));
		assertTrue(find(gs, of(t0, LESS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t0, ONE_ONE), LESS_GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_ONE), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t0, N_ONE), LESS_GREATER, of(t2, N_ONE)));
		assertTrue(find(gs, of(t0, N_ONE), CROSS, of(t2, N_ONE)));
		assertTrue(find(gs, of(t0, ONE_N), LESS_GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_N), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t0, EQUALS), LESS_GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t0, EQUALS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, LESS), LESS_GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t0, LESS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t0, ONE_ONE), LESS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_ONE), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t0, N_ONE), LESS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t0, N_ONE), CROSS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t0, ONE_N), LESS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t0, ONE_N), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t0, EQUALS), LESS, of(t3, LESS)));
		assertTrue(find(gs, of(t0, EQUALS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, LESS), LESS, of(t3, LESS)));
		assertTrue(find(gs, of(t0, LESS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, LESS_GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, CROSS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t0, ONE_ONE), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t0, N_ONE), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t0, ONE_N), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t0, EQUALS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t0, LESS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t0, GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t0, LESS_GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t0, CROSS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_ONE), GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_ONE), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t1, N_ONE), GREATER, of(t0, N_ONE)));
		assertTrue(find(gs, of(t1, N_ONE), CROSS, of(t0, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_N), GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_N), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t1, EQUALS), GREATER, of(t0, GREATER)));
		assertTrue(find(gs, of(t1, EQUALS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, LESS), GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, GREATER), GREATER, of(t0, GREATER)));
		assertTrue(find(gs, of(t1, GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, LESS_GREATER), GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS_GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t1, ONE_ONE), EQUALS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_ONE), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t1, N_ONE), EQUALS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t1, N_ONE), CROSS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_N), EQUALS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_N), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t1, EQUALS), EQUALS, of(t1, EQUALS)));
		assertTrue(find(gs, of(t1, EQUALS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, LESS), EQUALS, of(t1, LESS)));
		assertTrue(find(gs, of(t1, LESS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, GREATER), EQUALS, of(t1, GREATER)));
		assertTrue(find(gs, of(t1, GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, LESS_GREATER), EQUALS, of(t1, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS_GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), EQUALS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t1, ONE_ONE), GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_ONE), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t1, N_ONE), GREATER, of(t2, N_ONE)));
		assertTrue(find(gs, of(t1, N_ONE), CROSS, of(t2, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_N), GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_N), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t1, EQUALS), GREATER, of(t2, GREATER)));
		assertTrue(find(gs, of(t1, EQUALS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, LESS), GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, GREATER), GREATER, of(t2, GREATER)));
		assertTrue(find(gs, of(t1, GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, LESS_GREATER), GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS_GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t1, ONE_ONE), EQUALS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_ONE), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t1, N_ONE), EQUALS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t1, N_ONE), CROSS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_N), EQUALS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t1, ONE_N), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t1, EQUALS), EQUALS, of(t3, EQUALS)));
		assertTrue(find(gs, of(t1, EQUALS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, LESS), EQUALS, of(t3, LESS)));
		assertTrue(find(gs, of(t1, LESS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, GREATER), EQUALS, of(t3, GREATER)));
		assertTrue(find(gs, of(t1, GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, LESS_GREATER), EQUALS, of(t3, LESS_GREATER)));
		assertTrue(find(gs, of(t1, LESS_GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), EQUALS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, CROSS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t1, ONE_ONE), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t1, N_ONE), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, ONE_N), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t1, EQUALS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, LESS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, LESS_GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t1, CROSS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_ONE), LESS_GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_ONE), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t2, N_ONE), LESS_GREATER, of(t0, N_ONE)));
		assertTrue(find(gs, of(t2, N_ONE), CROSS, of(t0, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_N), LESS_GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_N), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t2, EQUALS), LESS_GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t2, EQUALS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, LESS), LESS_GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t2, LESS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), LESS_GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t2, ONE_ONE), LESS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_ONE), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t2, N_ONE), LESS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t2, N_ONE), CROSS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_N), LESS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_N), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t2, EQUALS), LESS, of(t1, LESS)));
		assertTrue(find(gs, of(t2, EQUALS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, LESS), LESS, of(t1, LESS)));
		assertTrue(find(gs, of(t2, LESS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), LESS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t2, ONE_ONE), LESS_GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_ONE), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t2, N_ONE), LESS_GREATER, of(t2, N_ONE)));
		assertTrue(find(gs, of(t2, N_ONE), CROSS, of(t2, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_N), LESS_GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_N), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t2, EQUALS), LESS_GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t2, EQUALS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, LESS), LESS_GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t2, LESS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), LESS_GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t2, ONE_ONE), LESS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_ONE), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t2, N_ONE), LESS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t2, N_ONE), CROSS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_N), LESS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t2, ONE_N), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t2, EQUALS), LESS, of(t3, LESS)));
		assertTrue(find(gs, of(t2, EQUALS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, LESS), LESS, of(t3, LESS)));
		assertTrue(find(gs, of(t2, LESS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, LESS_GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), LESS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, CROSS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t2, ONE_ONE), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t2, N_ONE), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, ONE_N), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t2, EQUALS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, LESS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, LESS_GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t2, CROSS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_ONE), GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_ONE), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t3, N_ONE), GREATER, of(t0, N_ONE)));
		assertTrue(find(gs, of(t3, N_ONE), CROSS, of(t0, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_N), GREATER, of(t0, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_N), CROSS, of(t0, ONE_N)));
		assertTrue(find(gs, of(t3, EQUALS), GREATER, of(t0, GREATER)));
		assertTrue(find(gs, of(t3, EQUALS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, LESS), GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, GREATER), GREATER, of(t0, GREATER)));
		assertTrue(find(gs, of(t3, GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, LESS_GREATER), GREATER, of(t0, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS_GREATER), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), GREATER, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), CROSS, of(t0, CROSS)));
		assertTrue(find(gs, of(t3, ONE_ONE), EQUALS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_ONE), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t3, N_ONE), EQUALS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t3, N_ONE), CROSS, of(t1, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_N), EQUALS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_N), CROSS, of(t1, ONE_N)));
		assertTrue(find(gs, of(t3, EQUALS), EQUALS, of(t1, EQUALS)));
		assertTrue(find(gs, of(t3, EQUALS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, LESS), EQUALS, of(t1, LESS)));
		assertTrue(find(gs, of(t3, LESS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, GREATER), EQUALS, of(t1, GREATER)));
		assertTrue(find(gs, of(t3, GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, LESS_GREATER), EQUALS, of(t1, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS_GREATER), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), EQUALS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), CROSS, of(t1, CROSS)));
		assertTrue(find(gs, of(t3, ONE_ONE), GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_ONE), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t3, N_ONE), GREATER, of(t2, N_ONE)));
		assertTrue(find(gs, of(t3, N_ONE), CROSS, of(t2, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_N), GREATER, of(t2, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_N), CROSS, of(t2, ONE_N)));
		assertTrue(find(gs, of(t3, EQUALS), GREATER, of(t2, GREATER)));
		assertTrue(find(gs, of(t3, EQUALS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, LESS), GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, GREATER), GREATER, of(t2, GREATER)));
		assertTrue(find(gs, of(t3, GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, LESS_GREATER), GREATER, of(t2, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS_GREATER), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), GREATER, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), CROSS, of(t2, CROSS)));
		assertTrue(find(gs, of(t3, ONE_ONE), EQUALS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_ONE), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t3, N_ONE), EQUALS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t3, N_ONE), CROSS, of(t3, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_N), EQUALS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t3, ONE_N), CROSS, of(t3, ONE_N)));
		assertTrue(find(gs, of(t3, EQUALS), EQUALS, of(t3, EQUALS)));
		assertTrue(find(gs, of(t3, EQUALS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, LESS), EQUALS, of(t3, LESS)));
		assertTrue(find(gs, of(t3, LESS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, GREATER), EQUALS, of(t3, GREATER)));
		assertTrue(find(gs, of(t3, GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, LESS_GREATER), EQUALS, of(t3, LESS_GREATER)));
		assertTrue(find(gs, of(t3, LESS_GREATER), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), EQUALS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, CROSS), CROSS, of(t3, CROSS)));
		assertTrue(find(gs, of(t3, ONE_ONE), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t3, N_ONE), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, ONE_N), N_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t3, EQUALS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, LESS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, LESS_GREATER), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t3, CROSS), N_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t4, ONE_ONE), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, N_ONE), ONE_N, of(t0, CROSS)));
		assertTrue(find(gs, of(t4, ONE_N), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, EQUALS), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, LESS), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, GREATER), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, LESS_GREATER), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, CROSS), ONE_N, of(t0, ONE_N)));
		assertTrue(find(gs, of(t4, ONE_ONE), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, N_ONE), ONE_N, of(t1, CROSS)));
		assertTrue(find(gs, of(t4, ONE_N), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, EQUALS), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, LESS), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, GREATER), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, LESS_GREATER), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, CROSS), ONE_N, of(t1, ONE_N)));
		assertTrue(find(gs, of(t4, ONE_ONE), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, N_ONE), ONE_N, of(t2, CROSS)));
		assertTrue(find(gs, of(t4, ONE_N), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, EQUALS), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, LESS), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, GREATER), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, LESS_GREATER), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, CROSS), ONE_N, of(t2, ONE_N)));
		assertTrue(find(gs, of(t4, ONE_ONE), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, N_ONE), ONE_N, of(t3, CROSS)));
		assertTrue(find(gs, of(t4, ONE_N), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, EQUALS), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, LESS), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, GREATER), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, LESS_GREATER), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, CROSS), ONE_N, of(t3, ONE_N)));
		assertTrue(find(gs, of(t4, ONE_ONE), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, N_ONE), ONE_ONE, of(t4, N_ONE)));
		assertTrue(find(gs, of(t4, ONE_N), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, EQUALS), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, LESS), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, GREATER), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, LESS_GREATER), ONE_ONE, of(t4, ONE_ONE)));
		assertTrue(find(gs, of(t4, CROSS), ONE_ONE, of(t4, ONE_ONE)));

		//no extra edges
		assertEquals(0, gs.getEdgeCount());
	}
	
	private boolean find(SelectivityGraph gs, SelectivityType source, SelectivityClass sym, SelectivityType target){
		GraphEdge<SelectivityType, SelectivityClass> edge = gs.getEdge(source, target, sym);
		if(edge != null){
			edge.remove();
			return true;
		}else{
			return false;
		}
	}
	
	@Test
	public void checkNumberOfPathsMatrix(){
		DistanceMatrix matrix = new SelectivityGraph(schema, 4).computeNumberOfPaths(Selectivity.QUADRATIC, 3);
		
		assertEquals(0, matrix.get(1, 0).getOrDefault(ONE_ONE, 0));
		assertEquals(0, matrix.get(1, 0).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(1, 0).getOrDefault(ONE_N, 0));
		assertEquals(4, matrix.get(1, 0).getOrDefault(EQUALS, 0));
		assertEquals(4, matrix.get(1, 0).getOrDefault(LESS, 0));
		assertEquals(8, matrix.get(1, 0).getOrDefault(GREATER, 0));
		assertEquals(8, matrix.get(1, 0).getOrDefault(LESS_GREATER, 0));
		assertEquals(8, matrix.get(1, 0).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(1, 1).getOrDefault(ONE_ONE, 0));
		assertEquals(0, matrix.get(1, 1).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(1, 1).getOrDefault(ONE_N, 0));
		assertEquals(4, matrix.get(1, 1).getOrDefault(EQUALS, 0));
		assertEquals(4, matrix.get(1, 1).getOrDefault(LESS, 0));
		assertEquals(4, matrix.get(1, 1).getOrDefault(GREATER, 0));
		assertEquals(4, matrix.get(1, 1).getOrDefault(LESS_GREATER, 0));
		assertEquals(8, matrix.get(1, 1).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(1, 2).getOrDefault(ONE_ONE, 0));
		assertEquals(0, matrix.get(1, 2).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(1, 2).getOrDefault(ONE_N, 0));
		assertEquals(4, matrix.get(1, 2).getOrDefault(EQUALS, 0));
		assertEquals(4, matrix.get(1, 2).getOrDefault(LESS, 0));
		assertEquals(8, matrix.get(1, 2).getOrDefault(GREATER, 0));
		assertEquals(8, matrix.get(1, 2).getOrDefault(LESS_GREATER, 0));
		assertEquals(8, matrix.get(1, 2).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(1, 3).getOrDefault(ONE_ONE, 0));
		assertEquals(0, matrix.get(1, 3).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(1, 3).getOrDefault(ONE_N, 0));
		assertEquals(4, matrix.get(1, 3).getOrDefault(EQUALS, 0));
		assertEquals(4, matrix.get(1, 3).getOrDefault(LESS, 0));
		assertEquals(4, matrix.get(1, 3).getOrDefault(GREATER, 0));
		assertEquals(4, matrix.get(1, 3).getOrDefault(LESS_GREATER, 0));
		assertEquals(8, matrix.get(1, 3).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(ONE_ONE, 0));
		assertEquals(4, matrix.get(1, 4).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(ONE_N, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(EQUALS, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(LESS, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(GREATER, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(LESS_GREATER, 0));
		assertEquals(0, matrix.get(1, 4).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(2, 0).getOrDefault(ONE_ONE, 0));
		assertEquals(4, matrix.get(2, 0).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(2, 0).getOrDefault(ONE_N, 0));
		assertEquals(60, matrix.get(2, 0).getOrDefault(EQUALS, 0));
		assertEquals(60, matrix.get(2, 0).getOrDefault(LESS, 0));
		assertEquals(68, matrix.get(2, 0).getOrDefault(GREATER, 0));
		assertEquals(68, matrix.get(2, 0).getOrDefault(LESS_GREATER, 0));
		assertEquals(68, matrix.get(2, 0).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(2, 1).getOrDefault(ONE_ONE, 0));
		assertEquals(4, matrix.get(2, 1).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(2, 1).getOrDefault(ONE_N, 0));
		assertEquals(60, matrix.get(2, 1).getOrDefault(EQUALS, 0));
		assertEquals(60, matrix.get(2, 1).getOrDefault(LESS, 0));
		assertEquals(60, matrix.get(2, 1).getOrDefault(GREATER, 0));
		assertEquals(60, matrix.get(2, 1).getOrDefault(LESS_GREATER, 0));
		assertEquals(68, matrix.get(2, 1).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(2, 2).getOrDefault(ONE_ONE, 0));
		assertEquals(4, matrix.get(2, 2).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(2, 2).getOrDefault(ONE_N, 0));
		assertEquals(60, matrix.get(2, 2).getOrDefault(EQUALS, 0));
		assertEquals(60, matrix.get(2, 2).getOrDefault(LESS, 0));
		assertEquals(68, matrix.get(2, 2).getOrDefault(GREATER, 0));
		assertEquals(68, matrix.get(2, 2).getOrDefault(LESS_GREATER, 0));
		assertEquals(68, matrix.get(2, 2).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(2, 3).getOrDefault(ONE_ONE, 0));
		assertEquals(4, matrix.get(2, 3).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(2, 3).getOrDefault(ONE_N, 0));
		assertEquals(60, matrix.get(2, 3).getOrDefault(EQUALS, 0));
		assertEquals(60, matrix.get(2, 3).getOrDefault(LESS, 0));
		assertEquals(60, matrix.get(2, 3).getOrDefault(GREATER, 0));
		assertEquals(60, matrix.get(2, 3).getOrDefault(LESS_GREATER, 0));
		assertEquals(68, matrix.get(2, 3).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(ONE_ONE, 0));
		assertEquals(36, matrix.get(2, 4).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(ONE_N, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(EQUALS, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(LESS, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(GREATER, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(LESS_GREATER, 0));
		assertEquals(0, matrix.get(2, 4).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(3, 0).getOrDefault(ONE_ONE, 0));
		assertEquals(68, matrix.get(3, 0).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(3, 0).getOrDefault(ONE_N, 0));
		assertEquals(564, matrix.get(3, 0).getOrDefault(EQUALS, 0));
		assertEquals(564, matrix.get(3, 0).getOrDefault(LESS, 0));
		assertEquals(580, matrix.get(3, 0).getOrDefault(GREATER, 0));
		assertEquals(580, matrix.get(3, 0).getOrDefault(LESS_GREATER, 0));
		assertEquals(580, matrix.get(3, 0).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(3, 1).getOrDefault(ONE_ONE, 0));
		assertEquals(68, matrix.get(3, 1).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(3, 1).getOrDefault(ONE_N, 0));
		assertEquals(564, matrix.get(3, 1).getOrDefault(EQUALS, 0));
		assertEquals(564, matrix.get(3, 1).getOrDefault(LESS, 0));
		assertEquals(564, matrix.get(3, 1).getOrDefault(GREATER, 0));
		assertEquals(564, matrix.get(3, 1).getOrDefault(LESS_GREATER, 0));
		assertEquals(580, matrix.get(3, 1).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(3, 2).getOrDefault(ONE_ONE, 0));
		assertEquals(68, matrix.get(3, 2).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(3, 2).getOrDefault(ONE_N, 0));
		assertEquals(564, matrix.get(3, 2).getOrDefault(EQUALS, 0));
		assertEquals(564, matrix.get(3, 2).getOrDefault(LESS, 0));
		assertEquals(580, matrix.get(3, 2).getOrDefault(GREATER, 0));
		assertEquals(580, matrix.get(3, 2).getOrDefault(LESS_GREATER, 0));
		assertEquals(580, matrix.get(3, 2).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(3, 3).getOrDefault(ONE_ONE, 0));
		assertEquals(68, matrix.get(3, 3).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(3, 3).getOrDefault(ONE_N, 0));
		assertEquals(564, matrix.get(3, 3).getOrDefault(EQUALS, 0));
		assertEquals(564, matrix.get(3, 3).getOrDefault(LESS, 0));
		assertEquals(564, matrix.get(3, 3).getOrDefault(GREATER, 0));
		assertEquals(564, matrix.get(3, 3).getOrDefault(LESS_GREATER, 0));
		assertEquals(580, matrix.get(3, 3).getOrDefault(CROSS, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(ONE_ONE, 0));
		assertEquals(308, matrix.get(3, 4).getOrDefault(N_ONE, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(ONE_N, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(EQUALS, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(LESS, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(GREATER, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(LESS_GREATER, 0));
		assertEquals(0, matrix.get(3, 4).getOrDefault(CROSS, 0));
	}
}
