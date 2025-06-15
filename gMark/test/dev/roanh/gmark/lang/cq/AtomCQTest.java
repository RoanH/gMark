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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

public class AtomCQTest{

	@Test
	public void equals(){
		AtomCQ atom = new AtomCQ(new VarCQ("s", false), new Predicate(0, "l"), new VarCQ("t", true));
		assertEquals(atom, new AtomCQ(new VarCQ("s", false), new Predicate(0, "l"), new VarCQ("t", true)));
		assertNotEquals(atom, new AtomCQ(new VarCQ("X", false), new Predicate(0, "l"), new VarCQ("t", true)));
		assertNotEquals(atom, new AtomCQ(new VarCQ("s", false), new Predicate(0, "X"), new VarCQ("t", true)));
		assertNotEquals(atom, new AtomCQ(new VarCQ("s", false), new Predicate(0, "l"), new VarCQ("X", true)));
	}
	
	@Test
	public void labelNeverInversed(){
		AtomCQ atom = new AtomCQ(new VarCQ("s", false), new Predicate(0, "l").getInverse(), new VarCQ("t", true));
		assertFalse(atom.getLabel().isInverse());
		assertEquals(atom.getLabel(), new Predicate(0, "l"));
	}
}
