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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class VarCQTest{

	@Test
	public void name(){
		VarCQ v = new VarCQ("test", false);
		assertEquals("test", v.getName());
		assertEquals("test", v.toString());
	}
	
	@Test
	public void equals(){
		VarCQ v = new VarCQ("test", false);
		assertEquals(v, new VarCQ("test", false));
		assertEquals(v, new VarCQ("test", true));//a CQ cannot contain two distinct variables with the same name but one being free while the other is not
		assertNotEquals(v, new VarCQ("nope", false));
		assertNotEquals(v, new VarCQ("nope", true));
	}
}
