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
package dev.roanh.gmark.output.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.conjunct.cpq.CPQ;
import dev.roanh.gmark.conjunct.cpq.ConcatCPQ;
import dev.roanh.gmark.conjunct.cpq.EdgeCPQ;
import dev.roanh.gmark.conjunct.cpq.IntersectionCPQ;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.exception.ConfigException;

public class ConversionCPQTest{
	private static Predicate pred0 = new Predicate(0, "0");
	private static Predicate pred1 = new Predicate(1, "1");
	private static CPQ label0 = CPQ.label(pred0);
	private static CPQ label1 = CPQ.label(pred1);
	private static CPQ label1i = CPQ.label(pred1.getInverse());
	private static CPQ concat0 = CPQ.concat(label0, label1, label1, label1i);
	private static CPQ concat1 = CPQ.concat(label1);
	private static CPQ concat2 = CPQ.concat(label0, label1);
	private static CPQ intersect0 = CPQ.intersect(label0, label1);
	private static CPQ intersect1 = CPQ.intersect(label0, label1, label1i);
	private static CPQ intersect2 = CPQ.intersect(intersect0, label1);
	private static CPQ intersect3 = CPQ.intersect(concat2, label1);
	
	@Test
	public void body0(){
		
	}
	
	@Test
	public void intersectToSQL0(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 0
			)
			INTERSECT
			SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			""".trim(),
			intersect0.toSQL()
		);
	}
	
	@Test
	public void intersectToSQL1(){
		assertEquals(
			"""
			  SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 0
			)
			INTERSECT
			SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			INTERSECT
			SELECT src, trg FROM (
			  SELECT trg AS src, src AS trg FROM edge WHERE label = 1
			)
			""".trim(),
			intersect1.toSQL()
		);
	}
	
	@Test
	public void intersectToSQL2(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT src, trg FROM (
			    SELECT src, trg FROM edge WHERE label = 0
			  )
			  INTERSECT
			  SELECT src, trg FROM (
			    SELECT src, trg FROM edge WHERE label = 1
			  )
			)
			INTERSECT
			SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			""".trim(),
			intersect2.toSQL()
		);
	}
	
	@Test
	public void intersectToSQL3(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT s0.src AS src, s1.trg AS trg
			  FROM
			    (
			      SELECT src, trg FROM edge WHERE label = 0
			    ) AS s0,
			    (
			      SELECT src, trg FROM edge WHERE label = 1
			    ) AS s1
			  WHERE s0.trg = s1.src
			)
			INTERSECT
			SELECT src, trg FROM (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			""".trim(),
			intersect3.toSQL()
		);
	}
	
	@Test
	public void concatToSQL(){
		assertEquals(
			"""
			SELECT s0.src AS src, s3.trg AS trg
			FROM
			  (
			    SELECT src, trg FROM edge WHERE label = 0
			  ) AS s0,
			  (
			    SELECT src, trg FROM edge WHERE label = 1
			  ) AS s1,
			  (
			    SELECT src, trg FROM edge WHERE label = 1
			  ) AS s2,
			  (
			    SELECT trg AS src, src AS trg FROM edge WHERE label = 1
			  ) AS s3
			WHERE s0.trg = s1.src AND s1.trg = s2.src AND s2.trg = s3.src
			""".trim(),
			concat0.toSQL()
		);
	}
	
	@Test
	public void monoConcatToSQL(){
		assertEquals("SELECT src, trg FROM edge WHERE label = 1", concat1.toSQL());
	}
	
	@Test
	public void predicateToSQL(){
		assertEquals("SELECT src, trg FROM edge WHERE label = 1", pred1.toSQL());
	}
	
	@Test
	public void inversePredicateToSQL(){
		assertEquals("SELECT trg AS src, src AS trg FROM edge WHERE label = 1", pred1.getInverse().toSQL());
	}
	
	@Test
	public void labelToSQL0(){
		assertEquals("SELECT src, trg FROM edge WHERE label = 0", label0.toSQL());
	}
	
	@Test
	public void labelToSQL1(){
		assertEquals("SELECT src, trg FROM edge WHERE label = 1", label1.toSQL());
	}
	
	@Test
	public void inverseLabelToSQL(){
		assertEquals("SELECT trg AS src, src AS trg FROM edge WHERE label = 1", label1i.toSQL());
	}
	
	@Test
	public void identityToSQL(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT src, src AS trg FROM edge
			  UNION
			  SELECT trg AS src, trg FROM edge
			)
			""".trim(),
			CPQ.IDENTITY.toSQL()
		);
	}
}
