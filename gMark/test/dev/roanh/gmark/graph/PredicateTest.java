/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
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
