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

public class CPQTest{

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
}
