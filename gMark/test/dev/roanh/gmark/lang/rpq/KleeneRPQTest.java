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
package dev.roanh.gmark.lang.rpq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

public class KleeneRPQTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	
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
}
