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
package dev.roanh.gmark.lang.rpq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;

public class RPQTest{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");

	@Test
	public void rpq0(){
		RPQ query = RPQ.labels(a, b);
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.CONCATENATION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(b, ast.getOperand(1).getEdgeAtom().getLabel());
	}
	
	@Test
	public void rpq1(){
		RPQ query = RPQ.disjunct(RPQ.label(a), RPQ.label(b), RPQ.kleene(RPQ.label(a)));
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(OperationType.DISJUNCTION, ast.getOperation());
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.DISJUNCTION, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(b, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals(OperationType.KLEENE, ast.getOperand(1).getOperation());
		
		ast = ast.getOperand(1);
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(a, ast.getOperand(0).getEdgeAtom().getLabel());
	}
}
