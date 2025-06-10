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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;

public class CQTest{
	private static final Predicate l0 = new Predicate(0, "zero");
	private static final Predicate l1 = new Predicate(1, "one");

	@Test
	public void formal0(){
		CQ query = CQ.empty();
		
		VarCQ f1 = query.addFreeVariable("f1");
		VarCQ f2 = query.addFreeVariable("f2");
		VarCQ b1 = query.addBoundVariable("b1");
		
		query.addAtom(f1, l0, b1);
		query.addAtom(b1, l1, f2);

		assertEquals("(f1, f2) ← one(b1, f2), zero(f1, b1)", query.toFormalSyntax());
	}
	
	@Test
	public void xml0(){
		assertEquals(
			"""
			<cq>
			  <variables>
			    <var free=false>b1</var>
			    <var free=true>f1</var>
			    <var free=true>f2</var>
			  </variables>
			  <formulae>
			    <atom>
			      <source>b1</source>
			      <symbol>1</symbol>
			      <target>f2</target>
			    </atom>
			    <atom>
			      <source>f1</source>
			      <symbol>0</symbol>
			      <target>b1</target>
			    </atom>
			  </formulae>
			</cq>
			""",
			CQ.parse("(f1, f2) ← one(b1, f2), zero(f1, b1)", List.of(l0, l1)).toXML()
		);
	}
	
	@Test
	public void cqToSQL(){
		assertEquals(
			"""
			SELECT
			  edge1.src AS f1,
			  edge2.src AS f2,
			  edge3.trg AS f3,
			  edge4.src AS f4,
			  edge4.trg AS f5
			FROM
			  edge edge0,
			  edge edge1,
			  edge edge2,
			  edge edge3,
			  edge edge4
			WHERE
			  edge0.label = 1
			  AND
			  edge1.label = 0
			  AND
			  edge2.label = 0
			  AND
			  edge3.label = 1
			  AND
			  edge4.label = 1
			  AND
			  edge1.src = edge3.src
			  AND
			  edge0.src = edge0.trg
			  AND
			  edge0.src = edge1.trg
			  AND
			  edge0.src = edge2.trg
			""".trim(),
			CQ.parse("(f1, f2, f3, f4, f5) ← one(b2, b2), zero(f1, b2), zero(f2, b2), one(f1, f3), one(f4, f5)", List.of(l0, l1)).toSQL()
		);
	}
	
	@Test
	public void cqToSQLThrows(){
		CQ cq = CQ.empty();
		assertThrows(IllegalStateException.class, cq::toSQL);
	}
	
	@Test
	public void cq0(){
		CQ query = CQ.parse("(f1, f2) ← zero(b2, b2), one(f1, b2), one(f2, b2)", List.of(l0, l1));
		
		QueryTree ast = query.toAbstractSyntaxTree();
		assertEquals(3, ast.getArity());
		assertEquals(OperationType.JOIN, ast.getOperation());
		
		assertEquals(OperationType.EDGE, ast.getOperand(0).getOperation());
		assertEquals(l0, ast.getOperand(0).getEdgeAtom().getLabel());
		assertEquals("b2", ast.getOperand(0).getAtom().getSource().getName());
		assertFalse(ast.getOperand(0).getAtom().getSource().isFree());
		assertEquals("b2", ast.getOperand(0).getAtom().getTarget().getName());
		assertFalse(ast.getOperand(0).getAtom().getTarget().isFree());
		
		assertEquals(OperationType.EDGE, ast.getOperand(1).getOperation());
		assertEquals(l1, ast.getOperand(1).getEdgeAtom().getLabel());
		assertEquals("f1", ast.getOperand(1).getAtom().getSource().getName());
		assertTrue(ast.getOperand(1).getAtom().getSource().isFree());
		assertEquals("b2", ast.getOperand(1).getAtom().getTarget().getName());
		assertFalse(ast.getOperand(1).getAtom().getTarget().isFree());
		
		assertEquals(OperationType.EDGE, ast.getOperand(2).getOperation());
		assertEquals(l1, ast.getOperand(2).getEdgeAtom().getLabel());
		assertEquals("f2", ast.getOperand(2).getAtom().getSource().getName());
		assertTrue(ast.getOperand(2).getAtom().getSource().isFree());
		assertEquals("b2", ast.getOperand(2).getAtom().getTarget().getName());
		assertFalse(ast.getOperand(2).getAtom().getTarget().isFree());
	}
	
	//TODO AST to CQ test
}
