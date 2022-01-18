package dev.roanh.gmark.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.conjunct.cpq.CPQ;
import dev.roanh.gmark.conjunct.cpq.EdgeCPQ;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;

public class ConversionTest{
	private static final Configuration config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
	private static final CPQ label0 = new EdgeCPQ(config.getPredicates().get(0));
	private static final CPQ label1 = new EdgeCPQ(config.getPredicates().get(1));
	private static final CPQ label1i = new EdgeCPQ(config.getPredicates().get(1).getInverse());
	
	
	
	
	@Test
	public void predicateToSQL(){
		Predicate p = config.getPredicates().get(1);
		
		assertEquals("(SELECT src, trg FROM edge WHERE label = 1)", p.toSQL());
		assertEquals("(SELECT trg AS src, src AS trg FROM edge WHERE label = 1)", p.getInverse().toSQL());
	}
	
	@Test
	public void labelToSQL(){
		assertEquals("(SELECT src, trg FROM edge WHERE label = 0)", label0.toSQL());
		assertEquals("(SELECT src, trg FROM edge WHERE label = 1)", label1.toSQL());
		assertEquals("(SELECT trg AS src, src AS trg FROM edge WHERE label = 1)", label1i.toSQL());
	}
	
	@Test
	public void identityThrows(){
		assertThrows(IllegalStateException.class, CPQ.IDENTITY.toSQL());
	}
}
