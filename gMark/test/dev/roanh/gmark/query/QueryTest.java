/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
package dev.roanh.gmark.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.gen.shape.QueryShape;
import dev.roanh.gmark.gen.workload.cpq.ConjunctCPQ;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.gmark.type.schema.Predicate;

public class QueryTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	private static final Predicate pred1 = new Predicate(1, "one");
	private static final CPQ label0 = CPQ.label(pred0);
	private static final CPQ label1 = CPQ.label(pred1);
	private static final Variable v0 = new Variable(0);
	private static final Variable v1 = new Variable(1);

	@Test
	public void string0(){
		assertEquals(
			"(?x0,?x1) ← (?x0,(a◦a◦a)*,?x1),(?x0,b,?x1)",
			new Query(
				List.of(
					new ConjunctCPQ(CPQ.parse("a◦a◦a"), v0, v1, true),
					new ConjunctCPQ(CPQ.parse("b"), v0, v1, false)
				),
				List.of(v0, v1),
				Selectivity.LINEAR,
				QueryShape.CYCLE
			).toString()
		);
	}
	
	@Test
	public void body0(){
		assertEquals(
			"""
			WITH RECURSIVE
			c0(src, trg) AS (
			  SELECT edge.src, edge.src
			  FROM edge
			  UNION
			  SELECT edge.trg, edge.trg
			  FROM edge
			  UNION
			  SELECT src, trg FROM edge WHERE label = 0
			),
			c0tc(src, trg) AS (
			  SELECT src, trg
			  FROM c0
			  UNION
			  SELECT head.src, tail.trg
			  FROM c0 AS head, c0tc AS tail
			  WHERE head.trg = tail.src
			),
			c1(src, trg) AS (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			SELECT DISTINCT
			  c0.src,
			  c0.trg
			FROM
			  c0,
			  c0tc,
			  c1
			WHERE
			  c0.src = c1.src
			  AND
			  c0.trg = c1.trg
			""".trim(),
			new Query(
				List.of(
					new ConjunctCPQ(label0, v0, v1, true),
					new ConjunctCPQ(label1, v0, v1, false)
				),
				List.of(v0, v1),
				Selectivity.LINEAR,
				QueryShape.CYCLE
			).toSQL()
		);
	}
	
	@Test
	public void body1(){
		assertEquals(
			"""
			WITH RECURSIVE
			c0(src, trg) AS (
			  SELECT src, trg FROM edge WHERE label = 0
			),
			c1(src, trg) AS (
			  SELECT src, trg FROM edge WHERE label = 1
			)
			SELECT "true"
			FROM edge
			WHERE EXISTS (
			  SELECT *
			  FROM
			    c0,
			    c1
			  WHERE
			    c0.src = c1.src
			    AND
			    c0.trg = c1.trg
			)
			""".trim(),
			new Query(
				List.of(
					new ConjunctCPQ(label0, v0, v1, false),
					new ConjunctCPQ(label1, v0, v1, false)
				),
				List.of(),
				Selectivity.LINEAR,
				QueryShape.CYCLE
			).toSQL()
		);
	}
}
