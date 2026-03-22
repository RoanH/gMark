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
package dev.roanh.gmark.lang.cpq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;

public class CPQTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");

	@Test
	public void dia0(){
		assertEquals(3, CPQ.parse("((a◦(b ∩ id)◦c) ∩ id)").getDiameter());
	}
	
	@Test
	public void dia1(){
		assertEquals(1, CPQ.parse("a").getDiameter());
	}
	
	@Test
	public void dia2(){
		assertEquals(3, CPQ.parse("(a◦a◦a)").getDiameter());
	}
	
	@Test
	public void dia3(){
		assertEquals(3, CPQ.parse("(a◦a◦a) ∩ (a◦a)").getDiameter());
	}
	
	@Test
	public void dia4(){
		assertEquals(1, CPQ.parse("a⁻").getDiameter());
	}
	
	@Test
	public void dia5(){
		assertEquals(0, CPQ.parse("id").getDiameter());
	}
	
	@Test
	public void CPQtoCQ(){
		assertEquals(
			"(src, trg) ← a(src, 0), b(0, trg), c(src, trg)",
			CPQ.parse("(a◦b) ∩ c").toQueryGraph().toQueryGraphCQ().toCQ().toFormalSyntax()
		);
	}

	@Test
	public void cpq0(){
		CPQ query = CPQ.labels(a, b);
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.CONCATENATION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(b, ast.getOperand(1).getEdgeAtom().getLabel());
	}
	
	@Test
	public void cpq1(){
		CPQ query = CPQ.intersect(CPQ.label(a), CPQ.label(b), CPQ.id());
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.INTERSECTION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.INTERSECTION, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(b, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.IDENTITY, ast.getOperand(1).getOperation());
	}
	
	@Test
	public void inverse0(){
		assertEquals("a⁻", CPQ.parse("a").inverse().toString());
	}
	
	@Test
	public void inverse1(){
		assertEquals("(a⁻ ∩ b⁻)", CPQ.parse("a ∩ b").inverse().toString());
	}
	
	@Test
	public void inverse2(){
		assertEquals("(b⁻◦a)", CPQ.parse("a⁻ ◦ b").inverse().toString());
	}
	
	@Test
	public void inverse3(){
		assertEquals("id", CPQ.parse("id").inverse().toString());
	}
	
	@Test
	public void inverse4(){
		assertEquals("(b⁻◦(a⁻ ∩ b⁻))", CPQ.parse("(a ∩ b) ◦ b").inverse().toString());
	}
}
