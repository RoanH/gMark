package dev.roanh.gmark.lang.cq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ParserCQTest{

	@Test
	public void parse0(){
		assertEquals("(f1, f2) ← one(b2, b2), zero(f1, b2), zero(f2, b2)", CQ.parse("(f1, f2) ← one(b2, b2), zero(f1, b2), zero(f2, b2)").toString());
	}
	
	//TODO more tests
}
