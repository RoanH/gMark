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
package dev.roanh.gmark.output;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.gen.shape.QueryShape;
import dev.roanh.gmark.gen.workload.cpq.ConjunctCPQ;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cq.CQ;
import dev.roanh.gmark.lang.rpq.RPQ;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.gmark.type.schema.Predicate;

public class OutputSQLTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	private static final Predicate pred1 = new Predicate(1, "one");
	private static final CPQ label0 = CPQ.label(pred0);
	private static final CPQ label1 = CPQ.label(pred1);
	private static final CPQ label1i = CPQ.label(pred1.getInverse());
	private static final CPQ intersect0 = CPQ.intersect(label0, label1);
	private static final Variable v0 = new Variable(0);
	private static final Variable v1 = new Variable(1);
	
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
	
	@Test
	public void intersectToSQL0(){
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
			""".trim(),
			intersect0.toSQL()
		);
	}
	
	@Test
	public void intersectToSQL1(){
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
			  INTERSECT
			  SELECT src, trg FROM (
			    SELECT trg AS src, src AS trg FROM edge WHERE label = 1
			  )
			)
			""".trim(),
			CPQ.intersect(label0, label1, label1i).toSQL()
		);
	}
	
	@Test
	public void intersectToSQL2(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT src, trg FROM (
			    SELECT src, trg FROM (
			      SELECT src, trg FROM (
			        SELECT src, trg FROM edge WHERE label = 0
			      )
			      INTERSECT
			      SELECT src, trg FROM (
			        SELECT src, trg FROM edge WHERE label = 1
			      )
			    )
			  )
			  INTERSECT
			  SELECT src, trg FROM (
			    SELECT src, trg FROM edge WHERE label = 1
			  )
			)
			""".trim(),
			CPQ.intersect(intersect0, label1).toSQL()
		);
	}
	
	@Test
	public void intersectToSQL3(){
		assertEquals(
			"""
			SELECT src, trg FROM (
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
			)
			""".trim(),
			CPQ.intersect(CPQ.concat(label0, label1), label1).toSQL()
		);
	}
	
	@Test
	public void disjunctToSQL0(){
		assertEquals(
			"""
			SELECT src, trg FROM (
			  SELECT src, trg FROM (
			    SELECT src, trg FROM edge WHERE label = 0
			  )
			  UNION
			  SELECT src, trg FROM (
			    SELECT src, trg FROM edge WHERE label = 1
			  )
			  UNION
			  SELECT src, trg FROM (
			    SELECT trg AS src, src AS trg FROM edge WHERE label = 1
			  )
			)
			""".trim(),
			RPQ.disjunct(RPQ.label(pred0), RPQ.label(pred1), RPQ.label(pred1.getInverse())).toSQL()
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
			CPQ.concat(label0, label1, label1, label1i).toSQL()
		);
	}
	
	@Test
	public void monoConcatToSQL(){
		assertEquals("SELECT src, trg FROM edge WHERE label = 1", CPQ.concat(label1).toSQL());
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
	
	@Test
	public void tc0(){
		assertEquals(
			"""
			SELECT s0.src AS src, s1.trg AS trg
			FROM
			  (
			    SELECT src, trg FROM edge WHERE label = 0
			  ) AS s0,
			  (
			    WITH RECURSIVE
			    base(src, trg) AS (
			      WITH RECURSIVE
			      base(src, trg) AS (
			        SELECT src, trg FROM edge WHERE label = 0
			      ),
			      tc(src, trg) AS (
			        SELECT src, trg
			        FROM base
			        UNION
			        SELECT head.src, tail.trg
			        FROM base AS head, tc AS tail
			        WHERE head.trg = tail.src
			      )
			      SELECT base.src, base.trg FROM base, tc
			    ),
			    tc(src, trg) AS (
			      SELECT src, trg
			      FROM base
			      UNION
			      SELECT head.src, tail.trg
			      FROM base AS head, tc AS tail
			      WHERE head.trg = tail.src
			    )
			    SELECT base.src, base.trg FROM base, tc
			  ) AS s1
			WHERE s0.trg = s1.src
			""".trim(),
			RPQ.concat(RPQ.label(pred0), RPQ.kleene(RPQ.kleene(RPQ.label(pred0)))).toSQL()
		);
	}
	
	@Test
	public void cqToSQL(){
		assertEquals(
			"""
			SELECT
			  edge1.src AS f1,
			  edge2.src AS f2,
			  edge3.trg AS f3,
			  edge4.src AS f4,
			  edge4.trg AS f5
			FROM
			  edge edge0,
			  edge edge1,
			  edge edge2,
			  edge edge3,
			  edge edge4
			WHERE
			  edge0.label = 1
			  AND
			  edge1.label = 0
			  AND
			  edge2.label = 0
			  AND
			  edge3.label = 1
			  AND
			  edge4.label = 1
			  AND
			  edge1.src = edge3.src
			  AND
			  edge0.src = edge0.trg
			  AND
			  edge0.src = edge1.trg
			  AND
			  edge0.src = edge2.trg
			""".trim(),
			CQ.parse("(f1, f2, f3, f4, f5) ← one(b2, b2), zero(f1, b2), zero(f2, b2), one(f1, f3), one(f4, f5)", List.of(pred0, pred1)).toSQL()
		);
	}
}
