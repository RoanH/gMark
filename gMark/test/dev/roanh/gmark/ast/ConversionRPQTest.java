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
package dev.roanh.gmark.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.rpq.RPQ;

public class ConversionRPQTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");

	@Test
	public void ast0(){
		RPQ query = RPQ.labels(a, b);
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.CONCATENATION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getLeft().getOperation());
		assertEquals(a, ast.getLeft().getPredicate());
		assertEquals(OperationType.EDGE, ast.getRight().getOperation());
		assertEquals(b, ast.getRight().getPredicate());
	}
	
	@Test
	public void ast1(){
		RPQ query = RPQ.disjunct(RPQ.label(a), RPQ.label(b), RPQ.kleene(RPQ.label(a)));
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.DISJUNCTION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getLeft().getOperation());
		assertEquals(a, ast.getLeft().getPredicate());
		assertEquals(OperationType.DISJUNCTION, ast.getRight().getOperation());
		
		ast = ast.getRight();
		assertEquals(OperationType.EDGE, ast.getLeft().getOperation());
		assertEquals(b, ast.getLeft().getPredicate());
		assertEquals(OperationType.KLEENE, ast.getRight().getOperation());
		
		ast = ast.getRight();
		assertEquals(OperationType.EDGE, ast.getLeft().getOperation());
		assertEquals(a, ast.getLeft().getPredicate());
	}
}
