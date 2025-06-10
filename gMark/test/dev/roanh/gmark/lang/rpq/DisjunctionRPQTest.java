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

public class DisjunctionRPQTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	private static final Predicate pred1 = new Predicate(1, "one");
	private static final RPQ label0 = RPQ.label(pred0);
	private static final RPQ label1 = RPQ.label(pred1);
	private static final RPQ label1i = RPQ.label(pred1.getInverse());
	
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
			RPQ.disjunct(label0, label1, label1i).toSQL()
		);
	}
}
