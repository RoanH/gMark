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
package dev.roanh.gmark.util.graph.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

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
}
