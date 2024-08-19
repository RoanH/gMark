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
package dev.roanh.gmark.lang.rpq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ParserRPQTest{

	@Test
	public void parse0(){
		assertEquals("(0◦(((1◦0) ∪ (1◦1))◦1⁻))", RPQ.parse("(0◦(((1◦0) ∪ (1◦1))◦1⁻))").toString());
	}
	
	@Test
	public void parse1(){
		assertEquals("(0◦(((1◦0) ∪ (1◦1))◦1⁻))", RPQ.parse("0◦(((1◦0) ∪ (1◦1))◦1⁻)").toString());
	}
	
	@Test
	public void parse2(){
		assertEquals("(a ∪ b ∪ c)", RPQ.parse("a ∪ b ∪ c").toString());
	}
	
	@Test
	public void parse3(){
		assertThrows(IllegalArgumentException.class, ()->RPQ.parse("(a ∪ b ∪ c"));
	}
	
	@Test
	public void parse4(){
		assertThrows(IllegalArgumentException.class, ()->RPQ.parse("a ∪ b ∪ c)"));
	}
	
	@Test
	public void parse5(){
		assertEquals("(a ∪ b⁻ ∪ c)", RPQ.parse("a ∪ b⁻ ∪ c").toString());
	}
	
	@Test
	public void parse6(){
		assertEquals("(a ∪ b⁻* ∪ c)", RPQ.parse("a ∪ b⁻* ∪ c").toString());
	}
	
	@Test
	public void parse7(){
		assertEquals("(a ∪ (a◦b⁻)* ∪ c)", RPQ.parse("a ∪ (a ◦ b⁻)* ∪ c").toString());
	}
}
