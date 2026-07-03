/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;

public class CanonFormTest{
	
	@Test
	public void canon0(){
		Predicate l1 = new Predicate(0, "a");
		Predicate l2 = new Predicate(1, "b");
		Predicate l3 = new Predicate(2, "c");
		
		CanonForm canon = CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l3), CPQ.labels(l1, l2)), false);
		assertEquals("s=0,t=1,l0=2,l1=1,l2=1,e0={2,3},e1={},e2={6},e3={7},e4={1},e5={1},e6={5},e7={4}", canon.toStringCanon());
		assertArrayEquals(new byte[]{2, 1, 24, 16, 72, -118, 76, 28, 121, 36, -102, 96}, canon.toBinaryCanon());
		assertEquals("AgEYEEiKTBx5JJpg", canon.toBase64Canon());
		
		canon = CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l2), CPQ.labels(l1, l3)), false);
		assertEquals("s=0,t=1,l0=2,l1=1,l2=1,e0={2,3},e1={},e2={6},e3={7},e4={1},e5={1},e6={5},e7={4}", canon.toStringCanon());
		assertArrayEquals(new byte[]{2, 1, 24, 16, 72, -118, 76, 28, 121, 36, -102, 96}, canon.toBinaryCanon());
		assertEquals("AgEYEEiKTBx5JJpg", canon.toBase64Canon());
	}

	@Test
	public void canon1(){
		Predicate l1 = new Predicate(0, "a");
		Predicate l2 = new Predicate(1, "b");
		Predicate l3 = new Predicate(2, "c");
		
		assertEquals(
			CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l3), CPQ.labels(l1, l2)), false),
			CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l2), CPQ.labels(l1, l3)), false)
		);
	}

	@Test
	public void canon2(){
		Predicate l1 = new Predicate(0, "a");
		Predicate l2 = new Predicate(1, "b");
		Predicate l3 = new Predicate(2, "c");
		
		assertEquals(
			CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l3), CPQ.intersect(l1, l2)), false),
			CanonForm.computeCanon(CPQ.intersect(CPQ.intersect(l1, l2), CPQ.labels(l1, l3)), false)
		);
	}
	
	@Test
	public void canon3(){
		Predicate l1 = new Predicate(0, "a");
		Predicate l2 = new Predicate(1, "b");
		Predicate l3 = new Predicate(2, "c");
		
		assertEquals(
			CanonForm.computeCanon(CPQ.intersect(CPQ.labels(l1, l3, l1), CPQ.id(), CPQ.intersect(l1, l2)), false),
			CanonForm.computeCanon(CPQ.intersect(CPQ.intersect(l1, l2), CPQ.labels(l1, l3, l1), CPQ.id()), false)
		);
	}
}
