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
package dev.roanh.gmark.lang.cq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

public class CQTest{
	private static final Predicate l0 = new Predicate(0, "zero");
	private static final Predicate l1 = new Predicate(1, "one");

	@Test
	public void formal0(){
		CQ query = CQ.empty();
		
		VarCQ f1 = query.addFreeVariable("f1");
		VarCQ f2 = query.addFreeVariable("f2");
		VarCQ b1 = query.addBoundVariable("b1");
		
		query.addAtom(f1, l0, b1);
		query.addAtom(b1, l1, f2);

		assertEquals("(f1, f2) ← zero(f1, b1), one(b1, f2)", query.toFormalSyntax());
	}
}
