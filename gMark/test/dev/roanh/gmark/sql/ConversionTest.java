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
package dev.roanh.gmark.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.conjunct.cpq.CPQ;
import dev.roanh.gmark.conjunct.cpq.ConcatCPQ;
import dev.roanh.gmark.conjunct.cpq.EdgeCPQ;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.exception.ConfigException;

public class ConversionTest{
	private static Configuration config;
	private static CPQ label0;
	private static CPQ label1;
	private static CPQ label1i;
	private static CPQ concat0;
	private static CPQ concat1;
	
	@BeforeAll
	public static void parseConfig(){
		try{
			config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
			label0 = new EdgeCPQ(config.getPredicates().get(0));
			label1 = new EdgeCPQ(config.getPredicates().get(1));
			label1i = new EdgeCPQ(config.getPredicates().get(1).getInverse());
			concat0 = new ConcatCPQ(Arrays.asList(label0, label1, label1, label1i));
			concat1 = new ConcatCPQ(Arrays.asList(label1));
		}catch(ConfigException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void concatToSQL(){
		assertEquals(
			"(SELECT s0.src AS src, s3.trg AS trg FROM"
			+ " (SELECT src, trg FROM edge WHERE label = 0) AS s0,"
			+ " (SELECT src, trg FROM edge WHERE label = 1) AS s1,"
			+ " (SELECT src, trg FROM edge WHERE label = 1) AS s2,"
			+ " (SELECT trg AS src, src AS trg FROM edge WHERE label = 1) AS s3"
			+ " WHERE s0.trg = s1.src AND s1.trg = s2.src AND s2.trg = s3.src)", concat0.toSQL()
		);
		assertEquals("(SELECT src, trg FROM edge WHERE label = 1)", concat1.toSQL());
	}
	
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
	public void identityToSQL(){
		assertEquals("((SELECT src, src AS trg FROM edge) UNION (SELECT trg AS src, trg FROM edge))", CPQ.IDENTITY.toSQL());
	}
}
