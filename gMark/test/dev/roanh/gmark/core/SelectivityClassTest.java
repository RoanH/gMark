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
package dev.roanh.gmark.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import static dev.roanh.gmark.core.SelectivityClass.*;

public class SelectivityClassTest{

	@Test
	public void oneOneConjunction(){
		assertEquals(ONE_ONE, ONE_ONE.conjunction(ONE_ONE));
		assertEquals(ONE_N, ONE_ONE.conjunction(ONE_N));
		assertEquals(ONE_ONE, ONE_ONE.conjunction(N_ONE));
		assertEquals(ONE_N, ONE_ONE.conjunction(EQUALS));
		assertEquals(ONE_N, ONE_ONE.conjunction(LESS));
		assertEquals(ONE_N, ONE_ONE.conjunction(GREATER));
		assertEquals(ONE_N, ONE_ONE.conjunction(LESS_GREATER));
		assertEquals(ONE_N, ONE_ONE.conjunction(CROSS));
	}
	
	@Test
	public void oneNConjunction(){
		assertEquals(ONE_ONE, ONE_N.conjunction(ONE_ONE));
		assertEquals(ONE_N, ONE_N.conjunction(ONE_N));
		assertEquals(ONE_ONE, ONE_N.conjunction(N_ONE));
		assertEquals(ONE_N, ONE_N.conjunction(EQUALS));
		assertEquals(ONE_N, ONE_N.conjunction(LESS));
		assertEquals(ONE_N, ONE_N.conjunction(GREATER));
		assertEquals(ONE_N, ONE_N.conjunction(LESS_GREATER));
		assertEquals(ONE_N, ONE_N.conjunction(CROSS));
	}
	
	@Test
	public void nOneConjunction(){
		assertEquals(N_ONE, N_ONE.conjunction(ONE_ONE));
		assertEquals(CROSS, N_ONE.conjunction(ONE_N));
		assertEquals(N_ONE, N_ONE.conjunction(N_ONE));
		assertEquals(N_ONE, N_ONE.conjunction(EQUALS));
		assertEquals(N_ONE, N_ONE.conjunction(LESS));
		assertEquals(N_ONE, N_ONE.conjunction(GREATER));
		assertEquals(N_ONE, N_ONE.conjunction(LESS_GREATER));
		assertEquals(N_ONE, N_ONE.conjunction(CROSS));
	}
	
	@Test
	public void equalsConjunction(){
		assertEquals(ONE_ONE, EQUALS.conjunction(ONE_ONE));
		assertEquals(ONE_N, EQUALS.conjunction(ONE_N));
		assertEquals(N_ONE, EQUALS.conjunction(N_ONE));
		assertEquals(EQUALS, EQUALS.conjunction(EQUALS));
		assertEquals(LESS, EQUALS.conjunction(LESS));
		assertEquals(GREATER, EQUALS.conjunction(GREATER));
		assertEquals(LESS_GREATER, EQUALS.conjunction(LESS_GREATER));
		assertEquals(CROSS, EQUALS.conjunction(CROSS));
	}
	
	@Test
	public void lessConjunction(){
		assertEquals(ONE_ONE, LESS.conjunction(ONE_ONE));
		assertEquals(ONE_N, LESS.conjunction(ONE_N));
		assertEquals(N_ONE, LESS.conjunction(N_ONE));
		assertEquals(LESS, LESS.conjunction(EQUALS));
		assertEquals(LESS, LESS.conjunction(LESS));
		assertEquals(LESS_GREATER, LESS.conjunction(GREATER));
		assertEquals(LESS_GREATER, LESS.conjunction(LESS_GREATER));
		assertEquals(CROSS, LESS.conjunction(CROSS));
	}
	
	@Test
	public void greaterConjunction(){
		assertEquals(ONE_ONE, GREATER.conjunction(ONE_ONE));
		assertEquals(ONE_N, GREATER.conjunction(ONE_N));
		assertEquals(N_ONE, GREATER.conjunction(N_ONE));
		assertEquals(GREATER, GREATER.conjunction(EQUALS));
		assertEquals(CROSS, GREATER.conjunction(LESS));
		assertEquals(GREATER, GREATER.conjunction(GREATER));
		assertEquals(CROSS, GREATER.conjunction(LESS_GREATER));
		assertEquals(CROSS, GREATER.conjunction(CROSS));
	}

	@Test
	public void lessGreaterConjunction(){
		assertEquals(ONE_ONE, LESS_GREATER.conjunction(ONE_ONE));
		assertEquals(ONE_N, LESS_GREATER.conjunction(ONE_N));
		assertEquals(N_ONE, LESS_GREATER.conjunction(N_ONE));
		assertEquals(LESS_GREATER, LESS_GREATER.conjunction(EQUALS));
		assertEquals(CROSS, LESS_GREATER.conjunction(LESS));
		assertEquals(LESS_GREATER, LESS_GREATER.conjunction(GREATER));
		assertEquals(CROSS, LESS_GREATER.conjunction(LESS_GREATER));
		assertEquals(CROSS, LESS_GREATER.conjunction(CROSS));
	}

	@Test
	public void crossConjunction(){
		assertEquals(ONE_ONE, CROSS.conjunction(ONE_ONE));
		assertEquals(ONE_N, CROSS.conjunction(ONE_N));
		assertEquals(N_ONE, CROSS.conjunction(N_ONE));
		assertEquals(CROSS, CROSS.conjunction(EQUALS));
		assertEquals(CROSS, CROSS.conjunction(LESS));
		assertEquals(CROSS, CROSS.conjunction(GREATER));
		assertEquals(CROSS, CROSS.conjunction(LESS_GREATER));
		assertEquals(CROSS, CROSS.conjunction(CROSS));
	}
	
	@Test
	public void negation(){
		assertEquals(ONE_N, N_ONE.negate());
		assertEquals(N_ONE, ONE_N.negate());
		assertEquals(GREATER, LESS.negate());
		assertEquals(LESS, GREATER.negate());
		assertEquals(CROSS, CROSS.negate());
		assertEquals(EQUALS, EQUALS.negate());
		assertEquals(LESS_GREATER, LESS_GREATER.negate());
		assertEquals(ONE_ONE, ONE_ONE.negate());
	}
	
	@Test
	public void subclassConstant(){
		for(SelectivityClass sel : values()){
			if(sel == ONE_ONE){
				assertEquals(Selectivity.CONSTANT, sel.getSelectivity());
			}else{
				assertNotEquals(Selectivity.CONSTANT, sel.getSelectivity());
			}
		}
	}
	
	@Test
	public void subclassLinear(){
		for(SelectivityClass sel : values()){
			if(sel != ONE_ONE && sel != CROSS){
				assertEquals(Selectivity.LINEAR, sel.getSelectivity());
			}else{
				assertNotEquals(Selectivity.LINEAR, sel.getSelectivity());
			}
		}
	}
	
	@Test
	public void subclassQuadratic(){
		for(SelectivityClass sel : values()){
			if(sel == CROSS){
				assertEquals(Selectivity.QUADRATIC, sel.getSelectivity());
			}else{
				assertNotEquals(Selectivity.QUADRATIC, sel.getSelectivity());
			}
		}
	}
}
