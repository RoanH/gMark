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
package dev.roanh.gmark.lang.cq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

public class QueryGraphCQTest{
	private static final Predicate l0 = new Predicate(0, "zero");
	private static final Predicate l1 = new Predicate(1, "one");
	
	@Test
	public void split0(){
		CQ query = CQ.empty();
		
		VarCQ b1 = query.addBoundVariable("b1");
		VarCQ b2 = query.addBoundVariable("b2");
		VarCQ b3 = query.addBoundVariable("b3");
		VarCQ f1 = query.addFreeVariable("f1");
		
		query.addAtom(b1, l0, f1);
		query.addAtom(f1, l1, b2);
		query.addAtom(b3, l0, b2);
		
		//b1 --l0-> f1 --l1-> b2 <-l0-- b3
		//comp 1     | comp 2
		
		List<QueryGraphCQ> components = query.toQueryGraph().splitOnFreeVariables();
		assertEquals(2, components.size());
		
		components.sort(Comparator.comparing(QueryGraphCQ::getVertexCount));
		assertEquals("(f1) ← zero(b1, f1)", components.get(0).toCQ().toFormalSyntax());
		assertEquals("(f1) ← one(f1, b2), zero(b3, b2)", components.get(1).toCQ().toFormalSyntax());
	}
	
	@Test
	public void split1(){
		CQ query = CQ.empty();
		
		VarCQ b1 = query.addBoundVariable("b1");
		VarCQ f1 = query.addFreeVariable("f1");
		
		query.addAtom(b1, l0, f1);
		
		//b1 --l0-> f1
		//comp 1
		
		List<QueryGraphCQ> components = query.toQueryGraph().splitOnFreeVariables();
		assertEquals(1, components.size());
		assertEquals("(f1) ← zero(b1, f1)", components.get(0).toCQ().toFormalSyntax());
	}
	
	@Test
	public void split2(){
		CQ query = CQ.empty();
		
		VarCQ f1 = query.addFreeVariable("f1");
		query.addAtom(f1, l0, f1);
		
		//self loop on f1, one component
		
		List<QueryGraphCQ> components = query.toQueryGraph().splitOnFreeVariables();
		assertEquals(1, components.size());
		assertEquals("(f1) ← zero(f1, f1)", components.get(0).toCQ().toFormalSyntax());
	}
	
	@Test
	public void split3(){
		CQ query = CQ.empty();
		
		VarCQ b1 = query.addBoundVariable("b1");
		VarCQ b2 = query.addBoundVariable("b2");
		VarCQ f1 = query.addFreeVariable("f1");
		VarCQ f2 = query.addFreeVariable("f2");
		
		query.addAtom(f1, l0, f1);
		query.addAtom(f1, l0, b1);
		query.addAtom(f1, l1, f2);
		query.addAtom(f1, l0, b2);
		query.addAtom(b2, l1, b2);
		query.addAtom(f2, l0, b2);
		query.addAtom(b1, l1, f2);
		
		//l0       l1
		//o        o
		//f1 -l0-> b2
		//| \       ^
		//l0  l1    l0
		//v      \v |
		//b1 -l1-> f2
		
		List<QueryGraphCQ> components = query.toQueryGraph().splitOnFreeVariables();
		System.out.println(components);
		assertEquals(4, components.size());
		
		components.sort(Comparator.comparing(QueryGraphCQ::getEdgeCount).thenComparing(QueryGraphCQ::getVertexCount));
		assertEquals("(f1) ← zero(f1, f1)", components.get(0).toCQ().toFormalSyntax());
		assertEquals("(f1, f2) ← one(f1, f2)", components.get(1).toCQ().toFormalSyntax());
		assertEquals("(f1, f2) ← one(b1, f2), zero(f1, b1)", components.get(2).toCQ().toFormalSyntax());
		assertEquals("(f1, f2) ← one(b2, b2), zero(f1, b2), zero(f2, b2)", components.get(3).toCQ().toFormalSyntax());
	}
}
