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

public class GenericEdgeTest{
	private static final Predicate pred0 = new Predicate(0, "zero");
	private static final Predicate pred1 = new Predicate(1, "one");
	private static final CPQ label0 = CPQ.label(pred0);
	private static final CPQ label1 = CPQ.label(pred1);
	private static final CPQ label1i = CPQ.label(pred1.getInverse());
	
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
}
