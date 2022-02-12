package dev.roanh.gmark.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.exception.ConfigException;

public class PredicateTest{
	private static Configuration config;

	@BeforeAll
	public static void parseConfig(){
		try{
			config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
		}catch(ConfigException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void inverseTest(){
		Predicate p = config.getPredicates().get(0);
		Predicate pi = p.getInverse();
		
		assertEquals(p.getID(), pi.getID());
		assertTrue(pi.isInverse());
		assertFalse(p.isInverse());
		assertNotEquals(p.getAlias(), pi.getAlias());
	}
	
	@Test
	public void equalsTest(){
		Predicate p = config.getPredicates().get(0);
		Predicate pi = p.getInverse();
		
		assertEquals(p, pi.getInverse());
		assertNotEquals(p, pi);
		assertEquals(pi, p.getInverse());
		assertNotEquals(p, new Object());
		assertNotEquals(p, config.getPredicates().get(1));
		assertNotEquals(pi, config.getPredicates().get(1));
	}
}
