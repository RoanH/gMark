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
package dev.roanh.gmark.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.lang.rpq.RPQ;
import dev.roanh.gmark.type.schema.Predicate;

public class QueryTreeTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");

	@Test
	public void ast0(){
		CPQ query = CPQ.labels(a, b);
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.CONCATENATION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(b, ast.getOperand(1).getPredicate());
	}
	
	@Test
	public void ast1(){
		CPQ query = CPQ.intersect(CPQ.label(a), CPQ.label(b), CPQ.id());
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.INTERSECTION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.INTERSECTION, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(b, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.IDENTITY, ast.getOperand(1).getOperation());
	}

	@Test
	public void ast2(){
		RPQ query = RPQ.labels(a, b);
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.CONCATENATION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(b, ast.getOperand(1).getPredicate());
	}
	
	@Test
	public void ast3(){
		RPQ query = RPQ.disjunct(RPQ.label(a), RPQ.label(b), RPQ.kleene(RPQ.label(a)));
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.DISJUNCTION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.DISJUNCTION, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(b, ast.getOperand(0).getPredicate());
		assertEquals(OperationType.KLEENE, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
	}
	
	@Test
	public void ast4(){
		CQ query = CQ.parse("(f1, f2) ← a(b2, b2), b(f1, b2), b(f2, b2)", List.of(a, b));
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(3, ast.getArity());
		assertEquals(OperationType.JOIN, ast.getOperation());
		
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getPredicate());
		
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(b, ast.getOperand(1).getPredicate());
		
		assertEquals(OperationType.EDGE, ast.getOperand(2).getOperation());
		assertEquals(b, ast.getOperand(2).getPredicate());
	}
}
