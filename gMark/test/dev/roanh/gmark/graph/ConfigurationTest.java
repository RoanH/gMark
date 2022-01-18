package dev.roanh.gmark.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;

public class ConfigurationTest{
	private static final Configuration config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));

	@Test
	public void inverseTest(){
		Predicate p = config.getPredicates().get(0);
		Predicate pi = p.getInverse();
		
		assertEquals(p.getID(), pi.getID());
		assertTrue(pi.isInverse());
		assertFalse(p.isInverse());
		assertEquals(p, pi.getInverse());
		assertNotEquals(p, pi);
		assertEquals(pi, p.getInverse());
	}
}
