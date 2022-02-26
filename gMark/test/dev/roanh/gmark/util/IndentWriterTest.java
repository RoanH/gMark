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
}
