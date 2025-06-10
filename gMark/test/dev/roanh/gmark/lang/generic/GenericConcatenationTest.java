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
package dev.roanh.gmark.lang.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;

public class GenericConcatenationTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	private static final Predicate pred1 = new Predicate(1, "one");
	private static final CPQ label0 = CPQ.label(pred0);
	private static final CPQ label1 = CPQ.label(pred1);
	private static final CPQ label1i = CPQ.label(pred1.getInverse());
	
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
}
