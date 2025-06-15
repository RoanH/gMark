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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ParserCQTest{

	@Test
	public void parse0(){
		assertEquals("(f1, f2) ← one(b2, b2), zero(f1, b2), zero(f2, b2)", CQ.parse("(f1, f2) ← one(b2, b2), zero(f1, b2), zero(f2, b2)").toString());
	}
	
	@Test
	public void parse1(){
		assertEquals("(f1) ← zero(f1, b1)", CQ.parse("(f1) ← zero(f1, b1)").toString());
	}
	
	@Test
	public void parse2(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← "));
	}
	
	@Test
	public void parse3(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse(" ← zero(f1, b1)"));
	}
	
	@Test
	public void parse4(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1)  zero(f1, b1)"));
	}
	
	@Test
	public void parse5(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1 ← zero(f1, b1)"));
	}
	
	@Test
	public void parse6(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("f1) ← zero(f1, b1)"));
	}
	
	@Test
	public void parse7(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← zero(f1, b1)a"));
	}
	
	@Test
	public void parse8(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← zero f1, b1)"));
	}
	
	@Test
	public void parse9(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← zero(f1, b1, b2)"));
	}
	
	@Test
	public void parse10(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← zero(f1)"));
	}
	
	@Test
	public void parse11(){
		assertThrows(IllegalArgumentException.class, ()->CQ.parse("(f1) ← zero()"));
	}
}
