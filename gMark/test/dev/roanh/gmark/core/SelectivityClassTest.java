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
package dev.roanh.gmark.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import static dev.roanh.gmark.core.SelectivityClass.*;

public class SelectivityClassTest{

	@Test
	public void oneOneConjunction(){
		assertEquals(ONE_ONE.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(ONE_ONE.conjunction(ONE_N), ONE_N);
		assertEquals(ONE_ONE.conjunction(N_ONE), ONE_ONE);
		assertEquals(ONE_ONE.conjunction(EQUALS), ONE_N);
		assertEquals(ONE_ONE.conjunction(LESS), ONE_N);
		assertEquals(ONE_ONE.conjunction(GREATER), ONE_N);
		assertEquals(ONE_ONE.conjunction(LESS_GREATER), ONE_N);
		assertEquals(ONE_ONE.conjunction(CROSS), ONE_N);
	}
	
	@Test
	public void oneNConjunction(){
		assertEquals(ONE_N.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(ONE_N.conjunction(ONE_N), ONE_N);
		assertEquals(ONE_N.conjunction(N_ONE), ONE_ONE);
		assertEquals(ONE_N.conjunction(EQUALS), ONE_N);
		assertEquals(ONE_N.conjunction(LESS), ONE_N);
		assertEquals(ONE_N.conjunction(GREATER), ONE_N);
		assertEquals(ONE_N.conjunction(LESS_GREATER), ONE_N);
		assertEquals(ONE_N.conjunction(CROSS), ONE_N);
	}
	
	@Test
	public void nOneConjunction(){
		assertEquals(N_ONE.conjunction(ONE_ONE), N_ONE);
		assertEquals(N_ONE.conjunction(ONE_N), CROSS);
		assertEquals(N_ONE.conjunction(N_ONE), N_ONE);
		assertEquals(N_ONE.conjunction(EQUALS), N_ONE);
		assertEquals(N_ONE.conjunction(LESS), N_ONE);
		assertEquals(N_ONE.conjunction(GREATER), N_ONE);
		assertEquals(N_ONE.conjunction(LESS_GREATER), N_ONE);
		assertEquals(N_ONE.conjunction(CROSS), N_ONE);
	}
	
	@Test
	public void equalsConjunction(){
		assertEquals(EQUALS.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(EQUALS.conjunction(ONE_N), ONE_N);
		assertEquals(EQUALS.conjunction(N_ONE), N_ONE);
		assertEquals(EQUALS.conjunction(EQUALS), EQUALS);
		assertEquals(EQUALS.conjunction(LESS), LESS);
		assertEquals(EQUALS.conjunction(GREATER), GREATER);
		assertEquals(EQUALS.conjunction(LESS_GREATER), LESS_GREATER);
		assertEquals(EQUALS.conjunction(CROSS), CROSS);
	}
	
	@Test
	public void lessConjunction(){
		assertEquals(LESS.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(LESS.conjunction(ONE_N), ONE_N);
		assertEquals(LESS.conjunction(N_ONE), N_ONE);
		assertEquals(LESS.conjunction(EQUALS), LESS);
		assertEquals(LESS.conjunction(LESS), LESS);
		assertEquals(LESS.conjunction(GREATER), LESS_GREATER);
		assertEquals(LESS.conjunction(LESS_GREATER), LESS_GREATER);
		assertEquals(LESS.conjunction(CROSS), CROSS);
	}
	
	@Test
	public void greaterConjunction(){
		assertEquals(GREATER.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(GREATER.conjunction(ONE_N), ONE_N);
		assertEquals(GREATER.conjunction(N_ONE), N_ONE);
		assertEquals(GREATER.conjunction(EQUALS), GREATER);
		assertEquals(GREATER.conjunction(LESS), CROSS);
		assertEquals(GREATER.conjunction(GREATER), GREATER);
		assertEquals(GREATER.conjunction(LESS_GREATER), CROSS);
		assertEquals(GREATER.conjunction(CROSS), CROSS);
	}

	@Test
	public void lessGreaterConjunction(){
		assertEquals(LESS_GREATER.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(LESS_GREATER.conjunction(ONE_N), ONE_N);
		assertEquals(LESS_GREATER.conjunction(N_ONE), N_ONE);
		assertEquals(LESS_GREATER.conjunction(EQUALS), LESS_GREATER);
		assertEquals(LESS_GREATER.conjunction(LESS), CROSS);
		assertEquals(LESS_GREATER.conjunction(GREATER), LESS_GREATER);
		assertEquals(LESS_GREATER.conjunction(LESS_GREATER), CROSS);
		assertEquals(LESS_GREATER.conjunction(CROSS), CROSS);
	}

	@Test
	public void crossConjunction(){
		assertEquals(CROSS.conjunction(ONE_ONE), ONE_ONE);
		assertEquals(CROSS.conjunction(ONE_N), ONE_N);
		assertEquals(CROSS.conjunction(N_ONE), N_ONE);
		assertEquals(CROSS.conjunction(EQUALS), CROSS);
		assertEquals(CROSS.conjunction(LESS), CROSS);
		assertEquals(CROSS.conjunction(GREATER), CROSS);
		assertEquals(CROSS.conjunction(LESS_GREATER), CROSS);
		assertEquals(CROSS.conjunction(CROSS), CROSS);
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
