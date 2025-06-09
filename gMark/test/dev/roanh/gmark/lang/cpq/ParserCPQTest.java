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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;

public class ParserCPQTest{

	@Test
	public void parse0(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", CPQ.parse("(0◦(((1◦0) ∩ (1◦1))◦1⁻))").toString());
	}
	
	@Test
	public void parse1(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", CPQ.parse("0◦(((1◦0) ∩ (1◦1))◦1⁻)").toString());
	}
	
	@Test
	public void parse2(){
		assertEquals("(a ∩ b ∩ c)", CPQ.parse("a∩b∩c").toString());
	}
	
	@Test
	public void parse3(){
		assertThrows(IllegalArgumentException.class, ()->CPQ.parse("(a ∩ b ∩ c"));
	}
	
	@Test
	public void parse4(){
		assertThrows(IllegalArgumentException.class, ()->CPQ.parse("a ∩ b ∩ c)"));
	}
	
	@Test
	public void parse5(){
		assertEquals(CPQ.IDENTITY, CPQ.parse("id"));
	}
	
	@Test
	public void parse6(){
		assertEquals("(b◦(a ∩ id))", CPQ.parse("b ◦ (a ∩ id)").toString());
	}
	
	@Test
	public void predicates(){
		assertArrayEquals(
			new int[]{0, 1, 2},
			CPQ.parse("a ∩ b ∩ c").toAbstractSyntaxTree().stream().filter(QueryTree::isLeaf).map(QueryTree::getEdgeAtom).map(EdgeQueryAtom::getLabel).mapToInt(Predicate::getID).distinct().sorted().toArray()
		);
	}
}
