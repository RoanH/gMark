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
package dev.roanh.gmark.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.query.conjunct.cpq.ConjunctCPQ;

public class QueryTest{
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
}
