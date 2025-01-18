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
package dev.roanh.gmark.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IndentWriterTest{

	@Test
	public void indentTest0(){
		IndentWriter writer = new IndentWriter(2);
		writer.print("test");
		assertEquals("  test", writer.toString());
	}
	
	@Test
	public void indentTest1(){
		IndentWriter writer = new IndentWriter();
		writer.print("test");
		assertEquals("test", writer.toString());
	}
	
	@Test
	public void indentTest2(){
		IndentWriter writer = new IndentWriter(10);
		writer.print("test");
		assertEquals("          test", writer.toString());
	}
	
	@Test
	public void indentTest3(){
		IndentWriter writer = new IndentWriter(12);
		writer.print("test");
		assertEquals("            test", writer.toString());
	}
	
	@Test
	public void indentTest4(){
		IndentWriter writer = new IndentWriter(12);
		writer.println("test");
		writer.print("test2");
		assertEquals("            test\n            test2", writer.toString());
	}
	@Test
	public void indentTest5(){
		IndentWriter writer = new IndentWriter(12);
		writer.print("test");
		writer.print("hello");
		assertEquals("            testhello", writer.toString());
	}
}
