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
package dev.roanh.gmark.lang.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguageSyntax;

public class GenericParserTest{

	@Test
	public void split0(){
		assertIterableEquals(Arrays.asList("abc"), GenericParser.split("abc", '.'));
	}
	
	@Test
	public void split1(){
		assertIterableEquals(Arrays.asList("abc", "def"), GenericParser.split("abc.def", '.'));
	}
	
	@Test
	public void split2(){
		assertIterableEquals(Arrays.asList("abc", "(def.123)"), GenericParser.split("abc.(def.123)", '.'));
	}
	
	@Test
	public void split3(){
		assertIterableEquals(Arrays.asList("7", "abc", "(def.(123.0))"), GenericParser.split("7.abc.(def.(123.0))", '.'));
	}
	
	@Test
	public void split4(){
		assertIterableEquals(Arrays.asList("(7.6)", "abc", "(def.(123.0))"), GenericParser.split("(7.6).abc.(def.(123.0))", '.'));
	}
	
	@Test
	public void split5(){
		assertThrows(IllegalArgumentException.class, ()->GenericParser.split("7.(234.5", '.'));
	}
	
	@Test
	public void split6(){
		assertIterableEquals(Arrays.asList("abc", ""), GenericParser.split("abc.", '.'));
	}
	
	@Test
	public void split7(){
		assertIterableEquals(Arrays.asList("abc"), GenericParser.split("abc ", '.'));
	}
	
	@Test
	public void split8(){
		assertIterableEquals(Arrays.asList("abc", "def"), GenericParser.split("abc .def ", '.'));
	}
	
	@Test
	public void split9(){
		assertIterableEquals(Arrays.asList("(abc)"), GenericParser.split("(abc)", '.'));
	}
	
	@Test
	public void predicate0(){
		assertThrows(IllegalArgumentException.class, ()->GenericParser.parsePredicate("1⁻⁻", new HashMap<String, Predicate>(), QueryLanguageSyntax.CHAR_INVERSE));
	}
	
	@Test
	public void predicate1(){
		Map<String, Predicate> labels = new HashMap<String, Predicate>();
		Predicate p =  GenericParser.parsePredicate("1⁻", labels, QueryLanguageSyntax.CHAR_INVERSE);
		assertEquals("1⁻", p.getAlias());
		assertTrue(p.isInverse());
		assertEquals(p, labels.get("1").getInverse());
	}
	
	@Test
	public void predicate2(){
		Map<String, Predicate> labels = new HashMap<String, Predicate>();
		Predicate p =  GenericParser.parsePredicate("1", labels, QueryLanguageSyntax.CHAR_INVERSE);
		assertEquals("1", p.getAlias());
		assertFalse(p.isInverse());
		assertEquals(p, labels.get("1"));
	}
}
