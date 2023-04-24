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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SimpleGraph.SimpleEdge;
import dev.roanh.gmark.util.SimpleGraph.SimpleVertex;

public class SimpleGraphTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");
	private static final Predicate c = new Predicate(2, "c");
	private static final Predicate d = new Predicate(3, "d");
	
	@Test
	public void build(){
		SimpleGraph<Predicate, Void> g = new SimpleGraph<Predicate, Void>(4);
		
		g.addVertex(a);
		g.addVertex(b);
		g.addVertex(c);
		g.addVertex(d);
		
		g.addEdge(a, b);
		g.addEdge(b, d);
		g.addEdge(a, c);
		
		assertEquals(4, g.getVertexCount());
		assertEquals(3, g.getEdgeCount());
	}

	@Test
	public void contract(){
		SimpleGraph<Predicate, Void> g = new SimpleGraph<Predicate, Void>(5);
		Predicate v = new Predicate(4, "v");
		
		g.addVertex(a);
		g.addVertex(b);
		g.addVertex(c);
		g.addVertex(d);
		SimpleVertex<Predicate, Void> vertex = g.addVertex(v);
		
		g.addEdge(a, b);
		g.addEdge(b, d);
		g.addEdge(a, c);
		SimpleEdge<Predicate, Void> edge = g.addEdge(c, d);
		
		g.contractEdge(edge, vertex);
		
		assertNotNull(g.getVertex(a));
		assertNotNull(g.getVertex(b));
		assertNotNull(g.getVertex(v));
		assertNull(g.getVertex(c));
		assertNull(g.getVertex(d));
		
		assertEquals(3, g.getVertexCount());
		assertEquals(3, g.getEdgeCount());
	}
}
