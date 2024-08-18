package dev.roanh.gmark.lang.rpq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ParserRPQTest{

	@Test
	public void parse0(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", RPQ.parse("(0◦(((1◦0) ∩ (1◦1))◦1⁻))").toString());
	}
	
	@Test
	public void parse1(){
		assertEquals("(0◦(((1◦0) ∩ (1◦1))◦1⁻))", RPQ.parse("0◦(((1◦0) ∩ (1◦1))◦1⁻)").toString());
	}
	
	@Test
	public void parse2(){
		assertEquals("(a ∩ b ∩ c)", RPQ.parse("a ∩ b ∩ c").toString());
	}
	
	@Test
	public void parse3(){
		assertThrows(IllegalArgumentException.class, ()->RPQ.parse("(a ∩ b ∩ c"));
	}
	
	@Test
	public void parse4(){
		assertThrows(IllegalArgumentException.class, ()->RPQ.parse("a ∩ b ∩ c)"));
	}
	
//	@Test
//	public void parse5(){
//		assertEquals(RPQ.IDENTITY, RPQ.parse("id"));
//	}
	
	@Test
	public void parse6(){
		assertEquals("(b◦(a ∩ id))", RPQ.parse("b ◦ (a ∩ id)").toString());
	}
	
	
	
	
}
